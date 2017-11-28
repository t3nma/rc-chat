# TODO

## Server (ChatServer.java)

- [X] Accept server port as argument from the cmd
- [X] Deal with message deliniation
- [X] Handle command **/nick name**
- [X] Handle command **/join room**
- [X] Handle command **/leave**
- [X] Handle command **/bye**
- [X] Destinguish client commands from simple messages
- [X] Escape simple messages starting with one or more **/**
- [X] Answer **OK**
- [X] Answer **ERROR**
- [X] Answer **MESSAGE**
- [X] Answer **NEWNICK**
- [X] Answer **JOINED**
- [X] Answer **LEFT**
- [X] Answer **BYE**
- [X] Handle client state (**init**, **outside**, **inside**)
- [ ] Handle command **/priv**
- [ ] Answer **PRIVATE**

## Client (ChatClient.java)

- [X] Accept DNS name and server port as arguments from the cmd
- [X] Implement 2 thread system (listen server and user input)
- [ ] Support friendly message display in the chat area

## General

- [ ] Test message deliniation with ncat
- [ ] Use wireshark to test protocol correctness

### Notes

* Each message client->server and server->client must end \n (the message itself mustn't contain \n)
* Deliver **ChatServer.java**, **ChatClient.java** and **grupo.txt**
* Include all .java files (in case of code separation)
* ONE member submission in moodle
* É PARA O 20 C*RALHO!

### Questions

* Client send raw messsages, Server concers about escaping, right?
* escape happens when the message starts with a minimum of two '/', right?
* should commands like "/bye ola ola2" be handled like "/bye" ?
* can we user System.exit(0) to exit the client on close ?
* does the message sender also receives the message (inside a room?)
* ISO-8859-1 ???
* can trim() user request ?!