$(function () {
  $("#tbl-sessions").on(
    'draw.dt',
    function () {
      const sessionChecksSelector = ".input-check-all-target";
      bindCheckAll("#input-check-all", sessionChecksSelector);

      $("#btn-add-target").on(
        'click',
        function (e) {
          swalInput("監視対象追加", "urlをカンマ区切りで入れてください",
            "https://broker.imse.magcruise.org/app", "Write broker urls", function (
              inputValue) {
            if (!inputValue) return false;
            new JsonRpcClient(new JsonRpcRequest(getServiceUrl(),
              "addBrokerUrls", [inputValue.split(",")], function (data) {
                location.reload();
              }, function (data, textStatus, errorThrown) {
                flashError("Fail to add.");
              })).rpc();
          });
        });

      $("#btn-all-stop").on(
        'click',
        function (e) {
          if ($(sessionChecksSelector + ":checked").length == 0) {
            swalAlert("セッションが選択されていません");
            return;
          }
          swalConfirm("セッション終了", "一括終了して良いですか？", "warning", function (e) {
            const targets = $(sessionChecksSelector + ":checked")
            function aux(i) {
              const processServiceUrl = $(targets[i]).data('broker-url')
                + '/json/GameProcessService';
              const pid = $(targets[i]).data('pid');
              if (targets.length == i) {
                location.reload();
                return;
              }
              stopProcess(processServiceUrl, pid, function () {
                aux(i + 1);
              });
            }
            aux(0);
          });
        });
      function stopProcess(processServiceUrl, pid, callback) {
        const client = new JsonRpcClient(new JsonRpcRequest(changeProtocolIfNeeded(processServiceUrl),
          "stopProcess", [pid], callback, function (data, textStatus, errorThrown) {
            flashError("Fail to stop.");
          }));
        client.setAuth(basicAuthUserId, basicAuthPassword);
        client.rpc();
      }
      $('.btn-stop').on('click', function (e) {
        const pid = $(this).data('pid');
        const processServiceUrl = $(this).data('broker-url') + '/json/GameProcessService';
        swalConfirm("セッション終了", "終了して良いですか？", "warning", function (e) {
          stopProcess(processServiceUrl, pid, function (data) {
            location.reload();
          });
        });
      });
    });
  $("#tbl-sessions").DataTable({
    "pageLength": 100,
    order: Array([4, 'desc'])
  });

});
