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
- [X] Handle command **/priv**
- [X] Answer **PRIVATE**

## Client (ChatClient.java)

- [X] Accept DNS name and server port as arguments from the cmd
- [X] Implement 2 thread system (listen server and user input)
- [X] Support friendly message display in the chat area

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

* ISO-8859-1 ???
- TRY UTF-8