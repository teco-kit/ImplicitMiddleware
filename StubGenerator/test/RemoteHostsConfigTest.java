package test;

import generator.RemoteHostsConfig;

import java.util.HashMap;

import junit.framework.TestCase;


public class RemoteHostsConfigTest extends TestCase {

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
	  HashMap<String, Short> map = new HashMap<String, Short>();
	  map.put("Ahilis", new Short((short)1));

      RemoteHostsConfig config = new RemoteHostsConfig("hosts.xml", map);

      config.writeClassToFile("../Middleware/middleware/config/RemoteConfig.class");

      middleware.config.RemoteConfig remoteConf =
         middleware.config.RemoteConfig.getInstance();



      //assertTrue(remoteConf.getTransportName("ahilis").
      //                      equals("transport.TCPClient"));
     // String[] params =
       //  remoteConf.getTransportAttributes("ahilis");
      //assertTrue(params[0].equals("127.0.0.1"));
      //assertTrue(params[1].equals("10001"));

   }

}
