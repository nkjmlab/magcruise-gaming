function getWebSocketBaseUrl() {
  function createWebSocketUrl(protocol) {
    const u = parseUri(document.URL);
    const urlPrefix = protocol + "://" + u.authority + "/";
    return urlPrefix + "websocket";
  }
  if (parseUri(location).protocol === "https") {
    return createWebSocketUrl("wss");
  } else {
    return createWebSocketUrl("ws");
  }
}

  function checkWebsocket() {
    const wsUrl = getWebSocketBaseUrl()+"/checkcon";
    const connection = new WebSocket(wsUrl);
    connection.onopen = function (e) {
      $("#websocket-check-result").html(
        $('<div class="alert alert-success">').html(
          '<span class="badge text-bg-success">OK</span> '
          + "ネットワーク環境は正常です．"));
      $("#btn-login").attr("disabled", null);
      connection.close();
    };
    connection.onerror = function (e) {
      $("#websocket-check-result").html(
        $('<div class="alert alert-danger">').html(
          '<span class="badge text-bg-danger">WARN</span> '
          + "Websocketによる通信が出来ません．"));
      $("#btn-login").attr("disabled", true);
    };
  }


function checkDevice(outputSelector, callback) {
  if (!callback) {
    callback = function() {
    };
  }
  const uaParser = new UAParser();
  let unrecommended = false;

  if (!uaParser.getOS() || !uaParser.getBrowser()) {
    unrecommended = true;
  }
  if (uaParser.getBrowser().name === "Chrome"
          && Number(uaParser.getBrowser().version.split(".")[0]) >= 54) {
  } else if (uaParser.getOS().name === "Windows" || uaParser.getOS().name === "Linux") {
    if (uaParser.getBrowser().name === "Chrome"
            && Number(uaParser.getBrowser().version.split(".")[0]) >= 54) {
    } else if (uaParser.getBrowser().name === "Firefox"
            && Number(uaParser.getBrowser().version.split(".")[0]) >= 47) {
    } else {
      unrecommended = true;
    }
  } else if (uaParser.getOS().name === "Android") {
    if (Number(uaParser.getOS().version) < 5.0) {
      unrecommended = true;
    }

    if (uaParser.getBrowser().name === "Chrome"
            && Number(uaParser.getBrowser().version.split(".")[0]) >= 54) {
    } else {
      unrecommended = true;
    }
  } else if ((uaParser.getOS().name === "iOS" || uaParser.getOS().name === "Mac OS")) {
    if (uaParser.getOS().version < 9.0) {
      unrecommended = true;
    }
    if (uaParser.getBrowser().name === "Chrome"
            && Number(uaParser.getBrowser().version.split(".")[0]) >= 54) {
    } else if (uaParser.getBrowser().name === "Firefox"
            && Number(uaParser.getBrowser().version.split(".")[0]) >= 47) {
    } else {
      unrecommended = true;
    }
  } else {
    unrecommended = true;
  }
  const osAndBrowser = (uaParser.getBrowser() ? uaParser.getBrowser().name + " "
          + uaParser.getBrowser().version : "unkown browser")
          + " ("
          + (uaParser.getOS() ? uaParser.getOS().name + " " + uaParser.getOS().version
                  : "unkown OS") + ")";
  if (unrecommended) {
    $(outputSelector).html(
            $('<div class="alert alert-warning">').html(
                    '<span class="badge text-bg-warning">WARN</span> ' + osAndBrowser
                            + "は推奨ブラウザ/OSではないため，正しく動作しない可能性があります．"));
  } else {
    $(outputSelector).html(
            $('<div class="alert alert-success">').html(
                    '<span class="badge text-bg-success">OK</span> ' + osAndBrowser + "は推奨環境です．"));
    callback();
  }
}