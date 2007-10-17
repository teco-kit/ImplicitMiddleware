package analyzer;

import java.util.Collection;

import model.ClassDesc;



public abstract class CostFunction {
	Collection<ClassDesc> classes;
	public CostFunction(Collection<ClassDesc> classes)
	{
		this.classes = classes;
	}
	public abstract double getValue();
}
