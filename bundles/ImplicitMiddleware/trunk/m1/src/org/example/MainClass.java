package org.example;

public class MainClass
{
	public static void main(String[] args) {
		Class_A a = new Class_A();
		Class_B b = new Class_B();
		String result_a = a.getName();
		String result_b = b.getName();
		
		MainClass mClass = new MainClass();
		System.out.println("Result A: " + result_a);
		System.out.println("Result B: " + result_b);
	}
	public String getName() {
		return "I'm MainClass";
	}
	public MainClass() {
	     System.out.println("THIS" + this.toString());
	}
}
