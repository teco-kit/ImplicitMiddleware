package middleware.config;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import middleware.helper.JDOMhelper;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class Hosts {
   private        Hashtable transportAttributes; // <String, String[]>
   private        Hashtable transportProtocol;   // <String, String>
   private static String    inputFile = "hosts.xml";
   
   public Hosts()
   {
      transportAttributes = new Hashtable();
      transportProtocol   = new Hashtable();
      readConfiguration();
   }
   
   public Hosts(String file)
   {
      inputFile           = file;
      transportAttributes = new Hashtable();
      transportProtocol   = new Hashtable();
      readConfiguration();
   }
   
   public Hashtable getTransportProtocol() {
      return transportProtocol;
   }

   public String getTransportName(String hostname) {
      return (String)transportProtocol.get(hostname);
   }

   public Hashtable getTransportAttributes() {
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
