package analyzer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import model.ClassDesc;
import model.ClassStatistics.ClassCall;

public class BasicWeightingFunction extends WeightingFunction {

	public BasicWeightingFunction(Collection<ClassDesc> classes)
	{
		super(classes);
	}
	public double getValue() {
		
		double res = 0.0;
		Iterator<ClassDesc> it = classes.iterator();
		while (it.hasNext())
		{
			ClassDesc current = it.next();
			Set<Entry<ClassDesc, ClassCall>> es = current.getClassStatistics().getCalledClasses().entrySet();
			Iterator<Entry<ClassDesc, ClassCall>> calledClassIt = es.iterator();
			while (calledClassIt.hasNext())
			{
				Entry<ClassDesc, ClassCall> currentEntry = calledClassIt.next();
				if (! currentEntry.getKey().getMachine().equals(current.getMachine()))
				{
					double m1 = currentEntry.getKey().getMachine().getDelay();
					double m2 = current.getMachine().getDelay();
					res += (double) (currentEntry.getValue().getNumber()) * maxOrNull(m1, m2);
				}
			}
		}
		return res;
	}
	
	private double maxOrNull(double m1, double m2) {
		if (m1 == 0.0 || m2 == 0.0)
			return 0.0;
		else return (Math.max(m1, m2));
	}

}
