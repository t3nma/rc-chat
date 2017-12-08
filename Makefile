PROGRAM=./chat

all: $(PROGRAM)

$(PROGRAM): ChatClient.java ChatServer.java Common.java
	javac ChatClient.java ChatServer.java Common.java

clean:
	rm -f *.class
