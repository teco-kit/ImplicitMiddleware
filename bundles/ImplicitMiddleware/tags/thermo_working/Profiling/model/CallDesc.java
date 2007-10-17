package model;

import helperClasses.HashCode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class CallDesc {
	private ThreadDesc thread;
	private MethodDesc method;
	private double entryTime;
	private double exitTime = 0;
	private double overhead = 0;
	private int stackDepht;
	private HashCode ticket;
	private double cumulativeTime = 0;
	private double baseTime = Double.NaN;
	
	private CallDesc parent = null;
	private List<CallDesc> children = new LinkedList<CallDesc>();
	
	public CallDesc(ThreadDesc thread, MethodDesc method, double entryTime, HashCode ticket, int stackDepht, CallDesc parent) {
		this.thread = thread;
		this.method = method;
		this.entryTime = entryTime;
		this.stackDepht = stackDepht;
		this.ticket = ticket;
		this.parent = parent;
	}
	
	public void addChild(CallDesc cd)
	{
		children.add(cd);
	}
	
	public void terminate(double exitTime, double overhead)
	{
		this.exitTime = exitTime;
		this.overhead = overhead;
		this.cumulativeTime = this.exitTime - this.entryTime;
	}
	
	public double getBaseTime()
	{
		if ( Double.doubleToLongBits(baseTime) == Double.doubleToLongBits(Double.NaN))
			computeBaseTime();
		return baseTime;
	}

	private void computeBaseTime() {
		baseTime = cumulativeTime;
		Iterator<CallDesc> it = children.iterator();
		while (it.hasNext())
			baseTime -= it.next().cumulativeTime;
	}

	public CallDesc getParent() {
		return parent;
	}

	public void setParent(CallDesc parent) {
		this.parent = parent;
	}

	public List<CallDesc> getChildren() {
		return children;
	}

	public double getEntryTime() {
		return entryTime;
	}

	public double getExitTime() {
		return exitTime;
	}

	public MethodDesc getMethod() {
		return method;
	}

	public double getOverhead() {
		return overhead;
	}

	public int getStackDepht() {
		return stackDepht;
	}

	public ThreadDesc getThread() {
		return thread;
	}

	public HashCode getTicket() {
		return ticket;
	}

	public double getCumulativeTime() {
		return cumulativeTime;
	}

	public void setBaseTime(double baseTime) {
		this.baseTime = baseTime;
	}
	
	

}
