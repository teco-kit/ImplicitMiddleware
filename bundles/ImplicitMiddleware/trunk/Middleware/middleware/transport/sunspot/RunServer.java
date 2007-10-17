/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package middleware.transport.sunspot;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class RunServer extends MIDlet { 
   // tree times if we are a midlet
   protected void startApp() throws MIDletStateChangeException {
      middleware.core.DispatchServer.startServer();
   }

   protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
   }

   protected void pauseApp() {
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
