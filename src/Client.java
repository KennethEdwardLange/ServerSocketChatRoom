import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;
/**
 * 
 * @author Kenneth Lange
 *
 * This class is used for a user to connect to a given sever on localhost on port 4444
 */
public class Client {
	
	//Global variables for easy access and convenience for editing
	int port = 4444;
	String address = "localhost";
	Socket socket;
	Scanner scanner;
	String username;
	
	public static void main(String[] args) {
		new Client();
	}

	public Client() {
		init();
	}
	
	private void init() {
		try {
			
			socket = new Socket(); 
			System.out.print("Enter your username: ");
			scanner = new Scanner(System.in);
			username = scanner.nextLine();
			//Connects to address, hard coded to "localhost" in this project, and chooses the correct port, hard coded to 4444.
			SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(address), port);
			socket.connect(socketAddress);
			if(socket != null) {
				System.out.println(socket.toString());
				run();
			}
		
		} catch (IOException ioe) {
			System.err.println(ioe + "\nUnable to  connect to '" + address + "' server on port '" + port + "' \nPlease make sure that the server is running.");
			}
	}
	
	private void run() {
		//Creates a new thread for the client to stay active to communicate with the server and it's clients
		Thread t = new Thread(new ServerScanner(socket));
		t.start();
		
		while(true) {
			try {
				System.out.println(username + ": ");
				String message = scanner.nextLine();
				//Adds "['username']" to the message.
				message = username + ": " + message;
				//Gets the output stream ready to be sent to.
				OutputStream out = socket.getOutputStream();
				//This is where it writes to the server.
				out.write(message.getBytes());
				if (message.contains("server.shutdown")) {
					System.err.println("You have shut down the server.");
					System.exit(-1);
				}
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * @author Kenneth Lange
	 *
	 * This class is directly used by the Client class for receiving the input stream back from the server that it connects to.
	 */
	private class ServerScanner implements Runnable {
		Socket socket;
		public ServerScanner(Socket socket) {
			this.socket = socket; //Used to make the socket transferable from one class to the next.
		}

		@Override
		public void run() {
			try {
				while (true) {
					InputStream in = socket.getInputStream();
					//stream needed to be converted into a byte array.
					byte[] b = new byte[256];
					//checks if the stream has any "available" data to be read. 
					if(in.available() > 0) {
						in.read(b); //reads the byte
						String message = new String(b);//Converts back to string then is put into users console.
						if (message.contains("server.shutdown")) {
							System.err.println("Client ended...\nServer has been shut down!");
							System.exit(-1);
						}
						System.out.println(message); 
					}

				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}