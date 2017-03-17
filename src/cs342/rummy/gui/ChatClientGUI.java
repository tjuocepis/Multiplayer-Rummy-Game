package cs342.rummy.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;

import cs342.rummy.ChatClient;
import cs342.rummy.gui.custom.*;
import cs342.rummy.gui.custom.UserInput.UserInputListener;
import cs342.rummy.gui.game.RummyGame;


@SuppressWarnings("serial")
public class ChatClientGUI extends JFrame implements EntryPanel.Listener,
													 UserInputListener,
													 ChatClient.UserInterface,
													 RummyGame.Listener,
													 WindowListener,
													 MouseListener,
													 ComponentListener {
	
	// +-----------+
	// | CONSTANTS |
	// ------------+
	
	private static final int
	LOGIN_WINDOW_WIDTH = 400,
	LOGIN_WINDOW_HEIGHT = 250;
	
	// +----------------+
	// | GUI COMPONENTS |
	// -----------------+
	
	private JPanel
	WINDOW_PANEL;
	
	private Container
	CONVO_CONTAINER,
	PEEPS_CONTAINER,
	RUMMY_CONTAINER,
	CHAT_CONTAINER;
	
	// +--------------+
	// | NEEDS REWORK |
	// +--------------+
	
	private int window_width = 1000;
	private int window_height = 600;
	private int left_panel_width = 200;
	private int right_panel_width = 200;
	
	private boolean consoleIncrease = true;
	private int console_height = 27;
	
	private LoginPanel loginPanel;
	
	private JPanel userPanel,chatPanel,chat,
				   convoPanel,convoControlPanel,
				   userControlPanel,chatInputPanel;
	private Console consolePanel;
	private CstmTextField convoTitle;
	private CstmButton joinConvo, startConvo, leaveConvo;
	
	private EntryPanel convoList;
	private HashMap<String, EntryPanel> peepLists;
	private JScrollPane convoListPane, peepListPane;
	
	private Color BACKGROUND = new Color(47, 79, 79);
	private String IP;
	private int PORT;
	private ChatClient CHAT_CLIENT;
	private String USERNAME = null;
	private ChatArea chatArea;
	private String currentConvo;
	private HashMap<String, RummyGame> rummyGames;
	
	ChatClientGUI(String ip, int port, Image icon) {
		super("Network Rummy");
		setIconImage(icon);
		IP = ip;
		PORT = port;
		setupGUI();
		CHAT_CLIENT = new ChatClient(IP, PORT, this);
	}
	
	private void setupGUI() {
		WINDOW_PANEL = new JPanel();
		setContentPane(WINDOW_PANEL);
		addWindowListener(this);
		rummyGames = new HashMap<String, RummyGame>();
		setupConsole();
		setupLoginView();
	}
	
	private void setupLoginView() {
		loginPanel = new LoginPanel();		
		WINDOW_PANEL.setLayout(new GridBagLayout());
		WINDOW_PANEL.add(loginPanel, new GridBagConstraints());
		WINDOW_PANEL.setBackground(Color.BLACK);
		setSize(LOGIN_WINDOW_WIDTH, LOGIN_WINDOW_HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void testNewChatView() {
		WINDOW_PANEL.removeAll();
		WINDOW_PANEL.setLayout(null);
		WINDOW_PANEL.addComponentListener(this);
		setupConvoPanel();
		convoPanel.setBounds(0, 0, left_panel_width, this.getHeight());
		setupUserPanel();
		userPanel.setBounds(this.getWidth() - right_panel_width, 0, right_panel_width, this.getHeight());
		setupChatPanel();
		chatPanel.setBounds(200, this.getHeight() - 100, this.getWidth() - 400, 100);
		setupConsole();
		consolePanel.setBounds(0, this.getHeight() - 27, this.getWidth(), 27);
		RUMMY_CONTAINER = new JPanel();
		WINDOW_PANEL.add(convoPanel);
		WINDOW_PANEL.add(RUMMY_CONTAINER);
		WINDOW_PANEL.add(chatPanel);
		WINDOW_PANEL.add(userPanel);
		WINDOW_PANEL.add(consolePanel);
		setSize(window_width, window_height);
		setResizable(true);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void setupChatPanel() {
		chatPanel = new JPanel();
		
		chatPanel.setLayout(new BorderLayout());
		chatPanel.setMaximumSize(new Dimension(400, 400));
		chatPanel.setMinimumSize(new Dimension(400, 400));
		chatPanel.setPreferredSize(new Dimension(400, 400));
		chatArea = new ChatArea();
		chat = new JPanel();
		chat.setLayout(new BorderLayout());
		chat.add(chatArea.SCROLL_PANE);
		
		chatInputPanel = new JPanel();
		chatInputPanel.setLayout(new BorderLayout());
		chatInputPanel.add(new UserInput(this));
		
		chatPanel.add(chat, BorderLayout.CENTER);
		chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
	}
	
	private void setupConvoPanel() {
		convoPanel = new JPanel();
		convoPanel.setLayout(new BoxLayout(convoPanel, BoxLayout.Y_AXIS));
		convoPanel.setBackground(BACKGROUND);
		convoList = new EntryPanel(this);
		convoList.PANEL.setBackground(BACKGROUND);
		convoList.PANEL.setForeground(new Color(255, 69, 0));
		
		convoList.enforceSingleSelection(true);
		
		convoControlPanel = new JPanel();
		convoTitle = new CstmTextField("", "Enter Convo Name");
		convoTitle.setPreferredSize(new Dimension(100, 27));
		convoTitle.setBackground(convoTitle.getBackground().brighter());
		convoTitle.setText(USERNAME + "'s Chat");
		joinConvo = new CstmButton("JOIN", "Join Convo");
		joinConvo.setPreferredSize(new Dimension(100, 27));
		joinConvo.setMaximumSize(new Dimension(200, 27));
		joinConvo.addMouseListener(this);
		startConvo = new CstmButton("START", "Start Convo");
		startConvo.setPreferredSize(new Dimension(100, 27));
		startConvo.setMaximumSize(new Dimension(200, 27));
		startConvo.addMouseListener(this);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		btnPanel.add(joinConvo);
		btnPanel.add(startConvo);
		convoControlPanel.setLayout(new BorderLayout());
		convoControlPanel.setMaximumSize(new Dimension(800, 54));
		convoControlPanel.setPreferredSize(new Dimension(200, 54));
		convoControlPanel.setMinimumSize(new Dimension(200, 54));
		convoControlPanel.add(convoTitle, BorderLayout.NORTH);
		convoControlPanel.add(btnPanel, BorderLayout.SOUTH);
		
		convoListPane = new JScrollPane(convoList.PANEL);
		convoPanel.add(convoListPane);
		
		convoPanel.add(convoControlPanel);
	}
	
	private void setupUserPanel() {
		userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		userPanel.setBackground(BACKGROUND);
		
		peepLists = new HashMap<String, EntryPanel>();
		peepListPane = new JScrollPane();
		
		userControlPanel = new JPanel();
		userControlPanel.setBackground(BACKGROUND);
		userControlPanel.setLayout(new BorderLayout());
		leaveConvo = new CstmButton("LEAVE", "Leave Convo");
		leaveConvo.addMouseListener(this);
		userControlPanel.add(leaveConvo, BorderLayout.CENTER);
		userControlPanel.setMaximumSize(new Dimension(100, 27));
		userControlPanel.setPreferredSize(new Dimension(100, 27));
		userControlPanel.setMinimumSize(new Dimension(100, 27));
		userPanel.add(peepListPane);
		userPanel.add(userControlPanel);
	}
	
	private void setupConsole() {
		consolePanel = new Console();
		consolePanel.addMouseListener(this);
	}

	private void updateLayout() {
		int W = WINDOW_PANEL.getWidth();
		int H = WINDOW_PANEL.getHeight() - console_height;
		
		final int
		sideWpref = 200, // side panels preferred width
		midWpref = 400, // middle panel preferred width
		sideWmin = 50,
		midWmin = 50,
		chatHmin = 150,
		gameWratio = 8, // game panel width ratio
		gameHratio = 5, // game panel height ratio
		gameHmin = midWmin * gameHratio / gameWratio;
		
		int	leftW = sideWpref;
		int rightW = sideWpref;
		
		if (W < leftW + rightW + midWpref) {
			leftW = (W - midWpref) / 2;
			rightW = (W - midWpref) - leftW;
			if (leftW < sideWmin) {
				leftW = sideWmin;
				rightW = sideWmin;
				if (W < leftW + rightW + midWmin) {
					leftW = (W - midWmin) / 2;
					rightW = (W - midWmin) - leftW;
				}
			}
		}
		
		int midW = W - leftW - rightW;
		int gameH = midW * gameHratio / gameWratio;
		int chatH = H - gameH;
		
		if (chatH < chatHmin) {
			chatH = chatHmin;
			gameH = H - chatH;
			if (gameH < gameHmin) {
				gameH = gameHmin;
				midW = gameH * gameWratio / gameHratio;
				chatH = H - gameH;
			}
			else
				midW = (H - chatH) * gameWratio / gameHratio;
			leftW = (W - midW) / 2;
			rightW = W - midW - leftW;
		}
		
		convoPanel.setBounds(0, 0, leftW, H);
		userPanel.setBounds(leftW + midW, 0, rightW, H);
		RUMMY_CONTAINER.setBounds(leftW, 0, midW, gameH);
		chatPanel.setBounds(leftW, gameH, midW, chatH);
		consolePanel.setBounds(0, H, W, console_height);
		validate();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() instanceof CstmButton) {
			CstmButton temp = (CstmButton) e.getSource();
			if (temp == joinConvo && currentConvo != null)
				CHAT_CLIENT.joinConvo(currentConvo);
			else if (temp == startConvo){
				CHAT_CLIENT.startConvo(convoTitle.getText().trim());
				convoTitle.setText("");
			} else if (temp == leaveConvo)
				CHAT_CLIENT.leaveConvo(currentConvo);
		} else if (e.getSource() == consolePanel) {
			if(consoleIncrease) {
				console_height = 81;
				consoleIncrease = false;
			} else {
				console_height = 27;
				consoleIncrease = true;
			}
			updateLayout();
		}
	}
	
	@Override
	public void sendClicked(String userInput) {
		if (currentConvo == null || currentConvo == "")
			return;
		EntryPanel peepList = peepLists.get(currentConvo);
		if (peepList != null) {
			List<String> selectedPeeps = peepList.getSelected();
			if (selectedPeeps.size() > 0) {
				for (String peep : selectedPeeps)
					if (!peep.equals(USERNAME))
						CHAT_CLIENT.sendChatMessage(peep, userInput);
				return;
			}
		}
		CHAT_CLIENT.sendChatMessage(currentConvo, userInput);
	}
	
	@Override
	public void entrySelected(EntryPanel list, String entry) {
		if (list == convoList) {
			currentConvo = entry;
			chatArea.showChat(entry);
			peepListPane.setViewportView(peepLists.get(entry).PANEL);
			if(rummyGames.containsKey(entry)){
				((RummyGame)RUMMY_CONTAINER).noLoop();
				WINDOW_PANEL.remove(RUMMY_CONTAINER);
				RummyGame rg = rummyGames.get(entry);
				RUMMY_CONTAINER = rg;
				WINDOW_PANEL.add(RUMMY_CONTAINER);
				rg.loop();
				updateLayout();
			}
		}
	}
	
	@Override
	public void log(String text) {
		//consolePanel.p(text);
	}
	
	@Override
	public void info(String info) {
		consolePanel.p(info);
	}

	@Override
	public void notice(String notice) {
		if (USERNAME == null)
			loginPanel.serverTalkLabel.setText(notice);
		consolePanel.p(notice);
	}
	
	@Override
	public void handleRummyEvent(String who, String where, Object event) {
		RummyGame rummyGame = rummyGames.get(where);
		rummyGame.handleRummyEvent(event);
	}

	
	@Override
	public void rummyEventOccured(Object event) {
		if (currentConvo != null)
			CHAT_CLIENT.sendRummyEvent(currentConvo, event);
	}
	
	@Override
	public void handleChatMessage(String who, String where, String what) {
		if (convoList.containsEntry(where) == false)
			conversationStartedNotice(where, who);
		
		if(who.toUpperCase().equals(USERNAME.toUpperCase()))
			chatArea.clientPrint(what, who, where);
		else
			chatArea.serverPrint(what, who, where);
	}

	@Override
	public void nameRequestResponse(String name, String response) {
		if (name == null)
			loginPanel.serverTalkLabel.setText(response);
		else {
			USERNAME = name;
			setTitle("Peep Chat : " + name);
			testNewChatView();
		}
	}

	@Override
	public void joinNotice(String who, String where, String context) {
		consolePanel.p(who + " joined " + where);
		EntryPanel peepList = peepLists.get(where);
		if (peepList != null) {
			peepList.addEntry(who);
			if (currentConvo != null && currentConvo.equals(where)) {
				peepListPane.setViewportView(peepLists.get(where).PANEL);
				if(who.equals(USERNAME)) {
					RummyGame rg = new RummyGame(USERNAME, this);
					rummyGames.put(where, rg);
					WINDOW_PANEL.remove(RUMMY_CONTAINER);
					RUMMY_CONTAINER = rg;
					WINDOW_PANEL.add(RUMMY_CONTAINER);
					updateLayout();
					rg.init();
				}
			}
		}
	}

	@Override
	public void leaveNotice(String who, String from, String context) {
		consolePanel.p(who + " left " + from);
		peepLists.get(from).removeEntry(who);
		if (currentConvo != null && currentConvo.equals(from))
			peepListPane.setViewportView(peepLists.get(from).PANEL);	
		if (rummyGames.containsKey(from))
			rummyGames.remove(from);
	}

	@Override
	public void conversationStartedNotice(String convo, String creator) {
		convoList.addEntry(convo);
		convoListPane.setViewportView(convoList.PANEL);		
		chatArea.createNewChat(convo);
		EntryPanel peepList = new EntryPanel(this);
		peepList.PANEL.setBackground(BACKGROUND);
		peepList.PANEL.setForeground(new Color(255, 69, 0));
		peepLists.put(convo, peepList);
		convoList.highlightEntry(convo, Color.RED);
		if(USERNAME.equals(creator) || currentConvo == null)
			convoList.selectEntry(convo);
	}
	
	@Override
	public void conversationEndedNotice(String convo) {
		convoList.removeEntry(convo);
		convoListPane.setViewportView(convoList.PANEL);
		chatArea.removeChat(convo);
		peepLists.remove(convo);
		if(rummyGames.containsKey(convo))
			rummyGames.remove(convo);
	}
	
	@Override
	public void clientShutdownNotice(String context) {
		ChatClientGUI.this.removeAll();
		ChatClientGUI.this.dispose();
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		CHAT_CLIENT.shutDown();
		ChatClientGUI.this.removeAll();
		ChatClientGUI.this.dispose();
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void componentHidden(ComponentEvent arg0) {}

	@Override
	public void componentMoved(ComponentEvent arg0) {}

	@Override
	public void componentResized(ComponentEvent arg0) {
		updateLayout();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {}

	@SuppressWarnings("serial")
	class LoginPanel extends JPanel {
		
		CstmTextField userNameTextField;
		CstmButton enterButton;
		CstmLabel userNameLabel, serverTalkLabel;
		
		LoginPanel() {
			super();
			setBackground(Color.BLACK);
			userNameTextField = new CstmTextField("","Enter your name");
			enterButton = new CstmButton("ENTER");
			userNameLabel = new CstmLabel("USER NAME", SwingConstants.CENTER);
			serverTalkLabel = new CstmLabel("", SwingConstants.CENTER);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			userNameTextField.setPreferredSize(new Dimension(200,30));
			userNameTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
			enterButton.setPreferredSize(new Dimension(200,30));
			enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			serverTalkLabel.setPreferredSize(new Dimension(450,30));
			serverTalkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			userNameTextField.addKeyListener(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) { }
					
					@Override
					public void keyReleased(KeyEvent e) { }
					
					@Override
					public void keyPressed(KeyEvent e) { 
						if(e.getKeyCode() == KeyEvent.VK_ENTER) {
							e.consume();
							CHAT_CLIENT.chatNameRequest(
									loginPanel.userNameTextField.getText().trim());
						}
					}
				});
			userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(userNameLabel);
		    add(userNameTextField);
		    add(enterButton);
		    add(serverTalkLabel);
			enterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CHAT_CLIENT.chatNameRequest(
							loginPanel.userNameTextField.getText().trim());
				}
			});
		}
	}
}
