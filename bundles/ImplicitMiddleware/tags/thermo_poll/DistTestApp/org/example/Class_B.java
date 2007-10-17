package org.example;

import java.util.Hashtable;
import java.util.Enumeration;
import middleware.core.*;

public class Class_B
{
	private int uniqueID;
	private Hashtable table = null;
	private String str;
	
	public Class_B() {}
	
	public Class_B(UniqueID uid, int i) {
	} 
	
	public Class_B(int i, double dob, String str) {
		   ByteStack stack      = new ByteStack(25);
		   MethodCallStub mStub = new MethodCallStub("Class_A");
		   stack.pushInt(i);
		   stack.pushDouble(21.23);
		   stack.pushFloat(21.23f);
		   stack.pushByte((byte)1);
		   stack.pushBool(true);
		   stack.pushString(str);
		   byte b = stack.popByte();
		   char c = stack.popChar();
		   ByteStack retStack = 
		         mStub.callStubMethod(1, 0, stack);
		   uniqueID = retStack.popInt();
	}
	
	public String getName() {
		return "I'm class B";
	}
	
	public void initHashtable(Hashtable t, int x) {
		Enumeration e  = t.keys();
		table = new Hashtable();
		while (e.hasMoreElements())
		{
			Object key = e.nextElement();
			table.put("xxx", new String[] {"sqlite", 
			                               "firebird", 
			                               "postgres", 
			                               "mysql"});
         table.put("xxx", new Integer(20));
		}

	}
//	
//	public void setUniqueID() {
//		getName();
//	}
//	
//	static public byte getX() {
//		return 2;
//	}
//	
//	public Object getXXX() {
//
//		return new Object();
//	}

}
