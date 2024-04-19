package org.magcruise.gaming.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	private JPanel panel = new JPanel();
	private JLabel label = new JLabel();

	public ImagePanel(String name, int width, int height) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel header = new JLabel(name);
		add(header);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(width, height));
		label.setVerticalAlignment(JLabel.TOP);
		label.setOpaque(true);
		label.setBackground(Color.WHITE);
		panel.add(label, 0);
		panel.validate();
		add(panel);
	}

	public void setImage(long id, String msg, BufferedImage image) {
		label.setIcon(new ImageIcon(image, ""));
	}

}
