package cs342.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

// +-----------------+
// | IOHANDLER CLASS |
// +-----------------+

public abstract class IOHandler implements Runnable {
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	Socket socket;
	protected volatile boolean shutDown = false;
	
	// +------------------+
	// | ABSTRACT METHODS |
	// +------------------+
	
	protected abstract void initialize(InputStream in, OutputStream out);
	protected abstract void processInput();
	protected abstract void handleShutDown();
	
	// +---------+
	// | METHODS |
	// +---------+
	
	public InetAddress getInetAddress() {
		if (socket != null)
			return socket.getInetAddress();
		return null;
	}
	
	final void shutDown() {
		if (!shutDown) {
			shutDown = true;
			handleShutDown();
			try {
				socket.close();
			} catch (Exception e) {}
		}
	}
	
	@Override
	public final void run() {
		while (!shutDown)
			processInput();
		try {
			socket.close();
		} catch (Exception e) {}
	}
}
