
/**
 * This is only a wrapper class to let other classes compile and to be used as
 * skeleton for the real one which is generated
 */
package middleware.core;


public class DispatcherHelper {
	ObjectHeap objHeap = null;

   public DispatcherHelper() {
      objHeap = ObjectHeap.getInstance();
   };

   public ByteStack methodCall(int classId,  Object    obj,
		                       int methodId, ByteStack stack) 
   {  
      return null;
   }
}
