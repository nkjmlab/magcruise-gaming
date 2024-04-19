function pollPlayersState(url, pid, interval, targetSelector, playerLink) {
  T.Shorthand.full();

  const request = new JsonRpcRequest(url, "getPlayersState", [pid]);
  request.delay = interval;
  request.done =
    function (data) {
      if ($(targetSelector + ">thead").length == 0) {
        $(targetSelector).append($('<thead>')).append($('<tbody>'));
      }

      const players = data.result.players;
      if (players[0] == null) {
        return;
      }

      (function tableHeader() {
        $(targetSelector + '>thead').empty();
        const rounds = new Array();
        for (let i = 0; i <= data.result.round; i++) {
          rounds.push(T.th(".round")({ round: i }, i));
        }
        $(targetSelector + '>thead').append(
          T.tr(T.th(".player")("Player"), T.th(".join")("Join"), rounds));
      }());

      $(targetSelector + " tbody").empty();
      $.each(data.result.requestCounts, function (player_id, rounds) {
        const p = players.filter(function (e) { return e['playerName'] == player_id })[0];


        (function playerColumn() {
          $(targetSelector + " tbody").append(
            T.tr({ player_id: player_id },
              T.td({ className: 'player' },
                p['assigned'] ? T.a({ className: 'btn btn-sm btn-primary btn-play', href: playerLink.replace('PID', pid).replace('PLAYERNAME', p['playerName']), target: '_blank' }, 'Play') : "",
                T.Text(" "),
                T.i({ className: 'fas fa-user' }),
                T.Text(fmt(' %s (%s)', p['playerName'], (location.href.indexOf("play.html") != -1 && sessionName.indexOf("anony") != -1 && !currentUser.admin) ? "anonymous" : p['operatorId'] + (sessionName.indexOf("anony") != -1 ? ": anonymized" : "")))
              )
            ));
        }());

        (function joinColumn() {
          const join_mark = p['joined'] ? T.span({ className: "badge text-bg-success", playerName: p['playerName'] }, 'Join') : T.span({ className: "badge text-bg-warning" }, 'Waiting');
          $(fmt(targetSelector + ' tr[player_id=%s]', player_id)).append(
            T.td({ className: 'join' }, join_mark)
          );
        }());
        (function roundColumns() {
          for (let k = 0; k <= data.result.round; k++) {
            const inputs = rounds[k];
            const labels = new Array();
            if (inputs != null) {
              for (let i = 0; i < inputs['true']; i++) {
                labels.push(T.span({ className: "badge text-bg-info" }, T.i({ className: "fas fa-check-square" }, " ")));
              }
              for (let i = 0; i < inputs['false']; i++) {
                labels.push(T.span({ className: "badge text-bg-warning" }, '...'));
              }
            }
            $(fmt(targetSelector + " tr[player_id=%s]", player_id)).append(
              T.td({ className: 'round', round: k }, labels));
          }
        }());
      });
    }
  const client = new JsonRpcClient(request);
  client.schedule();
  return client;
}
