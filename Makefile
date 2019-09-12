# Assignment 3 Makefile
# Ryan Anderson
# 5 September 2019

JAVAC = /usr/bin/javac
.SUFFIXES: .java .class

SRCDIR=src
BINDIR=bin
DOCDIR=doc

$(BINDIR)/%.class:$(SRCDIR)/%.java
	$(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES=SumLocal.class CloudDataThreaded.class

CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)

default: $(CLASS_FILES)

run:
	java -cp $(BINDIR) CloudDataThreaded

docs:
	javadoc -d $(DOCDIR) $(SRCDIR)/*.java

clean:
	rm $(BINDIR)/*.class
