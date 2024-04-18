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

import org.apache.commons.lang3.builder.ToStringBuilder;

@SuppressWarnings("serial")
public class RequestToAssignOperator extends RequestToUI implements GameProgress {

	private String playerName;
	private String operatorId;
	private String operatorName = "";
	private boolean joined = false;
	private boolean assigned = false;
	@SuppressWarnings("unused")
	private String message;
	private int roundnum = Integer.MIN_VALUE;

	public RequestToAssignOperator() {

	}

	public RequestToAssignOperator(String playerName, String operatorId) {
		this.playerName = playerName;
		this.operatorId = operatorId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setUserId(String operatorId) {
		this.operatorId = operatorId;
	}

	public boolean getJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorName() {
		return this.operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public boolean getAssigned() {
		return this.assigned;
	}

	public void setAssigned(boolean b) {
		this.assigned = b;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	@Override
	public void setMessage(String msg) {
		this.message = msg;
	}

	@Override
	public int getRoundnum() {
		return roundnum;
	}

}
