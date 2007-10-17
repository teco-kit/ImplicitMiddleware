package test;

import org.junit.Assert;
import middleware.core.ByteStack;


import java.util.Date;

public class MethodCallStubWrapper {

   //private int classId;

   public MethodCallStubWrapper(String className)  {

   }

   @org.junit.Test
      public ByteStack callStubMethod( int  objectId,
                                       int  methodId, 
                                       ByteStack stack )
      {
         switch (methodId) {
            case 1:
               {
                  Assert.assertTrue(objectId == 0);
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(10);
                  return retStack;
               }
            case 2:
               {
                  Assert.assertTrue(objectId == 0);
                  testConstr2(stack.popInt(), stack.popInt());

                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(10);
                  return retStack;
               }
            case 3:
               {
                  Assert.assertTrue(objectId == 0);
                  testConstr3(stack.popInt(), stack.popDouble(), stack.popString());
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(10);
                  return retStack;
               }
            case 4:
               {
                  Assert.assertTrue(objectId == 10);
                  ByteStack retStack = new ByteStack(12);
                  retStack.pushString(new String("xxxx"));
                  return retStack;
               }
            case 5:
               {
                  Assert.assertTrue(objectId == 10);
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(55);
                  return retStack;
               }
            case 6:
               {
                  Assert.assertTrue(objectId == 0);
                  ByteStack retStack = new ByteStack(1);
                  retStack.pushByte((byte)56);
                  return retStack;
               }
            case 7:
               {
                  Assert.assertTrue(objectId == 10);
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(11);
                  return retStack;
               }
            case 8:
               {
                  Assert.assertTrue(objectId == 10);
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(11);
                  return retStack;
               }
            case 9:
               {
                  Assert.assertTrue(objectId == 0);
                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(11);
                  return retStack;
               }
            case 10:
               {
                  Assert.assertTrue(objectId == 10);
                  testMakeSomeThing(stack.popByte(), stack.popChar(), stack.popShort(),
                                    stack.popInt(), stack.popLong(), stack.popFloat(),
                                    stack.popDouble(), stack.popString(), stack.popInt());

                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(801205);
                  return retStack;
               }
            case 11:
               {
                  Assert.assertTrue(objectId == 0);
                  testMakeSomeThing(stack.popByte(), stack.popChar(), stack.popShort(),
                                    stack.popInt(), stack.popLong(), stack.popFloat(),
                                    stack.popDouble(), stack.popString(), stack.popInt());

                  ByteStack retStack = new ByteStack(8);
                  retStack.pushDouble(8012.05);
                  return retStack;
               }
            case 12:
               {
                  Assert.assertTrue(objectId == 10);
                  testSetUID(stack.popInt(), stack.popInt());

                  return null;
               }
            case 13:
               {
                  Assert.assertTrue(objectId == 10);
                  testSetGetStr(stack.popInt());

                  ByteStack retStack = new ByteStack(4);
                  retStack.pushInt(11);
                  return retStack;
               }
         }
         return null;
      }


   public Object getObjectFromOID(int id) {
      return new Date(0);
   }

   public int getOidForObject(Object obj) {
      return 12;
   }
   
   public void addStubObject2Heap(Object obj) {
	      return;
   }
   
   private void testConstr2( int i, int id)
   {
      Assert.assertTrue(i == 20);
      Assert.assertTrue(id == 12); // Object
   }

   private void testConstr3( int i, double d, String str)
   {
      Assert.assertTrue( i == 20);
      Assert.assertTrue( d == 20.12);
      Assert.assertTrue(str.equals("xxxx"));
   }

   private void testMakeSomeThing( byte b, char c, short s, int i,
                                   long l, float f, double d, String str, int objId)
   {
      Assert.assertTrue( b == 1);
      Assert.assertTrue( c == 'c');
      Assert.assertTrue( s == 12);
      Assert.assertTrue( i == 25);
      Assert.assertTrue( l == 999999999l);
      Assert.assertTrue( f == 32.1231231f);
      Assert.assertTrue( d == 123123.123123);
      Assert.assertTrue(str.equals("hoho"));
      Assert.assertTrue(objId == 12);
   }
   private void testSetUID( int i, int objId)
   {
      Assert.assertTrue( i == 5);
      Assert.assertTrue( objId == 12);
   }

   private void testSetGetStr(int objId)
   {
      Assert.assertTrue( objId == 12);
   }

}

