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

import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import org.magcruise.gaming.ui.model.input.InputToUI;

@SuppressWarnings("serial")
public class RequestToInput extends RequestToUI {

  private volatile String label;
  private volatile int roundnum;
  private volatile String playerId;
  private volatile InputToUI[] inputs;
  private volatile int maxAutoResponseTime = 0;

  private volatile boolean autoInput = false;

  private volatile boolean finished = false;

  public RequestToInput(long id, String label, int roundnum, String playerId, InputToUI[] inputs,
      boolean autoInput) {
    this(id, label, roundnum, playerId, inputs, autoInput, 0);
  }

  public RequestToInput(long id, String label, int roundnum, String playerId, InputToUI[] inputs,
      boolean autoInput, int maxAutoResponseTime) {
    super(id);
    this.autoInput = autoInput;
    this.label = label;
    this.roundnum = roundnum;
    this.playerId = playerId;
    this.inputs = inputs;
    this.maxAutoResponseTime = maxAutoResponseTime;
  }

  public int getRoundnum() {
    return roundnum;
  }

  public void setRoundnum(int roundNumber) {
    this.roundnum = roundNumber;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public InputToUI[] getInputs() {
    return inputs;
  }

  public void setInputs(InputToUI[] inputs) {
    this.inputs = inputs;
  }

  public void setFinished(boolean b) {
    this.finished = b;
  }

  public boolean getFinished() {
    return this.finished;
  }

  public boolean isAutoInput() {
    return autoInput;
  }

  public InputFromWebUI[] getInputToUIs() {
    return InputToUI.toInputFromWebUIs(getInputs());
  }

  public int getMaxAutoResponseTime() {
    return maxAutoResponseTime;
  }

  public void setMaxAutoResponseTime(int maxAutoResponseTime) {
    this.maxAutoResponseTime = maxAutoResponseTime;
  }

}
