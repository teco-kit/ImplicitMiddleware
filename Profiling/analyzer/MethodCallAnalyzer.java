package analyzer;

import helperClasses.Configuration;
import helperClasses.HashCode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.CallDesc;
import model.ClassDesc;
import model.ClassStatistics;
import model.Machine;
import model.MethodDesc;
import model.ThreadDesc;

import org.jdom.Element;
import org.jdom.filter.AbstractFilter;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

public class MethodCallAnalyzer {
	public static Map<HashCode, MethodDesc> methods = new HashMap<HashCode, MethodDesc>();
	public static Map<HashCode, ClassDesc> classes = new HashMap<HashCode, ClassDesc>();
	public static Map<HashCode, ThreadDesc> threads = new HashMap<HashCode, ThreadDesc>();
	
	private Element trace;
	
	private String costFunction = "";
	
	public MethodCallAnalyzer(Element trace, String costFunction)
	{
		this.costFunction = costFunction;
		initialize(trace);
	}

	public void initialize(Element trace)
	{
		this.trace = trace;
		classes.put(ClassDesc.nullClass.getId(), ClassDesc.nullClass);
		
		initThreads();
		initClasses();
		initMethods();
		initMachines();
		makeCallGraph();
		makeStatistics();
		//Map<ClassDesc, Machine> bestDistri = generateBestDistribution();
		//Iterator<ClassDesc> it = bestDistri.keySet().iterator();
		/*
		while (it.hasNext())
		{
			ClassDesc cd = it.next();
			System.out.println(cd.getName() + " ! " + bestDistri.get(cd).getMachineID());
		}
		*/
		printClassStatistics();
		//StubMaker sm = new StubMaker(bestDistri);
		//sm.outputXmlFile();
		//sm.makeRepartition();
		
	}





	private Map<ClassDesc, Machine> generateBestDistribution() {
		double weight = Double.POSITIVE_INFINITY;
		Map<ClassDesc, Machine> bestDistri = new HashMap<ClassDesc, Machine>();
		
		Element config = Configuration.getInstance().getConfig();
		Distribution distri = new Distribution(classes);
		distri.map(config);
		CostFunction cf;
		if (costFunction.equals("SpeedCostFunction"))
			cf = new SpeedCostFunction(classes.values());
		else
			cf = new BasicCostFunction(classes.values());
		while (distri.makeNextDistribution())
		{
			double res = cf.getValue();
			if (weight>res)
			{
				weight = res;
				bestDistri.clear();
				Iterator<ClassDesc> it= classes.values().iterator();
				while (it.hasNext())
				{
					ClassDesc current = it.next();
					bestDistri.put(current, current.getMachine());
				}
			}
//			debug
			distri.printCurrentDistribution();
			System.out.println("Cost: " + res);
		}
		System.out.println("Best Costs: " + weight);
		return bestDistri;
	}

	public void makeStatistics() 
	{
		Iterator<ThreadDesc> it = threads.values().iterator();
		//System.out.println("Class Call statistics");
		while (it.hasNext())
		{
			ThreadDesc td = it.next();
			CallDesc calld = td.getRootCallDesc();
			statisticVisitor(calld);

		}
	}
	
	private void statisticVisitor(CallDesc calld)
	{
		makeClassStatistic(calld);
		Iterator<CallDesc> it = calld.getChildren().iterator();
		while (it.hasNext())
		{
			CallDesc cdchild = it.next();
			statisticVisitor(cdchild);
		}
	}

	private void makeClassStatistic(CallDesc calld) {
		ClassDesc current = calld.getMethod().getClassDesc();
		ClassDesc parent = calld.getParent().getMethod().getClassDesc();
		
		current.getClassStatistics().addCall(calld.getBaseTime());
		parent.getClassStatistics().addCallToClass(current, calld.getBaseTime());
		}

	@SuppressWarnings("unchecked")
	void initThreads() {
		List<Element> th = trace.getChildren("threadStart");
		Iterator<Element> it = th.iterator();
		while (it.hasNext())
		{
			Element t = it.next();
			ThreadDesc td = new ThreadDesc(new HashCode((new Integer(t.getAttributeValue("threadId"))).intValue()),
										   t.getAttributeValue("threadName"),
										   t.getAttributeValue("groupName"),
										   t.getAttributeValue("parentName"),
										   (new Double(t.getAttributeValue("time"))).doubleValue(), 
										   (new Integer(t.getAttributeValue("objIdRef"))).intValue()
										   );
			threads.put(td.getId(), td);
		}
	}
	
	@SuppressWarnings("unchecked")
	void initClasses() {
		List<Element> cl = trace.getChildren("classDef");
		Iterator<Element> it = cl.iterator();
		while (it.hasNext())
		{
			Element c = it.next();
			ClassDesc cd = new ClassDesc(c.getAttributeValue("name"),
										 new HashCode((new Integer(c.getAttributeValue("classId"))).intValue()),
										 c.getAttributeValue("sourceName")
										 );
			classes.put(cd.getId(), cd);
		}
	}
	
	@SuppressWarnings("unchecked")
	void initMethods() {
		List<Element> ml = trace.getChildren("methodDef");
		Iterator<Element> it = ml.iterator();
		while (it.hasNext())
		{
			Element m = it.next();
			MethodDesc md = new MethodDesc(new HashCode((new Integer(m.getAttributeValue("methodId"))).intValue()),
										   m.getAttributeValue("name"),
										   new HashCode((new Integer(m.getAttributeValue("classIdRef"))).intValue()),
										   //StartLineNumber und EndLineNumber werden nirgends verwendet!
										   (new Integer(m.getAttributeValue("startLineNumber"))).intValue(),
										   (new Integer(m.getAttributeValue("endLineNumber"))).intValue()
										   );
			methods.put(md.getMethodId(), md);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initMachines() {
		Element config = Configuration.getInstance().getConfig();
		Iterator<Element> machines = config.getChild("machines").getChildren().iterator();
		while (machines.hasNext())
		{
			Element current = machines.next();
			new Machine(current.getAttributeValue("machineName"), 
					current.getAttributeValue("description"),
					new Integer(current.getAttributeValue("memory")).intValue(),
					new Integer(current.getAttributeValue("speed")).intValue(),
					new Double(current.getAttributeValue("delay")).doubleValue(),
					new Integer(current.getAttributeValue("speed")).intValue()
					);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void makeCallGraph() {
		AbstractFilter methodEntryFilter = new ElementFilter("methodEntry");
		AbstractFilter methodExitFilter = new ElementFilter("methodExit");
		
		List<Element> callElements = trace.getContent(methodEntryFilter.or(methodExitFilter));
		Iterator<Element> it = callElements.iterator();
		
		while (it.hasNext())
		{
			Element c = it.next();
			if (c.getName().equals("methodEntry"))
			{
				ThreadDesc td = threads.get(new HashCode((new Integer(c.getAttributeValue("threadIdRef"))).intValue()));
				CallDesc previousCall = td.getLastOpenCall();
				CallDesc cd = new CallDesc(td,
				                           methods.get(new HashCode((new Integer(c.getAttributeValue("methodIdRef"))).intValue())),
										   (new Double(c.getAttributeValue("time"))).doubleValue(),
						                   new HashCode((new Integer(c.getAttributeValue("ticket"))).intValue()),
						                   (new Integer(c.getAttributeValue("stackDepth"))).intValue(),
						                   previousCall
						                   );

				if (previousCall != null)
				{
					previousCall.addChild(cd);
				}
				td.setLastOpenCall(cd);
				td.addCall(cd);
			}
			else
			{
				ThreadDesc td = threads.get(new HashCode((new Integer(c.getAttributeValue("threadIdRef"))).intValue()));
				CallDesc cd = td.getLastOpenCall();
				
				cd.terminate((new Double(c.getAttributeValue("time"))).doubleValue(),
						     (new Double(c.getAttributeValue("overhead"))).doubleValue());
		               
				
				td.setLastOpenCall(cd.getParent());
			}
		}
		
	}
	
	public void printCallTree()
	{
		Iterator<ThreadDesc> it = threads.values().iterator();
		while (it.hasNext())
		{
			ThreadDesc td = it.next();
			System.out.println("\nCall tree for thread " + td.getName());
			CallTree ct = new CallTree(td.getRootCallDesc());
			System.out.println(ct.getStringOutput());
			System.out.println("\n");
		}
	}
	
	public void printClassStatistics()
	{
		
		int i=0;
		System.out.print("set CLASSES:= {");
		
		Iterator<ClassDesc> it;
		it = classes.values().iterator();
		
		while (it.hasNext()){
			ClassDesc cd = it.next();
			if(cd.getClassStatistics().getTotalTime()==0 && cd.getClassStatistics().getCalledClasses().isEmpty())
			{
			 it.remove();
			 continue;
			}		
			System.out.print("\""+cd.getName()+"\"");
			if(it.hasNext())System.out.print(",");
		}
		System.out.println("};");
		
		
		System.out.print("param exec[CLASSES]:=");
		it = classes.values().iterator();	
		while (it.hasNext()){
			ClassDesc cd = it.next();	
			System.out.print("<\""+cd.getName()  +  "\"> " + (cd.getClassStatistics().getTotalTime() *1000));
			if(it.hasNext())System.out.print(",");
		}
		System.out.println(";");
		
		System.out.println("set CALLS:= CLASSES*CLASSES;");
		System.out.print("param comm[CALLS]:=\n\t|");
		
		it = classes.values().iterator();	
		while (it.hasNext()){
			ClassDesc cd = it.next();	
			System.out.print("\""+cd.getName()  + "\"\t");
			if(it.hasNext())System.out.print(",");
		}	
		System.out.println("|");
		
		it=classes.values().iterator();
		
		while (it.hasNext())
		{
			ClassDesc cd = it.next();	
			Iterator<ClassDesc> it2 = classes.values().iterator();
			System.out.print("|\""+cd.getName()  + "\"\t|");

			while (it2.hasNext())
			{
				ClassDesc cd2 = it2.next();
				if(cd.getClassStatistics().getCalledClasses().containsKey(cd2))
				{
					ClassStatistics.ClassCall cc = cd.getClassStatistics().getCalledClasses().get(cd2);
					System.out.print("\t"+cc.getNumber());
				}
				else
				{ 
					System.out.print("\t"+0);
				}
				if(it2.hasNext())System.out.print(",");
			}
			System.out.print("|\n");
		}
		System.out.print(";");
	}

}
