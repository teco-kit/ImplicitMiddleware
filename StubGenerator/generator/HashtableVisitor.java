/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import org.objectweb.asm.*;

public class HashtableVisitor {

   private static final String        hashtableClass = "java/util/Hashtable";
   private              MethodVisitor constructor    = null;
   private              ClassWriter   classWriter    = null;
   private              String        className      = null;

   public HashtableVisitor(String className, int constrAccess)
   {
      this.className = className;
      classWriter    = new ClassWriter(ClassWriter.COMPUTE_FRAMES);


      classWriter.visit(Opcodes.V1_4,
                        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                        className,
                        null,
                        "java/lang/Object",
                        null);

      constructor
         = classWriter.visitMethod(constrAccess, "<init>",
                                  "()V", null, null);
      constructor.visitCode();

      constructor.visitVarInsn(Opcodes.ALOAD, 0);
      constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                  "java/lang/Object", "<init>", "()V");

   }

   public void visitHashtable(Hashtable table  , String fieldName, 
                              String    keyType, String valueType)
   {
      classWriter.visitField(Opcodes.ACC_PRIVATE, fieldName,
                             "L" + hashtableClass + ";",
                             null, null).visitEnd();

      /****************************
       * table = new Hashtable(); *
       ****************************/
      constructor.visitVarInsn(Opcodes.ALOAD, 0);
      constructor.visitTypeInsn(Opcodes.NEW, hashtableClass);
      constructor.visitInsn(Opcodes.DUP);
      constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                  hashtableClass, "<init>", "()V");
      constructor.visitFieldInsn(Opcodes.PUTFIELD,
                                 className,
                                 fieldName, "L" + hashtableClass + ";");


      Enumeration e  = table.keys();
      while (e.hasMoreElements())
      {
         Object key = e.nextElement();

         constructor.visitVarInsn(Opcodes.ALOAD, 0);
         constructor.visitFieldInsn(Opcodes.GETFIELD, className,
                                    fieldName, "L" + hashtableClass + ";");
         if (keyType == "String")
            constructor.visitLdcInsn((String)key);
         else if (keyType == "int")
         {
            constructor.visitTypeInsn(Opcodes.NEW, "java/lang/Integer");
            constructor.visitInsn(Opcodes.DUP);
            constructor.visitIntInsn(Opcodes.BIPUSH, (Integer)key);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
            		                    "java/lang/Integer", "<init>", "(I)V");
         }
         else if (keyType == "short")
         {
            constructor.visitTypeInsn(Opcodes.NEW, "java/lang/Short");
            constructor.visitInsn(Opcodes.DUP);
            constructor.visitIntInsn(Opcodes.BIPUSH, (Short)key);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
            		                    "java/lang/Short", "<init>", "(S)V");
         }
         else
            return;

         if (valueType == "String")
         {
            constructor.visitLdcInsn((String)table.get(key));
         }
         else if (valueType == "String[]")
         {
            String[] value =  (String[])table.get(key);

            constructor.visitIntInsn(Opcodes.BIPUSH, value.length);
            constructor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
            int i = 0;
            for (String str : value)
            {
               constructor.visitInsn(Opcodes.DUP);
               constructor.visitIntInsn(Opcodes.BIPUSH, i);
               constructor.visitLdcInsn(str);
               constructor.visitInsn(Opcodes.AASTORE);
               i++;
            }
         }
         else if (valueType == "int")
         {
            constructor.visitTypeInsn(Opcodes.NEW, "java/lang/Integer");
            constructor.visitInsn(Opcodes.DUP);
            constructor.visitIntInsn(Opcodes.BIPUSH, (Integer)table.get(key));
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                                  "java/lang/Integer", "<init>", 
                                  "(I)V");
         }
         else if (valueType == "short")
         {
            constructor.visitTypeInsn(Opcodes.NEW, "java/lang/Short");
            constructor.visitInsn(Opcodes.DUP);
            constructor.visitIntInsn(Opcodes.BIPUSH, (Short)table.get(key));
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                                        "java/lang/Short", "<init>", 
                                        "(S)V");
         }
         else
            return;

         constructor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, hashtableClass,
                                     "put",
                                     "(Ljava/lang/Object;Ljava/lang/Object;)"
                                     + "Ljava/lang/Object;");
         constructor.visitInsn(Opcodes.POP);

      }
   }

   public void visitGetMethod(String fieldName)
   {
      MethodVisitor mv =
         classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                 "get"
                                 + Character.toUpperCase(fieldName.charAt(0))
                                 + fieldName.substring(1),
                                 "()" + "L" + hashtableClass + ";",
                                 null, null);
      mv.visitCode();

      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD,
                        className,
                        fieldName,
                        "L" + hashtableClass + ";");
      mv.visitInsn(Opcodes.ARETURN);
      mv.visitMaxs(0,0);
      mv.visitEnd();
   }

   public void visitSetMethod()
   {
   }
   
   public MethodVisitor getConstructor()
   {
	   return constructor;
   }

   public void visitEnd()
   {
      constructor.visitInsn(Opcodes.RETURN);
      constructor.visitMaxs(0,0);
      constructor.visitEnd();
      classWriter.visitEnd();
   }

   public ClassWriter getClassWriter()
   {
      return classWriter;
   }

   public void writeClassToFile(String outputFile)
   {
      try {
         new FileOutputStream(outputFile).write((classWriter).toByteArray());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
