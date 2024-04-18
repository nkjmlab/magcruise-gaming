package org.magcruise.gaming.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class MessagePanel extends JPanel {
  private JPanel panel = new JPanel();

  private Map<Long, JLabel> labels = new HashMap<>();

  public MessagePanel(String name, int width, int height) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(new JLabel(name));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(Color.WHITE);

    JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scroll.setPreferredSize(new Dimension(width, height));
    scroll.setBackground(Color.WHITE);
    this.add(scroll);
  }

  public synchronized JLabel setMessage(long id, Serializable msg) {
    JLabel label = new JLabel();
    labels.put(id, label);
    label.setVerticalAlignment(JLabel.TOP);
    label.setOpaque(true);
    label.setBackground(Color.WHITE);
    panel.add(label, 0);
    panel.validate();

    String text;
    if (msg == null) {
      text = "logged text is null.";
    } else {
      text = msg.toString();
    }
    String date = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + ": <br>";
    String html =
        "<html><div style='color: #3a87ad; border: 6px solid #FFFFFF; background-color: #EEEEEE; width: 640px;'>"
            + date + text + "</div></html>";
    label.setText(html);
    return label;
  }

  public void removeMessage(long removeMessageId) {
    JLabel l = labels.get(removeMessageId);
    panel.remove(l);
    labels.remove(removeMessageId);
    panel.validate();
  }
}
