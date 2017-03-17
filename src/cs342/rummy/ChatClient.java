package cs342.rummy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import cs342.network.Client;
import cs342.network.IOHandler;

// +------------------+
// | CHATCLIENT CLASS |
// +------------------+

public class ChatClient implements Client.Listener {
	
	// +----------------+
	// | USER INTERFACE |
	// +----------------+
	
	public interface UserInterface {
		
		public void info(String info);
		public void notice(String notice);
		public void handleRummyEvent(String who, String where, Object event);
		public void handleChatMessage(String who, String where, String what);
		public void nameRequestResponse(String name, String response);
		public void joinNotice(String who, String where, String context);
		public void leaveNotice(String who, String from, String context);
		public void conversationStartedNotice(String convo, String creator);
		public void conversationEndedNotice(String convo);
		public void clientShutdownNotice(String context);
		public void log(String text);
	}
	
	
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	private final Client CLIENT;
	private final UserInterface UI;
	private final ServerIOHandler SERVER; 
	private final SimpleDateFormat DATE_FORMAT;
	private volatile boolean shutDown = false;
	private String userName;
	
	// +-------------+
	// | CONSTRUCTOR |
	// +-------------+
	
	public ChatClient(String host, int port, UserInterface ui) {
		UI = ui;
		DATE_FORMAT = new SimpleDateFormat("'|'yyyy.MM.dd'|'HH:mm:ss.SSS'|'");
		SERVER = new ServerIOHandler();
		CLIENT = new Client(host, port, this);
	}
	
	// +----------------+
	// | PUBLIC METHODS |
	// +----------------+
	
	public void sendRummyEvent(String convoName, Object e) {
		SERVER.send(new Message(Message.Type.RUMMY_EVENT, convoName, null, null, e));
	}
	
	public void sendChatMessage(String convoName, String content) {
		SERVER.send(new Message(Message.Type.CHAT, convoName, null, content));
	}
	
	public void chatNameRequest(String name) {
		SERVER.send(new Message(Message.Type.CHATNAME, name));
	}
	
	public void joinConvo(String name) {
		SERVER.send(new Message(Message.Type.JOIN, name));
	}
	
	public void leaveConvo(String name) {
		SERVER.send(new Message(Message.Type.LEAVE, name));
	}
	
	public void startConvo(String name) {
		SERVER.send(new Message(Message.Type.START_CONVO, name));
	}
		
	
	public void shutDown() {
		if (shutDown == false) {	
			log("+++ User initiated a shutdown");
			internalShutDown(true);
		}
	}
	
	private void internalShutDown(boolean notifyServer) {
		if (shutDown == false) {
			shutDown = true;
			if (notifyServer) {
				log("+++ Notifying server about the shutdown");
				SERVER.send(new Message(Message.Type.SHUTDOWN, null, null, null));
			}
			CLIENT.shutDown();
			log("+++ Client has been shut down");
			UI.clientShutdownNotice("Client has been shut down");
		}
	}
	
	@Override
	public IOHandler getIOHandler() {
		return SERVER;
	}
	
	@Override
	public void failedToConnectToServer(IOException e) {
		log("### Failed to connect to server: " + e);
		internalShutDown(false);
	}
	
	@Override
	public void serverConnectionEstablished(String serverAddress) {
		log("+++ Server connection established (" + serverAddress + ")");
	}
	
	@Override
	public void serverConnectionTerminated(String serverAddress) {
		log("+++ Server connection terminated (" + serverAddress + ")");		
	}
			
	// +-----------------+
	// | PRIVATE METHODS |
	// +-----------------+
	
	private void log(String text) {
		UI.log(DATE_FORMAT.format(new Date()) + " ::: " + text);
	}
	
	private void serverCommunicationEstablished() {
		log("+++ Communication with server established");
	}
	
	private void handleMessage(Message msg) {
		if (msg.TYPE == Message.Type.DEBUG) {
			handleDebugMessage(msg);
			return;
		}
		log("Message received from server: " + msg);
		switch (msg.TYPE) {
			case INFO			: handleInfoMessage(msg); break;
			case NOTICE			: handleNoticeMessage(msg); break;
			case CHAT			: handleChatMessage(msg); break;
			case CHATNAME		: handleChatnameMessage(msg); break;
			case JOIN			: handleJoinMessage(msg); break;
			case LEAVE			: handleLeaveMessage(msg); break;
			case START_CONVO	: handleStartConvoMessage(msg); break;
			case CLOSE_CONVO	: handleCloseConvoMessage(msg); break;
			case SHUTDOWN		: handleShutdownMessage(msg); break;
			case RUMMY_EVENT	: handleRummyEventMessage(msg); break;
			default				: handleUnsupportedMessage(msg); break;
		}
	}
	
	private void handleInfoMessage(Message msg) {
		UI.info(msg.CONTENT);
	}
	
	private void handleNoticeMessage(Message msg) {
		UI.notice(msg.CONTENT);
	}
	
	private void handleRummyEventMessage(Message msg) {
		UI.handleRummyEvent(msg.NAME1, msg.NAME2, msg.PAYLOAD);
	}
	
	private void handleChatMessage(Message msg) {
		UI.handleChatMessage(msg.NAME1, msg.NAME2, msg.CONTENT);
	}
	
	private void handleChatnameMessage(Message msg) {
		userName = msg.NAME1;
		UI.nameRequestResponse(userName, msg.CONTENT);
	}
	
	private void handleJoinMessage(Message msg) {
		UI.joinNotice(msg.NAME1, msg.NAME2, msg.CONTENT);
	}
	
	private void handleLeaveMessage(Message msg) {
		UI.leaveNotice(msg.NAME1, msg.NAME2, msg.CONTENT);
	}
	
	private void handleStartConvoMessage(Message msg) {
		UI.conversationStartedNotice(msg.NAME1, msg.NAME2);
	}
	
	private void handleCloseConvoMessage(Message msg) {
		UI.conversationEndedNotice(msg.NAME1);
	}
	
	private void handleShutdownMessage(Message msg) {
		log("+++ Server has shut down... starting client shutdown...");
		internalShutDown(false);
	}
	
	private void handleDebugMessage(Message msg) {
		UI.info(msg.CONTENT);
	}
	
	private void handleUnsupportedMessage(Message msg) {
		log("??? Unsupported message received: " + msg);
	}
	
	
	
	// +------------------------------+
	// | SERVERIOHANDLER: INNER CLASS |
	// +------------------------------+
	
	private class ServerIOHandler extends IOHandler {
		
		private ObjectInputStream IN;
		private ObjectOutputStream OUT;
		
		private void send(Message msg) {
			try {
				if (OUT != null) {
					OUT.writeObject(msg);
					log("Message sent to the server: " + msg);
				}
			} catch (IOException e) {
				if (!shutDown)
					log("### Failed to send a message to the server: " + e);
				internalShutDown(false);
			}
		}
		
		@Override
		public void initialize(InputStream in, OutputStream out) {
			try {
				OUT = new ObjectOutputStream(out);
				IN = new ObjectInputStream(in);
				serverCommunicationEstablished();
			} catch (Exception e) {
				if (!shutDown)
					log("### Failed to create message IO streams "
						+ "to the server: " + e);
				internalShutDown(false);
			}
		}
		
		@Override
		public void processInput() {
			try {
				handleMessage((Message)IN.readObject());
			} catch (IOException e) {
				if (!shutDown)
					log("### Failed to receive a message from the server: " + e);
				internalShutDown(false);
			} catch (ClassNotFoundException e) {
				if (!shutDown)
					log("### Failed to receive a message from the server: " + e);
				internalShutDown(false);
			}
		}
		
		@Override
		protected void handleShutDown() {
			try {
				log("+++ Server connection is being shut down");
				if (IN != null)  IN.close();
				if (OUT != null) OUT.close();
			} catch (IOException e) {
				if (!shutDown)
					log("### Failed to shut down message IO streams"
						+ "from the server");
				internalShutDown(false);
			}
		}
	}
}
