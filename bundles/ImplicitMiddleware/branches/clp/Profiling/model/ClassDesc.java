package model;

import helperClasses.HashCode;

public class ClassDesc {
	private String name;
	private HashCode id;
	private String sourceName;
	
	private Machine machine = null;
	
	private ClassStatistics classStatistics;

	public static ClassDesc nullClass = new ClassDesc("nullClass", new HashCode(-1),"");
	
	public ClassDesc(String name, HashCode id, String sourceName) {
		this.name = name;
		this.id = id;
		this.sourceName = sourceName;
	}

	public boolean equals(Object o)
	{
		if (o.getClass().equals(this.getClass()))
		{
			return (this.id == ((ClassDesc) o).getId());
		}
		return false;
	}
	
	public int hashCode()
	{
		return id.hashCode();
	}

	public HashCode getId() {
		return id;
	}

	public void setId(HashCode id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public ClassStatistics getClassStatistics() {
		if (classStatistics == null)
			this.classStatistics = new ClassStatistics();
		return classStatistics;
	}

	public void setClassStatistics(ClassStatistics classStatistics) {
		this.classStatistics = classStatistics;
	}

	public Machine getMachine() {
		return machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	
}
