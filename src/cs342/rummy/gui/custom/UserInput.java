package cs342.rummy.gui.custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

public class UserInput extends JPanel implements MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ScrolableInputPane sip;
	private CstmButton SEND_BTN;
	private UserInputListener LISTENER;
	
	public UserInput(UserInputListener listener) {
		super();
		LISTENER = listener;
		sip = new ScrolableInputPane();
		SEND_BTN = new CstmButton("SEND", "Send msg");
		SEND_BTN.addMouseListener(this);
		this.setLayout(new BorderLayout());
		this.add(sip);
		this.add(SEND_BTN, BorderLayout.EAST);
	}
	
	class ScrolableInputPane extends JScrollPane {

		private static final long serialVersionUID = 1L;
		private CstmTextPane cstmPane;

		private ScrolableInputPane() {
			super();
			cstmPane = new CstmTextPane();
			this.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
			this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			this.setViewportView(cstmPane);
		}
	}


	class CstmTextPane extends JTextPane {
		private static final long serialVersionUID = 1L;
		private Color BACKGROUND = new Color(47, 79, 79);
		private Color TEXT_COLOR = new Color(255, 69, 0);
		private Font TEXT_FONT = new Font("Calibri", Font.BOLD, 14);

		private CstmTextPane() {
			super();
			super.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) { }
				
				@Override
				public void keyReleased(KeyEvent e) { }
				
				@Override
				public void keyPressed(KeyEvent e) { 
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						e.consume();
						SEND_BTN.doClick();
						mouseClicked(null);
					}
				}
			});
			this.setBackground(BACKGROUND);
			this.setForeground(TEXT_COLOR);
			this.setFont(TEXT_FONT);
			this.setEditorKit(new WrapEditorKit());
		}

		@Override
		public String getText() {
			String retVal = super.getText();
			this.setText(null);
			return retVal;
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		String txt = sip.cstmPane.getText().trim();
		if(txt != null && (txt.equals("") == false))
			LISTENER.sendClicked(txt);
			
	}
	@Override
	public void mouseEntered(MouseEvent e) { }
	@Override
	public void mouseExited(MouseEvent e) { }
	@Override
	public void mousePressed(MouseEvent e) { }
	@Override
	public void mouseReleased(MouseEvent e) { }
	
	public interface UserInputListener {
		public void sendClicked(String userInput);
	}
}


