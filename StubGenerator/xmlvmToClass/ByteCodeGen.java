package xmlvmToClass;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class ByteCodeGen {
	Element inXML;
	String classname;
	JavaClass clazz;
	ClassGen myClass;
	ConstantPoolGen cp;
	int classAccessFlags = 0;

	Map<String, Integer> fields = new HashMap<String, Integer>();


	public ByteCodeGen(Document doc)
	{
		this.inXML = doc.getRootElement().getChild("class");
		classname = inXML.getAttributeValue("name");

	}

	@SuppressWarnings("unchecked")
	public void generateClass(File outputFile)
	{
		String superclass = inXML.getAttributeValue("extends");
		if (inXML.getAttribute("package") != null)
			classname = new String(inXML.getAttributeValue("package")+ "." + classname);
		classAccessFlags = setAccessFlags(classAccessFlags, inXML);

		myClass = new ClassGen(classname, superclass, classname + ".class",  classAccessFlags, getInterfaces());

		cp = myClass.getConstantPool(); // cg creates constant pool



		List<Element> fields = inXML.getChildren("field");
		Iterator<Element> fieldIt = fields.iterator();
		while (fieldIt.hasNext())
		{
			Element field = fieldIt.next();
			Field f = generateField(field, cp);
//			System.out.println(cp.toString() + "\n" + cp.getSize());
			cp.addFieldref(f.getType().toString(), f.getName(), f.getSignature());
			this.fields.put(field.getAttributeValue("name"), cp.getSize()-1);
			myClass.addField(f);
			myClass.setConstantPool(cp);

//			System.out.println(cp.toString() + "\n" + cp.getSize());
		}

		List<Element> methods = inXML.getChildren("method");
		Iterator<Element> methodIt = methods.iterator();
		while (methodIt.hasNext())
			myClass.addMethod(generateMethod(methodIt.next()));


//		myClass.addMethod(this.generateMethod(it.next(), il).getMethod());
		// Allow instruction handles to be reused
//		myClass.addEmptyConstructor(Constants.ACC_PUBLIC);


		try {
			myClass.getJavaClass().dump(classname + ".class");
			myClass.getJavaClass().dump(outputFile);
		} catch(java.io.IOException e) { System.err.println(e); }

//		System.out.println(cp.toString());

	}

	private Field generateField(Element element, ConstantPoolGen constPool) 
	{
		int accessFlags = setAccessFlags(0, element);
		Type type = getType(element.getAttributeValue("type"));
		String name = element.getAttributeValue("name");

		FieldGen fg = new FieldGen(accessFlags, type, name, constPool);

		return new Field(fg.getField());
	}

	@SuppressWarnings("unchecked")
	private Method generateMethod(Element method)
	{
		InstructionList il = new InstructionList();
		int accessFlags = setAccessFlags(0, method);
		boolean isStatic = ((method.getAttribute("isStatic") != null) && method.getAttributeValue("isStatic").equals("true"));
		int localVarOffset = 1;
		if (isStatic)
			localVarOffset = 0;
		Element signature = method.getChild("signature");
		Element code = method.getChild("code");

//		make declaration
		MethodGen  mg;
		{
			Type returnType = getType(signature.getChild("return").getAttributeValue("type"));
			List<Type> argsType = new LinkedList<Type>();
			List<Element> argsElems = signature.getChildren("parameter");
			Iterator<Element> argsIt = argsElems.iterator();
			while (argsIt.hasNext())
				argsType.add(getType(argsIt.next().getAttributeValue("type")));
			Type[] parameters;
			if (argsType.size() == 0)
				parameters = Type.NO_ARGS;
			else
			{
				parameters = new Type[argsType.size()];
				parameters = argsType.toArray(parameters);
			}
			String[] argNames = makeArgNames(code.getChildren("var"), parameters.length);
			localVarOffset += parameters.length;


			mg = new MethodGen(accessFlags,
					returnType,               
					parameters,
					argNames, // arg names
					method.getAttributeValue("name"), classname,    // method, class
					il, cp);
		}
		{

			InstructionFactory factory = new InstructionFactory(myClass);
			{
				List<Element> instructions = code.getChildren();
				Iterator<Element> insIt = instructions.iterator();
				//skip the variables that are parameters and the this reference
				for (int i = 0; i < localVarOffset; i++)
					insIt.next();
				while (insIt.hasNext())
				{
					Element current = insIt.next();
					if (current.getName().equals("var") )//&& (new Integer(current.getAttributeValue("id")).intValue()) > localVarOffset)
						addLocalVar(current, mg, il);
					else
						addInstruction(current, il, cp, factory);
				}


			}

//			int name = lg.getIndex();
//			il.append(InstructionConstants.ACONST_NULL);
//			lg.setStart(il.append(new ASTORE(name))); // "name" valid from here
//			il.append(new ALOAD(1));
//			il.append(new PUSH(cp, lg.getLocalVariable(cp)));
//			il.append(new ASTORE(name));

//			il.append(InstructionConstants.RETURN);
		}
		mg.setMaxStack();
//		il.dispose();
		return mg.getMethod();
	}



	private void addInstruction(Element current, InstructionList il, ConstantPoolGen cp2, InstructionFactory factory) {
		String insName = current.getName();

		if (insName.equals("aload")){
//			il.append(new ALOAD(new Integer(current.getAttributeValue("index")).intValue()));
			il.append(InstructionFactory.createLoad(getType(current.getAttributeValue("type")), new Integer(current.getAttributeValue("index")).intValue()));
			
		}
		if (insName.equals("areturn")){
			il.append(new ARETURN());
		}
		if (insName.equals("astore")){
//			il.append(new ASTORE(new Integer(current.getAttributeValue("index")).intValue()));
			il.append(InstructionFactory.createStore(getType(current.getAttributeValue("type")), new Integer(current.getAttributeValue("index")).intValue()));
		}
		if (insName.equals("dload")){
			il.append(new DLOAD(new Integer(current.getAttributeValue("index")).intValue()));
		}
		if (insName.equals("dreturn")){
			il.append(new DRETURN());
		}
		if (insName.equals("dup")){
			il.append(new DUP());
		}
		if (insName.startsWith("invoke")){
			String classname = current.getAttributeValue("class-type");
			String methodname = current.getAttributeValue("method");
			Element signature = current.getChild("signature");
			String returnType = signature.getChild("return").getAttributeValue("type");
			Type[] parameters = makeArgTypes(signature.getChildren("parameter"));
			try {
				Class consts = Class.forName("org.apache.bcel.Constants");
				short type;
				type = consts.getField(insName.toUpperCase()).getShort(Constants.class);
				il.append(factory.createInvoke(classname, methodname, getType(returnType), parameters, type));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (insName.equals("getfield")){
			String class_name = current.getAttributeValue("class-type");
			String name = current.getAttributeValue("field");
			String type = current.getAttributeValue("type");

			il.append(factory.createGetField(class_name, name, getType(type)));
		}
		if (insName.equals("ldc")){
			String type = current.getAttributeValue("type");
			String value = current.getAttributeValue("value");
			int offset = addCP(type, value);

			il.append(new LDC(offset));
//			System.out.println(offset);
		}
		if (insName.equals("new")){
			il.append(factory.createNew(current.getAttributeValue("type")));
		}

		if (insName.equals("putfield")){
			String classtype = current.getAttributeValue("class-type");
			String type = current.getAttributeValue("type");
//			System.out.println(type);
			String name = current.getAttributeValue("field");
			Type t = getType(type);
			il.append(factory.createPutField(classtype, name, t));
		}
		if (insName.equals("return")){
			il.append(new RETURN());
		}

	}

	private int addCP(String type, String value) {
		if (type.equals("java.lang.String"))
			return cp.addString(value);
		return 0;
	}

	private void addLocalVar(Element current, MethodGen mg, InstructionList il) {
		String name = current.getAttributeValue("name");
		String type = current.getAttributeValue("type");
		int slot = new Integer(current.getAttributeValue("id")).intValue();

		mg.addLocalVariable(name, getType(type), slot, il.getStart(), il.getEnd());

	}

	private Method generateMethod1(Element method, InstructionList il)
	{
		int accessFlags = setAccessFlags(0, method);
		MethodGen  mg = new MethodGen(accessFlags,
				Type.VOID,               // return type
				new Type[] {             // argument types
				new ArrayType(Type.STRING, 1) },
				new String[] { "argv" }, // arg names
				"main", "HelloWorld",    // method, class
				il, cp);
		InstructionFactory factory = new InstructionFactory(myClass);

		ObjectType i_stream = new ObjectType("java.io.InputStream");
		ObjectType p_stream = new ObjectType("java.io.PrintStream");
		il.append(factory.createNew("java.io.BufferedReader"));
		il.append(InstructionConstants.DUP); // Use predefined constant
		il.append(factory.createNew("java.io.InputStreamReader"));
		il.append(InstructionConstants.DUP);
		il.append(factory.createFieldAccess("java.lang.System", "in", i_stream,
				Constants.GETSTATIC));
		il.append(factory.createInvoke("java.io.InputStreamReader", "<init>",
				Type.VOID, new Type[] { i_stream },
				Constants.INVOKESPECIAL));
		il.append(factory.createInvoke("java.io.BufferedReader", "<init>", Type.VOID,
				new Type[] {new ObjectType("java.io.Reader")},
				Constants.INVOKESPECIAL));

		LocalVariableGen lg = mg.addLocalVariable("in",
				new ObjectType("java.io.BufferedReader"), null, null);
		int in = lg.getIndex();
		lg.setStart(il.append(new ASTORE(in))); // "i" valid from here

		lg = mg.addLocalVariable("name", Type.STRING, null, null);
		int name = lg.getIndex();
		il.append(InstructionConstants.ACONST_NULL);
		lg.setStart(il.append(new ASTORE(name))); // "name" valid from here
		InstructionHandle try_start =
			il.append(factory.createFieldAccess("java.lang.System", "out", p_stream,
					Constants.GETSTATIC));

		il.append(new PUSH(cp, "Please enter your name> "));
		il.append(factory.createInvoke("java.io.PrintStream", "print", Type.VOID, 
				new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		il.append(new ALOAD(in));
		il.append(factory.createInvoke("java.io.BufferedReader", "readLine",
				Type.STRING, Type.NO_ARGS,
				Constants.INVOKEVIRTUAL));
		il.append(new ASTORE(name));
		GOTO g = new GOTO(null);
		InstructionHandle try_end = il.append(g);

		InstructionHandle handler = il.append(InstructionConstants.RETURN);
		mg.addExceptionHandler(try_start, try_end, handler, new ObjectType("java.io.IOException"));

		InstructionHandle ih =
			il.append(factory.createFieldAccess("java.lang.System", "out", p_stream,
					Constants.GETSTATIC));
		g.setTarget(ih);

		il.append(factory.createNew(Type.STRINGBUFFER));
		il.append(InstructionConstants.DUP);
		il.append(new PUSH(cp, "Hello, "));
		il.append(factory.createInvoke("java.lang.StringBuffer", "<init>",
				Type.VOID, new Type[] { Type.STRING },
				Constants.INVOKESPECIAL));
		il.append(new ALOAD(name));
		il.append(factory.createInvoke("java.lang.StringBuffer", "append",
				Type.STRINGBUFFER, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		il.append(factory.createInvoke("java.lang.StringBuffer", "toString",
				Type.STRING, Type.NO_ARGS,
				Constants.INVOKEVIRTUAL));

		il.append(factory.createInvoke("java.io.PrintStream", "println",
				Type.VOID, new Type[] { Type.STRING },
				Constants.INVOKEVIRTUAL));
		il.append(InstructionConstants.RETURN);
		mg.setMaxStack();




		return mg.getMethod();

	}

	private String[] makeArgNames(List<Element> vars, int number) {
		List<String> varNames = new LinkedList<String>();

		Iterator<Element> it = vars.iterator();
//		it.next();
		int i = 0;
		while (it.hasNext() && i < number)
		{
			Element current = it.next();
			if (!current.getAttributeValue("name").equals("this"))
			{
				int id = (new Integer(current.getAttributeValue("id")).intValue());
				varNames.add(current.getAttributeValue("name"));
				i++;
			}
		}

		String[] result = new String[varNames.size()];
		return varNames.toArray(result);
	}

	private Type[] makeArgTypes(List<Element> args)
	{
		Iterator<Element> it = args.iterator();
		int length = args.size();
		if (length > 0)
		{
			Type[] ret = new Type[length];
			for (int i = 0; i<length; i++)
				ret[i] = getType(it.next().getAttributeValue("type"));
			return ret;
		}
		else
			return Type.NO_ARGS;
	}

	private Type getType(String type)
	{
		if (type.equalsIgnoreCase("BOOLEAN"))
			return Type.BOOLEAN;
		if (type.equalsIgnoreCase("BYTE"))
			return Type.BYTE;
		if (type.equalsIgnoreCase("CHAR"))
			return Type.CHAR;
		if (type.equalsIgnoreCase("DOUBLE"))
			return Type.DOUBLE;
		if (type.equalsIgnoreCase("FLOAT"))
			return Type.FLOAT;
		if (type.equalsIgnoreCase("INT"))
			return Type.INT;
		if (type.equalsIgnoreCase("LONG"))
			return Type.LONG;
		if (type.equalsIgnoreCase("SHORT"))
			return Type.SHORT;
		if (type.equalsIgnoreCase("STRING"))
			return Type.STRING;
		if (type.equalsIgnoreCase("STRINGBUFFER"))
			return Type.STRINGBUFFER;
		if (type.equalsIgnoreCase("THROWABLE"))
			return Type.THROWABLE;
		if (type.equalsIgnoreCase("VOID"))
			return Type.VOID;
		if (type.endsWith("[]"))
		{
			type = type.substring(0, type.length() - 2);
			return getArrayType(type, 1);
		}


		else
			return new ObjectType(type);


	}

	private Type getArrayType(String type, int i) {
		if (type.endsWith("[]"))
		{
			type = type.substring(0, type.length() - 2);
			return getArrayType(type, i+1);
		}
		else
		{
			return new ArrayType(getType(type), i);
		}
	}

	private String[] getInterfaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	private int setAccessFlags(int accessFlags, Element XML) {

		List<Attribute> attr = XML.getAttributes();
		Iterator<Attribute> it = attr.iterator();
		while (it.hasNext())
			accessFlags = setAccessFlags(accessFlags, it.next());

		return accessFlags;

	}

	private int setAccessFlags(int accessFlags, Attribute at) {

		if (at.getName().equals("isPublic") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_PUBLIC);
		else if (at.getName().equals("isPrivate") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_PRIVATE);
		else if (at.getName().equals("isProtected") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_PROTECTED);
		else if (at.getName().equals("isStatic") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_STATIC);
		else if (at.getName().equals("isFinal") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_FINAL);
		else if (at.getName().equals("isSynchronized") && at.getValue().equals("true"))
			accessFlags = (int) (accessFlags | Constants.ACC_SYNCHRONIZED);

		//TODO complete list

		return accessFlags;

	}

}
