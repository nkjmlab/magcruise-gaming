package org.magcruise.gaming.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import org.magcruise.gaming.executor.RequestToRemoveMessage;
import org.magcruise.gaming.executor.RequestToShowImage;
import org.magcruise.gaming.executor.api.message.JoinInGame;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.ui.DefaultAutoInputer;
import org.magcruise.gaming.ui.api.RequesterToUI;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.model.Form;
import org.magcruise.gaming.ui.model.input.CheckboxInput;
import org.magcruise.gaming.ui.model.input.Input;
import org.magcruise.gaming.ui.model.input.NumberInput;
import org.magcruise.gaming.ui.model.input.RadioInput;
import org.magcruise.gaming.ui.model.input.TextAreaInput;
import org.magcruise.gaming.ui.model.input.TextInput;

@SuppressWarnings("serial")
public class SwingGui extends JFrame implements RequesterToUI {

  private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private MessagePanel msgPanel;
  private ImagePanel imgPanel;

  public SwingGui() {
    this("MAGCruise Logs", 640, 768);
  }

  public SwingGui(String name, int width, int height) {
    super(name);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.X_AXIS);
    getContentPane().setLayout(layout);
    setSize(width, height);
    addMessagePanel(new MessagePanel("All Messages", width, height));
    addImagePanel(new ImagePanel("Image", width, height));
  }

  @Override
  public void initialize() {
    setVisible(true);
  }

  private String wrapLabelTag(String e) {
    String start = "<div style='width: 600px;'>";
    String end = "</div>";
    return start + e + end;
  }

  private String wrapMsgTag(String e) {
    String start =
        "<div style='width:600px; color:#9b7a42; border:1px solid #fbeed5; background-color:#fcf8e3;'>";
    String end = "</div>";
    return start + e + end;
  }

  private String wrapHtmlTag(String e) {
    String start = "<html><body style='width: 600px; '>";
    String end = "</body></html>";
    return start + e + end;
  }

  public synchronized void addPanel(Component panel) {
    getContentPane().add(panel);
    pack();
  }

  public synchronized void addMessagePanel(MessagePanel panel) {
    getContentPane().add(panel);
    pack();
    this.msgPanel = panel;
  }

  public synchronized void addImagePanel(ImagePanel panel) {
    getContentPane().add(panel);
    pack();
    this.imgPanel = panel;
  }

  public synchronized void log(Serializable msg) {
    log.debug(msg);
  }

  class JoinDialog extends InputDialog {

    private String operatorId;

    JoinDialog(ProcessId processId, String msg, ActorName playerName, String operatorId) {
      super(processId, msg, playerName);
      this.operatorId = operatorId;
      addSubmitButton("OK");
      setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      GameExecutorManager.getInstance().request(processId, new JoinInGame(playerName, operatorId));
      setVisible(false);
    }

  }


  class CompositDialog extends InputDialog {

    final Form form;
    final Map<String, List<JRadioButton>> radioButtonsMap = new HashMap<>();
    final Map<String, List<JCheckBox>> checkBoxButtonsMap = new HashMap<>();
    final Map<String, List<String>> valsMap = new HashMap<>();

    final Map<String, JTextArea> textAreas = new HashMap<>();

    public CompositDialog(ProcessId processId, String msg, ActorName playerName, Form inputs) {
      super(processId, msg, playerName);
      this.form = inputs;
      for (Input input : inputs.getInputs()) {
        String decisonMsg = wrapHtmlTag(wrapLabelTag(input.getLabel()));
        String decisonName = input.getName().toString();

        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        scrollPanel.add(p);

        p.add(new JLabel(decisonMsg));

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);

        if (input instanceof NumberInput || input instanceof TextInput
            || input instanceof TextAreaInput) {

          String initVal = input.getValue() == null ? null : input.getValue().toString();
          JTextArea textArea = createTextArea(initVal);
          textAreas.put(decisonName, textArea);
          panel.add(textArea);

        } else if (input instanceof RadioInput) {
          RadioInput radioInput = (RadioInput) input;
          String initVal = input.getValue().toString();

          List<String> labels = radioInput.getOptionLabels();
          List<String> vals = radioInput.getOptionValues();
          List<JRadioButton> radios = createRadios(labels, vals, initVal);
          radioButtonsMap.put(decisonName, radios);
          valsMap.put(decisonName, vals);
          for (JRadioButton radio : radios) {
            panel.add(radio);
          }
        } else if (input instanceof CheckboxInput) {
          CheckboxInput checkBoxInput = (CheckboxInput) input;

          List<String> labels = checkBoxInput.getOptionLabels();
          List<String> vals = checkBoxInput.getOptionValues();
          @SuppressWarnings("unchecked")
          List<String> value = (List<String>) checkBoxInput.getValue();
          List<JCheckBox> checkBoxes = createCheckboxes(labels, vals, value);
          checkBoxButtonsMap.put(decisonName, checkBoxes);
          valsMap.put(decisonName, vals);
          for (JCheckBox checkBox : checkBoxes) {
            panel.add(checkBox);
          }

        } else {
          log.error("Invalid form type: " + input);
        }
        scrollPanel.add(panel);
      }
      addSubmitButton("Submit");
      setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

      for (String decisionName : textAreas.keySet()) {
        form.setInputVal(decisionName, textAreas.get(decisionName).getText());
      }
      for (String decisionName : radioButtonsMap.keySet()) {
        List<JRadioButton> radios = radioButtonsMap.get(decisionName);
        List<String> vals = valsMap.get(decisionName);
        for (JRadioButton b : radios) {
          if (b.isSelected()) {
            int indexOfselectedButton = radios.indexOf(b);
            form.setInputVal(decisionName, vals.get(indexOfselectedButton));
            break;
          }
        }
      }
      for (String decisionName : checkBoxButtonsMap.keySet()) {
        List<JCheckBox> radios = checkBoxButtonsMap.get(decisionName);
        List<String> vals = valsMap.get(decisionName);
        List<Serializable> result = new ArrayList<>();
        for (JCheckBox b : radios) {
          if (b.isSelected()) {
            int indexOfselectedButton = radios.indexOf(b);
            result.add(vals.get(indexOfselectedButton));
          }
        }
        form.setInputVal(decisionName, (Serializable) result);
      }
      if (!form.validate()) {
        form.setLabel(
            form.getLabel() + "<br>" + "Input value is invalied. " + form.invalidMessage());
        request(processId, new RequestToInput(form.getId(), form.getLabel(), roundnum,
            playerName.toString(), form.toInputToUIs(), false));
      } else {
        GameExecutorManager.getInstance().request(processId,
            new ReceiveInput(form.getId(), playerName.toString(), form.toInputFromWebUIs()));
      }
      setVisible(false);
    }
  }

  abstract class InputDialog extends JDialog implements ActionListener {
    protected int roundnum;
    protected ActorName playerName;
    protected JPanel scrollPanel = new JPanel();
    protected ProcessId processId;

    InputDialog(ProcessId processId, String msg, ActorName playerName) {
      this.processId = processId;
      this.playerName = playerName;

      getContentPane().setBackground(Color.WHITE);
      setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

      scrollPanel.setBackground(Color.WHITE);
      scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
      add(scrollPanel);
      JPanel panel = new JPanel();
      JLabel label = new JLabel(msg);
      panel.add(label);
      panel.setBackground(Color.WHITE);

      scrollPanel.add(panel);
      setModal(false);
      toFront();

      JScrollPane scroll = new JScrollPane(scrollPanel);
      scroll.setBackground(Color.WHITE);
      scroll.setBorder(new LineBorder(Color.WHITE, 0));
      getContentPane().add(scroll);

    }

    protected void addSubmitButton(String label) {
      JButton submit = new JButton(label);
      JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);

      panel.add(submit);
      getRootPane().setDefaultButton(submit);
      add(panel);
      submit.addActionListener(this);
      setSize(getPreferredSize());
      pack();
    }

    protected List<JRadioButton> createRadios(List<String> labels, List<String> vals,
        String initVal) {

      List<JRadioButton> radios = new ArrayList<>();
      ButtonGroup group = new ButtonGroup();
      for (int i = 0; i < labels.size(); i++) {
        String label = labels.get(i);
        JRadioButton radio = new JRadioButton(label);
        radio.setOpaque(false);
        if (vals.get(i).equals(initVal)) {
          radio.setSelected(true);
        }

        radios.add(radio);
        group.add(radio);
      }
      return radios;
    }

    protected List<JCheckBox> createCheckboxes(List<String> labels, List<String> vals,
        List<String> initVal) {

      List<JCheckBox> checboxes = new ArrayList<>();
      for (int i = 0; i < labels.size(); i++) {
        String label = labels.get(i);
        JCheckBox element = new JCheckBox(label);
        element.setOpaque(false);
        if (initVal.contains(vals.get(i))) {
          element.setSelected(true);
        }

        checboxes.add(element);
      }
      return checboxes;
    }

    protected JTextArea createTextArea(String initVal) {
      JTextArea textArea = new JTextArea(initVal, 4, 70);
      textArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
      textArea.setLineWrap(true);
      return textArea;
    }

  }

  @Override
  public SConstructor<? extends SwingGui> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), getWidth(), getHeight());
  }

  @Override
  public synchronized Serializable request(ProcessId processId, RequestToUI msg) {
    if (msg instanceof RequestToAssignOperator) {
      RequestToAssignOperator rmsg = (RequestToAssignOperator) msg;
      return new JoinDialog(processId, rmsg.getPlayerName() + "として参加します．",
          ActorName.of(rmsg.getPlayerName()), rmsg.getOperatorId());
    } else if (msg instanceof RequestToInput) {
      RequestToInput rmsg = (RequestToInput) msg;

      if (rmsg.isAutoInput()) {
        DefaultAutoInputer.requestAutoInput(processId, rmsg);
        return null;
      }

      ActorName toPlayerName = ActorName.of(rmsg.getPlayerId());

      String message = wrapHtmlTag(
          wrapMsgTag(toPlayerName + "の入力 (Command Id: " + rmsg.getId() + ") " + rmsg.getLabel()));
      return new CompositDialog(processId, message, toPlayerName,
          new Form(rmsg.getId(), rmsg.getLabel(), rmsg.getInputs()));
    } else if (msg instanceof RequestToShowMessage) {
      RequestToShowMessage rmsg = (RequestToShowMessage) msg;
      msgPanel.setMessage(rmsg.getId(),
          "@" + rmsg.getPlayerName() + ": " + rmsg.getMessage().getMessage());
    } else if (msg instanceof RequestToShowImage) {
      RequestToShowImage rmsg = (RequestToShowImage) msg;
      imgPanel.setImage(rmsg.getId(), "@" + rmsg.getPlayerName() + ": ", rmsg.getImage());
    } else if (msg instanceof RequestToRemoveMessage) {
      RequestToRemoveMessage rmsg = (RequestToRemoveMessage) msg;
      msgPanel.removeMessage(rmsg.getRemoveMessageId());
    }
    return null;
  }

  @Override
  public String getExpression() {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public void requestToRegisterGameSession(ProcessId processId) {
    // TODO 自動生成されたメソッド・スタブ

  }

}
