package cs342.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// +--------------+
// | SERVER CLASS |
// +--------------+

/** <br><b>A server implementation using Sockets.</b>
 *  <br>Manages client connections.
 *  <br>Takes cares of socket and thread handling.
 *  @author Tomas Juocepis
 *  @author Andriy Klyuka
 */
public final class Server {
	
	// +--------------------+
	// | LISTENER INTERFACE |
	// +--------------------+
	
	public interface Listener {
		
		public void failedToStartServer(IOException e);
		public void failedToCloseServer(IOException e);
		public void failedToConnectToClient(IOException e);
		public void serverStarted(int port);
		public void serverTerminated(int port);
		public void clientConnectionEstablished(String clientAddress);
		public IOHandler getIOHandler();
	}
	
	
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	public final int PORT;
	private final Listener LISTENER;
	private final List<IOHandler> CLIENTS;
	private ServerSocket serverSocket;
	private volatile boolean shutDown = false;
	
	// +-------------+
	// | CONSTRUCTOR |
	// +-------------+
	
	public Server(int port, Listener listener) {
		PORT = port;
		LISTENER = listener;
		CLIENTS = new ArrayList<IOHandler>();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				serverSocket = startServer();
				if (serverSocket == null)
					return;
				LISTENER.serverStarted(PORT);
				while (!shutDown)
					handleNewClients(serverSocket);
				LISTENER.serverTerminated(PORT);
			}
		}).start();
	}
	
	// +----------------+
	// | PUBLIC METHODS |
	// +----------------+
	
	public void shutDown() {
		if (shutDown == false) {
			for (IOHandler handler : CLIENTS)
				handler.shutDown();
			shutDown = true;
			closeServerSocket();
		}
	}
	
	public void shutDownClient(IOHandler handler) {
		handler.shutDown();
	}
	
	// +-----------------+
	// | PRIVATE METHODS |
	// +-----------------+
	
	private ServerSocket startServer() {
		try {
			return new ServerSocket(PORT);
		} catch (IOException e) {
			LISTENER.failedToStartServer(e);
			return null;
		}
	}
	
	private void handleNewClients(ServerSocket serverSocket) {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			if (!shutDown) {
				shutDown();
				LISTENER.failedToConnectToClient(e);
			}
			return;
		}
		try {
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			String address = socket.getInetAddress().toString();
			LISTENER.clientConnectionEstablished(address);
			IOHandler handler = LISTENER.getIOHandler();
			handler.socket = socket;
			handler.initialize(in, out);
			CLIENTS.add(handler);
			new Thread(handler).start();
		} catch (IOException e) {
			if (!shutDown) {
				if (socket != null)
					try { socket.close(); } catch (IOException e1) {}
				LISTENER.failedToConnectToClient(e);
			}
		}
	}
	
	private void closeServerSocket() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			if (!shutDown)
				LISTENER.failedToCloseServer(e);
		}
	}
}
