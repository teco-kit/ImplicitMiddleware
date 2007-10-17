package org.example;

public class Push
{
	final int constfun=1;
	
	public int stub(int a, String s) {
		pushString(s);
		pushInt(a);
		pushInt(constfun);
		
		return popInt();
	}
	public int fun(int a, String s)
	{
		return 0;
	}
	
	public void dispatch()
	{	
		switch(popInt())
		{
		case constfun:
		 pushInt(fun(popInt(),popString()));
		 break;
		}
	}
	
	public static void pushInt(int pushIt) {
		
	}
	
	public static void pushString(String pushIt) {
		
	}

	public static int popInt() {
		return 10;
	}
	
	public static String popString() {
		return "sads";
	}
}