/*
 *  XMLVM --- An XML-based Programming Language
 *  Copyright (c) 2004-2005 by Arno Puder
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  For more information, visit the XMLVM Home Page at
 *  http://www.xml11.org/xmlvm/
 */

/*
 * Created on Mar 21, 2005 by Arno
 */

package org.xmlvm;

import java.util.Date;
import java.util.Hashtable;

import org.jdom.*;

import edu.arizona.cs.mbel.instructions.*;
import edu.arizona.cs.mbel.mbel.*;
import edu.arizona.cs.mbel.signature.*;



/**
 * @author Arno
 *  
 */
public class ParseCIL
{

    private Module    module;

    private Namespace ns;

    private Hashtable map;



    public ParseCIL(Module module)
    {
        this.module = module;
    }



    public Document genXMLVM()
    {
        ns = Namespace.NO_NAMESPACE;
        //ns = Namespace.getNamespace("vm", "http://www.xmlvm.org");
        Element xml = new org.jdom.Element("xmlvm", ns);
        xml.addContent(new org.jdom.Comment("Generated: " + new Date()));
        TypeDef[] td = module.getTypeDefs();
        for (int i = 0; i < td.length; i++) {
            if (td[i].getFlags() != 0)
                xml.addContent(visitTypeDef(td[i]));
        }
        org.jdom.Document doc = new Document(xml);
        return doc;
    }



    private Element visitTypeDef(TypeDef td)
    {
        Element xml = new Element("class", ns);
        String name = td.getName();
        String namespace = td.getNamespace();
        TypeRef tr = td.getSuperClass();
        String base_class = tr.getNamespace() + "." + tr.getName();
        xml.setAttribute("name", td.getName(), ns);
        if (!namespace.equals(""))
            xml.setAttribute("package", namespace, ns);
        xml.setAttribute("extends", base_class, ns);

        // visit all fields
        Field[] f = td.getFields();
        for (int i = 0; i < f.length; i++) {
            xml.addContent(visitField(f[i]));
        }

        // visit all methods
        Method[] mt = td.getMethods();
        for (int i = 0; i < mt.length; i++)
            xml.addContent(visitMethod(mt[i]));
        return xml;
    }



    private Element visitField(Field f)
    {
        Element xml = new Element("field", ns);
        addFieldModifiers(f, xml);
        xml.setAttribute("name", f.getName(), ns);
        addType(f.getSignature().getType(), xml);
        return xml;
    }



    private Element visitMethod(Method mt)
    {
        Element xml = new Element("method", ns);
        String name = mt.getName();
        if (name.equals(".ctor"))
            name = "<init>";
        xml.setAttribute("name", name, ns);
        MethodBody mb = mt.getMethodBody();
        xml.setAttribute("stack", "" + mb.getMaxStack(), ns);
        xml.setAttribute("locals", "" + computeLocals(mt), ns);
        addMethodModifiers(mt, xml);
        addMethodSignature(mt.getSignature(), xml);
        addMethodCode(mb, xml);
        return xml;
    }



    private int computeLocals(Method mt)
    {
        /*
         * this method computes the number of locals we need. We use "locals"
         * here as defined in the JVM.
         */
        int locals = 0;

        // Add the number of local variables
        MethodBody mb = mt.getMethodBody();
        LocalVarList lv = mb.getLocalVarList();
        if (lv != null)
            locals += lv.getCount();

        // add the number of actual parameters for this method
        MethodSignature ms = mt.getSignature();
        locals += ms.getParameters().length;

        // add one more for the "this" pointer in case this is
        // not a static method
        if (ms.HasThis())
            locals++;

        return locals;
    }



    private void addMethodModifiers(Method mt, Element xml)
    {
        int attr = mt.getFlags();
        if ((attr & MethodAttributes.Static) != 0) {
            xml.setAttribute("isStatic", "true", ns);
        }
        if ((attr & MethodAttributes.Public) != 0) {
            xml.setAttribute("isPublic", "true", ns);
        }
        if ((attr & MethodAttributes.Private) != 0) {
            xml.setAttribute("isPrivate", "true", ns);
        }
    }



    private void addFieldModifiers(Field f, Element xml)
    {
        int attr = f.getFlags();
        if ((attr & FieldAttributes.Static) != 0) {
            xml.setAttribute("isStatic", "true", ns);
        }
        if ((attr & FieldAttributes.Public) != 0) {
            xml.setAttribute("isPublic", "true", ns);
        }
        if ((attr & FieldAttributes.Private) != 0) {
            xml.setAttribute("isPrivate", "true", ns);
        }
    }



    private void addMethodSignature(MethodSignature ms, Element xml)
    {
        Element xml_sig = new Element("signature", ns);

        // add return type
        ReturnTypeSignature ret = ms.getReturnType();
        Element xml_ret = new Element("return", ns);
        //addType(ret.getType(), xml_ret);
        addReturnType(ret, xml_ret);
        xml_sig.addContent(xml_ret);

        // add parameter types
        ParameterSignature[] ps = ms.getParameters();
        for (int i = 0; i < ps.length; i++) {
            Element xml_param = new Element("parameter", ns);
            ParameterSignature p = ps[i];
            addType(p.getType(), xml_param);
            xml_sig.addContent(xml_param);
        }
        xml.addContent(xml_sig);
    }



    private void addType(TypeSignature ts, Element xml)
    {
        String type = null;
        byte t = ts.getType();
        if (t == TypeSignature.ELEMENT_TYPE_STRING)
            type = "java.lang.String";
        if (t == TypeSignature.ELEMENT_TYPE_VOID)
            type = "void";
        if (t == TypeSignature.ELEMENT_TYPE_I4)
            type = "int";
        if (type == null) {
            System.err.println("Unknown type: " + ts);
            type = "UNKNOWN";
        }
        xml.setAttribute("type", type, ns);
    }



    private void addReturnType(ReturnTypeSignature ts, Element xml)
    {
        if (ts == null)
            // can be null for constructor
            return;
        int t = ts.getElementType();
        String type = null;
        if (t == SignatureConstants.ELEMENT_TYPE_VOID) {
            type = "void";
        }
        xml.setAttribute("type", type, ns);
    }



    private void addMethodCode(MethodBody mb, Element xml)
    {
        Element xml_code = new Element("code", ns);
        xml.addContent(xml_code);
        InstructionHandle[] ihl = mb.getInstructionList()
                .getInstructionHandles();
        buildBranchTable(ihl);
        //Instruction[] il = mb.getInstructionList().getInstructions();
        for (int i = 0; i < ihl.length; i++) {
            InstructionHandle ih = ihl[i];
            Integer label = (Integer) map.get(ih);
            if (label != null) {
                Element xml_label = new Element("label", ns);
                xml_label.setAttribute("id", "" + label, ns);
                xml_code.addContent(xml_label);
            }
            addInstruction(ihl[i].getInstruction(), xml_code);
        }
    }



    private void buildBranchTable(InstructionHandle[] ihl)
    {
        int counter = 0;

        map = new Hashtable();
        for (int i = 0; i < ihl.length; i++) {
            InstructionHandle ih = ihl[i];
            if (ih.getInstruction() instanceof BranchInstruction) {
                BranchInstruction bi = (BranchInstruction) ih.getInstruction();
                put(bi.getTargetHandle(), counter++);
            }
        }
    }



    private void put(InstructionHandle ih, int id)
    {
        if ((Integer) map.get(ih) == null)
            map.put(ih, new Integer(id));
    }



    private void addInstruction(Instruction inst, Element xml)
    {
        Element xml_inst = null;
        if (inst instanceof LDARG)
            xml_inst = visitInstructionLDARG((LDARG) inst);
        if (inst instanceof RET)
            xml_inst = visitInstructionRET((RET) inst);
        if (inst instanceof LDSTR)
            xml_inst = visitInstructionLDSTR((LDSTR) inst);
        if (inst instanceof CALL)
            xml_inst = visitInstructionCALL((CALL) inst);
        if (inst instanceof CALLVIRT)
            xml_inst = visitInstructionCALLVIRT((CALLVIRT) inst);
        if (inst instanceof NEWOBJ)
            xml_inst = visitInstructionNEWOBJ((NEWOBJ) inst);
        if (inst instanceof ADD)
            xml_inst = visitInstructionADD((ADD) inst);
        if (inst instanceof LDC)
            xml_inst = visitInstructionLDC((LDC) inst);
        if (inst instanceof BGE)
            xml_inst = visitBranchInstruction((BranchInstruction) inst,
                    "if_icmpge");
        if (inst instanceof BR)
            xml_inst = visitBranchInstruction((BranchInstruction) inst, "goto");
        if (inst instanceof LDFLD)
            xml_inst = visitAccessFieldInstruction(((LDFLD) inst).getField(), "getfield");
        if (inst instanceof STFLD)
            xml_inst = visitAccessFieldInstruction(((STFLD) inst).getField(), "putfield");
        if (xml_inst == null) {
            System.err.println(inst);
            xml_inst = new Element("UNKNOWN");
        }
        xml.addContent(xml_inst);
    }



    private Element visitAccessFieldInstruction(FieldRef ref, String opc)
    {
        Element xml = new Element(opc, ns);
        AbstractTypeReference pt = ref.getParent();
        TypeRef tr = (TypeRef) pt;
        String name = tr.getNamespace();
        if (!name.equals(""))
            name += ".";
        name += tr.getName();
        xml.setAttribute("class-type", name, ns);
        TypeSignature ts = ref.getSignature().getType();
        addType(ts, xml);
        xml.setAttribute("field", ref.getName(), ns);
        return xml;
    }



    private Element visitBranchInstruction(BranchInstruction inst, String cmd)
    {
        Element xml = new Element(cmd, ns);
        InstructionHandle target = inst.getTargetHandle();
        Integer label = (Integer) map.get(target);
        xml.setAttribute("label", "" + label, ns);
        return xml;
    }



    private Element visitInstructionLDC(LDC inst)
    {
        Element xml = new Element("sipush", ns);
        xml.setAttribute("type", "int", ns);
        xml.setAttribute("value", "" + inst.getConstantValue(), ns);
        return xml;
    }



    private Element visitInstructionADD(ADD inst)
    {
        Element xml = new Element("iadd", ns);
        return xml;
    }



    private Element visitInstructionNEWOBJ(NEWOBJ inst)
    {
        Element xml = new Element("new", ns);
        MethodDefOrRef m = inst.getMethod();
        AbstractTypeReference pt = m.getParent();
        TypeRef tr = (TypeRef) pt;
        String name = tr.getNamespace();
        if (!name.equals(""))
            name += ".";
        name += tr.getName();
        xml.setAttribute("type", name, ns);
        MethodSignature ms = null;
        if (m instanceof MethodRef)
            ms = ((MethodRef) m).getCallsiteSignature();
        if (m instanceof Method)
            ms = ((Method) m).getSignature();
        addMethodSignature(ms, xml);
        return xml;
    }



    private Element visitInstructionLDARG(LDARG inst)
    {
        Element xml = new Element("iload", ns);
        xml.setAttribute("index", "" + inst.getArgumentNumber(), ns);
        return xml;
    }



    private Element visitInstructionCALL(CALL inst)
    {
        MethodDefOrRef m = inst.getMethod();
        MethodSignature ms = null;
        if (m instanceof MethodRef)
            ms = ((MethodRef) m).getCallsiteSignature();
        if (m instanceof Method)
            ms = ((Method) m).getSignature();
        boolean hasThis = ms.HasThis();
        Element xml = new Element(hasThis ? "invokevirtual" : "invokestatic",
                                  ns);
        AbstractTypeReference pt = m.getParent();
        TypeRef tr = (TypeRef) pt;
        String name = tr.getNamespace();
        if (!name.equals(""))
            name += ".";
        name += tr.getName();
        xml.setAttribute("class-type", name, ns);
        name = m.getName();
        if (name.equals(".ctor"))
            name = "<init>";
        xml.setAttribute("method", name, ns);
        addMethodSignature(ms, xml);
        return xml;
    }



    private Element visitInstructionCALLVIRT(CALLVIRT inst)
    {
        MethodDefOrRef m = inst.getMethod();
        MethodSignature ms = null;
        if (m instanceof MethodRef)
            ms = ((MethodRef) m).getCallsiteSignature();
        if (m instanceof Method)
            ms = ((Method) m).getSignature();
        boolean hasThis = ms.HasThis();
        Element xml = new Element(hasThis ? "invokevirtual" : "invokestatic",
                                  ns);
        AbstractTypeReference pt = m.getParent();
        TypeRef tr = (TypeRef) pt;
        String name = tr.getNamespace();
        if (!name.equals(""))
            name += ".";
        name += tr.getName();
        xml.setAttribute("class-type", name, ns);
        name = m.getName();
        if (name.equals(".ctor"))
            name = "<init>";
        xml.setAttribute("method", name, ns);
        addMethodSignature(ms, xml);
        return xml;
    }



    private Element visitInstructionRET(RET inst)
    {
        return new Element("return", ns);
    }



    private Element visitInstructionLDSTR(LDSTR inst)
    {
        Element xml = new Element("ldc", ns);
        xml.setAttribute("type", "java.lang.String", ns);
        xml.setAttribute("value", inst.getString(), ns);
        return xml;
    }
}