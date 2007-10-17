// Dummy class
package middleware.config;

import java.util.Hashtable;

public class Classes
{
    private Classes()
    {
       class2Host = new Hashtable();
       class2Host.put(new Integer(1), new String("Afrodita"));
    }

    public String getHost(String className)
    {
        return null;
    }
    
    public int getClassId(String className)
    {
        return 0;
    }
    
    static public Classes getInstance()
    {
    	return null;
    }

    private Hashtable class2Host = null;
}
