package cs342.rummy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import cs342.network.IOHandler;
import cs342.network.Server;

// +------------------+
// | CHATSERVER CLASS |
// +------------------+

public class ChatServer implements Server.Listener {
	
	// +----------------+
	// | USER INTERFACE |
	// +----------------+
	
	public interface UserInterface {
		
		public void serverShutdownNotice(String context);
		public void log(String text);
	}
	
	
	
	// +-------------------+
	// | STATIC PARAMETERS |
	// +-------------------+
	
	public final static int
	MIN_NAME_LENGTH = 1,
	MAX_NAME_LENGTH = 16;
	
	public final static String
	DATE_FORMAT = "'|'yyyy.MM.dd'|'HH:mm:ss.SSS'|'";
	
	private final static Message
	SHUTDOWN_MESSAGE	= new Message(Message.Type.SHUTDOWN, null, null,
			  			  "Server is shutting down");
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	public final String SERVER_NAME;
	private final Server SERVER;
	private final UserInterface UI;
	private final SimpleDateFormat DATE_FORMATTER;
	private final List<Peep> FRESH_PEEPS;
	private final TreeMap<String, Peep> PEEPS;
	private final TreeMap<String, Convo> CONVOS;
	private final ServerBot SERVER_BOT;
	private final Convo PUBLIC_CONVO;
	private int peepCounter;
	
	// +-------------+
	// | CONSTRUCTOR |
	// +-------------+
	
	public ChatServer(int port, String name, UserInterface ui) {
		SERVER_NAME = name;
		UI = ui;
		DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
		FRESH_PEEPS = new ArrayList<Peep>();
		PEEPS = new TreeMap<String, Peep>();
		CONVOS = new TreeMap<String, Convo>();
		SERVER_BOT = new ServerBot();
		SERVER_BOT.NAME = "~ Fat Kat ~";
		PUBLIC_CONVO = new Convo("- PUBLIC -", SERVER_BOT);
		PUBLIC_CONVO.observable = true;
		CONVOS.put(PUBLIC_CONVO.NAME, PUBLIC_CONVO);
		PEEPS.put(SERVER_BOT.NAME, SERVER_BOT);
		SERVER = new Server(port, this);
	}
	
	// +----------------+
	// | PUBLIC METHODS |
	// +----------------+
	
	public void shutDown() {
		log("+++ Server is shutting down...");
		for (Convo convo : CONVOS.values()) {
			convo.INVITED.clear();
			convo.JOINED.clear();
		}
		CONVOS.clear();
		List<Peep> peeps = new ArrayList<Peep>(PEEPS.values());
		peeps.addAll(FRESH_PEEPS);
		FRESH_PEEPS.clear();
		PEEPS.clear();	
		for (Peep peep : peeps) {
			peep.send(SHUTDOWN_MESSAGE);
			SERVER.shutDownClient(peep);
		}
		SERVER.shutDown();
		log("+++ Server has shut down");
		UI.serverShutdownNotice("Server has been shut down");
	}
	
	@Override
	public IOHandler getIOHandler()	{
		Peep freshPeep = new Peep();
		FRESH_PEEPS.add(freshPeep);
		return freshPeep;
	}
	
	@Override
	public void failedToStartServer(IOException e) {
		log("### Server failed to start: " + e);
	}
	
	@Override
	public void failedToCloseServer(IOException e) {
		log("### Server failed to close: " + e);
	}
	
	@Override
	public void failedToConnectToClient(IOException e) {
		log("### Client failed to connect: " + e);
	}
	
	@Override
	public void serverStarted(int port) {
		log("+++ Server started on port: " + port);
	}

	@Override
	public void serverTerminated(int port) {
		log("+++ Server terminated on port: " + port);
	}

	@Override
	public void clientConnectionEstablished(String clientAddress) {
		log("+++ Client connected from: " + clientAddress);
	}
	
	// +-----------------+
	// | PRIVATE METHODS |
	// +-----------------+
	
	private void log(String text) {
		UI.log(DATE_FORMATTER.format(new Date()) + " ::: " + text);
	}
	
	private void peepCommunicationEstablished(Peep peep) {
		log("+++ Communication established with a client");
		peep.send(Message.Type.NOTICE, null, null,
				  "Welcome to the server, please identify yourself");
	}
	
	private void sendToPeeps(Message msg) {
		for (Peep peep : PEEPS.values())
			peep.send(msg);
	}
	
	private void handleMessage(Peep peep, Message msg) {
		log("Message received from client " + peep.getName() + " >> " + msg);
		if (peep.NAME == null)
			handleFreshPeepMessage(peep, msg);
		else switch (msg.TYPE) {
			case SHUTDOWN		: handleShutdownMessage(peep, msg); break;
			case JOIN			: handleJoinMessage(peep, msg); break;
			case LEAVE			: handleLeaveMessage(peep, msg); break;
			case START_CONVO	: handleStartConvoMessage(peep, msg); break;
			case CHAT			: handleChatMessage(peep, msg); break;
			case DEBUG			: handleDebugMessage(peep, msg); break;
			case RUMMY_EVENT	: handleRummyEventMessage(peep, msg); break;
			default				: handleUnsupportedMessage(peep, msg); break;
		}
	}
	
	private void handleFreshPeepMessage(Peep peep, Message msg) {
		if (msg.TYPE == Message.Type.CHATNAME)
			handleNameRequest(peep, msg.NAME1.trim());
		else if (msg.TYPE == Message.Type.SHUTDOWN)
			handleShutdownMessage(peep, msg);
		else
			peep.send(Message.Type.NOTICE, null, null,
					  "You must provide a valid chat name first");
	}
	
	private void handleShutdownMessage(Peep peep, Message msg) {
		log("+++ " + peep + " has left the server");
		SERVER.shutDownClient(peep);
		handlePeepLeavingServer(peep);
	}
	
	private void handleJoinMessage(Peep peep, Message msg) {
		Convo convo = CONVOS.get(msg.NAME1);
		if (convo != null)
			if (!peep.isIn(convo))
				if (!convo.inviteOnly || peep.isInvitedTo(convo))
					peep.join(convo);
				else
					peep.send(Message.Type.INFO, null, null,
							  "You do not have an invitation " +
							  "to this conversation");
			else
				peep.send(Message.Type.INFO, null, null,
						  "You are already in this conversation");
		else
			peep.send(Message.Type.INFO, null, null,
					  "Such conversation does not exist");
	}
	
	private void handleLeaveMessage(Peep peep, Message msg) {
		Convo convo = CONVOS.get(msg.NAME1);
		if (convo != null) {
			if (peep.isIn(convo)) {
				peep.leave(convo);
				if (convo.JOINED.isEmpty()) {
					CONVOS.remove(convo.NAME);
					if (convo.visible)
						sendToPeeps(new Message(Message.Type.CLOSE_CONVO,
									convo.NAME, null, null));
				}
			}
			else
				peep.send(Message.Type.INFO, null, null,
						  "You are not in this conversation anyway");
			return;
		}
		Peep leavePeep = PEEPS.get(msg.NAME1);
		if (leavePeep != null) {
			leavePeep.send(Message.Type.CLOSE_CONVO, peep.NAME, null, null);
			peep.send(Message.Type.CLOSE_CONVO, leavePeep.NAME, null, null);
			return;
		}
		peep.send(Message.Type.NOTICE, null, null,
				  "Such conversation does not exist");
	}
	
	private void handlePeepLeavingServer(Peep leavingPeep) {
		if (leavingPeep.NAME == null) {
			FRESH_PEEPS.remove(leavingPeep);
			return;
		}
		Iterator<Convo> i = CONVOS.values().iterator();
		while (i.hasNext()) {
			Convo convo = i.next();
			if (leavingPeep.isIn(convo)) {
				leavingPeep.leave(convo);
				if (convo.JOINED.isEmpty()) {
					i.remove();
					if (convo.visible)
						sendToPeeps(new Message(Message.Type.CLOSE_CONVO,
									convo.NAME, null, null));
				}
			}
		}
		for (Convo convo : CONVOS.values())
			if (leavingPeep.isIn(convo))
				leavingPeep.leave(convo);
		PEEPS.remove(leavingPeep.NAME);
	}
	
	private void handleStartConvoMessage(Peep peep, Message msg) {
		String convoName = msg.NAME1.trim();
		if (convoName.startsWith("::"))
			handleDebugMessage(peep, new Message(Message.Type.DEBUG,
								     null, null, msg.NAME1));
		else if (convoName.length() < MIN_NAME_LENGTH)
			peep.send(Message.Type.NOTICE, null, null,
					  "Your conversation name is too short");
		else if (convoName.length() > MAX_NAME_LENGTH)
			peep.send(Message.Type.NOTICE, null, null,
					  "Your conversation name is too long");
		else if (!Character.isLetterOrDigit(convoName.charAt(0)) ||
			 !Character.isLetterOrDigit(convoName.charAt(convoName.length()-1)))
			peep.send(Message.Type.NOTICE, null, null,
					  "Your conversation name's first and last characters " +
					  "must be alphanumeric");
		else if (CONVOS.containsKey(convoName) || PEEPS.containsKey(convoName))
			peep.send(Message.Type.NOTICE, null, null,
					  "The name '" + convoName + "' is already in use");
		else {
			for (Peep p : PEEPS.values())
				p.send(Message.Type.START_CONVO, convoName, peep.NAME, null);
			Convo newConvo = new Convo(convoName, peep);
			CONVOS.put(convoName, newConvo);
		}
	}
	
	private void handleChatMessage(Peep peep, Message msg) {
		Convo convo = CONVOS.get(msg.NAME1);
		if (convo != null) {
			if (peep.isIn(convo))
				if (convo.observable)
					for (Peep p : PEEPS.values())
						p.send(Message.Type.CHAT, peep.NAME, convo.NAME,
							   msg.CONTENT);
				else
					for (Peep p : convo.JOINED.values())
						p.send(Message.Type.CHAT, peep.NAME, convo.NAME,
							   msg.CONTENT);
			else
				peep.send(Message.Type.INFO, null, null,
						  "Sorry, but you are not a part of this conversation");
			return;
		}
		Peep convoPeep = PEEPS.get(msg.NAME1);
		if (convoPeep != null) {
			convoPeep.send(Message.Type.CHAT, peep.NAME, peep.NAME,
						   msg.CONTENT);
			peep.send(Message.Type.CHAT, peep.NAME, convoPeep.NAME,
					  msg.CONTENT);
			return;
		}
		peep.send(Message.Type.INFO, null, null,
				  "You can't chat in a conversation that does not exist");
	}
	
	private void handleRummyEventMessage(Peep peep, Message msg) {
		Convo convo = CONVOS.get(msg.NAME1);
		if (convo != null) {
			if (peep.isIn(convo)) {
				Collection<Peep> notifiedPeeps = convo.JOINED.values();
					//convo.observable ? PEEPS.values() : convo.JOINED.values();
				for (Peep p : notifiedPeeps)
					p.send(new Message(Message.Type.RUMMY_EVENT, peep.NAME,
									   convo.NAME, msg.CONTENT, msg.PAYLOAD));
			}
			else
				peep.send(Message.Type.INFO, null, null,
						  "Sorry, but you are not a part of this game");
			return;
		}
		peep.send(Message.Type.INFO, null, null,
				  "Sorry, this game appears to not exist");		
	}
	
	private void handleUnsupportedMessage(Peep peep, Message msg) {
		peep.send(Message.Type.INFO, null, null, "Unsupported message");
	}
	
	private void introduceToServer(Peep newPeep) {
		for (Convo convo : CONVOS.values())
			if (convo.visible) {
				newPeep.send(Message.Type.START_CONVO, convo.NAME, null, null);
				if (convo.observable)
					for (Peep peep : convo.JOINED.values())
						newPeep.send(Message.Type.JOIN, peep.NAME, convo.NAME, null);
			}
	}
	
	private void handleNameRequest(Peep peep, String name) {
		if (name.length() < MIN_NAME_LENGTH)
			peep.send(Message.Type.NOTICE, null, null,
					  "Your name is too short");
		else if (name.length() > MAX_NAME_LENGTH)
			peep.send(Message.Type.NOTICE, null, null,
					  "Your name is too long");
		else if (!Character.isLetterOrDigit(name.charAt(0))
			 || !Character.isLetterOrDigit(name.charAt(name.length() - 1)))
			peep.send(Message.Type.NOTICE, null, null,
					  "Your name's first and last characters " +
					  "must be alphanumeric");
		else if (PEEPS.containsKey(name) || CONVOS.containsKey(name))
			peep.send(Message.Type.NOTICE, null, null,
					  "This name is already in use");
		else {
			peep.NAME = name;
			peep.send(Message.Type.CHATNAME, name, null, "Name accepted");
			introduceToServer(peep);
			FRESH_PEEPS.remove(peep);
			PEEPS.put(peep.NAME, peep);
		}
	}
	
	private void handleDebugMessage(Peep peep, Message msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n:: ::: BEGIN DEBUG :::\n");
		String debugCommand = msg.CONTENT;
		if (debugCommand.contains("PEEPS"))
			sb.append(debugPEEPS());
		if (debugCommand.contains("FRESH"))
			sb.append(debugFRESH());
		sb.append(":: ::: END DEBUG :::\n");
		String debugResponse = sb.toString();
		UI.log(debugResponse);
		peep.send(Message.Type.DEBUG, null, null, debugResponse);
	}
	
	private String debugPEEPS() {
		StringBuilder sb = new StringBuilder();
		sb.append(":: PEEPS - list of logged in peeps:\n");
		int peepCount = 0;
		for (Peep peep : PEEPS.values())
			sb.append(":: ").append(++peepCount).append(". ")
			.append(peep.getName()).append(" (").append(peep.getInetAddress())
			.append(")\n");
		return sb.toString();
	}
	
	private String debugFRESH() {
		StringBuilder sb = new StringBuilder();
		sb.append(":: FRESH - list of connected but not logged in peeps:\n");
		int peepCount = 0;
		for (Peep peep : FRESH_PEEPS)
			sb.append(":: ").append(++peepCount).append(". ")
			.append(peep.getName()).append(" (").append(peep.getInetAddress())
			.append(")\n");
		return sb.toString();
	}
	
	
	
	// +--------------------+
	// | CONVO: INNER CLASS |
	// +--------------------+
	
	private class Convo implements Comparable<Convo> {
		
		private final String NAME;
		private final TreeMap<String, Peep> JOINED, INVITED;
		private boolean visible, observable, inviteOnly;
		
		private Convo(String convoName, Peep peep) {
			NAME = convoName;
			JOINED = new TreeMap<String, Peep>();
			INVITED = new TreeMap<String, Peep>();
			visible = true;
			observable = false;
			inviteOnly = false;
			join(peep);
		}
		
		private boolean isJoined(Peep peep) {
			return JOINED.containsKey(peep.NAME);
		}
		
		private boolean isInvited(Peep peep) {
			return INVITED.containsKey(peep.NAME);
		}
		
		private void join(Peep peep) {
			INVITED.remove(peep.NAME);
			JOINED.put(peep.NAME, peep);
			Message joinMessage = new Message(
								  Message.Type.JOIN, peep.NAME, NAME, null);
			if (observable)
				sendToPeeps(joinMessage);
			else {
				sendToAllJoined(joinMessage);
				for (Peep p : JOINED.values())
					if (p != peep)
						peep.send(new Message(
								  Message.Type.JOIN, p.NAME, NAME, null));
					
			}
		}
		
		private void leave(Peep peep) {
			Message leaveMessage = new Message(
								   Message.Type.LEAVE, peep.NAME, NAME, null);
			if (observable)
				sendToPeeps(leaveMessage);
			else {
				sendToAllJoined(leaveMessage);
				for (Peep p : JOINED.values())
					if (p != peep)
						peep.send(new Message(
								  Message.Type.LEAVE, p.NAME, NAME, null));
			}
			JOINED.remove(peep.NAME);
			INVITED.remove(peep.NAME);
		}
		
		private void sendToAllJoined(Message msg) {
			for (Peep peep : JOINED.values())
				peep.send(msg);
		}
		
		@Override
		public int compareTo(Convo otherConvo) {
			return NAME.compareTo(otherConvo.NAME);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)					return true;
			if (obj == null)					return false;
			if (getClass() != obj.getClass())	return false;
			Convo other = (Convo) obj;
			if (NAME == null) {
				if (other.NAME != null)			return false;
			} else if (!NAME.equals(other.NAME))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return NAME;
		}
	}
	
	
	
	// +-------------------------+
	// | SERVERDUDE: INNER CLASS |
	// +-------------------------+
	
	private class ServerBot extends Peep {
		
		@Override
		protected void send(Message msg) {
			if (msg.TYPE == Message.Type.CHAT && !msg.NAME1.equals(NAME)) {
				String peepText = msg.CONTENT.trim().toLowerCase();
				if (peepText.contains("dude"))
					handleMessage(this, new Message(
							Message.Type.CHAT, PUBLIC_CONVO.NAME, null,
							"Did someone mention me?"));
				if (peepText.contains("hello"))
					handleMessage(this, new Message(
							Message.Type.CHAT, PUBLIC_CONVO.NAME, null,
							"Hello to you too, " + msg.NAME1));
				if (peepText.contains("time"))
					handleMessage(this, new Message(
							Message.Type.CHAT, PUBLIC_CONVO.NAME, null,
							"Current date & time is: " + new Date()));
			}
		}
	}
	
	
	
	// +-------------------+
	// | PEEP: INNER CLASS |
	// +-------------------+
	
	private class Peep extends IOHandler implements Comparable<Peep> {
		
		protected String NAME;
		private final int ID;
		private ObjectInputStream IN;
		private ObjectOutputStream OUT;
		
		private Peep() {
			ID = peepCounter++;
		}
		
		protected void send(Message msg) {
			try {
				OUT.writeObject(msg);
				log("Message sent to client " + getName() + " << " + msg);
			} catch (IOException e) {
				if (!shutDown) {
					log("### Failed to send a message to client "
						+ getName() + ": " + e);
					shutDown();
				}
			}
		}
		
		private void send(Message.Type type, String name1, String name2,
														   String content) {
			send(new Message(type, name1, name2, content));
		}
		
		private boolean isIn(Convo convo) {
			return convo.isJoined(this);
		}
		
		private boolean isInvitedTo(Convo convo) {
			return convo.isInvited(this);
		}
		
		private void join(Convo convo) {
			convo.join(this);
		}
		
		private void leave(Convo convo) {
			convo.leave(this);
		}
		
		private String getName() {
			return (NAME == null ? ("Peep " + ID) : NAME);
		}
		
		@Override
		public int compareTo(Peep otherPeep) {
			return NAME.compareTo(otherPeep.NAME);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)					return true;
			if (obj == null)					return false;
			if (getClass() != obj.getClass())	return false;
			Peep other = (Peep) obj;
			if (NAME == null) {
				if (other.NAME != null)			return false;
				else if (ID != other.ID)		return false;
			} else if (!NAME.equals(other.NAME))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return NAME;
		}
		
		@Override
		public void initialize(InputStream in, OutputStream out) {
			try {
				OUT = new ObjectOutputStream(out);
				IN = new ObjectInputStream(in);
				peepCommunicationEstablished(this);
			} catch (Exception e) {
				if (!shutDown) {
					log("### Failed to create message IO streams to client "
						+ getName() + ": " + e);
					shutDown();
				}
			}
		}
		
		@Override
		public void processInput() {
			try {
				handleMessage(this, (Message)IN.readObject());
			} catch (IOException e) {
				if (!shutDown) {
					log("### Failed to receive a message from client "
						+ getName() + ": " + e);
					shutDown();
				}
			} catch (ClassNotFoundException e) {
				if (!shutDown) {
					log("### Failed to receive a message from client "
						+ getName() + ": " + e);
					shutDown();
				}
			}
		}
		
		@Override
		protected void handleShutDown() {
			handlePeepLeavingServer(this);
			try {
				log("+++ Client connection is being shut down");
				if (IN != null)  IN.close();
				if (OUT != null) OUT.close();
			} catch (IOException e) {
				if (!shutDown) {
					log("### Failed to shut down message IO streams for client "
						+ getName() + ": " + e);
					shutDown();
				}
			}
		}
	}
}
