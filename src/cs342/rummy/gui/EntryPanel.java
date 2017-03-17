package cs342.rummy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class EntryPanel {
	
	public final JPanel PANEL;
	private final TreeMap<String,Entry> treeEntries;
	private final HashMap<String,Entry> hashEntries;
	private final Listener LISTENER;
	
	private Color defaultColor = Color.BLACK;
	private Color selectColor = Color.GREEN;
	private Color hoverColor = Color.LIGHT_GRAY;
	private Color hoverSelectColor = Color.ORANGE;
	private Color textColor = Color.WHITE;
	private boolean singleSelection;
	private Entry lastSelected;
	
	EntryPanel(Listener listener) {
		PANEL = new JPanel();
		PANEL.setLayout(new BoxLayout(PANEL, BoxLayout.Y_AXIS));
		treeEntries = new TreeMap<String, Entry>();
		hashEntries = new HashMap<String, Entry>();
		LISTENER = listener;
	}
	
	public void enforceSingleSelection(boolean setting) {
		singleSelection = setting;
	}
	
	public boolean addEntry(String entryName) {
		return addEntry(entryName, 1, defaultColor);
	}
	
	public boolean addEntry(String entryName, Color color) {
		return addEntry(entryName, 1, color);
	}
	
	public boolean addEntry(String entryName, int sortingLevel) {
		return addEntry(entryName, sortingLevel, defaultColor);
	}
	
	public boolean addEntry(String entryName, int sortingLevel, Color color) {
		if (hashEntries.containsKey(entryName))
			return false;
		new Entry(entryName, sortingLevel, color);
		updatePanel();
		return true;
	}
	
	public boolean containsEntry(String entryName) {
		return hashEntries.containsKey(entryName);
	}
	
	public List<String> getSelected() {
		List<String> selection = new ArrayList<String>();
		for (Entry entry : treeEntries.values())
			if (entry.selected)
				selection.add(entry.NAME);
		return selection;
	}
	
	public boolean removeEntry(String entryName) {
		Entry entry = hashEntries.remove(entryName);
		if (entry == null)
			return false;
		treeEntries.remove(entry.priority + entryName);
		updatePanel();
		return true;
	}
	
	public boolean setPrefix(String entryName, String pfix) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.setPrefix(pfix);
		return true;
	}
	
	public boolean setSuffix(String entryName, String sfix) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.setSuffix(sfix);
		return true;
	}
	
	public void setDefaultColor(Color c) {
		defaultColor = c;
		for (Entry entry : treeEntries.values())
			if (!entry.selected)
				entry.setBackground(defaultColor);
	}
	
	public void setSelectColor(Color c) {
		selectColor = c;
		for (Entry entry : treeEntries.values())
			if (entry.selected)
				entry.setBackground(selectColor);
	}
	
	public void setHoverColor(Color c) {
		hoverColor = c;
	}
	
	public void setHoverSelectColor(Color c) {
		hoverSelectColor = c;
	}
	
	public boolean setEntryColor(String entryName, Color c) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.setColor(c);
		return true;
	}
	
	public boolean selectEntry(String entryName) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.select();
		return true;
	}

	public boolean deselectEntry(String entryName) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.deselect();
		return true;
	}
	
	public boolean highlightEntry(String entryName, Color c) {
		Entry entry = hashEntries.get(entryName);
		if (entry == null)
			return false;
		entry.setBackground(c);
		return true;
	}
	
	private void updatePanel() {
		PANEL.removeAll();
		for(Entry entry : treeEntries.values())
			PANEL.add(entry);
		PANEL.add(Box.createVerticalGlue());
	}
	
	
	
	@SuppressWarnings("serial")
	private class Entry extends JPanel implements MouseListener {
		
		private final JLabel LABEL;
		private final String NAME;
		private final int priority;
		private String prefix;
		private String suffix;
		private Color color;
		private boolean selected;
		
		Entry(String entryName, int p, Color c) {
			super();
			NAME = entryName;
			priority = p;
			prefix = "";
			suffix = "";
			color = c;
			setBackground(color);
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			LABEL = new JLabel(NAME);
			LABEL.setForeground(textColor);
			LABEL.setFont(new Font(getFont().getName(), Font.BOLD, 25));
			setMaximumSize(new Dimension(99999, 50));
			setPreferredSize(new Dimension(100, 50));
			setMinimumSize(new Dimension(1, 50));
			add(Box.createHorizontalGlue());
			add(LABEL);
			add(Box.createHorizontalGlue());
			hashEntries.put(entryName, this);
			treeEntries.put(priority + entryName, this);
			addMouseListener(this);
		}
		
		private void select() {
			if (selected)
				return;
			if (singleSelection && lastSelected != null) {
				lastSelected.selected = false;
				lastSelected.setBackground(color);
			}
			selected = true;
			lastSelected = this;
			setBackground(selectColor);
			LISTENER.entrySelected(EntryPanel.this, NAME);
		}
		
		private void deselect() {
			if (!selected || singleSelection)
				return;
			selected = false;
			setBackground(color);
		}
		
		private void setColor(Color c) {
			color = c;
			setBackground(color);
		}
		
		private void setPrefix(String pfix) {
			prefix = pfix;
			LABEL.setText(prefix + NAME + suffix);
		}
		
		private void setSuffix(String sfix) {
			suffix = sfix;
			LABEL.setText(prefix + NAME + suffix);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			if (selected)
				deselect();
			else
				select();
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (selected)
				setBackground(hoverSelectColor);
			else
				setBackground(hoverColor);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (selected)
				setBackground(selectColor);
			else
				setBackground(color);
		}
	}



	public interface Listener {
		public void entrySelected(EntryPanel list, String entry);
	}
}
