import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer
{
    static final public int BUFFER_SIZE             = 20000;

    static final private String ANS_PATTERN_OK      = "OK";
    static final private String ANS_PATTERN_ERROR   = "ERROR";
    static final private String ANS_PATTERN_MESSAGE = "MESSAGE %s %s";
    static final private String ANS_PATTERN_NEWNICK = "NEWNICK %s %s";
    static final private String ANS_PATTERN_JOINED  = "JOINED %s";
    static final private String ANS_PATTERN_LEFT    = "LEFT %s";
    static final private String ANS_PATTERN_BYE     = "BYE";

    static private HashSet<String> nameset;
    
    static private ServerSocketChannel ssc = null;
    static private Selector selector = null;

    
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
		    System.out.println("START OP_ACCEPT");
		    Socket s = ssc.socket().accept();
		    SocketChannel sc = s.getChannel();
		    sc.configureBlocking(false);
		    sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		    System.out.println("END OP_ACCEPT"); 
		}
		else if( key.isReadable() )
		{
		    // OP_READ
		    System.out.println("START OP_READ"); // debug
		    
		    SocketChannel sc = (SocketChannel)key.channel();
		    
		    // no User object associated ?
		    if(key.attachment() == null)
			key.attach(new User());
		    
		    sv_process_read(key,sc);
		    
		    System.out.println("END OP_READ"); // debug
		}
	    }
	    
	    // key processed, remove from ready state queue
	    it.remove();
	}
	
    }

    static private void sv_process_read(SelectionKey key, SocketChannel sc) throws Exception
    {
	User user = (User)key.attachment();
	String msg = get_soc_message(sc);

	// user closed connection ?
	if(msg == null)
	{
	    if(user.getState() == User.State.INSIDE)
	    {
		// LEFT name
		// para todos da sala
	    }

	    close_connection(key,sc);
	    System.out.println("CONNECTION CLOSED"); // debug
	    return;
	}	
	
	user.appendToBuffer(msg);
	if(!msg.endsWith("\n"))
	{
	    System.out.println("MESSAGE BUFFERED"); // debug
	    return;
	}
	
	String[] msg_tokens = msg.split(" ");

	switch(user.getState())
	{
	case INIT:

	    /*
	     * MSG INCOMES WITH COMMAND NOT ESCAPED,
	     * SO IF-ELSE FAILS IN COMPARING STRINGS
	     */
	    
	    if( msg_tokens[0].equals("/nick") && msg_tokens.length > 1 )
	    {
		if(nameset.contains(msg_tokens[1]))
		    sv_answer(ANS_PATTERN_ERROR, sc);
		else
		{
		    nameset.add(msg_tokens[1]);
		    user.setName(msg_tokens[1]);
		    user.setState(User.State.OUTSIDE);
		    sv_answer(ANS_PATTERN_OK, sc);
		}
	    }
	    else if( msg_tokens[0].equals("/bye") )
	    {
		nameset.remove(user.getName());
		sv_answer(ANS_PATTERN_BYE, sc);
		close_connection(key,sc);
	    }
	    else
		sv_answer(ANS_PATTERN_ERROR, sc);
	    
	    break;
	case OUTSIDE:
	    System.out.println("OUTSIDE YET TO DEAL");
	    break;
	case INSIDE:
	    System.out.println("INSIDE YET TO DEAL");
	    break;
	default:
	    System.out.println("DEFAULTING...");
	}	
    }

    static private void sv_answer(String msg, SocketChannel sc) throws Exception
    {
	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	buffer.put(msg.getBytes());
	buffer.flip();

	while(buffer.hasRemaining())
	    sc.write(buffer);
    }
    
    /**
     *
     * Close channel connection
     *
     **/
    static private void close_connection(SelectionKey key, SocketChannel sc) throws Exception
    {
	key.cancel();
	sc.socket().close();
    }
    
    /**
     *
     * Extract and return incoming socket message
     *
     **/
    static private String get_soc_message(SocketChannel sc) throws Exception
    {
	ByteBuffer buffer      = ByteBuffer.allocate(BUFFER_SIZE);
	CharsetDecoder decoder = (Charset.forName("UTF8")).newDecoder();

	sc.read(buffer);
	buffer.flip(); // switch to read mode

	// no data ?
	if(buffer.limit() == 0)
	    return null;

	return decoder.decode(buffer).toString();
    }
}

