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
	  return send(stack, "");
  }
  
  public int send(ByteStack stack, String address)
  {
     while (true) {
         try {
            Datagram dg        = conn.newDatagram(conn.getMaximumLength());

            if (!address.equals(""))
            {
            	dg.reset();
            	dg.setAddress(address);
            }
            dg.writeShort(1);
            dg.write(stack.getByteArray(), 0, stack.getByteArraySize());
            conn.send(dg);
            Debug.print("Datagram len "     + dg.getLength());
            Debug.print("Datagram sent to " + dg.getAddress());
      

            Debug.print("Done");
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
	  return receive(false);
  }
  
  public ByteStack receive(boolean saveAddres)
  {
	  while (true) {
		  try {
			  int len;
			  String address;
			  ByteStack returnStack = null;
			  
			  Datagram dg = conn.newDatagram(conn.getMaximumLength());
			  conn.receive(dg);
			  
			  Debug.print("Datagram from " + dg.getAddress() + " thread " + Thread.currentThread().getName());
			  len = dg.getLength() - 2;
			  if (dg.readShort() != 1)
			  {
				  checkConfirm(dg);
				  continue;
			  }
			  if (saveAddres) {
				  address = dg.getAddress();
				  returnStack = new ByteStack(len + address.length()*2);
				  returnStack.setByteArraySize(len);
				  dg.readFully(returnStack.getByteArray(), 0, len);
				  returnStack.pushString(address);
			  }
			  else
			  {
				  returnStack = new ByteStack(len);
				  returnStack.setByteArraySize(len);
				  dg.readFully(returnStack.getByteArray(), 0, len);
			  }

			  Debug.print("Datagram len " + len);
			  
			  Datagram confirmDg = conn.newDatagram(conn.getMaximumLength());
			  Debug.print("Sending confirm to " + dg.getAddress());

			  Debug.print("Ch1 " + confirmDg.getAddress());
			  if (confirmDg.getAddress().equals("0000.0000.0000.0000"))
				  confirmDg.setAddress(dg);
			  Debug.print("Ch2");
			  confirmDg.writeShort(3);
			  Debug.print("Ch3");
			  conn.send(confirmDg);
			  Debug.print("Sent confirm to " + dg.getAddress());
        
			  return returnStack;
		  } catch (TimeoutException e) {
			  //Debug.print("Timeout");
			  continue;
		  } catch (IOException e) {
			  //e.printStackTrace();
			  return null;
		  }
	  }

  }
  
  private void checkConfirm(Datagram dg) {
	  System.err.println("Checking confirm");
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
