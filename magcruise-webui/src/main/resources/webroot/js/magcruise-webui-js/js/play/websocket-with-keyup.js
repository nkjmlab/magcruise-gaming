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
  const handlersMap = {};
  handlersMap["RequestToInput"] = [function (requests) {
    procInputRequests(requests, pid, playerName, targetSelectors[0], callbackUrls)
  }];
  handlersMap["RequestToShowMessage"] = [function (messages) {
    procMessages(messages, targetSelectors[1])
  }];

  let connection = setUpConnection(wsUrl);

  $(document).on('keyup', 'input', function (ev) {
    connection.send("keyup");
  });

  function setUpConnection(wsUrl) {
    const _connection = new WebSocket(wsUrl);
    const timers = {};
    _connection.onmessage = function (e) {
      const messages = JSON.parse(e.data);
      if (messages.length != 0 && messages[0] === "keyup") {
        const typist = messages[1];
        if ($('.' + typist).length == 0) {
          $('#inputting').append(
            $('<div>').addClass(typist).addClass('balloon-1-left').append(
              "<strong>" + typist + "</strong> is inputting..."));

          $('.' + typist).show(0);
          timers[typist + "remove"] = setTimeout(function () {
            $('.' + typist).remove();
          }, 1000);
        } else {
          $('.' + typist).show();
          clearTimeout(timers[typist + "remove"]);
          timers[typist + "remove"] = setTimeout(function () {
            $('.' + typist).remove();
          }, 1000);
        }
        return;
      }
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
