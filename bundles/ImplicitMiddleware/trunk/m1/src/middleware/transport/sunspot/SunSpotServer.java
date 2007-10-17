package middleware.transport.sunspot;

import java.lang.Integer;
import middleware.transport.SendReceive;
import middleware.transport.SendReceiveServer;

public class SunSpotServer extends SendReceiveServer {
   private int listeningPort;

   public SunSpotServer(String args[]) {
      try {
         listeningPort = Integer.parseInt(args[1]);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public SendReceive startListening() {
      String[] connArray = { "", Integer.toString(listeningPort) };
      SunSpotConnection conn = new SunSpotConnection(connArray);
      conn.setAsServer(true);
      
      return conn;
   }
}
