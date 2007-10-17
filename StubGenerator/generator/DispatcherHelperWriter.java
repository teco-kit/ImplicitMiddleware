package generator;

import java.util.Vector;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class DispatcherHelperWriter extends ClassAdapter {

   private static final String stackClass = "middleware/core/ByteStack";
   private              String stubClass  = "middleware/core/MethodCallStub";
   private static final String dispHelp   = "middleware/core/DispatcherHelper";
   private static final String objHeap    = "middleware/core/ObjectHeap";
   private static final String uIdClass   = "middleware/core/UniqueID";

   private Vector<String> stubClasses  = null;
   private MethodVisitor  methodCall   = null;
   private ClassWriter    classWriter  = null;
   private Label[]        cLabels      = null;
   private Label[]        mLabels      = null;
   private Label          cDefLabel    = null;
   private Label          mDefLabel    = null;
   private int            numOfMethods = 0;
   private int            methodId     = 0;
   private int            classId      = 0;
   private String         className    = null;

   private int            classIdPos   = 1;
   private int            objPos       = 2;
   private int            methodIdPos  = 3;
   private int            stackPos     = 4;
   private int            retStackPos  = 5;
   private int            retValPos    = retStackPos + 1;

   public DispatcherHelperWriter(int numOfClasses, Vector<String> stubClasses) 
   {

      super(new ClassWriter(ClassWriter.COMPUTE_FRAMES));
      classWriter = (ClassWriter)cv;

      this.stubClasses = stubClasses;


      classWriter.visit(Opcodes.V1_3,
                        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                        dispHelp,
                        null,
                        "java/lang/Object",
                        null);

      classWriter.visitField(0, "objHeap",
                             "L" + objHeap + ";",
                             null, null).visitEnd();

      // -----------------------------------------------------------------
      // public DispatcherHelper() {
      //    objHeap = ObjectHeap.getInstance();
      // };
      MethodVisitor constructor
         = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                                  "()V", null, null);
      constructor.visitCode();

      constructor.visitVarInsn(Opcodes.ALOAD, 0);
      constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                  "java/lang/Object", "<init>", "()V");

      constructor.visitVarInsn(Opcodes.ALOAD, 0);
      constructor.visitMethodInsn(Opcodes.INVOKESTATIC,
                                  objHeap,
                                  "getInstance",
                                  "()L" + objHeap + ";");
      constructor.visitFieldInsn(Opcodes.PUTFIELD,
                                 dispHelp,
                                 "objHeap", "L" + objHeap + ";");

      constructor.visitInsn(Opcodes.RETURN);
      constructor.visitMaxs(0,0);
      constructor.visitEnd();

      // -----------------------------------------------------------------
      // public ByteStack methodCall(int classId,  Object    obj,
      //                             int methodId, ByteStack stack) 
      //  {
      //
      //    switch (classId) {
      //        case 1:
      //        {
      //           switch (methodId) {

      methodCall =
         classWriter.visitMethod(Opcodes.ACC_PUBLIC,
                                 "methodCall",
                                 "(ILjava/lang/Object;IL" + stackClass + ";)L"
                                 + stackClass + ";",
                                 null, null);

      methodCall.visitCode();
      methodCall.visitVarInsn(Opcodes.ILOAD, classIdPos);

      cLabels = new Label[numOfClasses];
      for (int i = 0; i < numOfClasses; i++)
         cLabels[i] = new Label();

      cDefLabel = new Label();

      // switch(classId)
      methodCall.visitTableSwitchInsn(1, numOfClasses, cDefLabel, cLabels);

   }

   public void setNumOfMethods(int numOfMethods)
   {
      this.numOfMethods = numOfMethods;
   }

   public void setClassId(int classId) {
      this.classId = classId;
   }

   @Override
   public void visit(int version     , int access      , String   name,
                     String signature, String superName, String[] interfaces)
   {
      System.out.println("ClassId " + classId);
      methodCall.visitLabel(cLabels[classId-1]);

      methodCall.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      methodCall.visitVarInsn(Opcodes.ILOAD, methodIdPos);

      mLabels = new Label[numOfMethods];
      for (int i = 0; i < numOfMethods; i++)
         mLabels[i] = new Label();

      mDefLabel = new Label();

      // switch(methodId)
      methodCall.visitTableSwitchInsn(1, numOfMethods, mDefLabel, mLabels);

      methodId  = 0;
      className = name;

   }

   @Override
   public MethodVisitor visitMethod(int      access, String name,
                                    String   desc  , String signature,
                                    String[] exceptions               )
   {

      retValPos    = retStackPos + 1;

      // case methodId:
      methodCall.visitLabel(mLabels[methodId++]);

      methodCall.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

      System.out.println("Method name " + name);
      System.out.println("Method signature " + desc);
      // inner classes are somehow compiled as methos ?
      if (name.contains("access$") || 
          ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE))
      {
         methodCall.visitInsn(Opcodes.ACONST_NULL);
         methodCall.visitInsn(Opcodes.ARETURN);
         return null;
      }

      if (name.equals("<init>"))
      {
         methodCall.visitTypeInsn(Opcodes.NEW, stackClass);
         methodCall.visitInsn(Opcodes.DUP);
         methodCall.visitInsn(Opcodes.ICONST_4);
         methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL, stackClass,
                                    "<init>"             , "(I)V");
         methodCall.visitVarInsn(Opcodes.ASTORE, retStackPos);


         popArgs(desc, name);

         // methodCall.visitTypeInsn(Opcodes.NEW, className);
         // methodCall.visitInsn(Opcodes.DUP);

         methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                                    className, name, desc);
         methodCall.visitVarInsn(Opcodes.ASTORE, ++retValPos);

         methodCall.visitVarInsn(Opcodes.ALOAD, retStackPos);
         methodCall.visitVarInsn(Opcodes.ALOAD, 0);
         methodCall.visitFieldInsn(Opcodes.GETFIELD, dispHelp,
                                   "objHeap", "L" + objHeap + ";");
         methodCall.visitVarInsn(Opcodes.ALOAD, retValPos);
         methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, objHeap,
                                    "insertObject", "(Ljava/lang/Object;)I");
         methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                    "pushInt", "(I)V");

      }
      else
      {
         // ------------------------------------------------------
         Type returnType = Type.getReturnType(desc);

         boolean isStatic = 
            ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC);

         if (returnType.getSort() != Type.VOID)
         { 
            methodCall.visitTypeInsn(Opcodes.NEW, stackClass);
            methodCall.visitInsn(Opcodes.DUP);
         }

         switch (returnType.getSort())
         {
            case Type.VOID:
            {
               int opCode = Opcodes.INVOKESTATIC;

               if (!isStatic)
               {
                  opCode = Opcodes.INVOKEVIRTUAL;
                  methodCall.visitVarInsn(Opcodes.ALOAD, objPos);
                  methodCall.visitTypeInsn( Opcodes.CHECKCAST, 
                                            className ); 
               }
               popArgs(desc, name);

               methodCall.visitMethodInsn(opCode, className, name, desc);

               retValPos++;
               methodCall.visitInsn(Opcodes.ACONST_NULL);
               methodCall.visitInsn(Opcodes.ARETURN);
               return null;

               // methodCall.visitInsn(Opcodes.ICONST_1);
               // newStackAndCallMethod(isStatic, name, desc);
               // break;
            }
            case Type.BYTE:
               methodCall.visitInsn(Opcodes.ICONST_1);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.ISTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.ILOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushByte", "(B)V");
               break;
            case Type.BOOLEAN:
               methodCall.visitInsn(Opcodes.ICONST_2);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.ISTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.ILOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushBool", "(Z)V");
               break;
            case Type.CHAR:
               methodCall.visitInsn(Opcodes.ICONST_2);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.ISTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.ILOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushChar", "(C)V");
               break;
            case Type.SHORT:
               methodCall.visitInsn(Opcodes.ICONST_2);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.ISTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.ILOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushShort", "(S)V");
               break;
            case Type.INT:
               methodCall.visitInsn(Opcodes.ICONST_4);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.ISTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.ILOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushInt", "(I)V");
               break;
            case Type.LONG:
               methodCall.visitIntInsn(Opcodes.BIPUSH, 8);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.LSTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.LLOAD  , retValPos++);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushLong", "(J)V");
               break;
            case Type.FLOAT:
               methodCall.visitInsn(Opcodes.ICONST_4);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.FSTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.FLOAD  , retValPos);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushFloat", "(F)V");
               break;
            case Type.DOUBLE:
               methodCall.visitIntInsn(Opcodes.BIPUSH, 8);
               newStackAndCallMethod(isStatic, name, desc);

               methodCall.visitVarInsn(Opcodes.DSTORE , retValPos);
               methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
               methodCall.visitVarInsn(Opcodes.DLOAD  , retValPos++);

               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                          stackClass, "pushDouble", "(D)V");
               break;
            default:
               if (returnType.getClassName().equals("java.lang.String"))
               {
                  int opCode = Opcodes.INVOKESTATIC;
                  
                  if (!isStatic)
                  {
                     opCode = Opcodes.INVOKEVIRTUAL;
                     methodCall.visitVarInsn(Opcodes.ALOAD, objPos);
                     methodCall.visitTypeInsn( Opcodes.CHECKCAST, 
                                               className );  
                  }
                  popArgs(desc, name);

                  methodCall.visitMethodInsn(opCode, className, name, desc);
                  methodCall.visitVarInsn(Opcodes.ASTORE , retValPos);
   

                  methodCall.visitVarInsn(Opcodes.ALOAD, retValPos);
                  methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                             "java/lang/String",
                                             "length", "()I");
                  
                  methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL   , stackClass,
                                             "<init>"                , "(I)V");
                  methodCall.visitVarInsn(Opcodes.ASTORE, retStackPos);

                  methodCall.visitVarInsn(Opcodes.ALOAD  , retStackPos);
                  methodCall.visitVarInsn(Opcodes.ALOAD  , retValPos);

                  methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                             stackClass, "pushString",
                                             "(Ljava/lang/String;)V");
               }
               else
               {

                  methodCall.visitInsn(Opcodes.ICONST_4);
                  newStackAndCallMethod(isStatic, name, desc);

                  methodCall.visitVarInsn(Opcodes.ASTORE, retValPos);


                  methodCall.visitVarInsn(Opcodes.ALOAD , retValPos);

                  methodCall.visitMethodInsn(Opcodes.INVOKESTATIC,
                                             stubClass,
                                             "getOidForObject",
                                             "(Ljava/lang/Object;)I");
                  methodCall.visitVarInsn(Opcodes.ISTORE, ++retValPos);

                  methodCall.visitVarInsn(Opcodes.ALOAD, retStackPos);
                  methodCall.visitVarInsn(Opcodes.ILOAD, retValPos);
                  methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                             stackClass,
                                             "pushInt", "(I)V");

               }
         } // end switch


      } // end if (name.equals("<init>")) {} else {}

      retValPos++;

      methodCall.visitVarInsn(Opcodes.ALOAD, retStackPos);
      methodCall.visitInsn(Opcodes.ARETURN);
     
      return null;
   } // end visitMethod

   public FieldVisitor visitField(int    access, String name, 
                                  String desc  , String signature,
                                  Object value)
   {
      System.out.println(" " + desc + " " + name);
      return null;
   }

   @Override
   public void visitEnd() {

      methodCall.visitLabel(mDefLabel);

      methodCall.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      methodCall.visitInsn(Opcodes.ACONST_NULL);
      methodCall.visitInsn(Opcodes.ARETURN);
      // methodCall.visitMaxs(0,0);
   }

   public void allGoodThingsCome2AnEnd() {
      methodCall.visitLabel(cDefLabel);
      methodCall.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      methodCall.visitInsn(Opcodes.ACONST_NULL);
      methodCall.visitInsn(Opcodes.ARETURN);

      // getObjectOrStub
      //

      /*
      MethodVisitor mvGetObject 
         = cv.visitMethod(Opcodes.ACC_PRIVATE, "__getObjectOrStub", 
                          "(I)Ljava/lang/Object;", null, null);
      mvGetObject.visitCode();


      mvGetObject.visitVarInsn(Opcodes.ALOAD, 0);
      mvGetObject.visitFieldInsn(Opcodes.GETFIELD, dispHelp,
                                "objHeap", "L" + objHeap + ";");
      mvGetObject.visitVarInsn(Opcodes.ILOAD, 1);
      mvGetObject.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                 objHeap,
                                 "getObject",
                                 "(I)Ljava/lang/Object;");

      mvGetObject.visitVarInsn(Opcodes.ASTORE, 2);

      Label l1 = new Label();
      Label l2 = new Label();

      mvGetObject.visitVarInsn(Opcodes.ALOAD, 2);
      mvGetObject.visitJumpInsn(Opcodes.IFNONNULL, l1);
      mvGetObject.visitLabel(l2);
      mvGetObject.visitVarInsn(Opcodes.ALOAD, 2);
      mvGetObject.visitInsn(Opcodes.ARETURN);
      mvGetObject.visitLabel(l1);
      mvGetObject.visitVarInsn(Opcodes.ALOAD, 2);
      mvGetObject.visitInsn(Opcodes.ARETURN);

      mvGetObject.visitMaxs(0,0);
      mvGetObject.visitEnd();
      */
      // End
      methodCall.visitMaxs(0,0);
      methodCall.visitEnd();
      classWriter.visitEnd();
   }

   public ClassWriter getClassWriter() {
      return classWriter;
   }

   public void setStubClass(String sClass) {
      stubClass = sClass;
   }

   private void newStackAndCallMethod(Boolean isStatic, 
                                      String  name    ,
                                      String  desc    )
   {
      int opCode = Opcodes.INVOKESTATIC;

      methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL   , stackClass,
                                  "<init>"               , "(I)V");
      methodCall.visitVarInsn(Opcodes.ASTORE, retStackPos);

      if (!isStatic)
      {
         opCode = Opcodes.INVOKEVIRTUAL;
         methodCall.visitVarInsn(Opcodes.ALOAD, objPos);
         methodCall.visitTypeInsn( Opcodes.CHECKCAST, 
                                   className );  
      }
      popArgs(desc, name);

      methodCall.visitMethodInsn(opCode, className, name, desc);

   }

   private void popArgs(String desc, String name)
   {
      int mStartPosition = retValPos + 1;

      Type[] types = Type.getArgumentTypes(desc);
      for (Type t : types)
      {
         retValPos++;
         switch (t.getSort())
         {
            case Type.VOID:
               break;
            case Type.BYTE:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popByte"            , "()B");
               methodCall.visitVarInsn(Opcodes.ISTORE, retValPos);
               break;
            case Type.BOOLEAN:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popBool"            , "()Z");
               methodCall.visitVarInsn(Opcodes.ISTORE, retValPos);
               break;
            case Type.CHAR:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popChar"            , "()C");
               methodCall.visitVarInsn(Opcodes.ISTORE, retValPos);
               break;
            case Type.SHORT:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popShort"           , "()S");
               methodCall.visitVarInsn(Opcodes.ISTORE, retValPos);
               break;
            case Type.INT:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popInt"             , "()I");
               methodCall.visitVarInsn(Opcodes.ISTORE, retValPos);
               break;
            case Type.LONG:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popLong"            , "()J");
               methodCall.visitVarInsn(Opcodes.LSTORE, retValPos++);
               break;
            case Type.FLOAT:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popFloat"           , "()F");
               methodCall.visitVarInsn(Opcodes.FSTORE, retValPos);
               break;
            case Type.DOUBLE:
               methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
               methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                           "popDouble"          , "()D");
               methodCall.visitVarInsn(Opcodes.DSTORE, retValPos++);
               break;
            default:
               if (t.getClassName().equals("java.lang.String"))
               {
                  methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
                  methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                             stackClass           ,
                                             "popString"          ,
                                             "()Ljava/lang/String;");
                  methodCall.visitVarInsn(Opcodes.ASTORE, retValPos);
               }
               else
               {
                  // methodCall.visitVarInsn(Opcodes.ALOAD, 0);
                  // methodCall.visitFieldInsn(Opcodes.GETFIELD, dispHelp,
                  //                           "objHeap", "L" + objHeap + ";");
                  int oidPos    = retValPos;
                  int retObjPos = ++retValPos;
                  methodCall.visitVarInsn(Opcodes.ALOAD, stackPos);
                  methodCall.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stackClass,
                                             "popInt"             , "()I");
                  methodCall.visitVarInsn(Opcodes.ISTORE, oidPos);
                  methodCall.visitVarInsn(Opcodes.ILOAD, oidPos);
                  methodCall.visitMethodInsn(Opcodes.INVOKESTATIC,
                                             stubClass,
                                             "getObjectOrStub",
                                             "(I)Ljava/lang/Object;");
                  System.out.println("DIMENSIONS " + t.getDimensions());
                  System.out.println("DIMENSIONS " + t.getInternalName());
                  if (t.getSort() == Type.ARRAY)
                     methodCall.visitTypeInsn( Opcodes.CHECKCAST, 
                                               "[" + t.getInternalName() + ";");
                  else
                     methodCall.visitTypeInsn( Opcodes.CHECKCAST, 
                                               t.getInternalName() );
                  methodCall.visitVarInsn(Opcodes.ASTORE, retObjPos);

                  if (stubClasses.contains(t.getInternalName()))
                  {
                     Label l1 = new Label();
                     Label l2 = new Label();

                     methodCall.visitVarInsn(Opcodes.ALOAD, retObjPos);
                     methodCall.visitJumpInsn(Opcodes.IFNONNULL, l1);
                     // if null
                     methodCall.visitLabel(l2);
                     methodCall.visitTypeInsn(Opcodes.NEW, t.getInternalName());
                     methodCall.visitInsn(Opcodes.DUP);

                     methodCall.visitTypeInsn(Opcodes.NEW, uIdClass);
                     methodCall.visitInsn(Opcodes.DUP);
                     methodCall.visitVarInsn(Opcodes.ILOAD, oidPos);
                     methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL,
                                                uIdClass,
                                                "<init>", "(I)V");
                     methodCall.visitMethodInsn(Opcodes.INVOKESPECIAL, t.getInternalName(),
                                                "<init>"             ,
                                                "(L" +  uIdClass + ";)V");
                     methodCall.visitVarInsn(Opcodes.ASTORE, retObjPos);
                     // if not null
                     methodCall.visitLabel(l1);
                  }

               }
               break;

         } // end switch
      } // end for

      if (name.equals("<init>"))
      {
         methodCall.visitTypeInsn(Opcodes.NEW, className);
         methodCall.visitInsn(Opcodes.DUP);
      }

      int position = mStartPosition;

      for (Type t : types)
      {
         switch (t.getSort())
         {
            case Type.VOID:
               break;
            case Type.BYTE:
               methodCall.visitVarInsn(Opcodes.ILOAD, position);
               break;
            case Type.BOOLEAN:
               methodCall.visitVarInsn(Opcodes.ILOAD, position);
               break;
            case Type.CHAR:
               methodCall.visitVarInsn(Opcodes.ILOAD, position);
               break;
            case Type.SHORT:
               methodCall.visitVarInsn(Opcodes.ILOAD, position);
               break;
            case Type.INT:
               methodCall.visitVarInsn(Opcodes.ILOAD, position);
               break;
            case Type.LONG:
               methodCall.visitVarInsn(Opcodes.LLOAD, position++);
               break;
            case Type.FLOAT:
               methodCall.visitVarInsn(Opcodes.FLOAD, position);
               break;
            case Type.DOUBLE:
               methodCall.visitVarInsn(Opcodes.DLOAD, position++);
               break;
            default:
               // If we increse retValPos above we have to increase
               // position here!! 
               methodCall.visitVarInsn(Opcodes.ALOAD, ++position);
               break;
         } // end switch
         position++;

      }
   }
}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
