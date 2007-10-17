package middleware.transport.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import middleware.core.ByteStack;
import middleware.helper.Debug;
import middleware.transport.SendReceive;


public class UDPConnection extends SendReceive {
   private DatagramSocket socket        = null;
   private String         parameters[]  = {"", ""};
   private InetAddress    remoteAddress = null;
   private int            port          = 8888;

   private static final short   sizeOfObjArr  = 100;
   private short     seqNumber   = 0;
   private Object    lockArray[] = new Object [sizeOfObjArr];
   private boolean   waitForMe[] = new boolean[sizeOfObjArr];
   private boolean   confirm  [] = new boolean[sizeOfObjArr];
   private Hashtable recvSeqNum  = new Hashtable();

   public UDPConnection(String args[])
   {
      this.parameters = args;
      this.port       = Integer.parseInt(args[1]);
      try {
         if (args[0].equals(""))
         {
            socket = new DatagramSocket(port);
         }
         else
         {
            socket = new DatagramSocket();
            this.remoteAddress = InetAddress.getByName(args[0]);
         }
      } catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (UnknownHostException e) {
         // TODO Auto-generated catch block
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
      return (socket != null);
   }

   public int send(ByteStack stack)
   {
      return send(stack, "");
   }

   private synchronized int addConf()
   {
      Object x = new Object();

      seqNumber++;

      if (seqNumber >= sizeOfObjArr)
         seqNumber = 0;

      lockArray[seqNumber] = x;

      return seqNumber;
   }

   public int send(ByteStack stack, String address)
   {
      while (true) {
         try {
            DatagramPacket dg = null;
            int           idx = addConf();

            stack.pushShort((short)idx);
            stack.pushShort((short)1);            

            if (address.equals(""))
            {
               dg = new DatagramPacket(stack.getByteArray(),
                                       stack.getByteArraySize(),
                                       remoteAddress, port);
               Debug.print("Remote address "     + remoteAddress.toString());
            }
            else
            {
               Debug.print("Send Address " + address);

               int portIdx = address.indexOf(":");
               String IP   = address.substring(0, portIdx);
               Debug.print("IP " + IP);
               int remPort = Integer.parseInt(address.substring(portIdx + 1));
               Debug.print("Port " + remPort);
               Debug.print("Address " + address);
               dg = new DatagramPacket(stack.getByteArray(),
                                       stack.getByteArraySize(),
                                       InetAddress.getByName(IP), remPort);
            }

            Object lockObj = lockArray[idx];

            waitForMe[idx] = true;
            confirm[idx]   = false;
            socket.send(dg);
            Debug.print("Datagram len "     + dg.getLength());
            Debug.print("Datagram sent to " + dg.getAddress().toString()
            		    + ":" + dg.getPort());

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
                     socket.send(dg);
                     continue;
                  }
               }
            }

            Debug.print("Done");
            return 0;
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
            short     dgType;
            String    address;
            String    respPort;
            ByteStack returnStack   = null;
            byte      buff[]        = new byte[1024];

            DatagramPacket dg = new DatagramPacket(buff, buff.length);
            socket.receive(dg);

            Debug.print("Datagram from " + dg.getAddress().toString() + 
            		    ":" + dg.getPort() +
                        " thread " + Thread.currentThread().getName());

            len = dg.getLength();

            address     = dg.getAddress().toString().replace('/', ' ').trim();
            respPort    = new Integer(dg.getPort()).toString();
            returnStack = new ByteStack(dg.getData());
            returnStack.setByteArraySize(len);
           
            dgType        = returnStack.popShort();
            recvSeqNumber = returnStack.popShort();
            
            Debug.print("Recv Datagram type " + dgType);
            Debug.print("Recv Datagram seq Num " + recvSeqNumber);
            Debug.print("Datagram len " + len);
            
            if (dgType != 1)
            {
               checkConfirm(recvSeqNumber);
               continue;
            }
  
            ByteStack confStack  = new ByteStack(4);      
            confStack.pushShort((short)recvSeqNumber);
            confStack.pushShort((short)3);

            DatagramPacket confirmDg = 
            	new DatagramPacket(confStack.getByteArray(),
            			           confStack.getByteArraySize(),
            			           dg.getAddress(), dg.getPort());

            socket.send(confirmDg);

            Debug.print("Sent confirm to " + dg.getAddress().toString() +
            		    ":" + dg.getPort() + 
                        " SeqNum "         + recvSeqNumber);
  
            if (!checkRecvSeqNum(recvSeqNumber, address))
            	continue; // duplicate

            if (saveAddress)
            	returnStack.pushString(address + ":" + respPort);

            return returnStack;
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
      socket.close();
      socket = null;
   }

}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
