package middleware.core;

import middleware.helper.Debug;

public class Dispatcher implements Runnable {

   ByteStack         msg         = null;
   DispatcherHelper  dispHelper  = null;
   ObjectHeap        objHeap     = null;
   DispatchServer    server      = null;
   private String address        = null;

   Dispatcher() {
   }

   Dispatcher(ByteStack receivedMsg, String address, DispatchServer server) {
      this.msg         = receivedMsg;
      this.address     = address;
      this.server      = server;
      this.dispHelper  = new DispatcherHelper();
      this.objHeap     = ObjectHeap.getInstance();
   }

   public void run() {
      dispatch(msg);
   }

   private void dispatch(ByteStack stack)
   {
      ByteStack returnStack = null;

      int classId = stack.popInt();
      Debug.print("Dispatch Classid " + classId);
      Object obj  = null;
      int objId   = stack.popInt();
      Debug.print("Dispatch objid b " + objId);
      if (objId != 0)
    	  obj  = objHeap.getObject(objId);
      Debug.print("Dispatch objid a " + objId );
      returnStack = 
    	  dispHelper.methodCall(classId, obj, stack.popInt(), stack);
      if (returnStack == null)
      {
         // send some dummy byte
         Debug.print("Dummy byte sent ");
         returnStack = new ByteStack(1);
         returnStack.pushByte((byte)0);
      }
      
      Debug.print("Dispatch answer len " + returnStack.getByteArraySize() );
 
      server.send( returnStack, address );

   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */

