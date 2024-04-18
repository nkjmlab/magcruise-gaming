package org.magcruise.gaming.executor;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.message.JoinInGame;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.executor.api.message.ShutdownRequest;
import org.magcruise.gaming.executor.api.message.UnexpectedShutdownRequest;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.MessageBox;
import org.magcruise.gaming.model.game.MessageBoxes;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.model.game.message.GameMessage;
import org.magcruise.gaming.ui.api.RequesterToUI;
import org.magcruise.gaming.ui.api.message.NotifyEndOfGame;
import org.magcruise.gaming.ui.api.message.NotifyEndOfRound;
import org.magcruise.gaming.ui.api.message.NotifyGameRecord;
import org.magcruise.gaming.ui.api.message.NotifyJoinInGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfRound;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.model.Form;
import org.magcruise.gaming.ui.model.attr.HtmlAttribute;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.util.Base64Utils;
import gnu.mapping.Procedure;
import gnu.mapping.Symbol;

public class GameMessenger {

  protected static Logger log = LogManager.getLogger();

  private Context context;
  private GameRecorder recorder;

  private RequesterToUI requester;

  private ProcessId processId;
  private MessageBoxes<ActorName, Message> systemMessageBoxes = new MessageBoxes<>();

  private GameExecutor gameExecutor;
  private ExecutorService messengerExecutorService =
      Executors.newCachedThreadPool(BasicThreadFactory.builder("msg-handler").build());
  private ExecutorService receivedInputExecutorService = Executors
      .newSingleThreadExecutor(BasicThreadFactory.builder("recieved-input-msg", false).build());

  public GameMessenger(GameExecutor gameExecutor, ProcessId processId, Context context,
      GameRecorder recorder, RequesterToUI requester) {
    this.gameExecutor = gameExecutor;
    this.processId = processId;
    this.context = context;
    this.recorder = recorder;
    this.requester = requester;
  }

  public synchronized void sendMessage(Message msg) {

    if (msg instanceof ReceiveInput) {
      try {
        log.info("Received input={}", msg);
        receivedInputExecutorService.submit(() -> {
          ReceiveInput rmsg = (ReceiveInput) msg;
          systemMessageBoxes.offer(ActorName.of(rmsg.getPlayerName()), rmsg);
          recorder.insertInput(rmsg.getRequestId(), processId, context.getRoundnum(),
              Symbol.parse(rmsg.getPlayerName()), rmsg.getParameters());
        });
      } catch (RejectedExecutionException e) {
        log.info("This game has been already shutdown. {}", msg);
      }
      return;
    }
    messengerExecutorService.submit(() -> {
      try {
        if (msg instanceof JoinInGame) {
          gameExecutor.joinInGame((JoinInGame) msg);
        } else if (msg instanceof NotifyStartOfGame) {
          gameExecutor.notifyStartOfGame();
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof NotifyStartOfRound) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof NotifyEndOfRound) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof NotifyGameRecord) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof NotifyEndOfGame) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof NotifyJoinInGame) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof JoinInGame) {
          requester.request(processId, (RequestToUI) msg);
        } else if (msg instanceof UnexpectedShutdownRequest) {
          messengerExecutorService.shutdown();
          receivedInputExecutorService.shutdown();
          gameExecutor.unexpectedShutdown();
        } else if (msg instanceof ShutdownRequest) {
          messengerExecutorService.shutdown();
          receivedInputExecutorService.shutdown();
          gameExecutor.shutdown();
        } else if (msg instanceof RequestToShowMessage) {
          RequestToShowMessage smsg = (RequestToShowMessage) msg;
          requester.request(processId, smsg);
        } else if (msg instanceof RequestToShowImage) {
          RequestToShowImage smsg = (RequestToShowImage) msg;
          requester.request(processId, smsg);
        } else if (msg instanceof RequestToRemoveMessage) {
          RequestToRemoveMessage smsg = (RequestToRemoveMessage) msg;
          requester.request(processId, smsg);
        } else {
          log.error("Message is discard... " + msg);
          throw new RuntimeException("Message is discard. " + msg);
        }
      } catch (Throwable e) {
        log.error("requester={}", requester);
        log.error(e, e);
        throw e;
      }
    });
  }

  public synchronized void sendGameMessage(GameMessage msg) {
    if (msg.getTo().toString().equals(context.getName().toString())) {
      context.receiveMessage(msg);
      return;
    }

    Player toPlayer = context.getPlayer(msg.getTo());
    if (toPlayer != null) {
      toPlayer.receiveMessage(msg);
    }
  }

  public synchronized void sendGameEvent(GameEvent event) {
    gameExecutor.notifyGameEvent(event);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public void registerSystemMessageBox(ActorName name, MessageBox<Message> systemMessageBox) {
    systemMessageBoxes.putMessageBox(name, systemMessageBox);
  }

  public void request(RequestToUI request) {
    requester.request(processId, request);
  }

  public long requestToShowMessage(ActorName toPlayerName, String msg) {
    RequestToShowMessage tmp = new RequestToShowMessage(toPlayerName.toString(),
        context.getRoundnum(), new MessageToUI(msg));
    sendMessage(tmp);
    return tmp.getId();
  }

  public void requestForInput(ActorName playerName, Form form, Consumer<Parameters> callback,
      boolean autoInput, int maxAutoResponseTime) {
    RequestToInput req = new RequestToInput(form.getId(), form.getLabel(), context.getRoundnum(),
        playerName.toString(), form.toInputToUIs(), autoInput, maxAutoResponseTime);
    requester.request(processId, req);
  }

  public void requestForInput(ActorName playerName, Form form, Procedure callback,
      boolean autoInput, int maxAutoResponseTime) {
    RequestToInput req = new RequestToInput(form.getId(), form.getLabel(), context.getRoundnum(),
        playerName.toString(), form.toInputToUIs(), autoInput, maxAutoResponseTime);
    requester.request(processId, req);
  }

  public Context forceToGetContext() {
    return context;
  }

  public long requestToEvalHtmlElement(ActorName toPlayerName, String msg) {
    RequestToShowMessage tmp = new RequestToShowMessage(toPlayerName.toString(),
        context.getRoundnum(), new MessageToUI(msg, new HtmlAttribute("message-area", "no-show")));
    sendMessage(tmp);
    return tmp.getId();
  }

  public long requestToShowImage(ActorName name, BufferedImage image, String formatName) {
    RequestToShowImage msg = new RequestToShowImage(name, context.getRoundnum(),
        Base64Utils.encode(image, formatName), formatName);
    sendMessage(msg);
    return msg.getId();
  }

  public void requestToRemoveMessage(long msgId) {
    sendMessage(new RequestToRemoveMessage(msgId));
  }

}
