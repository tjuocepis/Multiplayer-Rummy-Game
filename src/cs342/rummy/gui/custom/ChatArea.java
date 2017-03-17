package cs342.rummy.gui.custom;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.*;

import java.awt.*;
import java.util.HashMap;

public class ChatArea {

	private ChatPane chatPane;
	private JScrollBar vertical;
	public final JScrollPane SCROLL_PANE;
	private HashMap<String, StyledDocument> CONVO_TO_DOC;
	
	public ChatArea() {
		SCROLL_PANE = new JScrollPane();
		CONVO_TO_DOC = new HashMap<String, StyledDocument>();
		chatPane = new ChatPane();
		SCROLL_PANE.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		SCROLL_PANE.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		SCROLL_PANE.setViewportView(chatPane);
		vertical = SCROLL_PANE.getVerticalScrollBar();
	}
	
	public void showChat(String entry) {
		chatPane.doc = CONVO_TO_DOC.get(entry);
		chatPane.setStyledDocument(chatPane.doc);
	}

	public void createNewChat(String convo) {
		CONVO_TO_DOC.put(convo, new DefaultStyledDocument());
	}

	public void removeChat(String convo) {
		CONVO_TO_DOC.remove(convo);
	}
	
	public void println(String text, String where) {
		print(text + "\n", where);
	}
	
	public void print(String text, String where) {
		chatPane.doc = CONVO_TO_DOC.get(where);
		chatPane.print(text);
		setScrollBarMinimum();
	}
	
	public void print(String text, String where, Color c) {
		chatPane.doc = CONVO_TO_DOC.get(where);
		chatPane.print(text, c);
		setScrollBarMinimum();
	}
	
	public void clientPrint(String line, String who, String where) {
		chatPane.doc = CONVO_TO_DOC.get(where);
		chatPane.clientPrint(line);
		setScrollBarMinimum();
	}

	public void serverPrint(String line, String who, String where) {
		chatPane.doc = CONVO_TO_DOC.get(where);
		chatPane.serverPrint(line, who);
		setScrollBarMinimum();
	}

	private void setScrollBarMinimum() {
		vertical.setValue( vertical.getMinimum());
	}
	
	private class ChatPane extends JTextPane {

		private static final long serialVersionUID = 1L;

		private StyledDocument doc;
		private Style style;
		private SimpleAttributeSet align;
		
		private final Color
		BACKGROUND = new Color(47, 79, 79),
		DEFAULT_TEXT_COLOR = new Color(255, 255, 255),
		CLIENT_TEXT_COLOR = new Color(255, 128, 64),
		SERVER_TEXT_COLOR = new Color(64, 128, 255);
		
		private Insets MARGINS = new Insets(5, 5, 5, 5);
		private String FONT_NAME = "Calibri";
		private int FONT_SIZE = 16;

		private ChatPane() {
			super();
			super.setEditorKit(new WrapEditorKit());
			super.setEditable(false);
			super.setBackground(BACKGROUND);
			super.setMargin(MARGINS);
			style = this.addStyle("STYLE", null);
			align = new SimpleAttributeSet();
			StyleConstants.setFontFamily(style, FONT_NAME);
			StyleConstants.setFontSize(style, FONT_SIZE);
			StyleConstants.setBold(style, true);
		}
		
		private void print(String text) {
			StyleConstants.setAlignment(align, StyleConstants.ALIGN_LEFT);
			StyleConstants.setForeground(style, DEFAULT_TEXT_COLOR);
			placeText(text);
		}
		
		private void print(String text, Color c) {
			StyleConstants.setAlignment(align, StyleConstants.ALIGN_LEFT);
			StyleConstants.setForeground(style, c);
			placeText(text);
		}
		
		private void clientPrint(String text) {
			StyleConstants.setAlignment(align, StyleConstants.ALIGN_RIGHT);
			StyleConstants.setForeground(style, CLIENT_TEXT_COLOR);
			placeText(text + "\n");
		}

		private void serverPrint(String text, String name) {
			StyleConstants.setAlignment(align, StyleConstants.ALIGN_LEFT);
			StyleConstants.setForeground(style, SERVER_TEXT_COLOR.brighter());
			placeText(name + ": ");
			StyleConstants.setForeground(style, SERVER_TEXT_COLOR);
			placeText(text + "\n");
		}

		private void placeText(String line) {
			try {
				doc.setParagraphAttributes(doc.getLength(), line.length(),
						align, false);
				doc.insertString(doc.getLength(), line, style);
			} catch (BadLocationException e) {
			}
		}
	}
}