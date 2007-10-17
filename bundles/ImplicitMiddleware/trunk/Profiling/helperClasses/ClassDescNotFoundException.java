package helperClasses;

public class ClassDescNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ClassDescNotFoundException(String name)
	{
		System.err.println("No such ClassDesc: " + name);
	}

}
