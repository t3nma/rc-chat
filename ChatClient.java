import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ChatClient
{
    /* UI vars */
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    /* -- UI vars */

    
    private String serverIP;
    private int serverPort;
    private Socket clientSoc;
    
    
    /* Append message in chat area */
    public void printMessage(final String message)
    {
        chatArea.append(message);
    }

    
    public ChatClient(String serverName, int serverPort) throws Exception
    {
	// build UI
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener()
	{
            @Override
            public void actionPerformed(ActionEvent e)
	    {
                try {
		    System.out.println("@ actionPerformed(AE e)");
		    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                   chatBox.setText("");
                }
            }
        });

	// save server IP (addr from DNS name) and port
	serverIP = (InetAddress.getByName(serverName)).getHostAddress();
	this.serverPort = serverPort;
    }

    
    /* action performed after user input in text box */
    public void newMessage(String message) throws IOException
    {
	DataOutputStream outToserver = new DataOutputStream(clientSoc.getOutputStream());
	outToserver.writeBytes(message + "\n");
    }

    
    /* object's main method */
    public void run() throws Exception
    {
	/* WRITE
	DataOutputStream outToServer = new DataOutputStream(soc.getOutputStream());
	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	outToServer.writeBytes(inFromUser.readLine() + "\n");
	soc.close();
	*/

	/* READ
	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(soc.getInputStream()));
	String msg = inFromServer.readLine();
	System.out.println("Msg: " + msg);
	soc.close();*/

	clientSoc = new Socket(serverIP,serverPort);
	new Thread(new HSocListener()).start();
    }    


    private class HSocListener implements Runnable
    {
	public HSocListener() { }

	public void run()
	{
	    try
	    {
		while(true)
		{
		    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
		    String msg = inFromServer.readLine();
		    System.out.println("Received: " + msg);
		    if( msg.equals("ok") )
			break;
		}

		clientSoc.close();
	    }
	    catch(Exception e)
	    {
		e.printStackTrace();
	    }
	}	
    }
    
    
    /* create client and start its execution */
    public static void main(String[] args) throws Exception
    {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }
}
