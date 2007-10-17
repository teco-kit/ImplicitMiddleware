package model;

import helperClasses.HashCode;
import analyzer.MethodCallAnalyzer;

public class MethodDesc {
	private HashCode methodId;
	private String name;
	private ClassDesc classDesc;
	private int startLineNumber = -1;
	private int endLineNumber = -1;
	
	public static MethodDesc nullMethod = new MethodDesc(new HashCode(-1), "nullMethod", new HashCode(-1), -1, -1);
	
	public MethodDesc(HashCode methodId, String name, HashCode classIdRef, int startLineNumber, int endLineNumber) {
		this.methodId = methodId;
		this.name = name;
		this.classDesc = MethodCallAnalyzer.classes.get(classIdRef);
		this.startLineNumber = startLineNumber;
		this.endLineNumber = endLineNumber;
	}
	
	public boolean equals(Object o)
	{
		if (o.getClass().equals(this.getClass()))
		{
			return (this.methodId == ((MethodDesc) o).getMethodId());
		}
		return false;
	}
	
	public int hashCode()
	{
		return methodId.hashCode();
	}
	
	public ClassDesc getClassDesc() {
		return classDesc;
	}
	
	public void setClassDesc(ClassDesc clazz) {
		this.classDesc = clazz;
	}
	public void setClassDescByClassIdRef(int classIdRef) {
		this.classDesc = MethodCallAnalyzer.classes.get(new HashCode(classIdRef));
	}
	public int getEndLineNumber() {
		return endLineNumber;
	}
	public HashCode getMethodId() {
		return methodId;
	}
	public void setMethodId(HashCode methodId) {
		this.methodId = methodId;
	}
	public String getName() {
		return name;
	}
	public int getStartLineNumber() {
		return startLineNumber;
	}

}
