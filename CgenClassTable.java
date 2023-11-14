/*
Copyright (c) 2000 The Regents of the University of California.
All rights reserved.

Permission to use, copy, modify, and distribute this software for any
purpose, without fee, and without written agreement is hereby granted,
provided that the above copyright notice and the following two
paragraphs appear in all copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
*/

// This is a project skeleton file

import java.io.PrintStream;
import java.util.Vector;
/**
 * This file imports the necessary Enumeration class from the java.util package.
 */
import java.util.Enumeration;
import java.util.LinkedList;

/** This class is used for representing the inheritance tree during code
    generation. You will need to fill in some of its methods and
    potentially extend it in other useful ways. */
class CgenClassTable extends SymbolTable {

    /** All classes in the program, represented as CgenNode */
    private Vector<CgenNode> nds;

    /** This is the stream to which assembly instructions are output */
    private PrintStream str;

    private int stringclasstag;
    private int intclasstag;
    private int boolclasstag;


    // The following methods emit code for constants and global
    // declarations.

    /** Emits code to start the .data segment and to
     * declare the global names.
     * */
    private void codeGlobalData() {
        // The following global names must be defined first.
        /*
            .data
            .align  2
            .globl  class_nameTab
            .globl  Main_protObj
            .globl  Int_protObj
            .globl  String_protObj
            .globl  bool_const0
            .globl  bool_const1
            .globl  _int_tag
            .globl  _bool_tag
            .globl  _string_tag
        */
        str.print("  .data\n" + CgenSupport.ALIGN);
        str.println(CgenSupport.GLOBAL + CgenSupport.CLASSNAMETAB);
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitProtObjRef(TreeConstants.Main, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitProtObjRef(TreeConstants.Int, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitProtObjRef(TreeConstants.Str, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        BoolConst.falsebool.codeRef(str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        BoolConst.truebool.codeRef(str);
        str.println("");
        str.println(CgenSupport.GLOBAL + CgenSupport.INTTAG);
        str.println(CgenSupport.GLOBAL + CgenSupport.BOOLTAG);
        str.println(CgenSupport.GLOBAL + CgenSupport.STRINGTAG);

        // We also need to know the tag of the Int, String, and Bool classes
        // during code generation.
        /*
            _int_tag:
                .word  x
            _bool_tag:
                .word  y
            _string_tag:
                .word  z
        */
        str.println(CgenSupport.INTTAG + CgenSupport.LABEL
              + CgenSupport.WORD + intclasstag);
        str.println(CgenSupport.BOOLTAG + CgenSupport.LABEL
              + CgenSupport.WORD + boolclasstag);
        str.println(CgenSupport.STRINGTAG + CgenSupport.LABEL
              + CgenSupport.WORD + stringclasstag);
    }

    /** Generates memory manager code */
    private void codeMemoryManager() {
        /*
            .globl  _MemMgr_TEST
            _MemMgr_TEST:
            .word  0/1
        */
        str.println(CgenSupport.GLOBAL + "_MemMgr_TEST");
        str.println("_MemMgr_TEST:");
        str.println(CgenSupport.WORD + ((Flags.cgen_Memmgr_Test == Flags.GC_TEST) ? "1" : "0"));
    }

    /** Emits code to start the .text segment and to
     * declare the global names.
     * */
    private void codeGlobalText() {
        /*
            .globl  heap_start
            heap_start:
                .word  0
                .text
                .globl  Main_init
                .globl  Int_init
                .globl  String_init
                .globl  Bool_init
                .globl  Main.main
        */
        str.println(CgenSupport.GLOBAL + CgenSupport.HEAP_START);
        str.print(CgenSupport.HEAP_START + CgenSupport.LABEL);
        str.println(CgenSupport.WORD + 0);
        str.println("  .text");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitInitRef(TreeConstants.Main, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitInitRef(TreeConstants.Int, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitInitRef(TreeConstants.Str, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitInitRef(TreeConstants.Bool, str);
        str.println("");
        str.print(CgenSupport.GLOBAL);
        CgenSupport.emitMethodRef(TreeConstants.Main, TreeConstants.main_meth, str);
        str.println("");
    }


    /** Emits code to reserve space for and initialize all of the
     * constants.  Class names should have been added to the string
     * table (in the supplied code, is is done during the construction
     * of the inheritance graph), and code for emitting string constants
     * as a side effect adds the string's length to the integer table.
     * The constants are emmitted by running through the stringtable and
     * inttable and producing code for each entry. */
    private void codeConstants() {
        // Add constants that are required by the code generator.
        AbstractTable.stringtable.addString("");
        AbstractTable.inttable.addString("0");
        // string constants
        AbstractTable.stringtable.codeStringTable(stringclasstag, str);
        // int constants
        AbstractTable.inttable.codeStringTable(intclasstag, str);
        // true/false constant definition
        BoolConst.falsebool.codeDef(boolclasstag, str);
        BoolConst.truebool.codeDef(boolclasstag, str);
    }

    /** Creates data structures representing basic Cool classes (Object,
     * IO, Int, Bool, String).  Please note: as is this method does not
     * do anything useful; you will need to edit it to make if do what
     * you want.
     * */
    private void installBasicClasses() {
        AbstractSymbol filename
            = AbstractTable.stringtable.addString("<basic class>");

        // A few special class names are installed in the lookup table
        // but not the class list.  Thus, these classes exist, but are
        // not part of the inheritance hierarchy.  No_class serves as
        // the parent of Object and the other special classes.
        // SELF_TYPE is the self class; it cannot be redefined or
        // inherited.  prim_slot is a class known to the code generator.
        addId(TreeConstants.No_class,
              new CgenNode(new class_(0,
                    TreeConstants.No_class,
                    TreeConstants.No_class,
                    new Features(0),
                    filename),
               CgenNode.Basic, this));

        addId(TreeConstants.SELF_TYPE,
              new CgenNode(new class_(0,
                    TreeConstants.SELF_TYPE,
                    TreeConstants.No_class,
                    new Features(0),
                    filename),
               CgenNode.Basic, this));

        addId(TreeConstants.prim_slot,
              new CgenNode(new class_(0,
                    TreeConstants.prim_slot,
                    TreeConstants.No_class,
                    new Features(0),
                    filename),
               CgenNode.Basic, this));

        // The Object class has no parent class. Its methods are
        //        cool_abort() : Object    aborts the program
        //        type_name() : Str        returns a string representation
        //                                 of class name
        //        copy() : SELF_TYPE       returns a copy of the object
        class_ Object_class =
            new class_(0,
                 TreeConstants.Object_,
                 TreeConstants.No_class,
                 new Features(0)
               .appendElement(new method(0,
                      TreeConstants.cool_abort,
                      new Formals(0),
                      TreeConstants.Object_,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.type_name,
                      new Formals(0),
                      TreeConstants.Str,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.copy,
                      new Formals(0),
                      TreeConstants.SELF_TYPE,
                      new no_expr(0))),
                 filename);

        installClass(new CgenNode(Object_class, CgenNode.Basic, this));

        // The IO class inherits from Object. Its methods are
        //        out_string(Str) : SELF_TYPE  writes a string to the output
        //        out_int(Int) : SELF_TYPE      "    an int    "  "     "
        //        in_string() : Str            reads a string from the input
        //        in_int() : Int                "   an int     "  "     "
        class_ IO_class =
            new class_(0,
                 TreeConstants.IO,
                 TreeConstants.Object_,
                 new Features(0)
               .appendElement(new method(0,
                      TreeConstants.out_string,
                      new Formals(0)
                    .appendElement(new formal(0,
                           TreeConstants.arg,
                           TreeConstants.Str)),
                      TreeConstants.SELF_TYPE,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.out_int,
                      new Formals(0)
                    .appendElement(new formal(0,
                           TreeConstants.arg,
                           TreeConstants.Int)),
                      TreeConstants.SELF_TYPE,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.in_string,
                      new Formals(0),
                      TreeConstants.Str,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.in_int,
                      new Formals(0),
                      TreeConstants.Int,
                      new no_expr(0))),
                 filename);

        installClass(new CgenNode(IO_class, CgenNode.Basic, this));

        // The Int class has no methods and only a single attribute, the
        // "val" for the integer.
        class_ Int_class =
            new class_(0,
                 TreeConstants.Int,
                 TreeConstants.Object_,
                 new Features(0)
               .appendElement(new attr(0,
                    TreeConstants.val,
                    TreeConstants.prim_slot,
                    new no_expr(0))),
                 filename);

        installClass(new CgenNode(Int_class, CgenNode.Basic, this));

        // Bool also has only the "val" slot.
        class_ Bool_class =
            new class_(0,
                 TreeConstants.Bool,
                 TreeConstants.Object_,
                 new Features(0)
               .appendElement(new attr(0,
                    TreeConstants.val,
                    TreeConstants.prim_slot,
                    new no_expr(0))),
                 filename);

        installClass(new CgenNode(Bool_class, CgenNode.Basic, this));

        // The class Str has a number of slots and operations:
        //       val                              the length of the string
        //       str_field                        the string itself
        //       length() : Int                   returns length of the string
        //       concat(arg: Str) : Str           performs string concatenation
        //       substr(arg: Int, arg2: Int): Str substring selection
        class_ Str_class =
            new class_(0,
                 TreeConstants.Str,
                 TreeConstants.Object_,
                 new Features(0)
               .appendElement(new attr(0,
                    TreeConstants.val,
                    TreeConstants.Int,
                    new no_expr(0)))
               .appendElement(new attr(0,
                    TreeConstants.str_field,
                    TreeConstants.prim_slot,
                    new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.length,
                      new Formals(0),
                      TreeConstants.Int,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.concat,
                      new Formals(0)
                    .appendElement(new formal(0,
                           TreeConstants.arg,
                           TreeConstants.Str)),
                      TreeConstants.Str,
                      new no_expr(0)))
               .appendElement(new method(0,
                      TreeConstants.substr,
                      new Formals(0)
                    .appendElement(new formal(0,
                           TreeConstants.arg,
                           TreeConstants.Int))
                    .appendElement(new formal(0,
                           TreeConstants.arg2,
                           TreeConstants.Int)),
                      TreeConstants.Str,
                      new no_expr(0))),
                 filename);

        installClass(new CgenNode(Str_class, CgenNode.Basic, this));
    }

    // The following creates an inheritance graph from
    // a list of classes.  The graph is implemented as
    // a tree of `CgenNode', and class names are placed
    // in the base class symbol table.
    private void installClass(CgenNode nd) {
        AbstractSymbol name = nd.getName();
        if (probe(name) != null) return;
        nds.addElement(nd);
        addId(name, nd);
    }

    private void installClasses(Classes cs) {
        for (Enumeration e = cs.getElements(); e.hasMoreElements(); ) {
            installClass(new CgenNode((Class_)e.nextElement(),
              CgenNode.NotBasic, this));
        }
    }

    private void buildInheritanceTree() {
        for (Enumeration e = nds.elements(); e.hasMoreElements(); ) {
            setRelations((CgenNode)e.nextElement());
        }
    }

    private void setRelations(CgenNode nd) {
        CgenNode parent = (CgenNode)probe(nd.getParent());
        nd.setParentNd(parent);
        parent.addChild(nd);
    }

    /** Constructs a new class table and invokes the code generator */
    public CgenClassTable(Classes cls, PrintStream str) {
        nds = new Vector();

        this.str = str;

        stringclasstag = 4 /* Change to your String class tag here */;
        intclasstag =    2 /* Change to your Int class tag here */;
        boolclasstag =   3 /* Change to your Bool class tag here */;

        enterScope();
        if (Flags.cgen_debug) System.out.println("Building CgenClassTable");

        installBasicClasses();
        installClasses(cls);
        buildInheritanceTree();

        code(cls, str);

        exitScope();
    }

    
    public void code(Classes cls, PrintStream str) {
        if (Flags.cgen_debug) System.out.println("coding global data");
        codeGlobalData();

        if (Flags.cgen_debug) System.out.println("coding memory manager");
        codeMemoryManager();

        if (Flags.cgen_debug) System.out.println("coding constants");
        codeConstants();
       
        str.print(CgenSupport.CLASSNAMETAB.concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> classes = new LinkedList <AbstractSymbol> ();
        classes.add(TreeConstants.Object_);
        classes.add(TreeConstants.IO);
        classes.add(TreeConstants.Int);
        classes.add(TreeConstants.Bool);
        classes.add(TreeConstants.Str);
        for (Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_ curClass = (class_) e.nextElement();
            classes.add(curClass.getName());
        }

        for( int i = 0; i < classes.size(); i++){
            str.print(CgenSupport.WORD);
            ((StringSymbol)AbstractTable.stringtable.lookup(classes.get(i).toString())).codeRef(str);
            str.print(CgenSupport.NEWLINE);
        }

        //Object
        str.print(CgenSupport.CLASSOBJTAB.concat(CgenSupport.LABEL));
        
        for( int i = 0 ; i < classes.size(); i++){
            str.print(CgenSupport.WORD);
            str.print(classes.get(i).toString().concat(CgenSupport.PROTOBJ_SUFFIX));
            str.print(CgenSupport.NEWLINE);
            str.print(CgenSupport.WORD);
            str.print(classes.get(i).toString().concat(CgenSupport.CLASSINIT_SUFFIX));
            str.print(CgenSupport.NEWLINE);
        }

        str.print("Object".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> objectDispTab = new LinkedList <AbstractSymbol> ();
        objectDispTab.add(TreeConstants.cool_abort);
        objectDispTab.add(TreeConstants.type_name);
        objectDispTab.add(TreeConstants.copy);
        for( int i = 0 ; i < objectDispTab.size(); i++){
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(objectDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }
        //IO
        str.print("IO".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> ioDispTab = new LinkedList <AbstractSymbol> ();
        

        for( int i = 0 ; i < objectDispTab.size(); i++){
            ioDispTab.add(objectDispTab.get(i));
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(ioDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
            
        }

        ioDispTab.add(TreeConstants.out_string);
        ioDispTab.add(TreeConstants.out_int);
        ioDispTab.add(TreeConstants.in_string);
        ioDispTab.add(TreeConstants.in_int);

        for(int i = objectDispTab.size() ; i < ioDispTab.size() ; i++){
            str.print(CgenSupport.WORD);
            str.print("IO".concat(CgenSupport.METHOD_SEP).concat(ioDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }
        //Int
        str.print("Int".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> intDispTab = new LinkedList <AbstractSymbol> ();
        intDispTab.add(TreeConstants.cool_abort);
        intDispTab.add(TreeConstants.type_name);
        intDispTab.add(TreeConstants.copy);

        for( int i = 0 ; i < objectDispTab.size(); i++){
            intDispTab.add(objectDispTab.get(i));
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(intDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
            
        }
        //Bool
        str.print("Bool".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> boolDispTab = new LinkedList <AbstractSymbol> ();
        for( int i = 0 ; i < objectDispTab.size(); i++){
            boolDispTab.add(objectDispTab.get(i));
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(boolDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }

        //String
        str.print("String".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> stringDispTab = new LinkedList <AbstractSymbol> ();

        for(int i = 0 ; i < objectDispTab.size(); i++){
            stringDispTab.add(objectDispTab.get(i));
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(stringDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
            
        }

        stringDispTab.add(TreeConstants.length);
        stringDispTab.add(TreeConstants.concat);
        stringDispTab.add(TreeConstants.substr);
        for( int i = objectDispTab.size(); i < stringDispTab.size(); i++){

            str.print(CgenSupport.WORD);
            str.print("String".concat(CgenSupport.METHOD_SEP).concat(stringDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }
        for( int i = 5; i< classes.size(); i++){
            class_ curClass = (class_) lookup(classes.get(i));
            /*CgenSupport.emitProtObjRef(curClass.getName(), str);
            str.print(CgenSupport.LABEL);
            str.print(CgenSupport.WORD);
            str.print(CgenSupport.NEWLINE);
            int valConst = 3;
            for (Enumeration e = curClass.features.getElements(); e.hasMoreElements(); ) {
                Feature currFeature = (Feature) e.nextElement();
                if(currFeature instanceof attr){
                    valConst++;
                }
            }
            str.print(CgenSupport.WORD + valConst);
            str.print(CgenSupport.NEWLINE);
            str.print(CgenSupport.WORD);*/
            CgenSupport.emitDispTableRef(curClass.getName(), str);
            str.print(CgenSupport.LABEL);
            
            for(int p = 0 ; p < objectDispTab.size(); p++){
                stringDispTab.add(objectDispTab.get(p));
                str.print(CgenSupport.WORD);
                str.print("Object".concat(CgenSupport.METHOD_SEP).concat(stringDispTab.get(p).toString()));
                str.print(CgenSupport.NEWLINE);
            
            }
            for (Enumeration e = curClass.features.getElements(); e.hasMoreElements(); ) {
                Feature currFeature = (Feature) e.nextElement();
                if(currFeature instanceof method){
                    method currMethod = (method) currFeature;
                    str.print(CgenSupport.WORD);
                    CgenSupport.emitMethodRef(curClass.getName(), currMethod.name, str);
                    str.print(CgenSupport.NEWLINE);
                }

            }
            
        }

        //PROTOBJ ----------------------------------------------------------
        for( int i=0; i< classes.size(); i++){
            class_ curClass = (class_) lookup(classes.get(i));
            CgenSupport.emitProtObjRef(curClass.getName(), str);
            str.print(CgenSupport.LABEL);
            str.print(CgenSupport.WORD + i);
            str.print(CgenSupport.NEWLINE);
            int valOff = 3;
            for (Enumeration e = curClass.features.getElements(); e.hasMoreElements(); ) {
                Feature currFeature = (Feature) e.nextElement();
                if(currFeature instanceof attr) {
                    valOff++;
                }
            }
            str.print(CgenSupport.WORD + valOff);
            str.print(CgenSupport.NEWLINE);
            str.print(CgenSupport.WORD);
            CgenSupport.emitDispTableRef(curClass.getName(), str);
            str.print(CgenSupport.NEWLINE);

            for (Enumeration f = curClass.getFeatures().getElements(); f.hasMoreElements();) {
                Object currFeature = f.nextElement();
                
                if (currFeature instanceof attr){
                    attr currAttr = (attr) currFeature;
                    str.print(CgenSupport.WORD);

                    if (currAttr.type_decl.equals(TreeConstants.Str)) {
                        ((StringSymbol) AbstractTable.stringtable.lookup("")).codeRef(str);
                    } else if (currAttr.type_decl.equals(TreeConstants.Int)) {
                        ((IntSymbol) AbstractTable.inttable.lookup("0")).codeRef(str);
                    } else if (currAttr.type_decl.equals(TreeConstants.Bool)) {
                        str.print(CgenSupport.BOOLCONST_PREFIX + CgenSupport.EMPTYSLOT);
                    } else {
                        str.print(CgenSupport.EMPTYSLOT);
                    }
                    str.print(CgenSupport.NEWLINE);
                }
            }
        }
        



        
        
        /*
        //Main
        str.print("Main".concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
        LinkedList <AbstractSymbol> mainDispTab = new LinkedList <AbstractSymbol> ();
        //for
        for( int i = 0 ; i < objectDispTab.size(); i++){
            mainDispTab.add(objectDispTab.get(i));
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(mainDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
            
        }
        mainDispTab.add(TreeConstants.main_meth);

        for( int i = objectDispTab.size(); i < objectDispTab.size(); i++){
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(objectDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }


        for( int i = 0 ; i < mainDispTab.size(); i++){
            str.print(CgenSupport.WORD);
            str.print("Main".concat(CgenSupport.METHOD_SEP).concat(mainDispTab.get(i).toString()));
            str.print(CgenSupport.NEWLINE);
        }
        //clases a partir de Main
        // i = 6 porque ya se imprimieron los metodos de Object y el main ahora se imprimen las clases
       
        LinkedList <AbstractSymbol> classesDispTab = new LinkedList <AbstractSymbol> ();
        for( int i = 0 ; i < classes.size(); i++){
            str.print(classes.get(i).toString().concat(CgenSupport.DISPTAB_SUFFIX).concat(CgenSupport.LABEL));
            for(int j = 0 ; j < objectDispTab.size(); j++){
            classesDispTab.add(objectDispTab.get(j));            
            str.print(CgenSupport.WORD);
            str.print("Object".concat(CgenSupport.METHOD_SEP).concat(classesDispTab.get(j).toString()));
            str.print(CgenSupport.NEWLINE);
            }
        }
        */

        //                 Add your code to emit
        //                   - prototype objects
        //                   - class_nameTab
        //                   - dispatch tables

        if (Flags.cgen_debug) System.out.println("coding global text");
        codeGlobalText();

        //                 Add your code to emit
        //                   - object initializer
        for( int i=0; i< classes.size(); i++){
            class_ curClass = (class_) lookup(classes.get(i));
            CgenSupport.emitInitRef(curClass.getName(), str);
            str.print(CgenSupport.LABEL);
            CgenSupport.emitPrologue(3, str);
            if(!curClass.getName().equals(TreeConstants.Object_)){
                CgenSupport.emitJal(curClass.getParent().toString().concat(CgenSupport.CLASSINIT_SUFFIX), str );
            }
            int valOff = 3;
            for (Enumeration e = curClass.features.getElements(); e.hasMoreElements(); ) {
                Feature currFeature = (Feature) e.nextElement();
                if(currFeature instanceof attr){
                    attr currAttr = (attr) currFeature;
                    if(!(currAttr.init instanceof no_expr)){
                        currAttr.init.code(str);
                        CgenSupport.emitStore(CgenSupport.ACC, valOff, CgenSupport.SELF, str);
                        valOff++;
                    } 
                
                }
            }
            CgenSupport.emitEpilogue(3, true, str);

        }
        //                   - the class methods
        for(Enumeration e = cls.getElements(); e.hasMoreElements(); ) {
            class_ currClass = (class_) e.nextElement();
            for(Enumeration e2 = currClass.features.getElements(); e2.hasMoreElements(); ){

                Feature currMethod = (Feature) e2.nextElement();
                int valOff2 = 3;
                if(currMethod instanceof method){
                    method currMethod2 = (method) currMethod;
                    enterScope();
                    CgenSupport.emitMethodRef(currClass.getName(), currMethod2.name, str);
                    str.print(CgenSupport.LABEL);
                    CgenSupport.emitPrologue(valOff2, str);


                    for (int i = currMethod2.formals.getLength(); i > 0; i--) {
                        formal form = (formal) currMethod2.formals.getNth(i-1);
                        addId(form.name, valOff2);
                        valOff2++;
                    }

                    currMethod2.expr.code(str);
                    CgenSupport.emitEpilogue(valOff2, false, str);
                    exitScope();
                    
                } 



            }
        }   




        //                   - etc...
    }

    /** Gets the root of the inheritance tree */
    public CgenNode root() {
        return (CgenNode)probe(TreeConstants.Object_);
    }

}
