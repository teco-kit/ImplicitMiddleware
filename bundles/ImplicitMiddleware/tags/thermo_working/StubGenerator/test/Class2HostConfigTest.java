package test;

import generator.Class2HostConfig;

import java.util.Hashtable;

import junit.framework.TestCase;

public class Class2HostConfigTest extends TestCase {

   protected void setUp() throws Exception {
      super.setUp();
   }

   protected void tearDown() throws Exception {
      super.tearDown();
   }

   public void runTest() {
      testIt();
   }

   public void testIt() {
      Hashtable table = new Hashtable();
      table.put("Hera", "Hektor");
      table.put("Afrodita", "Ahil");
      table.put("Atina", "Ares");
      
      Hashtable table2 = new Hashtable();
      table2.put("Hera", 1);
      table2.put("Afrodita", 2);
      table2.put("Atina", 3);

      Class2HostConfig config = new Class2HostConfig(table, table2);

      //config.writeClassToFile("Classes.class");
      config.writeClassToFile("../Middleware/middleware/config/Classes.class");

      middleware.config.Classes clazz2Host =
         middleware.config.Classes.getInstance();

      assertTrue(clazz2Host.getHost("Hera").equals("Hektor"));
      assertTrue(clazz2Host.getHost("Afrodita").equals("Ahil"));
      assertTrue(clazz2Host.getHost("Atina").equals("Ares"));
      
      assertTrue(clazz2Host.getClassId("Hera")     == 1);
      assertTrue(clazz2Host.getClassId("Afrodita") == 2);
      assertTrue(clazz2Host.getClassId("Atina")    == 3);
   }
}
