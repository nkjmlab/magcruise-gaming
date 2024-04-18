window.addEventListener("load", function() {
  checkWebsocket("wss://echo.websocket.org/", "check-websocket-result");
}, false);

function checkWebsocket(wsUri, outputId) {
  const output = document.getElementById(outputId);
  testWebSocket();
  function testWebSocket() {
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
      onOpen(evt)
    };
    websocket.onclose = function(evt) {
      onClose(evt)
    };
    websocket.onmessage = function(evt) {
      onMessage(evt)
    };
    websocket.onerror = function(evt) {
      onError(evt)
    };
  }

  function onOpen(evt) {
    writeToScreen("1. CONNECTED (onOpen)");
    doSend("WebSocket rocks");
  }

  function onClose(evt) {
    writeToScreen("4. DISCONNECTED (onClose)");
  }

  function onMessage(evt) {
    writeToScreen('<span style="color: blue;">3. RESPONSE (onMessage): ' + evt.data + '</span>');
    websocket.close();
  }

  function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
  }

  function doSend(message) {
    writeToScreen("2. SENT (send): " + message);
    websocket.send(message);
  }

  function writeToScreen(message) {
    const elem = document.createElement("div");
    elem.style.wordWrap = "break-word";
    elem.innerHTML = message;
    output.appendChild(elem);
  }
}