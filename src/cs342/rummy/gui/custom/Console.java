package cs342.rummy.gui.custom;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console extends JScrollPane {

	private static final long serialVersionUID = 1L;
	private ChatPane chatPane;
	
	public Console() {
		super();
		chatPane = new ChatPane();
		this.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.setViewportView(chatPane);
	}
	
	public void p(String line) {
		chatPane.serverPrint(line);
	}
	@Override
	public void addMouseListener(MouseListener ml) {
		super.addMouseListener(ml);
		chatPane.addMouseListener(ml);
	}
	
	private class ChatPane extends JTextPane {

		private static final long serialVersionUID = 1L;
		private StyledDocument doc;
		private Style style;
		private Color BACKGROUND = new Color(47, 79, 79);
		private Color TEXT_COLOR = new Color(255, 69, 0);
		private Insets MARGINS = new Insets(0, 5, 5, 5);
		private String FONT_NAME = "Calibri";
		private int FONT_SIZE = 16;

		private ChatPane() {
			super();
			super.setEditorKit(new WrapEditorKit());
			super.setEditable(false);
			super.setBackground(BACKGROUND);
			super.setMargin(MARGINS);
			doc = this.getStyledDocument();
			style = this.addStyle("STYLE", null);
			StyleConstants.setFontFamily(style, FONT_NAME);
			StyleConstants.setFontSize(style, FONT_SIZE);
			StyleConstants.setBold(style, true);
		}

		private void serverPrint(String line) {
			StyleConstants.setForeground(style, TEXT_COLOR);
			print(line);
		}

		private void print(String line) {
			try {
				doc.insertString(0, line + "\n", style);
			} catch (BadLocationException e) {
			}
		}
	}
}
