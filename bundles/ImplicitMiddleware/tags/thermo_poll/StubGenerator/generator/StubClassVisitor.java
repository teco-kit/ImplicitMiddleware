package generator;

import org.objectweb.asm.*;

public class StubClassVisitor extends ClassAdapter {

   private              String className  = null;
   private static final String stackClass = "middleware/core/ByteStack";
   private              String stubClass  = "middleware/core/MethodCallStub";
   private static final String uIdClass   = "middleware/core/UniqueID";
   private              int    methodId   = 0;

   public StubClassVisitor(ClassVisitor visitor) {
      super(visitor);
   }

   public void setStubClass(String name) {
      stubClass = name;
   }

   @Override
      public void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces)
      {

         System.out.println("class " + name + " extends " + superName + " {");
         className = name;
         cv.visit(Opcodes.V1_3  , access   , name,
                  signature, superName, interfaces);
      }

   @Override
      public FieldVisitor visitField(int    access, String name, 
                                     String desc  , String signature,
                                     Object value)
      {
         System.out.println(" " + desc + " " + name);
         return null;
      }

   @Override
      public MethodVisitor visitMethod(int      access, String name,
                                       String   desc  , String signature,
                                       String[] exceptions               )
      {
         System.out.println(" " + name + desc + signature + access);
         MethodVisitor mv =
            cv.visitMethod(access, name, desc, signature, exceptions);

         Type[] types = Type.getArgumentTypes(desc);

         methodId++;
         int lastPos = 0;
         int argSize = 0;
         for (Type t : types)
            argSize += t.getSize();

         lastPos = argSize;

         mv.visitCode();
         if (name.equals("<init>"))
         {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                               "java/lang/Object", "<init>", "()V");
         }
         // ------------------------------------------------------
         // ByteStack stack  = new ByteStack(25);
         // The ByteStack is only created when there are arguments
         int byteStackPos = 0;
         if (types.length > 0)
         {
            byteStackPos = ++lastPos;
            mv.visitTypeInsn(Opcodes.NEW, stackClass);
            mv.visitInsn(Opcodes.DUP);
            // TODO FIXME 25 is no good calculate it
            mv.visitIntInsn(Opcodes.BIPUSH, 25);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                               stackClass,
                               "<init>", "(I)V");
            mv.visitVarInsn(Opcodes.ASTORE, byteStackPos);
         }
         // ------------------------------------------------------

         // ------------------------------------------------------
         // MethodCallStub mStub = new MethodCallStub(realClassName);
         int mStubPos = ++lastPos;
         mv.visitTypeInsn(Opcodes.NEW, stubClass);
         mv.visitInsn(Opcodes.DUP);

         mv.visitLdcInsn(className);
         mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            stubClass,
                            "<init>", "(Ljava/lang/String;)V");
         mv.visitVarInsn(Opcodes.ASTORE, mStubPos);

         // ------------------------------------------------------

         if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)
            argSize--;

         lastPos = pushArgsOnStack(mv      , types  , byteStackPos,
                                   mStubPos, lastPos, argSize      );

         // ------------------------------------------------------
         // ByteStack retStack =
         //   mStub.callStubMethod(objectID, methodId, stack);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitVarInsn(Opcodes.ALOAD, mStubPos);

         if (name.equals("<init>"))
         {
            mv.visitInsn(Opcodes.ICONST_0);
         }
         else if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
            mv.visitInsn(Opcodes.ICONST_0);
         } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "uniqueID", "I");
         }

         mv.visitIntInsn(Opcodes.BIPUSH, methodId);
         // The ByteStack is only created when there are arguments
         if (types.length > 0)
            mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
         else
            mv.visitInsn(Opcodes.ACONST_NULL);

         mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            stubClass,
                            "callStubMethod",
                            "(IIL" + stackClass + ";)L" + stackClass + ";");

         // ------------------------------------------------------


         if (name.equals("<init>"))
         {
            // ByteStack retStack =
            //   mStub.callStubMethod(objectId, methodId, stack);
            int returnStackPos = ++lastPos;
            mv.visitVarInsn(Opcodes.ASTORE, returnStackPos);

            // returnStack
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, returnStackPos);
            // uniqueID = retStack.popInt();
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                               stackClass, "popInt", "()I");
            mv.visitFieldInsn(Opcodes.PUTFIELD, className, "uniqueID", "I");

            // mStub.addStubObject2heap(this, oid)
            mv.visitVarInsn(Opcodes.ALOAD, mStubPos);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, className, "uniqueID", "I");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stubClass, 
                               "addStubObject2Heap", "(Ljava/lang/Object;I)V");
            mv.visitInsn(Opcodes.RETURN);
         }
         else
         {
            // ------------------------------------------------------
            // Method return code
            Type returnType = Type.getReturnType(desc);

            // ByteStack retStack =
            if (returnType.getSort() != Type.VOID)
            {
               int returnStackPos = ++lastPos;
               mv.visitVarInsn(Opcodes.ASTORE, returnStackPos);
               mv.visitVarInsn(Opcodes.ALOAD, returnStackPos);
            }

            switch (returnType.getSort())
            {
               case Type.VOID:
                  mv.visitInsn(Opcodes.POP);
                  mv.visitInsn(Opcodes.RETURN);
                  break;
               case Type.BYTE:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popByte", "()B");
                  mv.visitInsn(Opcodes.IRETURN);
                  break;
               case Type.BOOLEAN:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popBool", "()Z");
                  mv.visitInsn(Opcodes.IRETURN);
                  break;
               case Type.CHAR:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popChar", "()C");
                  mv.visitInsn(Opcodes.IRETURN);
                  break;
               case Type.SHORT:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popShort", "()S");
                  mv.visitInsn(Opcodes.IRETURN);
                  break;
               case Type.INT:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popInt", "()I");
                  mv.visitInsn(Opcodes.IRETURN);
                  break;
               case Type.LONG:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popLong", "()J");
                  mv.visitInsn(Opcodes.LRETURN);
                  break;
               case Type.FLOAT:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popFloat", "()F");
                  mv.visitInsn(Opcodes.FRETURN);
                  break;
               case Type.DOUBLE:
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass, "popDouble", "()D");
                  mv.visitInsn(Opcodes.DRETURN);
                  break;
               default:
                  if (returnType.getClassName().equals("java.lang.String"))
                  {
                     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                        stackClass, "popString",
                                        "()Ljava/lang/String;");
                     mv.visitInsn(Opcodes.ARETURN);
                  }
                  else
                  {
                     String returnTypeStr = returnType.getInternalName();
                     int    objIdPos      = ++lastPos;
                     int    returnObjPos  = ++lastPos;


                     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                        stackClass, "popInt", "()I");
                     mv.visitVarInsn(Opcodes.ISTORE, objIdPos);


                     mv.visitVarInsn(Opcodes.ALOAD, mStubPos);
                     mv.visitVarInsn(Opcodes.ILOAD, objIdPos);
                     mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                                         stubClass,
                                         "getObjectFromOID",
                                         "(I)Ljava/lang/Object;" );
                     mv.visitTypeInsn( Opcodes.CHECKCAST, returnTypeStr );
                     mv.visitVarInsn(Opcodes.ASTORE, returnObjPos);

                     Label elseLabel = new Label();
                     mv.visitVarInsn(Opcodes.ALOAD, returnObjPos);
                     mv.visitJumpInsn(Opcodes.IFNONNULL, elseLabel);

                     mv.visitTypeInsn(Opcodes.NEW, returnTypeStr);
                     mv.visitInsn(Opcodes.DUP);
                     mv.visitTypeInsn(Opcodes.NEW, uIdClass);
                     mv.visitInsn(Opcodes.DUP);
                     mv.visitVarInsn(Opcodes.ILOAD, objIdPos);
                     mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                        uIdClass,
                                        "<init>", "(I)V");
                     mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                        returnTypeStr, "<init>",
                                        "(L" +  uIdClass + ";)V");
                     mv.visitInsn(Opcodes.ARETURN);

                     mv.visitLabel(elseLabel);
                     mv.visitVarInsn(Opcodes.ALOAD, returnObjPos);
                     mv.visitInsn(Opcodes.ARETURN);

                  }

                  break;
            }
            // ------------------------------------------------------

         }

         mv.visitMaxs(0,0);
         mv.visitEnd();
         return null;
         // }
         //return mv;
      }

   @Override
      public void visitEnd() {
         System.out.println("}");
         try {
            FieldVisitor fv  = cv.visitField(Opcodes.ACC_PRIVATE,
                                             "uniqueID", "I",
                                             null, null);
            fv.visitEnd();
            MethodVisitor uidMethod = cv.visitMethod(Opcodes.ACC_PUBLIC,
                                                     "getUniqueID",
                                                     "()I", null, null); 
            uidMethod.visitCode();
            uidMethod.visitVarInsn(Opcodes.ALOAD, 0);
            uidMethod.visitFieldInsn(Opcodes.GETFIELD, 
                                     className, "uniqueID", "I");
            uidMethod.visitInsn(Opcodes.IRETURN);
            uidMethod.visitMaxs(0,0);
            uidMethod.visitEnd();

            MethodVisitor constr 
               = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", 
                                "(Lmiddleware/core/UniqueID;)V", null, null);
            constr.visitCode();

            constr.visitVarInsn(Opcodes.ALOAD, 0);
            constr.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", 
                                   "<init>", "()V");

            constr.visitTypeInsn(Opcodes.NEW, stubClass);
            constr.visitInsn(Opcodes.DUP);
            constr.visitLdcInsn(className);
            constr.visitMethodInsn(Opcodes.INVOKESPECIAL, stubClass, 
                                   "<init>", "(Ljava/lang/String;)V");
            constr.visitVarInsn(Opcodes.ASTORE, 2);


            // constr.visitVarInsn(Opcodes.ALOAD, 2);
            // constr.visitVarInsn(Opcodes.ALOAD, 0);
            // constr.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
            //                        stubClass, 
            //                        "addStubObject2Heap", 
            //                        "(Ljava/lang/Object;)V");

            constr.visitVarInsn(Opcodes.ALOAD, 0);
            constr.visitVarInsn(Opcodes.ALOAD, 1);
            constr.visitMethodInsn(Opcodes.INVOKEVIRTUAL, uIdClass, 
                                   "getUniqueID", "()I");
            constr.visitFieldInsn(Opcodes.PUTFIELD, className, 
                                  "uniqueID", "I");


            // mStub.addStubObject2heap(this, oid)
            constr.visitVarInsn(Opcodes.ALOAD, 2);
            constr.visitVarInsn(Opcodes.ALOAD, 0);
            constr.visitVarInsn(Opcodes.ALOAD, 0);
            constr.visitFieldInsn(Opcodes.GETFIELD, className, "uniqueID", "I");
            constr.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stubClass, 
                                   "addStubObject2Heap", "(Ljava/lang/Object;I)V");

            constr.visitInsn(Opcodes.RETURN);
            constr.visitMaxs(0,0);
            constr.visitEnd();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }

   private int pushArgsOnStack(MethodVisitor mv          , Type[] types,
                               int           byteStackPos, int    mStubPos,
                               int           lastPos     , int    argSize)
   {
      int pos = argSize;

      // We need to push the args the other way around to get them in the
      // correct order on the other side
      for (int i = types.length - 1; i >= 0; i--)
      {
         Type t = types[i];

         switch (t.getSort())
         {
            case Type.VOID:
               break;
            case Type.BYTE:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.ILOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushByte", "(B)V");
               break;
            case Type.CHAR:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.ILOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushChar", "(C)V");
               break;
            case Type.SHORT:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.ILOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushShort", "(S)V");
               break;
            case Type.INT:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.ILOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushInt", "(I)V");
               break;
            case Type.LONG:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.LLOAD, --pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushLong", "(J)V");
               break;
            case Type.FLOAT:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.FLOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushFloat", "(F)V");
               break;
            case Type.DOUBLE:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.DLOAD, --pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushDouble", "(D)V");
               break;
            case Type.BOOLEAN:
               mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
               mv.visitVarInsn(Opcodes.ILOAD, pos);
               mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                  stackClass,
                                  "pushBool", "(Z)V");
               break;
            default:
               System.out.println("Name "+ t.getInternalName() +  " " + pos);
               if (t.getClassName().equals("java.lang.String"))
               {
                   mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
                   mv.visitVarInsn(Opcodes.ALOAD, pos);
                   mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                      stackClass,
                                      "pushString", "(Ljava/lang/String;)V");
               }
               else
               {

                  // ------------------------------------------------------
                  // stack.pushInt(mStub.getOIdForObject(uid));
                  int oIDPos = ++lastPos;
 
                  mv.visitVarInsn(Opcodes.ALOAD, pos);

                  mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                     stubClass,
                                     "getOidForObject",
                                     "(Ljava/lang/Object;)I");
                  mv.visitVarInsn(Opcodes.ISTORE, oIDPos);

                  mv.visitVarInsn(Opcodes.ALOAD, byteStackPos);
                  mv.visitVarInsn(Opcodes.ILOAD, oIDPos);
                  mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                     stackClass,
                                     "pushInt", "(I)V");
                  // ------------------------------------------------------
               }
               break;
         } // end switch
         pos--;
      }  // end for (int i = types.length - 1; i >= 0; i--)

      return lastPos;
   }
} // class end

/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
