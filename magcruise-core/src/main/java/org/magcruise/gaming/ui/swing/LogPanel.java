package org.magcruise.gaming.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.lang3.SerializationUtils;

@SuppressWarnings("serial")
public class LogPanel extends JPanel {
	private JTextArea outputTextArea;

	public LogPanel(String name, int width, int height) {
		outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel(name));

		JScrollPane scroll = new JScrollPane(outputTextArea);
		scroll.setPreferredSize(new Dimension(width, height));
		scroll.setBackground(Color.WHITE);
		add(scroll);

	}

	public synchronized boolean logMessage(String from, String to, String text) {
		if (from != null && to != null && (!text.equals(""))) {
			outputTextArea.insert(from + "->" + to + ": " + text + System.lineSeparator(), 0);
			return true;
		} else {
			return false;
		}
	}

	public synchronized void setLog(Serializable msg) {
		Serializable text = SerializationUtils.clone(msg);
		if (text == null) {
			text = "logged text is null";
		} else if (text.equals("")) {
			text = "logged text is nothing";
		}
		outputTextArea.insert(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + ": " + text
				+ System.lineSeparator(), 0);
		;

	}
}
