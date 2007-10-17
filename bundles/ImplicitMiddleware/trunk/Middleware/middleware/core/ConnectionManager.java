package middleware.core;

import java.util.Hashtable;

import middleware.transport.SendReceive;
import middleware.transport.TransportFactory;


public class ConnectionManager {
	private static ConnectionManager instance    = null;
	private        Hashtable         connections = new Hashtable();
	private        Hashtable         threads     = new Hashtable();

	private ConnectionManager() {
		// Exists only to defeat instantiation.
	}
	
	public static ConnectionManager getInstance() {
		if(instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public boolean hasConnection(Short host)
	{
		return (connections.containsKey(host));
	}

	public SendReceive getConnection(Short host)
	{
		if (hasConnection(host))
		{
			Connection conn = (Connection)connections.get(host);
			SendReceive sendReceive = conn.getConnection();
			if (sendReceive.isConnected())
				return sendReceive;
			else
			{
				removeConnection(host);
				return getConnection(host);
			}
		}
		else
		{
			SendReceive sendReceive = TransportFactory.makeSendReceive(host);
			add(host, sendReceive);
			return sendReceive;
		}
	}

	/**
	 * Add a connection that has been established without the 
	 * use of the ConnectionManager e.g. a server connection.
	 * @param host
	 * @param sendReceive
	 */
	public Connection add(Short host, SendReceive sendReceive)
	{	
		Connection conn;
		
		if (connections.containsKey(host))
		{
			conn = (Connection)connections.get(host);
		}
		else
		{
			conn = new Connection(host, sendReceive);
			connections.put(host, conn);
			//threads.put(conn, Thread.currentThread());
			increment(host, sendReceive);
		}
		
		return conn;
	}
	
	public Thread getThread(SendReceive conn)
	{
		if (threads.containsKey(conn))
			return (Thread)threads.get(conn);
		else
			return null;
	}
	
	public void setThread(SendReceive conn, Thread thread)
	{
		threads.put(conn, thread);
		return;
	}
	
	public void increment(Short host, SendReceive sr)
	{
		Connection conn = (Connection)connections.get(host);
		conn.increment();
	}

	public void decrement(Short host, SendReceive sr)
	{
		Connection conn = (Connection)connections.get(host);

		if (conn.decrement() == 0)
			removeConnection(conn);
	}

	public void removeConnection(Short host)
	{
		Connection conn = (Connection)connections.get(host);
		
		if (conn.getConnection().isConnected())
			conn.getConnection().disconnect();
		
		connections.remove(host);
	}
	
	public void removeConnection(Connection key)
	{
		removeConnection(key.getHost());
	}

}
