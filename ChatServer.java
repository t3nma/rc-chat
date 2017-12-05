import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer
{
    static final private int BUFFER_SIZE             = 20000;

    static final public String ANS_OK               = "OK\n";
    static final public String ANS_ERROR            = "ERROR\n";
    static final public String ANS_BYE              = "BYE\n";
    
    static final private String ANS_PATTERN_MESSAGE = "MESSAGE [name] [message]\n";
    static final private String ANS_PATTERN_NEWNICK = "NEWNICK [old] [new]\n";
    static final private String ANS_PATTERN_JOINED  = "JOINED [name]\n";
    static final private String ANS_PATTERN_LEFT    = "LEFT [name]\n";
    static final private String ANS_PATTERN_PRIVATE = "PRIVATE [name] [message]\n";

    static private HashSet<String> nameset          = null;
    static private ServerSocketChannel ssc          = null;
    static private Selector selector                = null;

    
    static public void main( String args[] ) throws Exception
    {
	nameset = new HashSet<String>();
	
	try
	{
	    sv_configure(Integer.parseInt(args[0]));
	    sv_start();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    if(selector != null)
		selector.close();
	    
	    if(ssc != null)
		ssc.close();
	}
    }    

    /**
     *
     * Configure Server:
     *   create serverSocketChannel
     *   register selector
     *
     **/
    static private void sv_configure(int sv_port) throws Exception
    {
	ssc = ServerSocketChannel.open();
	ssc.configureBlocking(false);
	ssc.socket().bind(new InetSocketAddress(sv_port));
	selector = Selector.open();
	ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     *
     * Start Server's main execution loop
     * 
     **/
    static private void sv_start() throws Exception
    {
	while(true)
	{
	    // no ready channels?
	    if( selector.select() == 0)
		continue;
	    
	    // Get the keys corresponding to the ready channels and process each one
	    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
	    
	    while (it.hasNext())
	    {
		SelectionKey key = it.next();
		
		// What kind of activity is it?
		if( key.isAcceptable() )
	        {
		    // OP_ACCEPT
		    Socket s = ssc.socket().accept();
		    SocketChannel sc = s.getChannel();
		    sc.configureBlocking(false);
		    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		else if( key.isReadable() )
		{
		    // OP_READ		    
		    SocketChannel sc = (SocketChannel)key.channel();
		    
		    // no User object associated ?
		    if(key.attachment() == null)
			key.attach(new User());
		    
		    sv_process_read(key,sc);
		}
	    }
	    
	    // key processed, remove from ready state queue
	    it.remove();
	}
	
    }

    /**
     *
     * currently need handling of /priv command.
     *
     **/
    static private void sv_process_read(SelectionKey key, SocketChannel sc) throws Exception
    {
	User user = (User)key.attachment();
	String msg = get_soc_message(sc);

	// user closed connection ?
	if(msg == null)
	{
	    if(user.getState() == User.State.INSIDE)
		sv_answer_room(user.getRoom(), ANS_PATTERN_LEFT.replace("[name]",user.getName()), user.getName(), false);

	    close_connection(key,sc);
	    return;
	}	

	user.cacheMessage(msg);
	if(!msg.endsWith("\n"))
	    return;

	msg = user.getFullMessage().replace("\n","");
	String[] msg_tokens = msg.split(" ");

	switch(user.getState())
	{
	case INIT:
	    
	    if( msg_tokens[0].equals("/nick") && msg_tokens.length > 1 )
	    {
		if(nameset.contains(msg_tokens[1]))
		    sv_answer(ANS_ERROR, sc);
		else
		{
		    nameset.add(msg_tokens[1]);
		    user.setName(msg_tokens[1]);
		    user.setState(User.State.OUTSIDE);
		    sv_answer(ANS_OK, sc);
		}
	    }
	    else if( msg_tokens[0].equals("/bye") )
	    {	
		sv_answer(ANS_BYE, sc);
		close_connection(key,sc);
	    }
	    else
		sv_answer(ANS_ERROR, sc);
	    
	    break;
	case OUTSIDE:

	    if( msg_tokens[0].equals("/nick") && msg_tokens.length > 1 )
	    {
		if(nameset.contains(msg_tokens[1]))
		    sv_answer(ANS_ERROR, sc);
		else
		{
		    nameset.add(msg_tokens[1]);
		    nameset.remove(user.getName());
		    user.setName(msg_tokens[1]);
		    sv_answer(ANS_OK, sc);
		}
	    }
	    else if( msg_tokens[0].equals("/join") && msg_tokens.length > 1 )
	    {
		String room_msg = ANS_PATTERN_JOINED.replace("[name]",user.getName());
		sv_answer_room(msg_tokens[1], room_msg, user.getName(), false);
		user.setRoom(msg_tokens[1]);
		user.setState(User.State.INSIDE);
		sv_answer(ANS_OK, sc);
	    }
	    else if( msg_tokens[0].equals("/priv") && msg_tokens.length > 2 )
	    {
		String receiver = msg_tokens[1];
		
		if( !nameset.contains(receiver) )
		    sv_answer(ANS_ERROR, sc); // receiver doesn't exist, throw error to sender
		else
		{
		    msg = msg.substring(msg.indexOf(msg_tokens[2]));
		    String priv_msg = ANS_PATTERN_PRIVATE.replace("[name]", user.getName()).replace("[message]", msg);
		    sv_answer_priv(priv_msg, receiver);
		    sv_answer(ANS_OK, sc);
		}
	    }
	    else if( msg_tokens[0].equals("/bye") )
	    {
		sv_answer(ANS_BYE, sc);
		nameset.remove(user.getName());
		close_connection(key,sc);
	    }
	    else
		sv_answer(ANS_ERROR, sc);
	    
	    break;
	case INSIDE:

	    if( msg_tokens[0].equals("/nick") && msg_tokens.length > 1 )
	    {
		if(nameset.contains(msg_tokens[1]))
		    sv_answer(ANS_ERROR, sc);
		else
		{
		    String room_msg = ANS_PATTERN_NEWNICK.replace("[old]",user.getName()).replace("[new]",msg_tokens[1]);
		    sv_answer_room(user.getRoom(), room_msg, user.getName(), false);
		    sv_answer(ANS_OK, sc);
		    nameset.remove(user.getName());
		    nameset.add(msg_tokens[1]);
		    user.setName(msg_tokens[1]);
		}
	    }
	    else if( msg_tokens[0].equals("/join") && msg_tokens.length > 1 )
	    {
		String room_msg = ANS_PATTERN_LEFT.replace("[name]", user.getName());
		sv_answer_room(user.getRoom(), room_msg, user.getName(), false);
		user.setRoom(msg_tokens[1]);
		room_msg = ANS_PATTERN_JOINED.replace("[name]", user.getName());
		sv_answer_room(user.getRoom(), room_msg, user.getName(), false);
		sv_answer(ANS_OK, sc);
	    }
	    else if( msg_tokens[0].equals("/priv") && msg_tokens.length > 2)
	    {
		String receiver = msg_tokens[1];
		
		if( !nameset.contains(receiver) )
		    sv_answer(ANS_ERROR, sc); // receiver doesn't exist, throw error to sender
		else
		{
		    msg = msg.substring(msg.indexOf(msg_tokens[2]));
		    String priv_msg = ANS_PATTERN_PRIVATE.replace("[name]", user.getName()).replace("[message]", msg);
		    sv_answer_priv(priv_msg, receiver);
		    sv_answer(ANS_OK, sc);
		}
	    }
	    else if( msg_tokens[0].equals("/leave") )
	    {
		String room_msg = ANS_PATTERN_LEFT.replace("[name]", user.getName());
		sv_answer_room(user.getRoom(), room_msg, user.getName(), false);
		user.setState(User.State.OUTSIDE);
		sv_answer(ANS_OK, sc);
	    }
	    else if( msg_tokens[0].equals("/bye") )
	    {
		String room_msg = ANS_PATTERN_LEFT.replace("[name]", user.getName());
		sv_answer_room(user.getRoom(), room_msg, user.getName(), false);
		sv_answer(ANS_BYE, sc);
		nameset.remove(user.getName());
		close_connection(key, sc);
	    }
	    else
	    {
		if(msg.startsWith("//"))
		    msg = msg.substring(1); // escape first /

		String room_msg = ANS_PATTERN_MESSAGE.replace("[name]", user.getName()).replace("[message]", msg);
		sv_answer_room(user.getRoom(), room_msg, user.getName(), true);
	    }
	    
	    break;
	default:
	    sv_answer(ANS_ERROR, sc);
	}	
    }
    
    /**
     *
     * Extract and return incoming socket message
     *
     **/
    static private String get_soc_message(SocketChannel sc) throws Exception
    {
	ByteBuffer buffer      = ByteBuffer.allocate(BUFFER_SIZE);
	CharsetDecoder decoder = (Charset.forName("UTF-8")).newDecoder();

	sc.read(buffer);
	buffer.flip(); // switch to read mode

	// no data ?
	if(buffer.limit() == 0)
	    return null;

	return decoder.decode(buffer).toString();
    }

    /**
     *
     * Send message "msg" to the user identified by
     * the SocketChannel "sc"
     *
     **/
    static private void sv_answer(String msg, SocketChannel sc) throws Exception
    {
	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	buffer.put(msg.getBytes("UTF-8"));
	buffer.flip();

	System.out.println("SENDING: " + msg);
	
	while(buffer.hasRemaining())
	    sc.write(buffer);
    }

    /**
     *
     * Send message "msg" to all users inside room "room".
     *
     **/
    static private void sv_answer_room(String room, String msg, String from, boolean ans_self) throws Exception
    {
	Iterator<SelectionKey> it = selector.keys().iterator();

	while(it.hasNext())
	{
	    SelectionKey cur_key = it.next();

	    User user = (User)cur_key.attachment();
	    if(user == null                                 ||
	       user.getState() != User.State.INSIDE         ||
	       !user.getRoom().equals(room)                 ||
	       (user.getName().equals(from) && !ans_self))
		continue;
	    
	    SocketChannel sc = (SocketChannel)cur_key.channel();
	    sv_answer(msg,sc);
	}
    }

    /**
     *
     * Send private message "msg" to user "to".
     *
     **/
    static private void sv_answer_priv(String msg, String to) throws Exception
    {
	Iterator<SelectionKey> it = selector.keys().iterator();

	while(it.hasNext())
	{
	    SelectionKey cur_key = it.next();

	    User user = (User)cur_key.attachment();
	    if( user == null || !user.getName().equals(to) )
		continue;

	    SocketChannel sc = (SocketChannel)cur_key.channel();
	    sv_answer(msg,sc);
	    break;
	}
    }
    
    /**
     *
     * Close channel "sc"'s connection
     *
     **/
    static private void close_connection(SelectionKey key, SocketChannel sc) throws Exception
    {
	System.out.println("CONNECTION CLOSED"); // debug
	key.cancel();
	sc.socket().close();
    }
}

