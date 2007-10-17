package middleware.transport.udp;

import java.lang.Integer;
import middleware.transport.SendReceive;
import middleware.transport.SendReceiveServer;

public class UDPServer extends SendReceiveServer {
   private int listeningPort;

   public UDPServer(String args[]) {
      try {
         listeningPort = Integer.parseInt(args[1]);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public SendReceive startListening() {
      String[] connArray = { "", Integer.toString(listeningPort) };
      UDPConnection conn = new UDPConnection(connArray);
      
      return conn;
   }
}
