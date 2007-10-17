package test;

import java.util.Date;


public class Class_B
{
   public Class_B() {}

   public Class_B(int i, Date date) {
      System.out.println("An Integer value of " + i);
      Long time = date.getTime();
      System.out.println("The time as long " + time);
   }

   public Class_B(int i, double db, String str) {
      System.out.println("What a pitty String " + str);
   }

   public String getName() {
      return new String("xxxx");
   }

   public int getNum() {
      return 1231231;
   }

   static public byte getByte() {
      return 2;
   }

   public Object getObj() {
      return new Object();
   }

   public Date getDate() {
      return new Date();
   }

   public static Date getStaticDate() {
      return new Date();
   }

   public int makeSomeThing(byte   b, char   c,
                            short  s, int    i,
                            long   l, float  f,
                            double d, String str,
                            Date   date)
   {
      return 1;
   }

   public static double makeSomeThingStatic(byte   b, char   c,
         short  s, int    i,
         long   l, float  f,
         double d, String str,
         Date   date)
   {
      return 1.1;
   }

   public void setUID(int i, Date date) {
      System.out.println("The date as string is: " + date.toString());
      return;
   }

   public Date setGetStr(String[] strArr) {
      Date date = new Date();
      System.out.println("The date as string is: " + strArr[0]);
      return date;
   }
}

