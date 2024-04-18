let connection;
$(window).on('unload', function () {
  if (connection) {
    connection.onclose = function () {
    };
    connection.close();
  }
});

function pollPlayEventsByWebsocket(wsUrl, pid, playerName, targetSelectors, callbackUrls) {
  // JavaのgetClass().getSimpleName()と対応．
  let handlersMap = {};
  handlersMap["RequestToInput"] = [function (requests) {
    procInputRequests(requests, pid, playerName, targetSelectors[0], callbackUrls)
  }];
  handlersMap["RequestToShowMessage"] = [function (messages) {
    procMessages(messages, targetSelectors[1])
  }];

  let connection = setUpConnection(wsUrl);

  function setUpConnection(wsUrl) {
    const _connection = new WebSocket(wsUrl);
    _connection.onopen = e => {
      console.log("connection is open.");
    }
    _connection.onmessage = e => {
      const messages = JSON.parse(e.data);
      for (let i = 0; i < messages.length; i++) {
        const message = messages[i];
        const handlers = handlersMap[message.type];
        if (!handlers) {
          console.log("message arrived but no handlers registered for type: " + m.type);
          continue;
        }
        handlers.forEach(function (handler) {
          handler([message]);
        });

      }
    };
    _connection.onerror = e => {
      console.error("connection has an error.");
      setTimeout(() => { location.reload(); }, 3000);
    };

    _connection.onclose = e => {
      console.warn("connection is closed.");
      setTimeout(() => { connection = setUpConnection(wsUrl); }, 3000);
    };
    return _connection;
  }

  return {
    abort: function () {
      connection.close();
    }
  };
}
