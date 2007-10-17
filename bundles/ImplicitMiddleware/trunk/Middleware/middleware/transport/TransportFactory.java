package middleware.transport;
//==============================
// !!! Attention Dummy Class
//==============================
import middleware.config.ProtocolsConfig;
import middleware.config.RemoteConfig;
import middleware.transport.udp.UDPConnection;
import middleware.transport.udp.UDPServer;

public class TransportFactory {
	static RemoteConfig    hosts  = RemoteConfig.getInstance();
	static ProtocolsConfig config = ProtocolsConfig.getInstance();
	
	public static SendReceiveServer 
	makeSendReceiveServer(String transportProtocol, String[] attributes)
	{
		if (transportProtocol.equals("transport.udp.UDPServer"))
			return new UDPServer(attributes);
		else
		{
			System.err.println("Error in configuration: no such protocol" + 
			                   transportProtocol);
			return null;
		}
	}
	
	public static SendReceive makeSendReceive(Short hostname)
	{
		if (hosts.getTransportName(hostname).
		      equals("transport.udp.UDPConnection"))
		{
			return new UDPConnection(hosts.getTransportAttributes(hostname));
		}
		else
		{
			System.err.println("Error in configuration: no such host" + hostname);
			return null;
		}
	}
}
