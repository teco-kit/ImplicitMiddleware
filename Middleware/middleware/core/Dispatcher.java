/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package middleware.core;

import middleware.config.ProtocolsConfig;
import middleware.helper.Debug;
import middleware.transport.SendReceive;

public class Dispatcher implements Runnable {

   private ByteStack         msg         = null;
   private DispatcherHelper  dispHelper  = null;
   private ObjectHeap        objHeap     = null;
   private SendReceive       conn        = null;
   private String            address     = null;
   private Short             remoteHost  = null;
   private Short             localHost   = null;


   public Dispatcher(ByteStack      receivedMsg, 
                     String         address, 
                     Short          remoteHost, 
                     SendReceive    conn) 
   {
      this.msg         = receivedMsg;
      this.address     = address;
      this.remoteHost  = remoteHost;
      this.conn        = conn;
      this.dispHelper  = new DispatcherHelper();
      this.objHeap     = ObjectHeap.getInstance();
	  localHost        = ProtocolsConfig.getInstance().getHostname();
   }

   public void run() {
      dispatch(msg);
   }

   private void dispatch(ByteStack stack)
   {
      ByteStack returnStack = null;

      int classId = stack.popInt();
      Object obj  = null;
      int objId   = stack.popInt();
      
      if (objId != 0)
         obj  = objHeap.getObject(objId);

      returnStack =
         dispHelper.methodCall(classId, obj, stack.popInt(), stack);
      if (returnStack == null)
      {
         // send some dummy byte
         returnStack = new ByteStack(1);
         returnStack.pushByte((byte)0);
      }

      Debug.print("Dispatch answer len " + returnStack.getByteArraySize() );
      Debug.print("Dispatch remote host " + remoteHost );

      returnStack.pushShort(localHost.shortValue());
      returnStack.pushShort(remoteHost.shortValue());
      Debug.print("Dispatcher address " + address);
      conn.send( returnStack, address );
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */

