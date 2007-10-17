/*
 * XMLVM --- An XML-based Programming Language Copyright (c) 2004-2005 by Arno
 * Puder
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * 
 * For more information, visit the XMLVM Home Page at
 * http://www.xml11.org/xmlvm/
 */

/*
 * Created on Jul 3, 2004
 */



package org.xmlvm;


import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.bcel.classfile.*;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xmlvm.dep.Import;
import org.xmlvm.dep.Recursion;

import edu.arizona.cs.mbel.mbel.ClassParser;
import edu.arizona.cs.mbel.mbel.Module;



public class Main
{

    private JavaClass jvm_class;

    private Module    cil_class;

    private String    class_name;



    public Main(JavaClass clazz)
    {
        jvm_class = clazz;
        cil_class = null;
        class_name = null;
    }



    public Main(String option_class)
    {
        jvm_class = null;
        cil_class = null;

        try {
            if (option_class.endsWith(".exe")) {
                FileInputStream fin = new FileInputStream(option_class);
                ClassParser parser = new ClassParser(fin);
                cil_class = parser.parseModule();
                class_name = option_class.substring(0,
                        option_class.length() - 4);
            }
            else if (option_class.endsWith(".class")) {
                org.apache.bcel.classfile.ClassParser parser = new org.apache.bcel.classfile.ClassParser(
                        option_class);
                jvm_class = parser.parse();
                class_name = jvm_class.getClassName();
            }
            else {
                jvm_class = org.apache.bcel.Repository
                        .lookupClass(option_class);
                class_name = jvm_class.getClassName();
            }
        }
        catch (Exception ex) {
            System.err.println("Could not file '" + option_class + "'");
            System.exit(-1);
        }
    }



    public OutputStream getOutputStream(boolean option_console,
                                        boolean option_js, boolean option_cpp)
    {
        OutputStream out = null;
        try {
            if (option_console)
                out = System.out;
            else {
                int index = class_name.lastIndexOf('.');
                String path = class_name.substring(0, index + 1).replace('.',
                        File.separatorChar);
                class_name = class_name.substring(index + 1);
                if (!path.equals("")) {
                    File f = new File(path);
                    f.mkdirs();
                }
                String suffix = ".xmlvm";
                if (option_js)
                    suffix = ".js";
                if (option_cpp)
                    suffix = ".cpp";
                out = new FileOutputStream(path + class_name + suffix);
            }
        }
        catch (Exception ex) {
            System.err.println("Could not create file");
            System.exit(-1);
        }
        return out;
    }



    public Document genXMLVM()
    {
        if (jvm_class != null)
            return new ParseJVM(jvm_class).genXMLVM();
        return new ParseCIL(cil_class).genXMLVM();
    }



    public void genJS(Document doc, OutputStream out)
    {
        InputStream xslt = this.getClass().getResourceAsStream("/xmlvm2js.xsl");
        runXSLT(xslt, doc, out);
    }



    public void genCPP(Document doc, OutputStream out)
    {
        InputStream xslt = this.getClass()
                .getResourceAsStream("/xmlvm2cpp.xsl");
        runXSLT(xslt, doc, out);
    }



    public void genXML(Document doc, OutputStream out)
    {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, out);
        }
        catch (Exception ex) {
            System.err.println(ex);
        }

    }



    private void runXSLT(InputStream xsltFile, Document doc, OutputStream out)
    {
        try {
            OutputStream xmlvm_out = new ByteArrayOutputStream();
            XMLOutputter outputter = new XMLOutputter();
            outputter.output(doc, xmlvm_out);
            xmlvm_out.close();

            StringReader xmlvmReader = new StringReader(xmlvm_out.toString());
            Source xmlvmSource = new StreamSource(xmlvmReader);
            Source xsltSource = new StreamSource(xsltFile);
            Result result = new StreamResult(out);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer trans = transFactory.newTransformer(xsltSource);
            trans.transform(xmlvmSource, result);
        }
        catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }



    private static void usage(String error)
    {
        String[] msg = {
                "Usage: xmlvm [--js|--cpp] [--import] [--recursive] [--out=<file>] <class>",
                "  --js       : Generate JavaScript",
                "  --cpp      : Generate C++",
                "  --import   : Generate import list of referenced externals",
                "  --console  : Output is to be written to the console.",
                "  --recursive: Recursivley scan through the referenced externals",
                "  <class>    : Byte code to be translated. If <class> ends on '.exe',",
                "               the bytecode is assumed to the a .NET executable file",
                "               with the same name. If <class> ends on '.class', the",
                "               bytecode is assumed to be of JVM format in a file with",
                "               the same name. Otherwise, <class> is looked up via CLASSPATH.",
                "  If neither --js nor --cpp is specified, the output will be XMLVM.",
                "  If the option --console is not given, the output will be written to a",
                "  file with the same name as <class> and suffix one of .xmlvm, .js, or .cpp"};

        System.err.println("Error: " + error);
        for (int i = 0; i < msg.length; i++)
            System.err.println(msg[i]);
        System.exit(-1);
    }



    public static void main(String[] argv)
    {
        boolean option_js = false;
        boolean option_cpp = false;
        boolean option_console = false;
        boolean option_import = false;
        boolean option_recursive = false;

        String option_class = null;

        // Read command line arguments
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.equals("--js")) {
                option_js = true;
                continue;
            }
            if (arg.equals("--cpp")) {
                option_cpp = true;
                continue;
            }
            if (arg.equals("--console")) {
                option_console = true;
                continue;
            }
            if (arg.equals("--import")) {
                option_import = true;
                continue;
            }
            if (arg.equals("--recursive")) {
                option_recursive = true;
                continue;
            }
            if (option_class != null)
                usage("Unknown parameter: " + arg);
            option_class = arg;
        }

        // Check command line arguments
        if (option_js && option_cpp)
            usage("Cannot specify --js and --cpp at the same time");
        if (option_class == null)
            usage("No class file specified");

        Main main = new Main(option_class);
        Document doc = main.genXMLVM();
        if (option_import) {
            Import imp = new Import();
            imp.genImport(doc);
        }
        if (option_recursive) {
            Recursion rec = new Recursion();
            rec.startRecursion(doc);
        }

        OutputStream out = main.getOutputStream(option_console, option_js,
                option_cpp);
        if (option_js) {
            main.genJS(doc, out);
        }
        else if (option_cpp) {
            main.genCPP(doc, out);
        }
        else {
            main.genXML(doc, out);
        }
        try {
            out.close();
        }
        catch (Exception ex) {
            System.err.println("Error closing output file.");
        }
    }

}