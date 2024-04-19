package org.magcruise.gaming.model.game;

import java.io.Serializable;
import java.util.List;
import org.magcruise.gaming.common.KeyValueTable;
import org.magcruise.gaming.common.KeyValueTables;
import org.magcruise.gaming.executor.GameMessenger;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.model.game.message.GameMessage;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class ActorObject extends HistoricalObject {

	public ActorName name;

	public MessageBox<GameMessage> msgbox = new MessageBox<>();

	protected MessageBox<Message> systemMessageBox = new MessageBox<>();

	protected GameMessenger messenger;

	protected KeyValueTables keyValueTables;

	public ActorObject(Properties props, History history, MessageBox<GameMessage> msgbox,
			KeyValueTables keyValueTables) {
		super(props, history);
		this.msgbox = msgbox;
		this.keyValueTables = keyValueTables;
	}

	@Override
	public SConstructor<? extends ActorObject> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), getConstractorArgs());
	}

	public abstract Object[] getConstractorArgs();

	public void setMessenger(GameMessenger messenger) {
		this.messenger = messenger;
		messenger.registerSystemMessageBox(name, systemMessageBox);
	}

	public ActorName getName() {
		return name;
	}

	/**
	 * Sends a {@code GameMessage} message to other {@code Player} or {@code Context}.
	 *
	 * @param msg the message to send.
	 */
	public void sendMessage(GameMessage msg) {
		messenger.sendGameMessage(msg);
	}

	public void sendMessage(GameEvent event) {
		sendEvent(event);
	}

	/**
	 * Sends gaming framework message. This method is for GAMING SYSTEM DEVELOPER.
	 *
	 * @param msg the message to send.
	 */
	public void sendSystemMessage(Message msg) {
		messenger.sendMessage(msg);
	}

	public void sendEvent(GameEvent event) {
		messenger.sendGameEvent(event);
	}

	protected void request(RequestToUI request) {
		messenger.request(request);
	}

	/**
	 * Take a message from own message box.
	 *
	 * @return
	 */
	public Message takeMessage() {
		return msgbox.poll();
	}

	public <T extends GameMessage> T takeMessage(Class<T> clazz) {
		return msgbox.poll(clazz);
	}

	public List<GameMessage> takeAllMessages() {
		return msgbox.pollAll();
	}

	public <T extends GameMessage> List<T> takeAllMessages(Class<T> clazz) {
		return msgbox.pollAll(clazz);
	}

	/**
	 * Receive message from others.
	 *
	 * @param msg
	 */
	public void receiveMessage(GameMessage msg) {
		this.msgbox.offer(msg);
	}

	/**
	 * Receive message from others.
	 *
	 * @param msg
	 */
	public void receiveSystemMessage(Message msg) {
		this.systemMessageBox.offer(msg);
	}

	protected Object applyProcedure(String processId, String procedureName, Object... args) {
		return SchemeEnvironment.applyProcedure(processId, procedureName, args);
	}

	protected static Symbol toSymbol(String s) {
		return Symbol.parse(s);
	}

	protected Object getValue(String tableName, String key, int index) {
		return keyValueTables.get(tableName, key, index);
	}

	protected void roundValidation(Context ctx) {
		KeyValueTable expectedValues = keyValueTables.getKeyValueTable("expected");
		if (expectedValues == null) {
			log.warn("Round validation of {} is skipped because there is no expected value.",
					this.name);
			return;
		}
		for (String key : expectedValues.keySet()) {
			Object expected = expectedValues.get(key, ctx.getRoundnum());
			Serializable actual = getValue(toSymbol(key), ctx.getRoundnum());
			if (expected == null || actual == null) {
				log.warn(
						"Expected or actual is null : roundnum={},  name={}, val={}, expected={}, actual={}",
						ctx.getRoundnum(), name, key, expected, actual);
			} else if (!expected.toString().equals(actual.toString())) {
				log.error(
						"Validation Error : roundnum={},  name={}, val={}, expected={}, actual={}",
						ctx.getRoundnum(), name, key, expected, actual);
				throw new RuntimeException("Round validation error.");
			} else {
				log.info("Validation Success: name={}, val={}, expected={}, actual={}", name, key,
						expected, actual);
			}
		}

	}

	protected ActorName toActorName(String name) {
		return ActorName.of(name);
	}

}
