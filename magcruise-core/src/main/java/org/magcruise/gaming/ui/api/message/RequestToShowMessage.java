/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.magcruise.gaming.ui.api.message;

import org.magcruise.gaming.ui.model.message.MessageToUI;

@SuppressWarnings("serial")
public class RequestToShowMessage extends RequestToUI {

	private String playerName;
	private MessageToUI message;
	private int roundnum;

	public RequestToShowMessage(long id, String playerName, int roundnum, MessageToUI message) {
		super(id);
		this.playerName = playerName;
		this.roundnum = roundnum;
		this.message = message;
	}

	public RequestToShowMessage(String playerName, int roundnum, MessageToUI message) {
		this(generateId(), playerName, roundnum, message);
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public MessageToUI getMessage() {
		return message;
	}

	public void setMessage(MessageToUI message) {
		this.message = message;
	}

	public int getRoundnum() {
		return roundnum;
	}

	public void setRoundnum(int roundnum) {
		this.roundnum = roundnum;
	}

}
