/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package middleware.core;

import middleware.config.Classes;
import middleware.config.ProtocolsConfig;
import middleware.helper.Debug;
import middleware.transport.SendReceive;

public class MethodCallStub {
   private static ObjectHeap objHeap     = ObjectHeap.getInstance();
   private Short             remotePeer  = null;
   private Short             localPeer   = null;
   private SendReceive       socket      = null;
   private ConnectionManager connManager = null;
   private int               classId     = 0;
   private Object            lock        = new Object();
   private ByteStack         retStack    = null;
   private boolean           waitForMe   = false;

   /**
    * Constructor
    * @param the id of the class. Class IDs are used instead of names
    */
   public MethodCallStub(String className)
   {
	  remotePeer   = Classes.getInstance().getHost(className);
	  localPeer    = ProtocolsConfig.getInstance().getHostname();
      connManager  = ConnectionManager.getInstance();
      socket       = connManager.getConnection(remotePeer);
      this.classId = Classes.getInstance().getClassId(className);
      Debug.print("mstub classId " + classId   + 
    		      " className "    + className + 
    		      " remote peer "  + remotePeer);
      StubServer stubServer = new StubServer();
      Thread t1 = new Thread(stubServer);
      t1.start();
   }
   
   private class StubServer implements Runnable {   
	   public void run() {
		  // while (true)
		  // {   
			   retStack = socket.receive();
			   retStack.popShort(); // localHost
			   retStack.popShort(); // remoteHost
			   System.err.println("Stub Server Receive");
			   while(waitForMe) {
				   synchronized(lock) {
					   lock.notify();
				   }
			   }
			   System.err.println("Stub After Receive");
		 //  }
	   } 
   }

   /**
    * This method is called from the stub classes/objects to make a remote call
    * This method is called independent of what call is made (instancing,
    * static or non-static)
    * @param objectId the id of the object known by the remote dispatcher
    * @param methodId the id of the method. Methods are enumerated from top to
    * bottom instead of using their names
    * @param stack    the stack that contains all arguments for the method call
    */
   public ByteStack callStubMethod( int       objectId,
                                    int       methodId, 
                                    ByteStack stack    )
   {
      if (stack == null)
         stack = new ByteStack(32);
      
      stack.pushInt(methodId);
      stack.pushInt(objectId);
      stack.pushInt(classId);
      stack.pushShort(localPeer.shortValue());
      stack.pushShort(remotePeer.shortValue());
      waitForMe = true;
      socket.send(stack);

      Debug.print("Waiting for aswer");
      
      ByteStack returnStack = null;// = socket.receive();
      synchronized(lock) {
    	  try {
    		  lock.wait();
    		  waitForMe = false;
    		  returnStack = retStack;
    	  } catch (InterruptedException e) {
    		  // TODO Auto-generated catch block
    		  e.printStackTrace();
    	  }
      }
      Debug.print("retStack len " + returnStack.getByteArraySize());
      //FIXME call decrement somewhere
      //connManager.decrement(remotePeer, socket);

      return returnStack;
   }

   /**
    * Checks if an object is local to this machine, if this is the case it is
    * added to the local object heap in order for the dispatcher to find it
    * afterwards.
    * @param  obj the object
    * @return     the objectId of the object
    */
   public static int getOidForObject(Object obj) {
      if (objHeap.isStub(obj)) {
         return ((UniqueID) obj).getUniqueID();
      } else {
    	 int ret = objHeap.insertObject(obj);
    	 System.out.println("Object id " + ret);
    	 return ret;
      }
   }
   
   public static Object getObjectOrStub(int oid)
   {
       Object obj = objHeap.getObject(oid);
       if(obj != null)
           return obj;
       else
       {
          if (objHeap.isStub(oid))
        	  return (Object)objHeap.getStubObject(oid);
          else
        	  return null;
       }
   }

   /**
    * Inserts an object to the heap of remote objects This function has to be
    * called in the constructor of each stub in order to keep knowledge of 
    * remote and local objects
    * @param obj the object to be inserted
    */
   public void addStubObject2Heap(Object obj, int oid) {
      objHeap.insertStubObject(obj, oid);
   }

   /**
    * This function returns an object form an object id
    * The object has to be local of course
    * @param oID the ID of the wanted object
    */
   public static Object getObjectFromOID(int oID) {
      return objHeap.getObject(oID);
   }
}

// vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3:

