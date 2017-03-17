package cs342.rummy;

import java.io.Serializable;

// +---------------+
// | MESSAGE CLASS |
// +---------------+

public class Message implements Serializable {
	
	// +-------------------+
	// | MESSAGE TYPE ENUM |
	// +-------------------+
	
	public enum Type {
		INFO, NOTICE, CHAT, CHATNAME, JOIN, INVITE, LEAVE,
		START_CONVO, CLOSE_CONVO, SHUTDOWN, DEBUG,
		RUMMY_EVENT;
	}
	
	
	
	private static final long serialVersionUID = 1458556919901832043L;
	
	// +-----------+
	// | VARIABLES |
	// +-----------+
	
	final Type TYPE;
	final String NAME1, NAME2, CONTENT;
	final Object PAYLOAD;
	
	// +--------------+
	// | CONSTRUCTORS |
	// +--------------+
	
	Message(Type type, String name1) {
		this(type, name1, null, null, null);
	}
	
	Message(Type type, String name1, String name2) {
		this(type, name1, name2, null, null);
	}
	
	Message(Type type, String name1, String name2, String content) {
		this(type, name1, name2, content, null);
	}
	
	Message(Type type, Object obj) {
		this(type, null, null, null, obj);
	}
	
	Message(Type type, String name1, String name2, String content, Object obj) {
		TYPE = type;
		NAME1 = name1;
		NAME2 = name2;
		CONTENT = content;
		PAYLOAD = obj;
	}
	
	// +---------+
	// | METHODS |
	// +---------+
	
	@Override
	public String toString() {
		return "[ " + TYPE + " | " + NAME1 + " | " + NAME2 + " | " + CONTENT
				+ " | " + PAYLOAD + " ]";
	}
}
