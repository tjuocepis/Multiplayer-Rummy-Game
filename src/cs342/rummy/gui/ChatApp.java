package cs342.rummy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingConstants;

import cs342.network.PublicIP;
import cs342.rummy.ChatServer;
import cs342.rummy.gui.custom.*;

import javax.swing.BoxLayout;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import java.awt.FlowLayout;

public class ChatApp extends JFrame implements MouseListener, WindowListener {
	
	// +-----------+
	// | CONSTANTS |
	// ------------+
	
	private static final Image ICON;
	
	static {
		Image icon = null;
		try {
			icon = ImageIO.read((new File("img/icon/peep.png")));
		} catch (IOException e) {
			System.out.println("!!! >>> UNABLE TO LOAD THE PROGRAM ICON: " + e);
		}
		ICON = icon;
	}
	
	// +----------------+
	// | GUI COMPONENTS |
	// -----------------+
	
	private JPanel WINDOW_PANEL;
	
	// +--------------+
	// | NEEDS REWORK |
	// +--------------+
	
	private Color PANEL_BACKGROUND = new Color(0, 0, 0);
	private String ipAdrs;
	private JPanel SERVER_CLIENT_START;
	private JPanel SERVER;
	private JPanel CLIENT;
	private JTabbedPane RUNNING_SERVERS;
	private CstmTextField serverPort, hostName, clientPort;
	private CstmButton startClient, startServer;
	private JSplitPane HORIZONTAL_SPLIT;
	private List<ServerPanel> servers;
	private CstmLabel serverDebugLbl;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatApp frame = new ChatApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChatApp() {
		super("Peep Chat Public IP : " + PublicIP.get());
		setIconImage(ICON);
		
		servers = new ArrayList<ServerPanel>();
		ipAdrs = PublicIP.get();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(700,600));
		setResizable(true);
		setLocationRelativeTo(null);
		setBackground(PANEL_BACKGROUND);
		setupServerClientStart();
		setupRunningServers();
		HORIZONTAL_SPLIT = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
									SERVER_CLIENT_START , RUNNING_SERVERS);
		getContentPane().add(HORIZONTAL_SPLIT);
	}
	
	private void setupRunningServers() {
		RUNNING_SERVERS = new JTabbedPane(JTabbedPane.TOP);
		RUNNING_SERVERS.setBackground(Color.BLACK);
	}
	
	private void setupServerClientStart() {
		SERVER_CLIENT_START = new JPanel();
		SERVER_CLIENT_START.setLayout(new BoxLayout(SERVER_CLIENT_START, BoxLayout.X_AXIS));
		SERVER_CLIENT_START.setMinimumSize(new Dimension(700,100));
		SERVER = new JPanel();
		SERVER.setBackground(Color.BLACK);
		SERVER_CLIENT_START.add(SERVER);
		SERVER.setLayout(null);
		
		CstmLabel lblNewLabel = new CstmLabel("PORT:", SwingConstants.LEFT);
		lblNewLabel.setBounds(10, 11, 46, 14);
		SERVER.add(lblNewLabel);
		
		serverDebugLbl = new CstmLabel("", SwingConstants.LEFT);
		serverDebugLbl.setBounds(10, 38, 250, 14);
		SERVER.add(serverDebugLbl);
		
		serverPort = new CstmTextField("8080", "",SwingConstants.CENTER);
		serverPort.setBounds(73, 7, 63, 20);
		SERVER.add(serverPort);
		serverPort.setColumns(10);
		
		startServer = new CstmButton("Start Server");
		startServer.setBounds(10, 58, 126, 23);
		startServer.addMouseListener(this);
		SERVER.add(startServer);
		
		CLIENT = new JPanel();
		CLIENT.setBackground(Color.BLACK);
		SERVER_CLIENT_START.add(CLIENT);
		CLIENT.setLayout(null);
		
		CstmLabel hostLbl = new CstmLabel("HOST:", SwingConstants.LEFT);
		hostLbl.setBounds(10, 11, 48, 14);
		CLIENT.add(hostLbl);
		
		hostName = new CstmTextField("localhost", "", SwingConstants.CENTER);
		hostName.setBounds(65, 8, 86, 20);
		CLIENT.add(hostName);
		hostName.setColumns(10);
		
		CstmLabel clientPortLbl = new CstmLabel("PORT:", SwingConstants.LEFT);
		clientPortLbl.setBounds(10, 35, 48, 14);
		CLIENT.add(clientPortLbl);
		
		clientPort = new CstmTextField("8080", "",SwingConstants.CENTER);
		clientPort.setBounds(65, 32, 86, 20);
		CLIENT.add(clientPort);
		clientPort.setColumns(10);
		
		startClient = new CstmButton("Start Client");
		startClient.setBounds(10, 60, 141, 23);
		startClient.addMouseListener(this);
		CLIENT.add(startClient);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() instanceof CstmButton) {
			CstmButton temp = (CstmButton)e.getSource();
			if(temp == startServer){
				int port = 0;
				String host = "localhost";
				try{
					port = Integer.parseInt(serverPort.getText().trim());
					if(isPortOpen(port)) {
						createServerPanel(port);
						new ChatClientGUI(host, port, ICON);
						serverDebugLbl.setText("");
					} else {
						serverDebugLbl.setText("PORT "+port+" IS IN USE!");
					}
				} catch(NumberFormatException ex) { }
			} else if(temp == startClient) {
				int port = 0;
				String host = null;
				try{
					port = Integer.parseInt(clientPort.getText().trim());
					host = hostName.getText().trim();
					if(host != null && (host.equals("") == false)) 
						new ChatClientGUI(host, port, ICON);
				} catch(NumberFormatException ex) { }
			}
		}
	}

	private void createServerPanel(int port) {
		ServerPanel server = new ServerPanel(port, ipAdrs);
		servers.add(server);
		RUNNING_SERVERS.addTab(server.serverName, server);
		server.myIndex = RUNNING_SERVERS.getTabCount() - 1;
		RUNNING_SERVERS.setSelectedIndex(server.myIndex);
	}
	

	@Override
	public void windowClosed(WindowEvent e) {
		for(ServerPanel server: servers)
			server.shutDown();
		ChatApp.this.removeAll();
		ChatApp.this.dispose();
	}
	
	private static boolean isPortOpen(int port) {
		boolean portTaken = false;
	    ServerSocket socket = null;
	    try {
	        socket = new ServerSocket(port);
	        portTaken = true;
	    } catch (IOException e) {
	        portTaken = false;
	    } finally {
	        if (socket != null)
	            try {
	                socket.close();
	            } catch (IOException e) {}
	    }
	    return portTaken;
	}
	
	class ServerPanel extends JPanel implements ChatServer.UserInterface{
		private static final long serialVersionUID = 1L;
		final int PORT;
		ChatServer server;
		final String serverName;
		final String IP;
		private Console CONSOLE;
		int myIndex;
		
		ServerPanel(int port, String ip) {
			super();
			serverName = "Server: " + port;
			PORT = port;
			IP = ip;
			CONSOLE = new Console();
			this.setLayout(new BorderLayout());
			this.add(simplePanel(), BorderLayout.NORTH);
			this.add(CONSOLE, BorderLayout.CENTER);
			startServer();
		}
		
		private void startServer() {
			server = new ChatServer(PORT, serverName, this);
		}

		JPanel simplePanel() {
			JPanel panel = new JPanel();
			panel.setBackground(Color.BLACK);
			panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
			
			
			CstmButton btnNewButton = new CstmButton("Close Server");
			btnNewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					server.shutDown();
					RUNNING_SERVERS.remove(myIndex);
					servers.remove(this);
					for(ServerPanel server : servers)
						if(server.myIndex > myIndex)
							server.myIndex--;
				}
			});
			panel.add(btnNewButton);
			
			CstmLabel lblStartTime = new CstmLabel("START TIME: " + new Date(), SwingConstants.LEFT);
			panel.add(lblStartTime);
			
			CstmLabel lblIp = new CstmLabel("IP: " + IP, SwingConstants.LEFT);
			panel.add(lblIp);
			
			CstmLabel lblPort = new CstmLabel("PORT: " + PORT, SwingConstants.LEFT);
			panel.add(lblPort);

			return panel;
		}
		
		private void shutDown(){
			server.shutDown();
		}
		
		@Override
		public void serverShutdownNotice(String context) {
			CONSOLE.p(context);
		}
		@Override
		public void log(String text) {
			CONSOLE.p(text);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosing(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
}
