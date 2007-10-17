/************************************
 * Copyright TECO (www.teco.edu)    *
 * @author Dimitar Yordanov         *
 ************************************/
package generator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassGenerator {

   private DispatcherHelperWriter dspHelp     = null;
   private ClassWriter            classWriter = null;

   public static void Main(String[] inputFiles,
                           String[] outputFiles,
                           String   stubClass,
                           Vector<String> stubClasses)
   {
      int numOfClasses = inputFiles.length;

      DispatcherHelperWriter dspHelper =
         new DispatcherHelperWriter(numOfClasses, stubClasses);

      int i = 0;
      ClassGenerator dispGenerator = null;
      for (String inFile : inputFiles)
      {
         ClassGenerator classGenerator;
         i++;

         classGenerator = new ClassGenerator(inFile, stubClass);
         dispGenerator  = new ClassGenerator(inFile, i, dspHelper);
         classGenerator.writeClassToFile(outputFiles[i-1]);
      }
      dspHelper.allGoodThingsCome2AnEnd();
      dispGenerator.writeDispatcherToFile(
            "../Middleware/middleware/core/DispatcherHelper.class");
   }

   public ClassGenerator(String                 classFile,
                         int                    classId  ,
                         DispatcherHelperWriter dspHelper)
   {
      try {
         dspHelp = dspHelper;

         MethodCounter mCounter = new MethodCounter();
         ClassReader firstRead  =
            new ClassReader(new FileInputStream(classFile));
         firstRead.accept(mCounter, 0);

         System.out.println("Number of methods " +
                            mCounter.getNumOfMethods());

         dspHelp.setNumOfMethods(mCounter.getNumOfMethods());
         dspHelp.setClassId(classId);

         ClassReader cr =
            new ClassReader(new FileInputStream(classFile));
         cr.accept(dspHelp, 0);


      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public ClassGenerator(String classFile,
                         String stubClass)
   {
      try {
         classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
         StubClassVisitor classVisitor = new StubClassVisitor(classWriter);
         if (stubClass != null)
            classVisitor.setStubClass(stubClass);
         ClassReader cr =
            new ClassReader(new FileInputStream(classFile));
         cr.accept(classVisitor, 0);

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void writeClassToFile(String outputFile)
   {
      try {
         new FileOutputStream(outputFile).write((classWriter).toByteArray());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void writeDispatcherToFile(String outputFile)
   {
      try {
         ClassWriter dispClassWriter = dspHelp.getClassWriter();
         new FileOutputStream(outputFile).
            write((dispClassWriter).toByteArray());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
/* vim: set expandtab tabstop=3 shiftwidth=3 softtabstop=3: */
