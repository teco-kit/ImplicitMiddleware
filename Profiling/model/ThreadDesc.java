package model;

import helperClasses.HashCode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



public class ThreadDesc {
	private HashCode id;
	private String name;
	private String groupName;
	private String parentName;
	private double startTime;
	private int threadObjectId;
	
	private Map<HashCode, CallDesc> callList = new HashMap<HashCode, CallDesc>();
	
	private CallDesc rootCallDesc = null; 	
	private CallDesc lastOpenCall = null;
	
	public ThreadDesc(HashCode id, String name, String groupName, String parentName, double startTime, int threadObjectId) {
		this.id = id;
		this.name = name;
		this.groupName = groupName;
		this.parentName = parentName;
		this.startTime = startTime;
		this.threadObjectId = threadObjectId;
		
		rootCallDesc = new CallDesc( this, MethodDesc.nullMethod, 0.0, new HashCode(0), 0, null);
		rootCallDesc.setBaseTime(0.0);
		rootCallDesc.setParent(rootCallDesc);
		lastOpenCall = rootCallDesc;

	}

	public boolean equals(Object o)
	{
		if (o.getClass().equals(this.getClass()))
		{
			return (this.id == ((ThreadDesc) o).getId());
		}
		return false;
	}
	
	public int hashCode()
	{
		return id.hashCode();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public int getThreadObjectId() {
		return threadObjectId;
	}

	public void setThreadObjectId(int threadObjectId) {
		this.threadObjectId = threadObjectId;
	}

	public Map<HashCode, CallDesc> getCallList() {
		return callList;
	}

	public void addCall(CallDesc cd)
	{
		callList.put(cd.getTicket(), cd);
	}
	
	public CallDesc getCall(HashCode key)
	{
		return callList.get(key);
	}
	
	public CallDesc getLastOpenCall()
	{
		return lastOpenCall;
	}
	
	public void setLastOpenCall(CallDesc lastOpenCall)
	{
		this.lastOpenCall = lastOpenCall;
	}

	public CallDesc getRootCallDesc() {
		return rootCallDesc;
	}

	public void setRootCallDesc(CallDesc rootCallDesc) {
		this.rootCallDesc = rootCallDesc;
	}
	
}
