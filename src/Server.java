import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 * @author Kenneth Lange
 * 
 * This class runs a chat server over 'localhost' on port 4444 for a client to connect and send messages to.
 *  
 */
public class Server {
	
	//Global variables for easy access and convenience for editing
	int port = 4444;
	String address = "localhost";
	ServerSocket server;
	int numusers;
	
	public static void main(String[] args) {
		new Server();
	}
	
	ArrayList<ConnectedClients> clientList;

	public Server() {
		init();
	}
	
	/**
	 * This method initializes the sever class for a proper setup
	 */
	private void init() {
		clientList = new ArrayList<ConnectedClients>();
		server = null;
		numusers = 0;

		try {
			server = new ServerSocket(port);
			System.out.println(server);
			run();
		} catch (IOException e) {
			System.out.println("Could not listen on port: " + port);
			System.exit(-1);
		}
	}
	/**
	 * This method runs the server on the given socket and starts a new thread to be used in that socket.
	 */
	private void run() {
		while (true) {

			Socket socket = null;
			try {
				socket = server.accept();
				
				clientList.add(new ConnectedClients(socket, ++numusers));
				//This thread keeps track of the socket it is using and the number of users that is currently connected to that socket.
				Thread t = new Thread(new ClientChecker(this, socket, numusers));
				t.start();

			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.exit(-1);
			}

		}
	}
	
	public void close() {
		System.exit(-1);
	}

	/**
	 * 
	 * @author Kenneth Lange
	 *
	 * This helper class is used by the Server class to check if any other clients are trying to connect
	 * Then it tries to read data by sent by that client and return the data it receives back to other clients.
	 * 
	 */
	private class ClientChecker implements Runnable {

		Server server;
		Socket socket;
		int id;

		public ClientChecker(Server server, Socket socket, int id) {
			this.server = server;
			this.socket = socket;
			this.id = id;
		}

		public void run() {

			while (true) {
				try {

					InputStream in = socket.getInputStream();
					byte[] b = new byte[256];
					//InputStreams need to use byte arrays to read them.
					if (in.available() > 0) {
						in.read(b);
						server.messenger(new String(b), id);
						System.out.println(new String(b));
					}

				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method is responsible for taking a users input and sending it to all the other users.
	 * It also uses an id so it doesn't receive the message that it sends.
	 * 
	 * @param string
	 * @param id
	 */
	public void messenger(String string, int id) {
		if(string.contains("server.shutdown")) {
			//Something I was playing with to see if I could close all the clients and the server at once.
			//It works :)
			for(int i = 0; i < clientList.size();i++) {
				if( !clientList.get(i).checkId(id) ) {
					clientList.get(i).sendString(string);
				}
			}
			close();
			return; //Stops the Server.
		}
		
		for(int i = 0; i < clientList.size();i++) {
			if( !clientList.get(i).checkId(id) ) { //If the id matches the client, it skips sending the message back to that client.
				clientList.get(i).sendString(string);
			}
		}
	}
	/**
	 * 
	 * @author Kenenth Lange
	 *
	 *
	 * This helper class is used by the server to keep track of which client is which based on an id system.
	 * 
	 */
	private class ConnectedClients{
		Socket socket;
		int id;
		
		public ConnectedClients(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
		}
		/**
		 * This helper method is used when checking who sent the message and used to see who hasn't yet seen the message sent by that user.
		 * @param id
		 * @return
		 */
		public boolean checkId(int id) {
			return this.id == id;
		}
		/**
		 * This method sends a string back to a client
		 * @param s
		 */
		public void sendString(String s) {
			try {
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(s.getBytes());
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}