package analyzer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import model.ClassDesc;
import model.ClassStatistics.ClassCall;

public class SpeedCostFunction extends CostFunction {

	public SpeedCostFunction(Collection<ClassDesc> classes)
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
			
			double time = current.getClassStatistics().getTotalTime();
			time = time / (double) current.getMachine().getSpeed() * 2600;
			System.out.println(current.getName() + " time " + time);
			res += time;

		}
		System.out.println("Total cost " + res);
		return res;
	}
	
	private double maxOrNull(double m1, double m2) {
		if (m1 == 0.0 || m2 == 0.0)
			return 0.0;
		else return (Math.max(m1, m2));
	}


}
