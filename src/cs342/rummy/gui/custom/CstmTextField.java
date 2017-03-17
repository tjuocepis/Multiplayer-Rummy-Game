package cs342.rummy.gui.custom;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CstmTextField extends JTextField{
	
	private Color BACKGROUND = new Color(47, 79, 79);
	private Color FOREGROUND = new Color(255, 69, 0);
	private Font TEXT_FONT = new Font("Calibri", Font.BOLD, 16);
	
	public CstmTextField(String txt, String toolTip){
		this(txt, toolTip, 0);
	}
	
	public CstmTextField(String txt, String toolTip, int column){
		super();
		super.setText(txt);
		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setForeground(FOREGROUND);
		super.setToolTipText(toolTip);
		super.setFont(TEXT_FONT);
		if(column == 0)
			super.setColumns(4);
		else
			super.setColumns(column);
		super.setBackground(BACKGROUND);
	}
}
