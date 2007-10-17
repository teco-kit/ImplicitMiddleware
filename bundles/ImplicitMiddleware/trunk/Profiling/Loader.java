import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import analyzer.*;

public class Loader {
	static File f = new File("profile.trcxml");
	static File fOut = new File("profile.xml");
	static String costFunction = "";
	static SAXBuilder xmlBuilder = new SAXBuilder();
	static XMLOutputter xmlOutput = new XMLOutputter();
	
	/**
	 * Usage:
	 * 1st arg: Pofiling trace
	 * 2nd arg: configFile
	 * 3rd arg: costFunction
	 * 
	 */
	public static void main(String args[])
	{
		if (args.length > 0)
			f = new File(args[0]);
		if (args.length > 1)
			helperClasses.Configuration.setConfigFile(new File(args[1]));
		if (args.length > 2)
			costFunction = args[2];
		try {
			Document d = xmlBuilder.build(f);
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(d, new FileOutputStream(fOut));
			MethodCallAnalyzer mca = new MethodCallAnalyzer(d.getRootElement(), costFunction);
//			mca.printCallTree();
			
		} 
		catch (JDOMException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
	}
	

}
