PROGRAM=./chat

all: $(PROGRAM)

$(PROGRAM): ChatClient.java ChatServer.java
	javac ChatClient.java ChatServer.java

clean:
	rm -f *.class
