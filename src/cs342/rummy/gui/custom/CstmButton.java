package cs342.rummy.gui.custom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import javax.swing.JButton;

public class CstmButton extends JButton{
	private Color BUTTON_BACKGROUND = new Color(47, 79, 79).darker().darker().darker();
	private Color BUTTON_FOREGROUND = new Color(255, 69, 0);
	private Color HOVER_BACKGROUND = BUTTON_BACKGROUND.brighter();
    private Color PRESSED_BACKGROUND = HOVER_BACKGROUND.brighter();

    public CstmButton() {
        this(null);
    }

    public CstmButton(String text) {
    	this(text, null);
    }
    
    public CstmButton(String text, String toolTip){
    	this(text, toolTip, null);
    }
    
    public CstmButton(String text, String toolTip, MouseListener lstnr){
    	 super(text);
         super.setContentAreaFilled(false);
         super.setBackground(BUTTON_BACKGROUND);
         super.setForeground(BUTTON_FOREGROUND);
         super.setFocusPainted(false);
         super.setToolTipText(toolTip);
         super.addMouseListener(lstnr);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed()) {
            g.setColor(PRESSED_BACKGROUND);
        } else if (getModel().isRollover()) {
            g.setColor(HOVER_BACKGROUND);
        } else {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }
}
