package cs342.rummy;

import java.util.Scanner;


public class ConsoleChatApp implements ChatServer.UserInterface, ChatClient.UserInterface {
	
	
	
	public static void main(String[] args) {
		new ConsoleChatApp();
	}
	
	
	
	private Scanner keyboard = new Scanner(System.in);
	
	public ConsoleChatApp() {
		System.out.println("::: CONSOLE CHATAPP :::\n");
		System.out.print("JOIN OR START SERVER? (J/S): ");
		if ((keyboard.nextLine().trim().toUpperCase() + " ").charAt(0) == 'S') {
			System.out.println("Starting a server...");
			ChatServer server = new ChatServer(8080, "LOL", this);
			while (true) {
				String input = keyboard.nextLine().trim().toUpperCase();
				if (input.charAt(0) == 'Q') {
					server.shutDown();
					break;
				}
				else
					System.out.println("Unrecognized input");
			}
		}
		else {
			System.out.println("Starting a client...");
			ChatClient client = new ChatClient("localhost", 8080, this);
			while (true) {
				String input = keyboard.nextLine().trim().toUpperCase();
				if (input.startsWith("-QUIT")) {
					client.shutDown();
					break;
				}
				else if (input.startsWith("-NAME ") && input.length() > 6)
					client.chatNameRequest(input.substring(6).trim());
				else if (input.startsWith("-JOIN ") && input.length() > 6)
					client.joinConvo(input.substring(6).trim());
				else if (input.startsWith("-LEAVE ") && input.length() > 7)
					client.leaveConvo(input.substring(7).trim());
				else if (input.startsWith("-SAY ") && input.length() > 5) {
					String inputSub = input.substring(5).trim();
					int colonIndex  = inputSub.indexOf(":");
					if(colonIndex != -1) {
						String name = inputSub.substring(0, colonIndex).trim();
						String content = inputSub.substring(colonIndex + 1, inputSub.length()).trim();
						client.sendChatMessage(name,content);
					}
				}
				else if (input.startsWith("-CREATE ") && input.length() > 8)
					client.startConvo(input.substring(8).trim());
			}
		}
		System.out.println("\n::: DONE :::");
	}
	
	@Override
	public void log(String text) {
		System.out.println(text);
	}

	@Override
	public void info(String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notice(String notice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleChatMessage(String where, String who, String what) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nameRequestResponse(String name, String response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void joinNotice(String who, String where, String context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveNotice(String who, String from, String context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conversationStartedNotice(String convo, String creator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conversationEndedNotice(String convo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientShutdownNotice(String context) {
		System.out.println("::: CLIENT HAS SHUT DOWN: " + context + " :::");
		System.exit(0);
	}

	@Override
	public void serverShutdownNotice(String context) {
		System.out.println("::: SERVER HAS SHUT DOWN: " + context + " :::");
		System.exit(0);
	}

	@Override
	public void handleRummyEvent(String who, String where, Object event) {
		// TODO Auto-generated method stub
		
	}
}
