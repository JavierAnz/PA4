##############################
#    PA5J RISC-V Version     #
##############################

# useful assignment variables
ASSN=5J
CLASS=cs143
CLASSDIR=/usr/class/cs143/cool

# java useful variables
JAVAC=javac
JFLAGS=-g -nowarn
CLASSPATH := ${CLASSDIR}/lib:.:/usr/java/lib/rt.jar

# files that students can modify
SRC = CgenNode.java CgenSupport.java TreeConstants.java cool-tree.java IntSymbol.java StringSymbol.java BoolConst.java

# example COOL file
EXAMPLE = example.cl

# files that students should NOT modify
CSRC = \
	ASTConstants.java \
	ASTLexer.java \
	AbstractSymbol.java \
	AbstractTable.java \
	Flags.java \
	IdSymbol.java \
	IdTable.java \
	IntTable.java \
	ListNode.java \
	Cgen.java \
	StringTable.java \
	SymbolTable.java \
	SymtabExample.java \
	TokenConstants.java \
	TreeNode.java \
	Utilities.java

TSRC = mycoolc
LIBS = lexer parser semant
CFIL = ${CSRC} ${CGEN} ${SRC}
CLS = ${CFIL:.java=.class}
OUTPUT = example.output

source: ${SRC} ${TSRC} ${LSRC} ${LIBS} ${CSRC} ${EXAMPLE}

cgen: Makefile ${CLS}
	@rm -f cgen
	echo '#!/bin/sh' >> cgen
	echo 'java -classpath ${CLASSPATH} Cgen $$*' >> cgen
	chmod 755 cgen

dotest: cgen example.cl
	@echo "\nRunning code generator on example.cl\n"
	-./mycoolc example.cl

## cool-tree.class is not a real class file, but we need to have it
## for dependency tracking
${CLS}: ${CFIL}
	${JAVAC} ${JFLAGS} -classpath ${CLASSPATH} ${CFIL}
	touch cool-tree.class

# SOURCE FILES
# these dependencies allow you to get the starting files for
# the assignment. They will not overwrite a file you already have.

${SRC} ${EXAMPLE}:
	-${CLASSDIR}/etc/copy-skel ${ASSN} ${SRC} ${EXAMPLE}

${LIBS}:
	${CLASSDIR}/etc/link-object ${ASSN} $@

${TSRC} ${CSRC}:
	-ln -s ${CLASSDIR}/src/PA${ASSN}/$@ $@

.PHONY: source clean dotest

clean:
	rm -rf ${CSRC} ${TSRC} ${LIBS} cgen *.class *.s
