
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
      ByteStack retStack;
      
	  switch (classId) {
         case 1: {
            switch (methodId) {
               case 1:
                  retStack = new ByteStack(4);
                  ByteStack rokoko = new ByteStack(stack.popInt());
                  retStack.pushInt(objHeap.insertObject(rokoko));
                  return retStack;
               case 3:
                  retStack = new ByteStack(4);
                  System.out.println("xxx");
                  ByteStack rokok = new ByteStack(stack.popInt());
                  retStack.pushInt(objHeap.insertObject(rokok));
                  return retStack;
               case 5:
                  return null;

               default:
                  break;
            }

         }
      	case 2:
      		return new ByteStack(stack.popInt());
      	case 3:
      	   return new ByteStack(stack.popInt());
      	default:
      	   break;
      }
      
      return null;
   }
}
