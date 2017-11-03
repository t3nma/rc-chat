# TODO

## Server (ChatServer.java)

- [ ] Accept server port as argument from the cmd
- [ ] Deal with message deliniation
- [ ] Handle command **/nick name**
- [ ] Handle command **/join room**
- [ ] Handle command **/leave**
- [ ] Handle command **/bye**
- [ ] Destnguish client commands from simple messages
- [ ] Escape simple messages starting with one or more **/**
- [ ] Answer **OK**
- [ ] Answer **ERROR**
- [ ] Answer **MESSAGE**
- [ ] Answer **NEWNICK**
- [ ] Answer **JOINED
- [ ] Answer **LEFT**
- [ ] Answer **BYE**
- [ ] Handle client state (**init**, **outside**, **inside**)
- [ ] Handle command **/priv**
- [ ] Answer **PRIVATE**

## Client (ChatClient.java)

- [ ] Accept DNS name and server port as arguments from the cmd
- [ ] Implement 2 thread system (listen server and user input)
- [ ] Support friendly message display in the chat area


### Notes

* Each message client->server and server->client must end \n (the message itself mustn't contain \n)
* Deliver **ChatServer.java**, **ChatClient.java** and **grupo.txt**
* Include all .java files (in case of code separation)
* ONE member submission in moodle
* É PARA O 20 C*RALHO!