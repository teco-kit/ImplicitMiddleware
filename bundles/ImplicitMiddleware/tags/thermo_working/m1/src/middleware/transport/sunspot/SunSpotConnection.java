package middleware.transport.sunspot;
import java.io.*;
import javax.microedition.io.*;
//import javax.microedition.io.Connection
import com.sun.spot.io.j2me.radiogram.*;
//import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoAckException;
import com.sun.spot.peripheral.TimeoutException;
import com.sun.spot.peripheral.radio.LowPanPacketDispatcher;

import middleware.transport.SendReceive;
import middleware.core.ByteStack;
import middleware.helper.Debug;

public class SunSpotConnection extends SendReceive {
   private RadiogramConnection conn = null;
   private String   parameters[]    = {"", ""};
   private String   address         = "";
   private int      port            = 100;
   private boolean  isServer        = false;
   private Datagram lastDatagram    = null;

   static {
      LowPanPacketDispatcher.getInstance().initBaseStation();
   }
   public SunSpotConnection(String args[])
   {
      this.parameters = args;
      this.address    = args[0];
      this.port       = Integer.parseInt(args[1]);
      try {
         conn = (RadiogramConnection)
            Connector.open("radiogram://" + address + ":" + port);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
  
  public void setAsServer(boolean isServer)
  {
     this.isServer = isServer;
  }

  public void setParameters(String parameters[])
  {
     this.parameters[0] = parameters[0];
     this.parameters[1] = parameters[1];
  }

  public String[] getParameters() {
     return this.parameters;
  }

  public boolean isConnected() {
     return (conn != null);
  }

  public int send(ByteStack stack)
  {
     while (true) {
         try {
            Datagram dg        = conn.newDatagram(conn.getMaximumLength());
            Datagram confirmDg = conn.newDatagram(conn.getMaximumLength());

            if (isServer && lastDatagram != null) {
               dg.reset();
               dg.setAddress(lastDatagram);
            }

            dg.write(stack.getByteArray(), 0, stack.getByteArraySize());
            conn.send(dg);
            Debug.print("Datagram len "     + dg.getLength());
            Debug.print("Datagram sent to " + dg.getAddress());
            //conn.setTimeout(1000); // wait 1000ms for receive
            //Debug.print("Wait");
            //conn.receive(confirmDg);
            Debug.print("Done");
            conn.setTimeout(-1);
            return 0;
         } catch (NoAckException e) {
            Debug.print("No Ack");
            continue;
         } catch (TimeoutException e) {
            Debug.print("Timeout");
            continue;
         } catch (IOException e) {
            e.printStackTrace();
            return 1;
         }
      }

  }

  public ByteStack receive()
  {
     try {
        int len;

        Datagram dg = conn.newDatagram(conn.getMaximumLength());
        conn.receive(dg);
        Debug.print("Datagram from " + dg.getAddress());
        len = dg.getLength();
        ByteStack returnStack = new ByteStack(len);
        returnStack.setByteArraySize(len);
        dg.readFully(returnStack.getByteArray(), 0, len);
        lastDatagram = dg;
        
        return returnStack;

     } catch (IOException e) {
        //e.printStackTrace();
     }
     return null;
  }

  public void disconnect() {
     try {
        conn.close();
     } catch (IOException e) {
        e.printStackTrace();
     }
     conn = null;
  }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
