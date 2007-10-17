package middleware.core;

import middleware.config.Classes;
import middleware.config.ProtocolsConfig;
import middleware.transport.SendReceive;
import middleware.helper.Debug;

public class MethodCallStub {
   private static ObjectHeap objHeap     = ObjectHeap.getInstance();
   private String            remotePeer  = null;
   private String            hostname    = null;
   private SendReceive       socket      = null;
   private ConnectionManager connManager = null;
   private int               classId     = 0;

   /**
    * Constructor
    * @param the id of the class. Class IDs are used instead of names
    */
   public MethodCallStub(String className)
   {
      hostname     = ProtocolsConfig.getInstance().getHostname();
      remotePeer   = Classes.getInstance().getHost(className);
      connManager  = ConnectionManager.getInstance();
      socket       = connManager.getConnection(remotePeer);
      this.classId = Classes.getInstance().getClassId(className);
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
      Debug.print("method id " + methodId);
      stack.pushInt(objectId);
      Debug.print("objectId " + objectId);
      stack.pushInt(classId);
      Debug.print("class id " + classId);
      // put the hostname in the packet to let the remote station know the sender
      stack.pushString(hostname);
      socket.send(stack);

      connManager.increment(remotePeer, socket);
      Debug.print("Waiting for aswer");
      
      ByteStack returnStack = socket.receive();
      Debug.print("retStack len " + returnStack.getByteArraySize());
      connManager.decrement(remotePeer, socket);

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
         return objHeap.insertObject(obj);
      }
   }

   /**
    * Inserts an object to the heap of remote objects This function has to be
    * called in the constructor of each stub in to keep knowledge of remote and
    * local objects
    * @param obj the object to be inserted
    */
   public void addStubObject2Heap(Object obj) {
      objHeap.insertStubObject(obj);
   }

   /**
    * This function returns an object form an object id
    * The object has to be local of course
    * @param oID the ID of the wanted object
    */
   public Object getObjectFromOID(int oID) {
      return objHeap.getObject(oID);
   }
}

/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */

