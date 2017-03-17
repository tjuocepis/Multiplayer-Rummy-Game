package cs342.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//+--------------+
//| CLIENT CLASS |
//+--------------+

public class Client {
	
	// +--------------------+
	// | LISTENER INTERFACE |
	// +--------------------+
	
	public interface Listener {
		
		public void failedToConnectToServer(IOException e);
		public void serverConnectionEstablished(String serverAddress);
		public void serverConnectionTerminated(String serverAddress);
		public IOHandler getIOHandler();
	}
	
	
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	public final String SERVER_ADDRESS;
	public final int SERVER_PORT;
	private final Listener LISTENER;
	private IOHandler serverCommunication;
	
	// +-------------+
	// | CONSTRUCTOR |
	// +-------------+
	
	public Client(String host, int port, Listener listener) {
		SERVER_ADDRESS = host;
		SERVER_PORT = port;
		LISTENER = listener;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Socket socket = null;
				try {
					socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();
					String address = socket.getInetAddress().toString();
					LISTENER.serverConnectionEstablished(address);
					serverCommunication = LISTENER.getIOHandler();
					serverCommunication.socket = socket;
					serverCommunication.initialize(in, out);
					new Thread(serverCommunication).start();
				} catch (IOException e) {
					if (socket != null)
						try { socket.close(); } catch (IOException e1) {}
					LISTENER.failedToConnectToServer(e);
				}
			}
		}).start();
	}
	
	// +----------------+
	// | PUBLIC METHODS |
	// +----------------+
	
	public void shutDown() {
		if (serverCommunication != null)
			serverCommunication.shutDown();
	}
}
