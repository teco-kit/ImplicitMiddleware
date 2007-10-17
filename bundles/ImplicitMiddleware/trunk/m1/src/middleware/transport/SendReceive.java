package middleware.transport;

import middleware.core.ByteStack;

public abstract class SendReceive {


	public void setParameters(String parameters[])
	{

	}

	public abstract String[]  getParameters();
	
	public abstract boolean   isConnected();

	public abstract void      disconnect();

	public abstract int       send(ByteStack stack);
	
	public abstract ByteStack receive();

}
