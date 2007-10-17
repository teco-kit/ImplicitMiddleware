package model;

import java.util.HashMap;
import java.util.Map;


public class ClassStatistics {
	public class ClassCall{
		private int number = 0;
		private double totalCallTime = 0.0;
				
		public void addCall(double callTime)
		{
			number++;
			totalCallTime += callTime;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public double getTotalCallTime() {
			return totalCallTime;
		}

		public void setTotalCallTime(double totalCallTime) {
			this.totalCallTime = totalCallTime;
		}
	}
	//Which classes have been called?
	private Map<ClassDesc, ClassCall> calledClasses = new HashMap<ClassDesc, ClassCall>();
	//How many times has this class been called
	private int totalCalls = 0;
	//Total time spent in this class
	private double totalTime = 0.0;
	//How many instances were created
	private int instances = 0;
	
	public void addCallToClass(ClassDesc cd, double callTime)
	{
		ClassCall cc;
		if (calledClasses.containsKey(cd))
			cc = calledClasses.get(cd);

		else 
		{
			cc = new ClassCall();
			calledClasses.put(cd, cc);
		}
		cc.addCall(callTime);
	
	}

	public int getInstances() {
		return instances;
	}
	public void addInstance() {
		instances++;
	}
	
	public void addCall(double callTime) {
		totalCalls++;
		totalTime += callTime;
	}
	
	public int getTotalCalls() {
		return totalCalls;
	}
	public void setTotalCalls(int totalCalls) {
		this.totalCalls = totalCalls;
	}

	
	public double getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public Map<ClassDesc, ClassCall> getCalledClasses() {
		return calledClasses;
	}


	
}
