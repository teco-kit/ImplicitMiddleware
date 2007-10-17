package middleware.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ConfigurationMaker {

	/**
	 * @uml.property  name="hostsFile"
	 */
	File hostsFile = new File("hosts.xml");
	/**
	 * @uml.property  name="classesFile"
	 */
	File classesFile = new File("classes.xml");


	public ConfigurationMaker()
	{


	}

	public static void main(String args[])
	{
		ConfigurationMaker cm = new ConfigurationMaker();
		cm.makeConfig();

	}

	public void makeConfig()
	{
		OutputStream fileOutStream; 

		try {
			fileOutStream = new FileOutputStream(hostsFile);
			writeXMLFile(makeHostConfig(), fileOutStream);
			fileOutStream = new FileOutputStream(classesFile);
			writeXMLFile(makeClassesConfig(), fileOutStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Document makeHostConfig()
	{
		Document doc = new Document();
		Element root = new Element("hosts");

		Element host1 = new Element("server");
		host1.setAttribute("IP", "127.0.0.1");
		host1.setAttribute("port", "10001");
		root.addContent(host1);

		Element host2 = new Element("client");
		host2.setAttribute("IP", "127.0.0.1");
		host2.setAttribute("port", "10001");
		root.addContent(host2);

		doc.setRootElement(root);

		return doc;		
	}

	public Document makeClassesConfig()
	{
		Document doc = new Document();
		Element root = new Element("classes");

		Element complexCalculator = new Element("ComplexCalculator");
		complexCalculator.setAttribute("host", "server");
		root.addContent(complexCalculator);

		Element complexNumber = new Element("ComplexNumber");
		complexNumber.setAttribute("host", "client");
		root.addContent(complexNumber);

		doc.setRootElement(root);

		return doc;		
	}

	public void writeXMLFile(Document doc, OutputStream s)
	{
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());

		try {
			outputter.output(doc, s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
