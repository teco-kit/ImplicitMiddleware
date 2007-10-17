package model;
/**
 * 
 * Contains the classes that will not be distributed and are available on each machine
 *
 */
public class IgnoredClasses {
	public final static String[] ignored = {
		"java.util.",
		"java.lang.",
		"nullClass"	
	};
	
	public static boolean isIgnored(String classname)
	{
		boolean res = false;
		for (int i = 0; (i < ignored.length) && (res == false); i++)
			if (classname.startsWith(ignored[i]))
				res = true;
		return res;
	}

}
