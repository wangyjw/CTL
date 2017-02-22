all: OBJS

FLAGS = -g
JC = javac

SRCS =\
	Model.java\
	Parser.java\
	StateSetTest.java\
	KripkeModel.java

OBJS: $(SRCS:.java=.class)

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

clean:
	rm -f *.class

tar:
	tar czvf java.tgz .
