package middleware.core;

import java.util.Hashtable;
import java.util.Vector;

public class ObjectHeap {

   // We cannot use HashMap, Map or generics in CLDC 1.1
   private Hashtable keyObject   = new Hashtable();
   private Hashtable objectKey   = new Hashtable();
   private Vector    stubObjects = new Vector();
   private static ObjectHeap instance = null;

   private ObjectHeap() {
      // Exists only to defeat instantiation.
   }

   public static ObjectHeap getInstance() {
      if(instance == null) {
         instance = new ObjectHeap();
      }
      return instance;
   }

   public int insertObject(Object obj) {
      int oID = getUniqueID(obj);

      keyObject.put(new Integer(oID), obj);
      objectKey.put(obj, new Integer(oID));

      return oID;
   }
   
   public void insertStubObject(Object obj) {
	  stubObjects.addElement(obj);
   }
   
   public boolean isStub(Object obj) {
	   return stubObjects.contains(obj);
   }

   public Object getObject(int objectID) {
      return keyObject.get(new Integer(objectID));
   }

   /**
    * Searches for the specified object in the HashMap and returns its ID
    * otherwise returns a new ID
    * @param o
    * @return
    */
   public int getUniqueID(Object o) {
      if (containsObject(o)) {
         return ((Integer)objectKey.get(o)).intValue();
      } else {
         int res = o.hashCode();
         while (keyObject.containsKey(new Integer(res)))
            res = o.hashCode()+1;
         return res;
      }
   }

   public boolean containsUniqueID(String key) {
      return keyObject.containsKey(key);
   }

   public boolean containsObject(Object o) {
      // TODO containsValue ???
      return keyObject.contains(o);
   }

   public void removeObjectByID(String objectID) {
      objectKey.remove(keyObject.get(objectID));
      keyObject.remove(objectID);
   }

   public void removeObjectByObject(Object o) {
      keyObject.remove(objectKey.get(o));
      objectKey.remove(o);
   }
}

