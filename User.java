import java.nio.*;
import java.nio.charset.*;
    
public class User
{   
    private State state;
    private String name;
    private String room;
    private ByteBuffer buffer;

    static enum State
    {
	INIT,
	OUTSIDE,
	INSIDE
    }
    
    User()
    {
	state = State.INIT;
	name = null;
	room = null;
	buffer = ByteBuffer.allocate(ChatServer.BUFFER_SIZE);
    }

    State getState()
    {
	return state;
    }

    void setState(State state)
    {
	this.state = state;
    }

    String getName()
    {
	return name;
    }

    void setName(String name)
    {
	this.name = name;
    }

    String getRoom()
    {
	return room;
    }

    void setRoom(String room)
    {
	this.room = room;
    }

    void appendToBuffer(String msg)
    {
	buffer.put(msg.getBytes());
    }

    String getFullMessage()
    {
	buffer.flip(); // switch to read mode

	byte[] bytes;
	if(buffer.hasArray())
	    bytes = buffer.array();
	else
	{
	    bytes = new byte[buffer.remaining()];
	    buffer.get(bytes);
	}
	
	buffer.clear(); // prepare for next write mode
	
	return new String(bytes, Charset.forName("UTF8"));
    }
}
