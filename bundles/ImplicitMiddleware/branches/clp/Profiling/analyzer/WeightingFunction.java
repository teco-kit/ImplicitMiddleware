package analyzer;

import java.util.Collection;

import model.ClassDesc;



public abstract class WeightingFunction {
	Collection<ClassDesc> classes;
	public WeightingFunction(Collection<ClassDesc> classes)
	{
		this.classes = classes;
	}
	public abstract double getValue();
}
