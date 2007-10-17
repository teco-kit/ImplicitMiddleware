package middleware.core;

import java.util.Hashtable;

import middleware.transport.SendReceive;
import middleware.transport.TransportFactory;


public class ConnectionManager {
	private static ConnectionManager instance = null;

	//private Map<String, Map<Thread, Connection>> connections = new HashMap<String, Map<Thread, Connection>>();
	private Hashtable connections   = new Hashtable();

	private ConnectionManager() {
		// Exists only to defeat instantiation.
	}
	public static ConnectionManager getInstance() {
		if(instance == null) {
			instance = new ConnectionManager();
		}
		return instance;
	}

	public boolean hasConnection(String host)
	{
		if (connections.containsKey(host))
		{
		   Hashtable threadConnections = (Hashtable)connections.get(host);
		   return (threadConnections.containsKey(Thread.currentThread()));
		}
		else 
			return false;
	}

	public SendReceive getConnection(String host)
	{
		if (hasConnection(host))
		{
			Hashtable threadConnections = (Hashtable)connections.get(host);
			Connection conn = 
				(Connection)threadConnections.get(Thread.currentThread());
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
	public Connection add(String host, SendReceive sendReceive)
	{
		if (!connections.containsKey(host))
			connections.put(host, new Hashtable());
		
		Hashtable cur = (Hashtable)connections.get(host);
		
		Connection conn;
		if (cur.containsKey(Thread.currentThread()))
		{
			conn = (Connection)cur.get(Thread.currentThread());
		}
		else
		{
			conn = new Connection(host, sendReceive);
			cur.put(Thread.currentThread(), conn);
			increment(host, sendReceive);
		}
		return conn;
	}
	
	public void increment(String host, SendReceive sr)
	{
		Hashtable threadConnections = (Hashtable)connections.get(host);
		Connection conn = 
			(Connection)threadConnections.get(Thread.currentThread());
		conn.increment();
	}

	public void decrement(String host, SendReceive sr)
	{
		Hashtable threadConnections = (Hashtable)connections.get(host);
		Connection conn = 
			(Connection)threadConnections.get(Thread.currentThread());

		if (conn.decrement() == 0)
			removeConnection(conn);
	}

	public void removeConnection(String host)
	{
		Hashtable threadConnections = (Hashtable)connections.get(host);
		Connection conn = 
			(Connection)threadConnections.get(Thread.currentThread());
		
		if (conn.getConnection().isConnected())
			conn.getConnection().disconnect();
		
		threadConnections.remove(Thread.currentThread());
	}
	
	public void removeConnection(Connection key)
	{
		removeConnection(key.getHost());
	}

}
