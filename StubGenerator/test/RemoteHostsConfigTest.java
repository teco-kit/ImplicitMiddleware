package test;

import junit.framework.TestCase;

import generator.RemoteHostsConfig;


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

      RemoteHostsConfig config = new RemoteHostsConfig("hosts.xml");

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
