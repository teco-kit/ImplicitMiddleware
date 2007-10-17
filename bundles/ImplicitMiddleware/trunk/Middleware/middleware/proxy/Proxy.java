package middleware.proxy;

import java.util.Enumeration;
import java.util.Hashtable;

import middleware.config.ProtocolsConfig;
import middleware.config.RemoteConfig;
import middleware.core.ByteStack;
import middleware.core.ConnectionManager;
import middleware.helper.Debug;
import middleware.transport.SendReceive;
import middleware.transport.SendReceiveServer;
import middleware.transport.TransportFactory;


public class Proxy implements Runnable
{
	static RemoteConfig    hosts  = RemoteConfig.getInstance();
	static ProtocolsConfig config = ProtocolsConfig.getInstance();
   
   public static void main(String[] args) 
   {
      Hashtable   protocols  = config.getProtocols();
      Enumeration keysEnum   = protocols.keys();

      while (keysEnum.hasMoreElements()) {
         Object nextKey = keysEnum.nextElement();

         Proxy proxy = 
            new Proxy((String)nextKey, 
                      (String [])protocols.get(nextKey));
         Thread t          = new Thread(proxy);
         t.start();
      }
   }

   private String            protocol;
   private String            attributes[];
   private SendReceive       conn;
   private ConnectionManager connManager = null;
   private SendReceive       socket      = null;

   public Proxy(String protocol, String[] attributes)
   {
      this.protocol   = protocol;
      this.attributes = attributes;
      connManager     = ConnectionManager.getInstance();
   }

   public void run()
   {
      SendReceiveServer serverSocket = 
    	  TransportFactory.makeSendReceiveServer(protocol, attributes);
      conn = serverSocket.startListening();


      while (true)
      {
         try
         {
            ByteStack msg = conn.receive(true);
            if (msg == null)
               continue;
            Debug.print("INFO: new connection opened by client");
            
            String address   = msg.popString();
            short remotePeer = msg.popShort();

            System.out.println("Remote Peer " + remotePeer);
            socket = connManager.getConnection(new Short(remotePeer));
            msg.pushShort(remotePeer);
            if (connManager.getThread(socket) == null)
            {
            	System.err.println("Confimation manager was started");
            	Thread t = new Thread(new ConfServer(socket, address));
            	connManager.setThread(socket, t);
                t.start();
            }
            socket.send(msg);

         } catch (NullPointerException e) {
            System.err.println("Connection was reset by client peer1.");
            continue;
         } catch (Exception e) {
            System.err.println("Connection was reset by client peer.");
            e.printStackTrace();
         }
      }
   }
   
   private class ConfServer implements Runnable {
	   private SendReceive socketIn;
	   private String      address;
	   
	   public ConfServer(SendReceive socketX, String address) {
		   socketIn     = socketX;
		   this.address = address;
	   }
	   
	   public void run() {
		   while (true)
		   {   
			   try
			   {
				   ByteStack msg = socketIn.receive();
				   if (msg == null)
					   continue;
				   Debug.print("INFO: new connection opened by client");

				   short  remotePeer = msg.popShort();
				   System.out.println("Remote Peer " + remotePeer);
				   msg.pushShort(remotePeer);
				   conn.send(msg, address);

			   } catch (NullPointerException e) {
				   System.err.println("Connection was reset by client peer1.");
				   continue;
			   } catch (Exception e) {
				   System.err.println("Connection was reset by client peer.");
				   e.printStackTrace();
			   }
		   }
	   } 
   }
}

