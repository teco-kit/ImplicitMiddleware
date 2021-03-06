package middleware.core;

import middleware.transport.SendReceive;

public class Connection {
	private String      host;
	private SendReceive sr;
	private int         counter = 0;
	
	public Connection(String host, SendReceive sr)
	{
		this.host = host;
		this.sr = sr;
	}
	
	public String getHost() {
		return host;
	}


	public SendReceive getConnection() {
		return sr;
	}
	
	public int increment()
	{
		counter++;
		return counter;
	}
	
	public int decrement()
	{
		counter--;
		return counter;
	}
}

