
import generator.Class2HostConfig;
import generator.ClassGenerator;
import generator.DispatcherHelperWriter;
import generator.HostProtocolsConfig;
import generator.RemoteHostsConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.util.Hashtable;

//import stub.XMLVMStub;

public class Distributor {

   static File config = new File("../Thermometer/repartition.xml");
   static File appDir = new File("../Thermometer");
   static SAXBuilder xmlBuilder    = new SAXBuilder();
   static String     corePath      = "middleware/core";
   static String     helperPath    = "middleware/helper";
   static String     transportPath = "middleware/transport";
   static String[]   neededSources = 
   { 
      corePath + "/ByteStack",
      corePath + "/Connection",
      corePath + "/ConnectionManager",
      corePath + "/Dispatcher",
      corePath + "/DispatchServer",
      corePath + "/MethodCallStub",
      corePath + "/ObjectHeap",
      corePath + "/RunServer",
      corePath + "/UniqueID",
      helperPath + "/Debug",  
      transportPath + "/SendReceive",
      transportPath + "/SendReceiveServer",
      transportPath + "/TransportFactory",
      transportPath + "/sunspot/SunSpotConnection",
      transportPath + "/sunspot/SunSpotServer"
   };


   /**
    * Usage: args[0] is the path of the config xml file and args[1] 
    * is the path of the original app
    *
    * @param args
    */
   @SuppressWarnings("unchecked")
      public static void main(String args[])
      {
         if (args.length >= 2)
         {
            config = new File(args[0]);
            appDir = new File(args[1]);
         }
         try {
            System.out.println("Config file = " + config.getCanonicalPath());
            System.out.println("Application path = " + appDir.getCanonicalPath());
         } catch (IOException e) {
            System.err.println("Argument(s) invalid");
            e.printStackTrace();
            System.exit(0);
         }

         try {
            HashMap<String,Integer> class2classId = 
               new HashMap<String, Integer>();
            Element rootElement = xmlBuilder.build(config).getRootElement();
            Element machines    = rootElement.getChild("machines");
            Iterator<Element> machIt = machines.getChildren().iterator();

            while (machIt.hasNext())
            {
               int currClassId = 0;
               Element curMach = machIt.next();
               Iterator<Element> classIt = 
                  curMach.getChildren("class").iterator();

               while (classIt.hasNext())
               {
                  String nextClass = classIt.next().getAttributeValue("name");
                  class2classId.put(nextClass, ++currClassId);

               }
            }

            // Generate the Stubs
            putStubAndConfig(machines, rootElement, class2classId);

            // Generate the Dispatcher Helper file
            // Copy all classes and interfaces
            putDispHelper(machines, class2classId);

         } catch (JDOMException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

      }


   private static void putDispHelper(Element machines, HashMap<String,Integer> class2classId) {
      Iterator<Element> machIt = machines.getChildren().iterator();
      machIt = machines.getChildren().iterator();

      while (machIt.hasNext())
      {
         Element curMach = machIt.next();
         String machName = curMach.getName();
         Iterator<Element> classIt = curMach.getChildren("class").iterator();
         Iterator<Element> intfIt  = curMach.getChildren("interface").iterator();
         Iterator<Element> stubIt  = curMach.getChildren("stub").iterator();

         Vector<String> stubClasses = new Vector<String>();
         while (stubIt.hasNext())
         {
            String nextStub = stubIt.next().getAttributeValue("name");
            stubClasses.addElement(nextStub.replace('.', '/'));
         }

         DispatcherHelperWriter dspHelper =
            new DispatcherHelperWriter(curMach.getChildren("class").size(),
                                       stubClasses);

         ClassGenerator dispGenerator = null;

         while (classIt.hasNext())
         {
            String nextClass = classIt.next().getAttributeValue("name");

            dispGenerator = 
               new ClassGenerator(getInFile(machName, nextClass),
                                  class2classId.get(nextClass), 
                                  dspHelper);
            putSource(machName, nextClass);
         }

         dspHelper.allGoodThingsCome2AnEnd();
         if (dispGenerator != null)
         {
            String outFile;

            outFile = getOutFile(machName, 
                                 "middleware.core.DispatcherHelper");
            dispGenerator.writeDispatcherToFile(outFile);
         }


         // copy all intarface files
         while (intfIt.hasNext())
            putSource(machName, intfIt.next().getAttributeValue("name"));
      }
   }

   private static void putStubAndConfig(Element                 machines, 
                                        Element                 rootElement,
                                        HashMap<String,Integer> class2classId) 
   {
      Iterator<Element> machIt = machines.getChildren().iterator();

      try {
         machIt = machines.getChildren().iterator();
         while (machIt.hasNext())
         {
            Element curMach = machIt.next();
            String machName = curMach.getName();
            Iterator<Element> stubIt = curMach.getChildren("stub").iterator();
            Hashtable class2Host = new Hashtable();
            Hashtable classIds   = new Hashtable();
            while (stubIt.hasNext())
            {
               String nextStub = stubIt.next().getAttributeValue("name");

               ClassGenerator classGenerator;
               classGenerator = new ClassGenerator(getInFile(machName, nextStub),
                                                   (String)null);
               classGenerator.writeClassToFile(getOutFile(machName, nextStub));


               XPath xpathExpr = XPath.newInstance("//class[@name='" +
                                                   nextStub + "']/..");
               Element realHostNode =
                  (Element)(xpathExpr.selectSingleNode(rootElement));

               class2Host.put(nextStub.replace('.', '/'), 
                              realHostNode.getName());
               classIds.put(nextStub.replace('.', '/'), 
                            class2classId.get(nextStub));
               // putStub(machName, nextStub);
            }
            Class2HostConfig config = new Class2HostConfig(class2Host,
                                                           classIds);
            config.writeClassToFile(getOutFile(machName,
                                               "middleware.config.Classes"));

            RemoteHostsConfig   remoteHosts = 
               new RemoteHostsConfig("../" + machName + "/hosts.xml");
            remoteHosts.writeClassToFile(
                                         getOutFile(machName,
                                                    "middleware.config.RemoteConfig"));
            HostProtocolsConfig hostConfig  = 
               new HostProtocolsConfig("../" + machName + "/config.xml");
            hostConfig.writeClassToFile(getOutFile(machName,
                                                   "middleware.config.ProtocolsConfig"));

            putMiddlewareSources(machName);

         }
      } catch (JDOMException e) {
            e.printStackTrace();
      }
   }
   //private static void putClassesConfig(String machName, classes) {
   //   Hashtable class2host = new Hashtable();

   //   class2host.put(machName, classId++);
   //}


   private static void putSource(String machName, String className)
   {
      String target = ("../" + machName + "/src");
      File original = new File (getInFile(machName, className));
      File copy     = new File (getOutFile(machName, className));

      //fileCopy(original, copy);
      try {
         original = new File (appDir + "/" + getFile(className) + ".java");
         if (!original.canRead())
            original = 
               new File (appDir + "/src/" + getFile(className) + ".java");
         copy     = new File (target + "/" + getFile(className) + ".java");

         fileCopy(original, copy);
      }
      catch (Exception e)
      {
         System.err.println("could not find source: " + original.getAbsolutePath());
      }
   }

   private static void putMiddlewareSources(String machName)
   {
      String target = ("../" + machName + "/src");
      String middlewarePath = "../Middleware/";
      File   original;
      File   copy;

      for (String className : neededSources)
      {

         try {
            original = 
               new File (middlewarePath + getFile(className) + ".java");
            copy     = 
               new File (target + "/" + getFile(className) + ".java");

            fileCopy(original, copy);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            System.err.println("could not find source: " + 
                               middlewarePath + getFile(className) + ".java");
         }
      }
   }

   //private static void putStub(String machName, String className)
   //{
   //File original = new File (getInFile(machName, className));
   //File stub     = new File (getOutFile(machName, className));

   //try {
   //String[] args={original.getCanonicalPath(), stub.getCanonicalPath()};
   //XMLVMStub.main(args);
   //}
   //catch (IOException e)
   //{
   //	e.printStackTrace();
   //}
   //}

   private static String getInFile(String machName, String className)
   {
      File f = new File(appDir + "/" + getFile(className) + ".class");
      if (!f.canRead())
         f = new File (appDir + "/bin/" + getFile(className) + ".class");
      if (!f.canRead())
         f = new File (appDir + "/build/" + getFile(className) + ".class");

      return (f.getAbsolutePath());
   }

   private static String getOutFile(String machName, String className)
   {
      String target = ("../" + machName + "/src");
      String fileName = target + "/" +
         getFile(className) +
         ".class";
      File outFile    = new File (fileName);

      outFile.getParentFile().mkdirs();
      return (fileName);
   }


   private static String getFile(String className)
   {
      String packagePath = "";

      if (className.lastIndexOf(".") > 0)
      {
         packagePath = 
            className.substring(0, (className.lastIndexOf(".") + 1));
         className   = 
            className.substring(className.lastIndexOf(".") + 1);
      }

      packagePath = packagePath.replace('.', '/');

      System.out.println(packagePath);
      System.out.println(className);

      return ( packagePath + className );
   }


   private static void fileCopy(File fromFile, File toFile)
   {
      FileInputStream from = null;
      FileOutputStream to  = null;
      try {
         from = new FileInputStream(fromFile);
         toFile.getParentFile().mkdirs();
         to = new FileOutputStream(toFile);
         byte[] buffer = new byte[4096];
         int bytesRead;

         while ((bytesRead = from.read(buffer)) != -1)
            to.write(buffer, 0, bytesRead); // write
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally {
         if (from != null)
            try {
               from.close();
            } catch (IOException e) {
               ;
            }
         if (to != null)
            try {
               to.close();
            } catch (IOException e) {
               ;
            }
      }

   }

   public static void writeXMLFile(Document doc, File f)
   {
      XMLOutputter outputter = new XMLOutputter();
      outputter.setFormat(Format.getPrettyFormat());

      try {
         outputter.output(doc, new FileOutputStream(f));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
