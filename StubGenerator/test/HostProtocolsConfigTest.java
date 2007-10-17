package test;

import java.util.Hashtable;
import junit.framework.TestCase;

import generator.HostProtocolsConfig;


public class HostProtocolsConfigTest extends TestCase {

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

      HostProtocolsConfig config = new HostProtocolsConfig("config.xml");

      config.writeClassToFile("../Middleware/middleware/config/ProtocolsConfig.class");

      middleware.config.ProtocolsConfig protArgs =
         middleware.config.ProtocolsConfig.getInstance();

      //assertTrue(protArgs.getHostname().equals("hektor"));
      Hashtable table = protArgs.getProtocols();
      //String[] params = (String[]) table.get("transport.TCPServer");
      //assertTrue(params[0].equals("127.0.0.1"));
      //assertTrue(params[1].equals("10001"));

   }

}
