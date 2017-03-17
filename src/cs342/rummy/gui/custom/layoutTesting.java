package cs342.rummy.gui.custom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class layoutTesting extends JFrame implements ComponentListener {

	public static void main(String[] args) {
		new layoutTesting("DEMO").setVisible(true);
	}
	
	
	final JPanel PANEL;
	final Component LEFT, CENTER, pgreen, RIGHT;
	final JScrollPane spred, sporange;
	
	layoutTesting(String frameTitle) {
		super(frameTitle);
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(200, 200));
		
		PANEL = new JPanel();
		PANEL.setLayout(null);
		PANEL.addComponentListener(this);
		//PANEL.setLayout(new GridBagLayout());
		
		LEFT = makePanel(Color.RED);
		//JLabel lb1 = new JLabel("PUBLIC CHANNEL YO");
		//LEFT.add(lb1);
		//pred.setPreferredSize(new Dimension(0, 0));
		
		spred = new JScrollPane(LEFT);
		spred.setBounds(0, 0, 200, this.getHeight());
		
		CENTER = makePanel(Color.BLUE);
		CENTER.setBounds(200, 0, this.getWidth() - 400, this.getHeight() - 100);
		
		pgreen = makePanel(Color.GREEN);
		//pgreen.setPreferredSize(new Dimension(0, 100));
		//JScrollPane spgreen = new JScrollPane(pgreen);
		//spgreen.setPreferredSize(new Dimension(0,50));
		pgreen.setBounds(200, this.getHeight() - 100, this.getWidth() - 400, 100);
		
		RIGHT = makePanel(Color.ORANGE);
		//porange.setPreferredSize(new Dimension(200, 1000));
		
		sporange = new JScrollPane(RIGHT);
		sporange.setBounds(this.getWidth() - 200, 0, 200, this.getHeight());
		
		PANEL.add(spred);
		PANEL.add(CENTER);
		PANEL.add(pgreen);
		PANEL.add(sporange);
		
		/*GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		PANEL.add(spred, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		PANEL.add(pblue, c);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		
		PANEL.add(spgreen, c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		PANEL.add(sporange, c);		
		*/
		setContentPane(PANEL);
	}
	
	JPanel makePanel(Color c) {
		JPanel p = new JPanel();
		p.setBackground(c);
		return p;
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		int W = PANEL.getWidth();
		int H = PANEL.getHeight();
		
		System.out.println("RESIZED: " + W + "," + H);
		
		int L = 200;
		int R = 200;
		int T = 100;
		
		if (W < L + R + 100) {
			L = (W - 100) / 2;
			R = (W - 100) - L;
			if (L < 50) {
				L = 50;
				R = 50;
				if (W < L + R) {
					L = W / 2;
					R = W - L;
				}
			}
		}
		
		int CW = W - (L + R);
		T = CW * 3 / 4;
				
		if (H - T  < 100) {
			T = H - 100;
			CW = T * 4 / 3;
			L = (W - CW) / 2;
			R = W - CW - L;
		}
		
		
		spred.setBounds(0, 0, L, H);
		sporange.setBounds(W - R, 0, R, H);
		CENTER.setBounds(L, 0, CW, T);
		pgreen.setBounds(L, T, CW, H - T);
		validate();
		
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
