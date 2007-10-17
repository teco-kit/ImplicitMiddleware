package test;

import java.io.File;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Assert;

public class StubClassGeneratorTest extends TestCase {

   protected void setUp() throws Exception {
      Runtime.getRuntime().exec("javac test/Class_B.java").waitFor();

      File     f           = new File("test/Class_B.class");
      String[] inOutStrArr = {f.getAbsolutePath()};
      Vector<String> stubClasses = new Vector<String>();
      stubClasses.addElement("test/Class_B");

      generator.ClassGenerator.Main( inOutStrArr,
                                     inOutStrArr,
                                     "test/MethodCallStubWrapper",
                                     stubClasses
                                   );
   }

   protected void tearDown() throws Exception {
      System.out.println("Test Done!");
   }

   protected void runTest() {
      testIt();
   }

   public void testIt() {
      assertTrue(true);
      Class_B b1 = new Class_B();
      Class_B b2 = new Class_B(20, new Date());
      Class_B b3 = new Class_B(20, 20.12, "xxxx");

      Assert.assertTrue(b1.getName().equals("xxxx"));
      Assert.assertTrue(b2.getNum() == 55);
      Assert.assertTrue(Class_B.getByte() == 56);
      Assert.assertTrue(((Date) b3.getObj()).getTime() == 0);
      Assert.assertTrue(b1.getDate().getTime() == 0);
      Assert.assertTrue(Class_B.getStaticDate().getTime() == 0);
      int ret = b2.makeSomeThing((byte)1, 'c', 
                                 (short)12, 25, 999999999l,
                                 32.1231231f, 123123.123123, 
                                 "hoho", new Date(50));

      Assert.assertTrue(ret == 801205);
      double dRet = Class_B.makeSomeThingStatic((byte)1, 'c', 
                                                (short)12, 
                                                25, 999999999l,
                                                32.1231231f, 
                                                123123.123123, 
                                                "hoho", new Date(50));

      Assert.assertTrue(dRet == 8012.05);
      b3.setUID(5, new Date(80));
      String strArray[] = {"", "asdfa"};
      Assert.assertTrue(b1.setGetStr(strArray).getTime() == 0);
   }

}

