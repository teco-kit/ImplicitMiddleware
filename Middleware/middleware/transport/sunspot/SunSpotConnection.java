package middleware.transport.sunspot;
import java.io.*;
import java.util.Hashtable;

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

   private static final short   sizeOfObjArr = 100;
   private short     seqNumber   = 0; 
   private Object    lockArray[] = new Object[sizeOfObjArr];
   private boolean   waitForMe[] = new boolean[sizeOfObjArr];
   private boolean   confirm[]   = new boolean[sizeOfObjArr];
   private Hashtable recvSeqNum  = new Hashtable();

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
      for (int i = 0; i < sizeOfObjArr; i++)
    	  waitForMe[i] = false;
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

   private synchronized int addConf(Datagram dg)
   {
      Object x = new Object();
      seqNumber++;

      if (seqNumber >= sizeOfObjArr)
         seqNumber = 0;

      lockArray[seqNumber] = x;
      try {
         dg.writeShort(seqNumber);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return seqNumber;
   } 

   public int send(ByteStack stack, String address)
   {
      while (true) {
         try {
            Datagram dg = conn.newDatagram(conn.getMaximumLength());

            if (!address.equals(""))
            {
               dg.reset();
               dg.setAddress(address);
            }
            dg.writeShort(1);
            int idx        = addConf(dg);
            Object lockObj = lockArray[idx]; 

            dg.write(stack.getByteArray(), 0, stack.getByteArraySize());
            waitForMe[idx] = true;
            confirm[idx]   = false;
            conn.send(dg);
            Debug.print("Datagram len "     + dg.getLength());
            Debug.print("Datagram sent to " + dg.getAddress());
          
            while (true)
            {
            	synchronized(lockObj) {
            		lockObj.wait(2000);
            		if (confirm[idx]) 
            		{
            			waitForMe[idx] = false;
            			break;
            		}
            		else
            		{
            			conn.send(dg);
            			continue;
            		}
            	}
            }

            Debug.print("Done");
            return 0;
         } catch (NoAckException e) {
            Debug.print("No Ack");
            continue;
         } catch (IOException e) {
            e.printStackTrace();
            return 1;
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public ByteStack receive()
   {
      return receive(false);
   }

   public ByteStack receive(boolean saveAddress)
   {
      while (true) {
         try {
            int       len;
            short     recvSeqNumber;
            String    address;
            ByteStack returnStack   = null;

            Datagram dg = conn.newDatagram(conn.getMaximumLength());
            conn.receive(dg);

            Debug.print("Datagram from " + dg.getAddress() + " thread " + 
                        Thread.currentThread().getName());
            len = dg.getLength() - 4;
            short dgType  = dg.readShort();
            recvSeqNumber = dg.readShort();

            if ( dgType != 1)
            {
               checkConfirm(recvSeqNumber);
               continue;
            }
            
            if (saveAddress) 
            {
               address     = dg.getAddress();
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

            if (confirmDg.getAddress().equals("0000.0000.0000.0000"))
               confirmDg.setAddress(dg);

            confirmDg.writeShort(3);
            confirmDg.writeShort(recvSeqNumber);
            conn.send(confirmDg);

            Debug.print("Sent confirm to " + dg.getAddress() + " SeqNum " + 
                        recvSeqNumber);
            
            if (!checkRecvSeqNum(recvSeqNumber, dg.getAddress()))
            	continue; // duplicate

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

   private synchronized void checkConfirm(short recvSeqNumber) {
	   System.err.println("Confirm seq " + recvSeqNumber);
         
	   while(waitForMe[recvSeqNumber]) {
		   synchronized(lockArray[recvSeqNumber]) {
			   confirm[recvSeqNumber] = true;
			   lockArray[recvSeqNumber].notify(); 
		   }         
	   }
   }
   
   private boolean checkRecvSeqNum(short recvSeqNumber, String address) {
	   if (!recvSeqNum.contains(address))
	   {
		   recvSeqNum.put(address, new Short(recvSeqNumber));
		   return true;
	   }
	   
	   short lastSeqNum = ((Short)recvSeqNum.get(address)).shortValue();
	   
	   if (recvSeqNumber >= (lastSeqNum+1)%sizeOfObjArr)
	   {
		   recvSeqNum.remove(address);
		   recvSeqNum.put(address, new Short(recvSeqNumber));
		   return true;
	   }
	   
	   return false;
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
