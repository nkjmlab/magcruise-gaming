package org.magcruise.gaming.model.game;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.magcruise.gaming.executor.CallbackForInput;
import org.magcruise.gaming.executor.CallbackMethodForInput;
import org.magcruise.gaming.executor.CallbackProcedureForInput;
import org.magcruise.gaming.executor.api.message.JoinInGame;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.game.message.Alert;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.model.task.SingleTaskLatch;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.model.Form;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;
import org.nkjmlab.util.java.lang.ParameterizedStringFormatter;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.Procedure;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class Player extends ActorObject {

  public enum PlayerType {
    HUMAN, AGENT;

    public static PlayerType of(Symbol playerType) {
      if (playerType.toString().equalsIgnoreCase("human")) {
        return HUMAN;
      } else if (playerType.toString().equalsIgnoreCase("agent")) {
        return AGENT;
      } else {
        throw new IllegalArgumentException("playerType should be 'human or 'agent.");
      }
    }
  }

  private PlayerType playerType;

  private String operatorId;

  protected PlayerParameter playerParameter;

  private Map<Long, CallbackForInput<?>> waitingCallbacks = new ConcurrentHashMap<>();

  private boolean autoInput = false;

  public Player(PlayerParameter playerParameter) {
    super(playerParameter.getProps(), playerParameter.getHistory(), playerParameter.getMsgbox(),
        playerParameter.getKeyValueTable());
    this.playerType = playerParameter.getPlayerType();
    this.name = playerParameter.getPlayerName();
    this.operatorId = playerParameter.getOperatorId();
    this.playerParameter = playerParameter;
    this.autoInput = isAgent() ? true : playerParameter.getAutoInput();
  }

  public void joinInGame(Context ctx, JoinInGame msg) {
    ctx.showMessageToAll("{} is join in this session.", msg.getPlayerName());
    log.info("{} is assigned to {} in {}", msg.getOperatorId(), msg.getPlayerName(),
        ctx.getProcessId().toString());
  }

  public void rejoinInGame(Context ctx, JoinInGame msg) {}

  @Override
  public SConstructor<? extends Player> toConstructor(ToExpressionStyle style) {
    return new SConstructor<>(
        (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "  " : "")
            + SConstructor.toConstructor(style, getClass(), getConstractorArgs()));
  }

  @Override
  public Object[] getConstractorArgs() {
    return new Object[] {getPlayerParameter()};
  }

  protected PlayerParameter getPlayerParameter() {
    return new PlayerParameter(name, playerType, operatorId, props, history, msgbox, autoInput,
        keyValueTables);
  }

  public boolean isAgent() {
    switch (playerType) {
      case AGENT:
        return true;
      default:
        return false;
    }
  }

  public boolean isHuman() {
    return !isAgent();
  }

  public Context getContext() {
    return messenger.forceToGetContext();
  }

  /**
   * Shows message to the human operator assigned to this {@code Player}.
   *
   * @param msg the message shown to the operator
   * @return
   */
  public long showMessage(String msg, Object... params) {
    return showMessage(name, msg, params);
  }

  public void sendExternalCss(String href) {
    showMessage("<link rel='stylesheet' href='" + href + "'>");
  }

  public long sendExternalJavaScript(String href) {
    return showMessage("<script src='>" + href + "'></script>");
  }

  public long sendJavaScriptTag(String script, Object... params) {
    return showMessage("<script>" + script + "</script>", params);
  }

  public long sendStyleTag(String css, Object... params) {
    return showMessage("<style>" + css + "</style>", params);
  }

  public void showAlert(String title, String text, String type) {
    sendJavaScriptTag("swalAlert(\"{}\",\"{}\",\"{}\");", title, text, type);
  }

  public void appendHtml(String selector, String html) {
    sendJavaScriptTag("$(\"{}\").append(\"{}\");", selector, html);
  }

  public void setHtml(String selector, String html) {
    sendJavaScriptTag("$(\"{}\").html(\"{}\");", selector, html);
  }

  /**
   * Shows message to the human operator assigned to another {@code Player}.
   *
   * @param toPlayerName the player name of recipient
   * @param msg the message shown to the operator
   * @return
   */
  public long showMessage(ActorName toPlayerName, String msg, Object... params) {
    return messenger.requestToShowMessage(toPlayerName,
        ParameterizedStringFormatter.DEFAULT.format((String) msg, params));
  }

  protected void showAlertMessage(Alert alert, String msg) {
    showMessage(
        "<div class='alert alert-" + alert.toString().toLowerCase() + "'>" + msg + "</div>");
  }

  /**
   * Sends synchronized input request to the human operator assigned to this {@code Player}. This
   * method will return after the callback method will be finished.
   *
   * @param form the form shown to the human operator.
   * @param callback the callback procedure which taking {@code Parameters} object.
   */
  public void syncRequestToInput(Form form, Consumer<Parameters> callback) {
    Context ctx = getContext();
    tryToReplaceInputWhenAutoInput(ctx, form);
    messenger.requestForInput(name, form, callback, autoInput, ctx.getMaxAutoResponseTime());
    waitForResponseAndApplyCallback(form.getId(), callback);
  }

  public void syncRequestToInput(Form form, Consumer<Parameters> callback,
      Consumer<Exception> errorHandler) {
    try {
      syncRequestToInput(form, callback);
    } catch (Exception e) {
      errorHandler.accept(e);
    }
  }

  public void syncRequestToInput(Form form) {}

  public void syncRequestToConfirm(String msg) {
    syncRequestToInput(new Form(msg), (params) -> {
    });
  }

  public void syncRequestToInput(Form form, Procedure callback) {
    Context ctx = getContext();
    tryToReplaceInputWhenAutoInput(ctx, form);
    messenger.requestForInput(name, form, callback, autoInput, ctx.getMaxAutoResponseTime());
    waitForResponseAndApplyCallback(form.getId(), callback);
  }

  private void tryToReplaceInputWhenAutoInput(Context ctx, Form form) {
    if (!autoInput) {
      return;
    }
    if (!keyValueTables.exists("auto-input")) {
      if (ctx.roundValidation) {
        log.warn("no auto-input table for {}", getName());
      }
      return;
    }
    form.getInputs().forEach(i -> {
      String name = i.getName();
      Object o = getValue("auto-input", name, ctx.getRoundnum());
      if (o != null) {
        i.setValue((Serializable) o);
      } else {
        if (ctx.roundValidation) {
          log.warn(
              "auto-input table does not have {} of {} on round {} " + System.lineSeparator()
                  + "{}",
              name, getName(), ctx.getRoundnum(), keyValueTables.getKeyValueTable("auto-input"));
        } else {
          log.debug("auto-input table does not have the value. {} of {} on round {} ", name,
              getName(), ctx.getRoundnum());
        }

      }
    });
  }

  private List<ScheduledExecutorService> executorServices = new ArrayList<>();

  private List<Future<?>> futures = new ArrayList<>();

  private void waitForResponseAndApplyCallback(long requestId, Object callback) {
    CountDownLatch latch = new CountDownLatch(1);
    CallbackForInput<?> waitCallback = null;
    if (callback instanceof Procedure) {
      waitCallback = new CallbackProcedureForInput(requestId, (Procedure) callback, latch);
    } else {
      @SuppressWarnings("unchecked")
      Consumer<Parameters> callback2 = (Consumer<Parameters>) callback;
      waitCallback = new CallbackMethodForInput(requestId, callback2, latch);
    }
    waitingCallbacks.put(requestId, waitCallback);

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
        BasicThreadFactory.builder(name + "-wait-input-" + requestId, false).build());

    synchronized (executorServices) {
      executorServices.add(executor);
    }

    ScheduledFuture<?> future = executor.scheduleWithFixedDelay(() -> {
      try {
        synchronized (systemMessageBox) {
          if (systemMessageBox.isEmpty()) {
            return;
          }
          for (Message msg : systemMessageBox.pollAll()) {
            log.debug("pop message={}", msg);
            if (msg instanceof ReceiveInput && ((ReceiveInput) msg).getRequestId() == requestId) {
              if (waitingCallbacks.get(requestId) == null) {
                continue;
              }
              waitingCallbacks.get(requestId).callback((ReceiveInput) msg);
              waitingCallbacks.remove(requestId);
              log.trace("UserInput input executor will be shutdowned.={}", executor);
              // NOTE
              // requestを受けとったら，そのrequestをwaitしているexecutorを後片付け．
              // taskの内部なので，ここでawaitTerminationすると返ってこなくなる．
              executor.shutdown();
              synchronized (executorServices) {
                executorServices.remove(executor);
              }
            } else {
              systemMessageBox.offer(msg);
            }
          }
        }
      } catch (Exception e) {
        log.error(e, e);
      }

    }, 0, 200, TimeUnit.MILLISECONDS);

    synchronized (futures) {
      futures.add(future);
    }
    try {
      log.trace("UserInput input... requestId={} ", requestId);
      while (!latch.await(250, TimeUnit.MILLISECONDS)) {
        if (Thread.currentThread().isInterrupted()) {
          log.trace("Interrupted!!");
          throw new InterruptedException();
        }
      }
      log.trace("Received input and done callback proc!! requestId={}", requestId);
    } catch (InterruptedException e) {
      log.trace(e);
      // NOTE SingleTaskLatch.finishiStageなどにより別スレッドから入力待ちをキャンセルされたら，
      // 現在待っているexecutorsを後片付け．
      finishExecutorServices();
      throw new TaskInterruptedException(e);
    }
  }

  /**
   * toPlayerNameで指定したPlayerを操作するヒューマンプレーヤに同期的な入力依頼を送る．
   *
   * @param toPlayerName
   * @param form
   * @param callback
   */
  public void syncRequestToInput(Symbol toPlayerName, Form form, Consumer<Parameters> callback) {
    syncRequestToInput(toPlayerName, form, callback);
  }

  public void syncRequestToInput(Symbol toPlayerName, Form form, Procedure callback) {
    syncRequestToInput(toPlayerName, form, callback);
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public long sendHtmlElement(String tag, Object... params) {
    return messenger.requestToEvalHtmlElement(name,
        ParameterizedStringFormatter.DEFAULT.format((String) tag, params));
  }

  public void requestToAssignOperator(String operatorId) {
    if (isAgent()) {
      this.operatorId = DefAssignmentRequest.AGENT_OPERATOR;
      return;
    }
    request(new RequestToAssignOperator(name.toString(), operatorId));
    if (autoInput) {
      sendSystemMessage(new JoinInGame(name, operatorId));
      this.operatorId = operatorId;
    }
  }

  private void finishExecutorServices() {
    synchronized (futures) {
      futures.forEach(f -> {
        if (f.isDone()) {
          return;
        }
        f.cancel(true);
      });
    }
    synchronized (executorServices) {
      executorServices.forEach(executor -> {
        ExecutorServiceUtils.shutdownAndAwaitTermination(executor, 10, TimeUnit.MILLISECONDS);
      });
      executorServices.clear();
    }
  }

  public void finish(Context ctx, GameEvent msg, SingleTaskLatch latch) {
    latch.finish(name);
    finishExecutorServices();
  }

  public long requestToEvalScripts(String... urlsOrScripts) {
    LList urls = LList.makeList(Arrays.asList(urlsOrScripts));
    String s = createMessageForGetAndEvalScripts(urls);
    return sendHtmlElement(
        "<script> if(typeof loadedScripts === 'undefined'){loadedScripts= new Array();}" + s
            + "</script>");
  }

  private String createMessageForGetAndEvalScripts(LList urlsOrScripts) {
    if (urlsOrScripts.isEmpty()) {
      return "";
    }
    Pair p = (Pair) urlsOrScripts;
    String e = (String) p.getCar();

    if (e.startsWith("http:") || e.startsWith("/")) {
      return String.format(
          "$.getScript($.inArray('%s', loadedScripts)==-1?'%s':null)"
              + ".always(function(script, textStatus) {" + "%s" + "});"
              + "if($.inArray('%s', loadedScripts)==-1){loadedScripts.push('%s');}",
          e, e, createMessageForGetAndEvalScripts((LList) p.getCdr()), e, e);
    } else {
      return e + System.lineSeparator() + createMessageForGetAndEvalScripts((LList) p.getCdr());
    }

  }

  public long showImage(BufferedImage image, String formatName) {
    return messenger.requestToShowImage(name, image, formatName);
  }

  public void removeMessage(long msgId) {
    messenger.requestToRemoveMessage(msgId);
  }
}
