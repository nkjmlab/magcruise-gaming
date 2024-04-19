function pollAssignmentAndCall(url, pid, operatorsList, interval, targetSelector, callbackUrl) {
  function sendAssignment(playerName, operatorId) {
    const request = new JsonRpcRequest(callbackUrl, "sendAssignment", [pid, playerName, operatorId]);
    new JsonRpcClient(request).rpc()
  }
  function procDefaultAssignment(player) {
    let match = false;
    operatorsList.forEach(function (e) {
      if (player.operatorId === e) {
        sendAssignment(player.playerName, player.operatorId);
        match = true;
        return;
      }
    });
    return match;
  }
  function getAssignments() {
    const defer = $.Deferred();
    const request = new JsonRpcRequest(url, "getNewAssignmentRequests", [pid]);
    request.delay = interval;
    request.done = function (data) {
      if (!data.result || data.result.length == 0 || $('#send_def_btn')[0] != null) { return; }
      const table = $('<table>').addClass('table');
      let requireAssignment = false;
      for (let i = 0; i < data.result.length; i++) {
        const player = data.result[i];
        if (procDefaultAssignment(player)) {
          continue;
        }

        const tr = $('<tr>').addClass('assign_player').append(
          $('<td>').attr('width', '30%').text(player.playerName));
        const select = $('<select>').attr('id', player.id).attr('player.playerName',
          player.playerName).attr('size', 5);
        operatorsList.forEach(function (e) {
          select.append($('<option>').val(e).text(e));
        });
        tr.append($('<td>').append(select));
        table.append(tr);
        requireAssignment = true;
      }
      if (requireAssignment) {
        table.append($('<input>').addClass('btn btn-primary').attr('id', 'send_def_btn').attr(
          'type', 'button').val('Submit'));
        $(targetSelector).append(table);
      }
      defer.resolve();
      client.abort();
    }
    const client = new JsonRpcClient(request);
    client.repeat(6);
    return defer.promise();
  }

  getAssignments().done(
    function () {
      $('#send_def_btn').on(
        'click',
        function () {
          if ($(targetSelector + " tr.assign_player").length != $(targetSelector
            + " tr.assign_player option:selected").length) {
            alert("Users are unassigned.");
            return;
          }
          $(targetSelector + " select").each(
            function () {
              sendAssignment($(this).attr('player.playerName'), $(this).children(
                "option:selected").val());
            });
          $(targetSelector).remove();
        });
    });
}

function pollAssignment(url, pid, operatorsList, interval, targetSelector) {
  pollAssignmentAndCall(url, pid, operatorsList, interval, targetSelector, url);
}
