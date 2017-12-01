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
    private int server_port;
    private Socket client_soc;
    
    /* Append message in chat area */
    public void printMessage(String message)
    {
	message = message.replace("\n","");
	String[] tokens = message.split(" ");
	
	if(tokens[0].equals("MESSAGE"))
	    message = tokens[1] + ": " + message.substring(message.indexOf(tokens[2]));
	else if(tokens[0].equals("NEWNICK"))
	    message = tokens[1] + " mudou de nome para " + tokens[2];
	else if(tokens[0].equals("JOINED"))
	    message = tokens[1] + " juntou-se á sala";
	else if(tokens[0].equals("LEFT"))
	    message = tokens[1] + " saiu da sala";
	else if(tokens[0].equals("PRIVATE"))
	    message = tokens[1] + ": " + message.substring(message.indexOf(tokens[2]));
	
        chatArea.append(message + "\n");
    }

    
    public ChatClient(String server_name, int server_port) throws Exception
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
		    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                   chatBox.setText("");
                }
            }
        });

	// save server IP (addr from DNS name) and port
	serverIP = (InetAddress.getByName(server_name)).getHostAddress();
	this.server_port = server_port;
    }

    
    /* action performed after user input in text box */
    public void newMessage(String message) throws IOException
    {
	message = message.trim();
	System.out.println("TRYING TO SEND: " + message);
	DataOutputStream outToserver = new DataOutputStream(client_soc.getOutputStream());
	outToserver.writeBytes(message + "\n");
    }

    
    /* object's main method */
    public void run() throws Exception
    {
	client_soc = new Socket(serverIP,server_port);
	new Thread(new HSocListener()).start();
    }    


    private class HSocListener implements Runnable
    {	
	public HSocListener() { }

	public void run()
	{
	    try
	    {
		BufferedReader inFromServer;
		boolean alive = true;
		
		while(alive)
		{
		    inFromServer = new BufferedReader(new InputStreamReader(client_soc.getInputStream()));
		    String msg = inFromServer.readLine() + "\n"; // readLine() removes end of line

		    if(msg.equals(ChatServer.ANS_BYE))
			alive = false;

		    printMessage(msg);
		}

		client_soc.close();
		System.exit(0);
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
