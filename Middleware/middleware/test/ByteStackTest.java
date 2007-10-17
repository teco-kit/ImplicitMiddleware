package middleware.test;

import junit.framework.TestCase;
import middleware.core.ByteStack;

public class ByteStackTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
		System.out.println("Test Done!");
	}
	
	protected void runTest() {
		testByteStack();
		testPushPopByte();
		testPushPopBool();
		testPushPopShort();
		testPushPopInt();
		testPushPopLong();
		testPushPopFloat();
		testPushPopDouble();
		testPushPopChar();
		testPushPopString();
		testComplexFunctionality();
	}
	
	public void testComplexFunctionality() {
		byte   b_val = 5;
		boolean bool = false;
		short  s_val = 50;
		int    i_val = 500000;
		long   l_val = 4632263246599820411l;
		float  f_val = 50.21f;
		double d_val = 50000987897.2123123;
		char   c_val = 'c';
		String str   = "Hello World!";
		ByteStack stack = new ByteStack();
		
		stack.pushByte(b_val);
		stack.pushBool(bool);
		stack.pushShort(s_val);
		stack.pushInt(i_val);
		stack.pushLong(l_val);
		stack.pushFloat(f_val);
		stack.pushDouble(d_val);
		stack.pushChar(c_val);
		stack.pushString(str);
		assertTrue(str.equals(stack.popString()));
		assertTrue(c_val == stack.popChar());
		assertTrue(d_val == stack.popDouble());
		assertTrue(f_val == stack.popFloat());
		assertTrue(l_val == stack.popLong());
		assertTrue(i_val == stack.popInt());
		assertTrue(s_val == stack.popShort());
		assertTrue(bool == stack.popBool());
		assertTrue(b_val == stack.popByte());
		assertTrue(stack.isEmpty());
		stack.pushByte(b_val);
		stack.pushLong(l_val);
		stack.pushDouble(d_val);
		assertTrue(d_val == stack.popDouble());
		stack.pushFloat(f_val);
		stack.pushInt(i_val);
		assertTrue(i_val == stack.popInt());
		assertTrue(f_val == stack.popFloat());
		assertTrue(l_val == stack.popLong());
		assertTrue(b_val == stack.popByte());
		assertTrue(stack.isEmpty());
	}

	public void testByteStack() {
		ByteStack stack = new ByteStack();
		assertTrue(stack.isEmpty());
	}

	public void testPushPopByte() {
      ByteStack stack = new ByteStack();
		byte b_val      = Byte.MIN_VALUE;

      for (; b_val < Byte.MAX_VALUE; b_val++)
      {
         stack.pushByte(b_val);
         assertTrue(b_val == stack.popByte());
      }

      stack.pushByte(Byte.MAX_VALUE);
      assertTrue(Byte.MAX_VALUE == stack.popByte());
	}
	
	public void testPushPopBool() {
		boolean bool    = true;
		ByteStack stack = new ByteStack();
		
		stack.pushBool(bool);
		assertTrue(bool == stack.popBool());

      bool = false;
		stack.pushBool(bool);
		assertTrue(bool == stack.popBool());
	}
	
	public void testPushPopShort() {
		short s_val     = Short.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (; s_val < Short.MAX_VALUE; s_val++)
      {
         stack.pushShort(s_val);
         assertTrue(s_val == stack.popShort());
      }
	}

	public void testPushPopInt() {
		int i_val       = Integer.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (int i = 0; i < 1000; i++, i_val++)
      {
         stack.pushInt(i_val);
         assertTrue(i_val == stack.popInt());
      }

      i_val = Integer.MAX_VALUE;
      for (int i = 0; i < 1000; i++, i_val--)
      {
         stack.pushInt(i_val);
         assertTrue(i_val == stack.popInt());
      }
	}
	
	public void testPushPopLong() {
		long l_val      = Long.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (int i = 0; i < 1000; i++, l_val++)
      {
         stack.pushLong(l_val);
         assertTrue(l_val == stack.popLong());
      }

      l_val = Long.MAX_VALUE;
      for (int i = 0; i < 1000; i++, l_val--)
      {
         stack.pushLong(l_val);
         assertTrue(l_val == stack.popLong());
      }
	}

	public void testPushPopFloat() {
		float f_val     = Float.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (int i = 0; i < 1000; i++, f_val++)
      {
         stack.pushFloat(f_val);
         assertTrue(f_val == stack.popFloat());
      }

      f_val = Float.MAX_VALUE;
      for (int i = 0; i < 1000; i++, f_val--)
      {
         stack.pushFloat(f_val);
         assertTrue(f_val == stack.popFloat());
      }
   }

	public void testPushPopDouble() {
		double d_val    = Double.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (int i = 0; i < 1000; i++, d_val++)
      {
         stack.pushDouble(d_val);
         assertTrue(d_val == stack.popDouble());
      }

      d_val = Double.MAX_VALUE;
      for (int i = 0; i < 1000; i++, d_val--)
      {
         stack.pushDouble(d_val);
         assertTrue(d_val == stack.popDouble());
      }
	}

	public void testPushPopChar() {
		char c_val      = Character.MIN_VALUE;
		ByteStack stack = new ByteStack();
		
      for (; c_val < Character.MAX_VALUE; c_val++)
      {
         stack.pushChar(c_val);
         assertTrue(c_val == stack.popChar());
      }
	}

	public void testPushPopString() {
		String str = "Hello World!";
		ByteStack stack = new ByteStack();
		
		stack.pushString(str);
		assertTrue(str.equals(stack.popString()));
	}
}

