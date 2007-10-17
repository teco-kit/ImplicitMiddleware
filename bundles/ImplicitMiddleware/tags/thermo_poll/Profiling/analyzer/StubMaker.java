package analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import model.ClassDesc;
import model.Machine;

public class StubMaker {
	private Map<Machine, List<ClassDesc>> machineClasses = new HashMap<Machine, List<ClassDesc>>();
	private Map<Machine, List<ClassDesc>> machineStubs = new HashMap<Machine, List<ClassDesc>>();
	private Map<ClassDesc, Machine> bestDistri;


	public StubMaker(Map<ClassDesc, Machine> bestDistri) {
		this.bestDistri = bestDistri;
		Iterator<Machine> machIt = Machine.getMachinesVector().iterator();
		while (machIt.hasNext())
		{
			Machine current = machIt.next();
			machineClasses.put(current, new LinkedList<ClassDesc>());
			machineStubs.put(current, new LinkedList<ClassDesc>());
		}

		Iterator<Entry<ClassDesc, Machine>> classIt = bestDistri.entrySet().iterator();
		while (classIt.hasNext())
		{
			Entry<ClassDesc, Machine> current = classIt.next();
			machineClasses.get(current.getValue()).add(current.getKey());
			Iterator<ClassDesc> calledClassesIt = current.getKey().getClassStatistics().getCalledClasses().keySet().iterator();
			while (calledClassesIt.hasNext())
			{
				ClassDesc currentCall = calledClassesIt.next();
				if (!bestDistri.get(currentCall).equals(current.getValue()))
				{
					machineStubs.get(current.getValue()).add(currentCall);
				}
			}
		}

	}

	public void outputXmlFile() {
		File outXML = new File("repartition.xml");
		OutputStream fileOutStream; 

		try {
			fileOutStream = new FileOutputStream(outXML);
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());

			outputter.output(new Document(makeOutputXml()), fileOutStream);


		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}


	}

	private Element makeOutputXml()
	{
		Element xml = new Element("root");
		Element m2c = new Element("machines");
		Element c2m = new Element("classes");

		Iterator<Entry<Machine, List<ClassDesc>>> mcIt = machineClasses.entrySet().iterator();
		while (mcIt.hasNext())
		{
			Entry<Machine, List<ClassDesc>> machList = mcIt.next();
			if (!machList.getKey().getMachineName().equals("localMachine"))
			{
				Element machElem = new Element(machList.getKey().getMachineName());
				Iterator<ClassDesc> classIt = machList.getValue().iterator();
				while (classIt.hasNext())
				{
					machElem.addContent(new Element("class").setAttribute("name", classIt.next().getName()));
				}
				Iterator<ClassDesc> stubIt = machineStubs.get(machList.getKey()).iterator();
				while (stubIt.hasNext())
				{
					machElem.addContent(new Element("stub").setAttribute("name", stubIt.next().getName()));
				}
				m2c.addContent(machElem);
			}
		}
		xml.addContent(m2c);

		Iterator<Entry<ClassDesc, Machine>> cmIt = bestDistri.entrySet().iterator();
		while (cmIt.hasNext())
		{
			Entry<ClassDesc,Machine> current = cmIt.next();
			if (!current.getValue().getMachineName().equals("localMachine"))
				c2m.addContent(new Element(current.getKey().getName()).setAttribute("host", current.getValue().getMachineName()));
		}
		xml.addContent(c2m);

		return xml;
	}

	public void makeRepartition() {
		// TODO Auto-generated method stub

	}


}
