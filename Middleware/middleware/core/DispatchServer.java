/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package middleware.core;

import java.util.Hashtable;
import java.util.Enumeration;

import middleware.config.ProtocolsConfig;
import middleware.helper.Debug;
import middleware.transport.SendReceive;
import middleware.transport.SendReceiveServer;
import middleware.transport.TransportFactory;


public class DispatchServer implements Runnable
{
   /**
    * Start a listening server for each used protocol
    */
   public static void startServer()
   {
      ProtocolsConfig config = ProtocolsConfig.getInstance();
      Hashtable   protocols  = config.getProtocols();
      Enumeration keysEnum   = protocols.keys();

      while (keysEnum.hasMoreElements()) {
         Object nextKey = keysEnum.nextElement();

         DispatchServer ds = 
            new DispatchServer((String)nextKey, 
                               (String [])protocols.get(nextKey));
         Thread t          = new Thread(ds);
         t.start();
      }
   }

   private String      transport;
   private String      attributes[];
   private SendReceive conn;

   public DispatchServer(String transport, String[] attributes) {
      this.transport  = transport;
      this.attributes = attributes;
   }

   public void run()
   {
      SendReceiveServer serverSocket = 
         TransportFactory.makeSendReceiveServer(transport, attributes);
      conn = serverSocket.startListening();
      while (true)
      {
         try
         {
            ByteStack msg = conn.receive(true);
            if (msg == null)
            	continue;
            Debug.print("INFO: new connection opened by client");
            
            // address and remoteHost refer to different 
            // machines if we use a proxy
            String address    = msg.popString();
            msg.popShort(); //local address
            Short  remoteHost = new Short(msg.popShort());

            Thread t = new Thread(new Dispatcher(msg, address, remoteHost, conn));
            t.start();

         } catch (NullPointerException e) {
            //conn = serverSocket.startListening();
            System.err.println("Connection was reset by client peer1.");
            continue;
         } catch (Exception e) {
            System.err.println("Connection was reset by client peer.");
            e.printStackTrace();
         }
      }
   }
}

