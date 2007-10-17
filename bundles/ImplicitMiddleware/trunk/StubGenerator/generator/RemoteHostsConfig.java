/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import middleware.config.Hosts;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RemoteHostsConfig {

   private static final String className = "middleware/config/RemoteConfig";
   private static final String hashtableClass = "java/util/Hashtable";
   private static final String transProt      = "transportProtocol";
   private static final String transAttr      = "transportAttributes";
   private static final String instanceField  = "instance";
   private              ClassWriter writer    =  null;

   public RemoteHostsConfig(String inputXML, 
		                    HashMap<String, Short> machName2machId)
   {
      Hosts confFile = new Hosts(inputXML);

      HashtableVisitor visitor =
         new HashtableVisitor(className, Opcodes.ACC_PRIVATE);
      
      Hashtable<Short, String> tProto     = new Hashtable<Short, String>();
      Hashtable<String, String> tProtoStr = confFile.getTransportProtocol();
      Enumeration<String> enumer  = tProtoStr.keys();
      
      while (enumer.hasMoreElements()) {
    	  String  mName = (String)enumer.nextElement();
    	  tProto.put(machName2machId.get(mName), tProtoStr.get(mName));
      }
      
      Hashtable<Short, String[]>  tAttr    = new Hashtable<Short, String[]>();
      Hashtable<String, String[]> tAttrStr = confFile.getTransportAttributes();
      Enumeration<String> enumerAttr       = tProtoStr.keys();
      
      while (enumerAttr.hasMoreElements()) {
    	  String  mName = (String)enumerAttr.nextElement();
    	  tAttr.put(machName2machId.get(mName), tAttrStr.get(mName));
      }
      
      visitor.visitHashtable(tProto, transProt,
                             "short", "String");
      visitor.visitHashtable(tAttr, transAttr,
                             "short", "String[]");
      writer = visitor.getClassWriter();

      writer.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
                        instanceField,
                        "L" + className + ";",
                        null, null).visitEnd();

      visitGetInstance();
      visitGetTransportName();
      visitGetTransportAttributes();

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

   private void visitGetTransportName()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC, 
                            "getTransportName", 
                            "(Ljava/lang/Short;)Ljava/lang/String;",
                            null, null);
      mv.visitCode();
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD, 
                        className, 
                        transProt, 
                        "L" + hashtableClass + ";");
      mv.visitVarInsn(Opcodes.ALOAD, 1);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                         hashtableClass, 
                         "get", 
                         "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
      mv.visitInsn(Opcodes.ARETURN);

      mv.visitMaxs(0,0);
      mv.visitEnd();
   }

   private void visitGetTransportAttributes()
   {
      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC, 
                        "getTransportAttributes", 
                        "(Ljava/lang/Short;)[Ljava/lang/String;",
                        null, null);
      mv.visitCode();
      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitFieldInsn(Opcodes.GETFIELD, 
                        className, 
                        transAttr, 
                        "L" + hashtableClass + ";");
      mv.visitVarInsn(Opcodes.ALOAD, 1);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                         hashtableClass, 
                         "get", 
                         "(Ljava/lang/Object;)Ljava/lang/Object;");
      mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/String;");
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
