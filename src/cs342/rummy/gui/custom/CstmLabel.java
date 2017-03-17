package cs342.rummy.gui.custom;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

public class CstmLabel extends JLabel{
	
	private Font LABEL_TEXT_FONT = new Font("Calibri", Font.BOLD, 16);
	private Color FOREGROUND = new Color(255, 69, 0);
	private Color BACKGROUND = new Color(0, 0, 0);
	
	public CstmLabel(String text ,int alignment){
		super(text);
		super.setHorizontalAlignment(alignment);
		super.setForeground(FOREGROUND);
		super.setFont(LABEL_TEXT_FONT);
		super.setBackground(BACKGROUND);
	}

}
