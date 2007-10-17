package middleware.config;

import helper.JDOMhelper;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;


import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * Reads the file config.xml and gathers information such as the present host
 * name, used protocols and so on.
 * Implemented as a singleton
 * @author Jean-Thomas Célette
 *
 */
public class Config {
   private static String inputFile = "config.xml";
   private String                      hostname  = "";
   private Element                     config    = null;
   private Element                     transport = null;
   private Hashtable<String, String[]> protocols = null; 

   public Config()
   {
      protocols = new Hashtable<String, String[]>();
      readConfiguration();
   }
   
   public Config(String file)
   {
      inputFile = file;
      protocols = new Hashtable<String, String[]>();
      readConfiguration();
   }

   private void readConfiguration()
   {
      SAXBuilder builder = new SAXBuilder();
      File configFile    = new File(inputFile);
      try {
         config = builder.build(configFile).getRootElement();
         hostname = config.getChild("name").getAttributeValue("hostname");
         transport = config.getChild("transport");
         //Iterator<Element> protIt = transport.getChildren().iterator();
         Iterator protIt = transport.getChildren().iterator();
         while (protIt.hasNext())
         {
            Element prot = (Element)protIt.next();
            protocols.put(prot.getName(),
                          JDOMhelper.getAttributeValues(prot.getAttributes()));
         }

      } catch (JDOMException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * @return  the hostname
    *
    */
   public String getHostname() {
      return hostname;
   }

   public Hashtable<String, String[]> getProtocols() {
      return protocols;
   }

}

