package middleware.core;
/**
 * This class' only purpose is to allow the generic addition of a constructor to an existing class
 * without conflicting with an existing constructor. Such a constuctor is only usefull
 * for classes that are represented by stubs on the current JavaVM. It should be
 * ClassName(UniqueID id){this.objectID = id.getUniqueID;)
 * Such a constructor is necessary if a stub of an object of a remote class
 * that already exists in the distributed application is needed on the current JavaVM.
 * While such a constuctor only creates a local object and sets its stub objectID, 
 * the use of any other constructor would imply the creation of a new remote Object.
 * 
 * 
 * @author Jean-Thomas Célette
 *
 */
public class UniqueID {
	/**
	 * @uml.property  name="uniqueID"
	 */
	private int uniqueID;
	
	public UniqueID(int objectID)
	{
	   this.uniqueID = objectID;
	}
	
   // public String toString()
   // {
   //    return uniqueID;
   // }
	
	/**
	 * @return  the uniqueID
	 * @uml.property  name="uniqueID"
	 */
	public int getUniqueID()
	{
		return uniqueID;
	}

	/**
	 * @param uniqueID  the uniqueID to set
	 * @uml.property  name="uniqueID"
	 */
	public void setUniqueID(int objectID)
	{
		this.uniqueID = objectID;
	}
	
	public boolean equals(Object o)
	{
		if(o.getClass().equals(this.getClass()))
		{
			if ( ((UniqueID) o).getUniqueID() == this.getUniqueID() )
					return true;
		}
		return false;
	}
}

