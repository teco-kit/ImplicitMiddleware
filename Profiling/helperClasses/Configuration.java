package helperClasses;

import java.io.File;
import java.io.IOException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * 
 * Reads the file config.xml and gathers information such as the present host name, used protocols and so on.
 * Implemented as a singleton
 * @author Jean-Thomas Célette
 *
 */
public class Configuration {
	private static Configuration instance = null;
	private Element config = null;
	static File configFile = new File("profilingConfig.xml");
	
	public static Configuration getInstance()
	{
		if(instance == null) {
			instance = new Configuration();
		}
		return instance;
	}
	
	private Configuration()
	{
		readConfiguration();
	}


	public static void setConfigFile(File f)
	{
		configFile = f;
	}

	private void readConfiguration() 
	{
		SAXBuilder builder = new SAXBuilder();
		
		try {
			config = builder.build(configFile).getRootElement();
			
			
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Element getConfig()
	{
		return config;
	}
}