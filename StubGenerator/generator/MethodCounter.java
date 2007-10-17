/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import org.objectweb.asm.*;

public class MethodCounter extends ClassAdapter {

   private int    numOfMethods = 0;

   public MethodCounter()
   {
      super(new ClassWriter(0));
   }
   
   @Override
   public MethodVisitor visitMethod(int      access, String name,
                                    String   desc  , String signature,
                                    String[] exceptions               )
   {
      numOfMethods++;
      return null;
   }

   @Override
   public void visitEnd() {
   }

   public int getNumOfMethods() {
      return numOfMethods;
   }
   
} // class end

/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
