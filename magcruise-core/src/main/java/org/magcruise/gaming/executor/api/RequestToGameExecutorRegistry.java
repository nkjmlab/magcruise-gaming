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
package org.magcruise.gaming.executor.api;

import org.magcruise.gaming.ui.model.input.InputFromWebUI;

/**
 * MAGCruiseCoreが実行するゲームに関係するインタフェース. MAGCruiseBrokerがGUIからのリクエストに応答する形で使われるフロントエンドのサービス．
 * Web経由で用いるのでインタフェースはPOJOにする．
 *
 * @author nakaguchi, nkjm
 *
 */
public interface RequestToGameExecutorRegistry extends RequesterToGameExecutor {

  void joinInGame(String processId, String playerName);

  void sendAssignment(String processId, String playerName, String operatorId);

  void sendInput(String processId, String playerName, long requestId, int roundnum,
      InputFromWebUI[] inputs);

  void sendAutoInput(String processId, String playerName, long requestId, int roundnum,
      InputFromWebUI[] inputs, int maxAutoResponseTime);

}
