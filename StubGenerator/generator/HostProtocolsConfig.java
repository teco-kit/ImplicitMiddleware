/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileOutputStream;
import org.objectweb.asm.*;

import middleware.config.Config;

public class HostProtocolsConfig {

   private static final String className = "middleware/config/ProtocolsConfig";
   private static final String fieldName      = "protocols";
   private static final String hostName       = "hostName";
   private static final String instanceField  = "instance";
   private              ClassWriter writer    =  null;

   public HostProtocolsConfig(String inputXML, Short host)
   {
      Config confFile = new Config(inputXML);

      HashtableVisitor visitor =
         new HashtableVisitor(className, Opcodes.ACC_PRIVATE);
      visitor.visitHashtable(confFile.getProtocols(), fieldName,
                             "String"               , "String[]");
      
      writer               = visitor.getClassWriter();
      MethodVisitor constr = visitor.getConstructor();
  
      constr.visitVarInsn(Opcodes.ALOAD, 0);
      
      constr.visitTypeInsn(Opcodes.NEW, "java/lang/Short");
      constr.visitInsn(Opcodes.DUP);
      constr.visitIntInsn(Opcodes.BIPUSH, host);
      constr.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                             "java/lang/Short", "<init>", 
                             "(S)V");

      constr.visitFieldInsn(Opcodes.PUTFIELD, className, hostName,
                            "Ljava/lang/Short;");
      
      writer.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
                        instanceField,
                        "L" + className + ";",
                        null, null).visitEnd();

      writer.visitField(Opcodes.ACC_PRIVATE,
                        hostName,
                        "Ljava/lang/Short;",
                        null, null).visitEnd();

      visitGetInstance();
      visitGetHostname();
      visitor.visitGetMethod(fieldName);

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

   private void visitGetHostname()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC,
                            "getHostname",
                            "()Ljava/lang/Short;",
                            null, null);
      mv.visitCode();
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD,
                        className,
                        hostName,
                        "Ljava/lang/Short;");

      mv.visitInsn(Opcodes.ARETURN);
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
