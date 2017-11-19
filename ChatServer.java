import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer
{
    // A pre-allocated buffer for the received data
    static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();


    static public void main( String args[] ) throws Exception
    {
	int serverPort = Integer.parseInt( args[0] );
	ServerSocketChannel ssc = null;
	
	try
	{
	    // Create and configure server socket channel
	    ssc = ServerSocketChannel.open();
	    ssc.configureBlocking( false );
	    ssc.socket().bind( new InetSocketAddress(serverPort) ); 
	    
	    // Create selector and register it in the socket channel
	    Selector selector = Selector.open();
	    ssc.register( selector, SelectionKey.OP_ACCEPT );

	    while (true)
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
			// accept connection
			System.out.println("OP_ACCEPT");
			Socket s = ssc.socket().accept();
			
			// make it non-block and register in the selector
			SocketChannel sc = s.getChannel();
			sc.configureBlocking( false );
			sc.register( selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE );

			System.out.println("ACCEPTED");
		    }
		    else if( key.isReadable() )
		    {
			// OP_READ
			System.out.println("OP_READ");

			SocketChannel sc = (SocketChannel)key.channel();
			String message = processInput(sc);
			if(message == null)
			{
			    key.cancel();
			    try {
				sc.socket().close();
				System.out.println("Connection closed!");
			    } catch(Exception e) {
				e.printStackTrace();
			    }
			}
			else {
			    System.out.print("MSG: " + message);
			    
			    if( message.equals("quit") )
				answer(sc,"ok\n");
			    else
				answer(sc,message+"\n");
			}
		    }
		}
		
		// current key was processed, remove it from ready queue
		it.remove();
	    }
	}
	catch( IOException ie )
	{
	    System.err.println(ie);
	}
	finally
	{
	    if(ssc != null)
		ssc.close();
	}
    }
    
    
    // Just read the message from the socket and send it to stdout
    static private String processInput( SocketChannel sc ) throws IOException
    {
	ByteBuffer buffer = ByteBuffer.allocate( 16384 );
	Charset charset = Charset.forName("UTF8");
	CharsetDecoder decoder = charset.newDecoder();

	sc.read(buffer);
	buffer.flip();

	if( buffer.limit() == 0 )
	    return null;

	return decoder.decode(buffer).toString();
    }

    static private void answer( SocketChannel sc, String msg ) throws IOException
    {
	ByteBuffer buffer = ByteBuffer.allocate( 16384 );
	buffer.put(msg.getBytes());
	buffer.flip();

	while(buffer.hasRemaining())
	    sc.write(buffer);
	buffer.clear();
    }
}
