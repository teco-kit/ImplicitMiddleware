package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class Machine {
	private static Vector<Machine> machines = new Vector<Machine>();
	private static Map<String, Machine> machineDict = new HashMap<String, Machine>();
	private static int idCounter = 0;
	public static Machine localMachine = new Machine("localMachine", "for classes that are on each machine", Integer.MAX_VALUE, Integer.MAX_VALUE, 0.0, Double.POSITIVE_INFINITY);
	
	private int machineID;
	private String machineName;
	private String description;
	private int memory;
	private int speed;
	
	private double delay;
	private double throughput;
	
	


	public Machine(String machineName, String description, int memory, int speed, double delay, double throughput) {
		this.machineName = machineName;
		this.description = description;
		this.memory = memory;
		this.speed = speed;
		this.delay = delay;
		this.throughput = throughput;
		machineID = idCounter;
		idCounter++;
		machines.add(this);
		machineDict.put(machineName, this);
	}
	
	public double getDelay() {
		return delay;
	}
	public String getDescription() {
		return description;
	}
	public String getMachineName() {
		return machineName;
	}
	public int getMemory() {
		return memory;
	}
	public int getSpeed() {
		return speed;
	}
	public double getThroughput() {
		return throughput;
	}
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((machineName == null) ? 0 : machineName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Machine other = (Machine) obj;
		if (machineName == null) {
			if (other.machineName != null)
				return false;
		} else if (!machineName.equals(other.machineName))
			return false;
		return true;
	}
	
	public static Machine getFirstMachine()
	{
		return machines.elementAt(1); //because first element is the stub for the local machine
	}
	public static boolean hasNextMachine(Machine m)
	{
		if (machines.size() > m.machineID + 1)
			return true;
		return false;
	}
	
	public static Machine getNextMachine(Machine m)
	{
		if (hasNextMachine(m))
			return machines.elementAt(m.machineID + 1);
		else
			throw new java.lang.NullPointerException();
	}
	
	public static Machine getMachineByName(String name)
	{
		return machineDict.get(name);
	}
	
	public int getMachineID() {
		return machineID;
	}

	public static Vector<Machine> getMachinesVector() {
		return machines;
	}
}
