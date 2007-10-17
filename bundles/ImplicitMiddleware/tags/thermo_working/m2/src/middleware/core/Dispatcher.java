package middleware.core;

import middleware.transport.SendReceive;
import middleware.helper.Debug;

public class Dispatcher implements Runnable {

   SendReceive       socket      = null;
   ByteStack         initialMsg  = null;
   DispatcherHelper  dispHelper  = null;
   ConnectionManager connManager = null;
   ObjectHeap        objHeap     = null;

   Dispatcher() {
   }

   Dispatcher(ByteStack receivedMsg, SendReceive socket) {
      this.socket      = socket;
      this.initialMsg  = receivedMsg;
      this.dispHelper  = new DispatcherHelper();
      this.connManager = ConnectionManager.getInstance();
      this.objHeap     = ObjectHeap.getInstance();
   }

   public void run() {
      dispatch(initialMsg);
      while (socket.isConnected())
         dispatch(socket.receive());
   }

   private void dispatch(ByteStack stack)
   {
      String    remoteHost;
      ByteStack returnStack = null;
         
      if (stack == null)
         return;
      
      remoteHost = stack.popString();
      Debug.print("Dispatching msg from " + remoteHost);

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
         returnStack.pushInt(0);
      }
      
      Debug.print("Dispatch String len " + returnStack.getByteArraySize() );
 
      connManager.add(remoteHost, socket);
      connManager.increment(remoteHost, socket);
      
      socket.send( returnStack );

      connManager.decrement(remoteHost, socket);
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */

