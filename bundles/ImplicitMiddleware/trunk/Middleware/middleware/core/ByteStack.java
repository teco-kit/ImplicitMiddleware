/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package middleware.core;

public class ByteStack {

   /*
    * We must stay compatible to CLDC 1.1,
    * which means no generics, no annotations :-(
    */
   private byte[] byteStack;
   // stackSize is the size of all elements in the stack
   // without the empty space
   private int    stackSize     = 0;
   // StackRealSize is the size of the stack with the empty space
   private int    stackRealSize = 0;

   /**
    * Creates an empty ByteStack
    */
   public ByteStack() {
      enlarge(0);
   }

   /**
    * Creates a ByteStack with size
    * @param size the size of the stack in bytes
    */
   public ByteStack(int size) {
      byteStack     = new byte[size];
      stackRealSize = size;
   }

   /**
    * Creates a ByteStack from a byte array
    */
   public ByteStack(byte[] array) {
      //Shall we copy?
      byteStack     = array;
      stackSize     = array.length;
      stackRealSize = array.length;
   }

   /**
    * Appends one ByteStack elements to the current array
    */
   public void append(ByteStack newStack) {
      byte[] array2 = newStack.getByteArray();
      int    len    = newStack.getByteArraySize(); 
      enlarge(len);
      
      for (int i = 0; i < len; i++)
         byteStack[stackSize + i] = array2[i];

      stackSize += len;
   }
   
   /**
    * Pushes a byte on the ByteStack
    * @param  toPush the byte value to be pushed
    */
   public void pushByte(byte toPush) {
      enlarge(1);

      byteStack[stackSize] = (toPush);
      stackSize++;
   }

   /**
    * Pushes a boolean on the ByteStack
    * @param  toPush the boolean value to be pushed
    */
   public void pushBool(boolean toPush) {
      enlarge(1);

      if (toPush)
         byteStack[stackSize] = 1;
      else
         byteStack[stackSize] = 0;

      stackSize++;
   }

   /**
    * Pushes a short on the ByteStack
    * @param  toPush the short value to be pushed
    */
   public void pushShort(short toPush) {
      enlarge(2);

      byteStack[stackSize]     = (byte)(toPush      & 0xff);
      byteStack[stackSize + 1] = (byte)(toPush >> 8 & 0xff);

      stackSize += 2;
   }

   /**
    * Pushes an int on the ByteStack
    * @param  toPush the int value to be pushed
    */
   public void pushInt(int toPush) {
      enlarge(4);

      byteStack[stackSize]     = (byte)(toPush        & 0xff);
      byteStack[stackSize + 1] = (byte)(toPush >>   8 & 0xff);
      byteStack[stackSize + 2] = (byte)(toPush >>  16 & 0xff);
      byteStack[stackSize + 3] = (byte)(toPush >>> 24);

      stackSize += 4;
   }

   /**
    * Pushes an long on the ByteStack
    * @param  toPush the long value to be pushed
    */
   public void pushLong(long toPush) {
      enlarge(8);

      byteStack[stackSize]     = (byte)(toPush        & 0xff);
      byteStack[stackSize + 1] = (byte)(toPush >>   8 & 0xff);
      byteStack[stackSize + 2] = (byte)(toPush >>  16 & 0xff);
      byteStack[stackSize + 3] = (byte)(toPush >>  24 & 0xff);
      byteStack[stackSize + 4] = (byte)(toPush >>  32 & 0xff);
      byteStack[stackSize + 5] = (byte)(toPush >>  40 & 0xff);
      byteStack[stackSize + 6] = (byte)(toPush >>  48 & 0xff);
      byteStack[stackSize + 7] = (byte)(toPush >>> 56);

      stackSize += 8;
   }

   /**
    * Pushes a float on the ByteStack
    * @param  toPush the float value to be pushed
    */
   public void pushFloat(float toPush) {
      pushInt(Float.floatToIntBits(toPush));

      return;
   }

   /**
    * Pushes a double on the ByteStack
    * @param  toPush the double value to be pushed
    */
   public void pushDouble(double toPush) {
      pushLong(Double.doubleToLongBits(toPush));
      
      return;
   }

   /**
    * Pushes a char on the ByteStack
    * @param  toPush the char value to be pushed
    */
   public void pushChar(char toPush) {
      enlarge(2);

      byteStack[stackSize]     = (byte)(toPush      & 0xff);
      byteStack[stackSize + 1] = (byte)(toPush >> 8 & 0xff);

      stackSize += 2;
   }

   /**
    * Pushes a String on the ByteStack
    * @param  toPush the String value to be pushed
    */
   public void pushString(String toPush) {
      try {
         char[] strChar = toPush.toCharArray();
         int    strLen  = strChar.length * 2; 
         
         enlarge(strLen);
         
         for (int i = 0; i < strChar.length; i++)
            pushChar(strChar[i]);

         pushInt(strLen);

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Pops a byte from the ByteStack
    * @return the byte value
    */
   public byte popByte() {
      return byteStack[--stackSize];
   }

   /**
    * Pops a boolean from the ByteStack
    * @return the boolean value
    */
   public boolean popBool() {
      byte top = byteStack[--stackSize];

      return true ? (top == 1) : false;
   }

   /**
    * Pops a short from the ByteStack
    * @return the short value
    */
   public short popShort() {
      short retVal = (short)(((byteStack[stackSize - 1] & 0xff) << 8) |
                              (byteStack[stackSize - 2] & 0xff));

      stackSize -= 2;

      return retVal;
   }

   /**
    * Pops int from the ByteStack
    * @return the int value
    */
   public int popInt() {
      int retVal = (((byteStack[stackSize - 1] & 0xff) << 24) |
                    ((byteStack[stackSize - 2] & 0xff) << 16) |
                    ((byteStack[stackSize - 3] & 0xff) << 8)  |
                    ((byteStack[stackSize - 4] & 0xff)));

      stackSize -= 4;

      return retVal;
   }

   /**
    * Pops long from the ByteStack
    * @return the long value
    */
   public long popLong() {
      long retVal = (long)
            ((((long)(byteStack[stackSize - 1] & 0xff)) << 56) |
             (((long)(byteStack[stackSize - 2] & 0xff)) << 48) |
             (((long)(byteStack[stackSize - 3] & 0xff)) << 40) |
             (((long)(byteStack[stackSize - 4] & 0xff)) << 32) |
             (((long)(byteStack[stackSize - 5] & 0xff)) << 24) |
             (((long)(byteStack[stackSize - 6] & 0xff)) << 16) |
             (((long)(byteStack[stackSize - 7] & 0xff)) << 8)  |
             (((long)(byteStack[stackSize - 8] & 0xff))));


      stackSize -= 8;

      return retVal;
   }

   /**
    * Pops float from the ByteStack
    * @return the float value
    */
   public float popFloat() {
      return (Float.intBitsToFloat(popInt()));
   }

   /**
    * Pops double from the ByteStack
    * @return the double value
    */
   public double popDouble() {
      return (Double.longBitsToDouble(popLong()));
   }

   /**
    * Pops char from the ByteStack
    * @return the char value
    */
   public char popChar() {
      char retVal = (char)(((byteStack[stackSize - 1] & 0xff) << 8) |
                           ((byteStack[stackSize - 2] & 0xff)));

      stackSize -= 2;

      return retVal;
   }

   /**
    * Pops String from the ByteStack
    * @return the String object
    */
   public String popString() {
      String retVal = null;
      try {
         int strLen = popInt();

         char[] charArr = new char[strLen/2];
         for (int i = charArr.length - 1; i >= 0 ; i--)
            charArr[i] = popChar();
         retVal     = new String(charArr);
      } catch (Exception e) {
         e.printStackTrace();
      }

      return retVal;
   }

   /**
    * Checks if the stack is empty
    * @return true if stack is empty
    */
   public boolean isEmpty() {
      return (stackSize <= 0);
   }

   /**
    * Returns a reference to the byte array
    * Attention! Normally you cannot use the length of the array to determine how
    * much bytes to transfer. Use getByteArraySize() instead.
    * @return byte[] reference to the byte array in the stack
    */
   public byte[] getByteArray() {
      return byteStack;
   }

   /**
    * Returns the size in bytes of all elements in the byte array
    * Attention the length of the byte[] and the length of all elements will
    * most probably differ. Because of the way the stack is grown
    *
    * @return byte[] reference to the byte array in the stack
    */
   public int getByteArraySize() {
      return stackSize;
   }
   
   /**
    * This function is used from classes that operate 
    * on the raw byte array that belongs to the ByteStack
    * @param newSize the size to be set
    */
   public void setByteArraySize(int newSize) {
      stackSize = newSize;
   }

   /*
    * Checks if the stack is empty
    * @param bytes of the new elements
    */
   private void enlarge(int bytes) {
      if ((stackRealSize - stackSize) > bytes)
         return; // There is enough room for the new one

      stackRealSize += bytes + 32;
      byte[] byteStackTmp =  new byte[stackRealSize];
      for (int i = 0; i < stackSize; i++)
         byteStackTmp[i] = byteStack[i];

      byteStack = byteStackTmp;

      return;
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
