package middleware.core;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class RunServer extends MIDlet {
   // Once if we are a normal host app
   public static void main(String args[])
   {
      middleware.core.DispatchServer.startServer();
   }
   
   // tree times if we are a midlet
   protected void startApp() throws MIDletStateChangeException {
      DispatchServer.startServer();
   }

   protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
   }

   protected void pauseApp() {
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
