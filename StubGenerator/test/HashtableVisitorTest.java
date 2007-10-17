package test;

import java.util.Hashtable;
import org.objectweb.asm.*;

import junit.framework.TestCase;

import generator.HashtableVisitor;


public class HashtableVisitorTest extends TestCase {

	public void setUp() throws Exception {
	}

	public void tearDown() throws Exception {
	}
	
	public void runTest() {
		testIt();
	}

	public void testIt() {
		Hashtable table = new Hashtable();
		table.put("Hera", "Hektor");
		table.put("Afrodita", "Ahil");
		table.put("Atina", "Ares");
		
		HashtableVisitor visitor = 
			new HashtableVisitor("test/HashtableClass", Opcodes.ACC_PUBLIC);
		visitor.visitHashtable(table, "classTable", "String", "String");
		visitor.visitGetMethod("classTable");
		visitor.visitEnd();
		visitor.writeClassToFile("test/HashtableClass.class");
		
		HashtableClass hClass = new HashtableClass();
		Hashtable result = hClass.getClassTable();
		
		assertTrue(((String)result.get("Hera")).equals("Hektor"));
		assertTrue(((String)result.get("Afrodita")).equals("Ahil"));
		assertTrue(((String)result.get("Atina")).equals("Ares"));
	}
}
