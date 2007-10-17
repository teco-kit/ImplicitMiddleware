package middleware.transport;

import middleware.config.ProtocolsConfig;
import middleware.config.RemoteConfig;
//import middleware.transport.particle.ParticleClient;
//import middleware.transport.particle.ParticleServer;
import middleware.transport.sunspot.SunSpotConnection;
import middleware.transport.sunspot.SunSpotServer;

public class TransportFactory {
	static RemoteConfig    hosts  = RemoteConfig.getInstance();
	static ProtocolsConfig config = ProtocolsConfig.getInstance();
	
	public static SendReceiveServer 
	makeSendReceiveServer(String transportProtocol, String[] attributes)
	{
		//if (transportProtocol.equals("transport.TCPServer"))
		//	return new TCPServer(attributes);
		//if (transportProtocol.equals("transport.particle.ParticleServer"))
		//	return ParticleServer.getParticleServer(attributes);
		if (transportProtocol.equals("transport.sunspot.SunSpotServer"))
			return new SunSpotServer(attributes);

		//		Handle new Protocols here
		
		else
		{
			System.err.println("Error in configuration: no such protocol" + 
			                   transportProtocol);
			return null;
		}
	}
	
	public static SendReceive makeSendReceive(String hostname)
	{
		//if (hosts.getTransportName(hostname).equals("transport.TCPClient"))
		//	return new TCPClient(hosts.getTransportAttributes(hostname));
		//if (hosts.getTransportName(hostname).equals("transport.particle.ParticleClient"))
		//	return new ParticleClient(hosts.getTransportAttributes(hostname));
		if (hosts.getTransportName(hostname).
		      equals("transport.sunspot.SunSpotConnection"))
		{
			return new SunSpotConnection(hosts.getTransportAttributes(hostname));
		}
		//Handle new Protocols here
		
		else
		{
			System.err.println("Error in configuration: no such host" + hostname);
			return null;
		}
	}
}
