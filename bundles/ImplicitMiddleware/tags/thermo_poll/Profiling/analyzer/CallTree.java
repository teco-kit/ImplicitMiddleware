package analyzer;

import java.util.Iterator;
import java.util.List;

import model.CallDesc;

public class CallTree {

	private CallDesc origin;
	String stringOutput = null;
	String currentIndent = "";
	String indent = "  ";

	public CallTree(CallDesc cd)
	{
		this.origin = cd;
	}
	
	public void makeStringOutput()
	{
		currentIndent = "";
		stringOutput = "";
		visit(origin);
	}
	
	private void visit(CallDesc cd)
	{
		stringOutput = stringOutput + callDescription(cd);
		List<CallDesc> l = cd.getChildren();
		Iterator<CallDesc> it = l.iterator();
		inc();
		while (it.hasNext())
		{
			visit(it.next());
		}
		dec();
	}
	
	private String callDescription(CallDesc cd)
	{
//		System.out.println(cd.toString());
//		System.out.println(cd.getMethod().toString());
//		System.out.println(cd.getMethod().getClassDesc().toString());
		String className = cd.getMethod().getClassDesc().getName();
		String methodName = cd.getMethod().getName();
		String cumulativeTime = new Double(cd.getCumulativeTime()*1000.0).toString();
		String baseTime = new Double(cd.getBaseTime()*1000.0).toString();
		
		return (currentIndent + className + "." + methodName +  " - cumulativeTime: " + cumulativeTime + " - baseTime: " + baseTime + "  \n");
	}
	
	public String getStringOutput()
	{
		if (stringOutput == null)
			makeStringOutput();
		return stringOutput;
	
	}
	
	private void inc()
	{
		currentIndent = currentIndent + indent;
	}

	private void dec()
	{
		currentIndent = currentIndent.substring(0, currentIndent.length() - indent.length());
	}
}
