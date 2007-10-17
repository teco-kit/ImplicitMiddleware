package middleware.config;

import helper.JDOMhelper;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;


import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Hosts {
   private        Hashtable<String, String[]> transportAttributes;
   private        Hashtable<String, String> transportProtocol;
   private static String inputFile = "hosts.xml";
   
   public Hosts()
   {
      transportAttributes = new Hashtable<String, String[]>();
      transportProtocol   = new Hashtable<String, String>();
      readConfiguration();
   }
   
   public Hosts(String file)
   {
      inputFile           = file;
      transportAttributes = new Hashtable<String, String[]>();
      transportProtocol   = new Hashtable<String, String>();
      readConfiguration();
   }
   
   public Hashtable<String, String> getTransportProtocol() {
      return transportProtocol;
   }

   public String getTransportName(String hostname) {
      return (String)transportProtocol.get(hostname);
   }

   public Hashtable<String, String[]> getTransportAttributes() {
      return transportAttributes;
   }

   public String[] getTransportAttributes(String hostname) {
      return (String[])transportAttributes.get(hostname);
   }

   private void readConfiguration() 
   {
      SAXBuilder builder = new SAXBuilder();
      File configFile    = new File(inputFile);
      try {
         Element config;
         config = builder.build(configFile).getRootElement();

         Iterator hostIt = config.getChildren().iterator();
         while (hostIt.hasNext())
         {
            Element host      = (Element)hostIt.next();
            Element transport = (Element)host.getChildren().get(0);
            transportProtocol.put(host.getName(), transport.getName());
            transportAttributes.put(host.getName(),
                  JDOMhelper.getAttributeValues(transport.getAttributes()));
         }

      } catch (Exception e) {
         System.err.println("Could not read Host Config!");
         e.printStackTrace();
         System.exit(-1);
      }
   }
}
