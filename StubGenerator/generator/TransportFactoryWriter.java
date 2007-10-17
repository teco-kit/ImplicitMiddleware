/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import middleware.config.Config;
import middleware.config.Hosts;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TransportFactoryWriter {
   private static final String      remConfClass   = 
      "middleware/config/RemoteConfig";
   private static final String      protoConfClass = 
      "middleware/config/ProtocolsConfig";
   private static final String      sendRecvServ   = 
      "middleware/transport/SendReceiveServer";
   private static final String      sendRecv       = 
      "middleware/transport/SendReceive";

   private static final String      className      = 
      "middleware/transport/TransportFactory";


   private static final String      configField    = "config";
   private static final String      hostField      = "hosts";
   private              ClassWriter writer         =  null;

   private              String      localInputXML  =  null;
   private              String      remoteInputXML =  null;

   public TransportFactoryWriter(String localInputXML, String remoteInputXML)
   {
      MethodVisitor constructor = null;
      MethodVisitor mv          = null;

      this.localInputXML  = localInputXML;
      this.remoteInputXML = remoteInputXML;

      writer    = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

      writer.visit(Opcodes.V1_4,
                   Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                   className,
                   null,
                   "java/lang/Object",
                   null);

      writer.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
                        hostField,
                        "L" + remConfClass + ";",
                        null, null).visitEnd();
      writer.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
                        configField,
                        "L" + protoConfClass + ";",
                        null, null).visitEnd();

      mv = writer.visitMethod(Opcodes.ACC_STATIC, 
                              "<clinit>", "()V", null, null);
      mv.visitCode();
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                         remConfClass, 
                         "getInstance", 
                         "()L" + remConfClass + ";");
      mv.visitFieldInsn(Opcodes.PUTSTATIC, 
                        "middleware/transport/TransportFactory", 
                        hostField, "L" + remConfClass + ";");

      mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                         protoConfClass, 
                         "getInstance", "()L" + protoConfClass + ";");
      mv.visitFieldInsn(Opcodes.PUTSTATIC, 
                        className, 
                        configField, "L" + protoConfClass + ";");
      mv.visitInsn(Opcodes.RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();

      constructor
         = writer.visitMethod(Opcodes.ACC_PRIVATE, "<init>",
                              "()V", null, null);
      constructor.visitCode();

      constructor.visitVarInsn(Opcodes.ALOAD, 0);
      constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                  "java/lang/Object", "<init>", "()V");

      constructor.visitInsn(Opcodes.RETURN);
      constructor.visitMaxs(0,0);
      constructor.visitEnd();

      visitMakeSendReceiveServer();
      visitMakeSendReceive();

      writer.visitEnd();
   }

   private void visitMakeSendReceiveServer()
   {
      Config confFileLocal = new Config(localInputXML);
      Hashtable<String, String[]> localProtocols  =  
         confFileLocal.getProtocols();
      Enumeration<String> keysEnum   = localProtocols.keys();

      MethodVisitor mv =
         writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, 
                            "makeSendReceiveServer", 
                            "(Ljava/lang/String;[Ljava/lang/String;)L"
                            + sendRecvServ + ";", 
                            null, null);
      mv.visitCode();
      
      while (keysEnum.hasMoreElements()) {
         String nextKey = keysEnum.nextElement();

         mv.visitVarInsn(Opcodes.ALOAD, 0);
         mv.visitLdcInsn(nextKey);
         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "java/lang/String", 
                            "equals", "(Ljava/lang/Object;)Z");

         Label l1 = new Label();
         Label l2 = new Label();

         mv.visitJumpInsn(Opcodes.IFEQ, l1);

         mv.visitLabel(l2);
         mv.visitTypeInsn(Opcodes.NEW, "middleware/" + nextKey.replace('.', '/'));
         mv.visitInsn(Opcodes.DUP);
         mv.visitVarInsn(Opcodes.ALOAD, 1);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                            "middleware/" + nextKey.replace('.', '/'), 
                            "<init>", "([Ljava/lang/String;)V");
         mv.visitInsn(Opcodes.ARETURN);

         mv.visitLabel(l1);
      }

      mv.visitFieldInsn(Opcodes.GETSTATIC, 
    		            "java/lang/System", 
    		            "err", "Ljava/io/PrintStream;");
      mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuffer");
      mv.visitInsn(Opcodes.DUP);
      mv.visitLdcInsn("Error in configuration: no such protocol");
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
    		             "java/lang/StringBuffer", "<init>", 
    		             "(Ljava/lang/String;)V");
      Label l3 = new Label();
      mv.visitLabel(l3);

      mv.visitVarInsn(Opcodes.ALOAD, 0);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
    		             "java/lang/StringBuffer", 
    		             "append", 
    		             "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
    		             "java/lang/StringBuffer", 
    		             "toString", "()Ljava/lang/String;");
      Label l4 = new Label();
      mv.visitLabel(l4);
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
    		             "java/io/PrintStream", 
    		             "println", "(Ljava/lang/String;)V");


      mv.visitInsn(Opcodes.ACONST_NULL);
      mv.visitInsn(Opcodes.ARETURN);

      mv.visitMaxs(0, 0);
      mv.visitEnd();
   }

   private void visitMakeSendReceive()
   {
	   Hosts confFileRemote = new Hosts(remoteInputXML);
	   
	   Hashtable<String, String>   remoteProtocols = 
		   confFileRemote.getTransportProtocol();
	   Enumeration<String> keysEnum = remoteProtocols.elements();
	   
	   MethodVisitor mv =
		   writer.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, 
				              "makeSendReceive", 
				              "(Ljava/lang/Short;)L" + sendRecv + ";", 
				              null, null);
	   mv.visitCode();
	  
	   while (keysEnum.hasMoreElements()) {
		   String nextKey = keysEnum.nextElement();
		     
		   Label l0 = new Label();
		   mv.visitLabel(l0);

		   mv.visitFieldInsn(Opcodes.GETSTATIC, 
				   		     className, 
				   		     "hosts", "L" + remConfClass + ";");
		   mv.visitVarInsn(Opcodes.ALOAD, 0);
		   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
	    		              remConfClass, 
	    		              "getTransportName", 
		   					  "(Ljava/lang/Short;)Ljava/lang/String;");
		   Label l1 = new Label();
		   mv.visitLabel(l1);

		   mv.visitLdcInsn(nextKey);
		   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
		    	              "java/lang/String", 
		    	              "equals", "(Ljava/lang/Object;)Z");
		   Label l2 = new Label();
		   mv.visitJumpInsn(Opcodes.IFEQ, l2);
		   Label l3 = new Label();
		   mv.visitLabel(l3);

		   mv.visitTypeInsn(Opcodes.NEW, 
	    		            "middleware/" + nextKey.replace('.', '/'));
		   mv.visitInsn(Opcodes.DUP);
		   mv.visitFieldInsn(Opcodes.GETSTATIC, 
		    	             className, 
		    	             "hosts", "L" + remConfClass + ";");
	       mv.visitVarInsn(Opcodes.ALOAD, 0);
	       mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
	    		              remConfClass, 
	    		              "getTransportAttributes", 
	       					  "(Ljava/lang/Short;)[Ljava/lang/String;");
	       mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
		    	              "middleware/" + nextKey.replace('.', '/'), 
		    	              "<init>", "([Ljava/lang/String;)V");
	      
	       mv.visitInsn(Opcodes.ARETURN);
	       mv.visitLabel(l2);
	   }

	   mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", 
			             "err", "Ljava/io/PrintStream;");
	   mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuffer");
	   mv.visitInsn(Opcodes.DUP);
	   mv.visitLdcInsn("Error in configuration: no such host");
	   mv.visitMethodInsn(Opcodes.INVOKESPECIAL, 
			              "java/lang/StringBuffer", 
			              "<init>", "(Ljava/lang/String;)V");
	   mv.visitVarInsn(Opcodes.ALOAD, 0);
	   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
			              "java/lang/StringBuffer", 
			              "append", 
			              "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
	   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
			              "java/lang/StringBuffer", 
			              "toString", "()Ljava/lang/String;");
	   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
			              "java/io/PrintStream", 
			              "println", "(Ljava/lang/String;)V");
	   Label l4 = new Label();
	   mv.visitLabel(l4);

	   mv.visitInsn(Opcodes.ACONST_NULL);
	   mv.visitInsn(Opcodes.ARETURN);

	   mv.visitMaxs(0, 0);
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
