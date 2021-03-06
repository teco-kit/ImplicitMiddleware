package middleware.core;

import java.util.Hashtable;

public class ObjectHeap {

   // We cannot use HashMap, Map or generics in CLDC 1.1
   private Hashtable keyObject   = new Hashtable();
   private Hashtable objectKey   = new Hashtable();
   private Hashtable stubObjects = new Hashtable();
   private Hashtable stubOids    = new Hashtable();
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
   
   public void insertStubObject(Object obj, int oid) {
	  stubObjects.put(obj, new Integer(oid));
	  stubOids.put(new Integer(oid), obj);
   }
   
   public boolean isStub(Object obj) {
	   return stubObjects.contains(obj);
   }
   
   public boolean isStub(int oid) {
	   return stubOids.contains(new Integer(oid));
   }
   
   public Object getStubObject(int objectID) {
	      return stubObjects.get(new Integer(objectID));
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

