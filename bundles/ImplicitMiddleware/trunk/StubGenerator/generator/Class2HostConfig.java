/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileOutputStream;
import java.util.Hashtable;
import org.objectweb.asm.*;

public class Class2HostConfig {

   private static final String      hashtableClass = "java/util/Hashtable";
   private static final String      className      = "middleware/config/Classes";
   private static final String      fieldHost      = "class2Host";
   private static final String      fieldClassId   = "class2classId";
   private static final String      instanceField  = "instance";
   private              ClassWriter writer         =  null;

   public Class2HostConfig(Hashtable class2Host, Hashtable classIds)
   {
      HashtableVisitor visitor =
         new HashtableVisitor(className, Opcodes.ACC_PRIVATE);
      visitor.visitHashtable(class2Host, fieldHost, "String", "short");
      visitor.visitHashtable(classIds, fieldClassId, "String", "int");
      writer = visitor.getClassWriter();

      writer.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
                        instanceField,
                        "L" + className + ";",
                        null, null).visitEnd();

      visitGetInstance();
      visitGetHost();
      visitGetClassId();

      visitor.visitEnd();
   }

   private void visitGetInstance()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                            "getInstance",
                            "()L" + className + ";",
                            null, null);
      mv.visitCode();

      Label l1 = new Label();
      Label l2 = new Label();
      mv.visitFieldInsn(Opcodes.GETSTATIC, className,
                        instanceField, "L" + className + ";");
      mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
      mv.visitLabel(l2);
      mv.visitTypeInsn(Opcodes.NEW, className);
      mv.visitInsn(Opcodes.DUP);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                         className, "<init>", "()V");
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
                        instanceField, "L" + className + ";");
      mv.visitLabel(l1);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      mv.visitFieldInsn(Opcodes.GETSTATIC, className,
                        instanceField, "L" + className + ";");


      mv.visitInsn(Opcodes.ARETURN);
      mv.visitMaxs(0,0);
      mv.visitEnd();
   }

   private void visitGetHost()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC,
                            "getHost",
                            "(Ljava/lang/String;)Ljava/lang/Short;",
                            null, null);
      mv.visitCode();

      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD,
                        className,
                        fieldHost,
                        "L" + hashtableClass + ";");

      mv.visitVarInsn(Opcodes.ALOAD, 1);

      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                         hashtableClass,
                         "get",
                         "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");

      mv.visitInsn(Opcodes.ARETURN);
      mv.visitMaxs(0,0);
      mv.visitEnd();
   }
   
   private void visitGetClassId()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC,
                            "getClassId",
                            "(Ljava/lang/String;)I",
                            null, null);
      mv.visitCode();

      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD,
                        className,
                        fieldClassId,
                        "L" + hashtableClass + ";");

      mv.visitVarInsn(Opcodes.ALOAD, 1);
      
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                         hashtableClass,
                         "get",
                         "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
      
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                         "java/lang/Integer", "intValue", "()I");
      mv.visitInsn(Opcodes.IRETURN);

      mv.visitMaxs(0,0);
      mv.visitEnd();
   }

   public ClassWriter getClassWriter()
   {
      return writer;
   }

   public void writeClassToFile(String outputFile)
   {
      try {
         new FileOutputStream(outputFile).write((writer).toByteArray());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */

