// Dummy class
package middleware.config;

import java.util.Hashtable;

public class Classes
{
    private Classes()
    {
       class2Host = new Hashtable();
       class2Host.put(new Integer(1), new Short((short)1));
    }

    public Short getHost(String className)
    {
        return null;
    }
    
    public int getClassId(String className)
    {
        return 0;
    }
    
    public static Classes getInstance()
    {
    	return null;
    }

    private Hashtable class2Host = null;
}
