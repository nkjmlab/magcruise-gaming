function pollPlayerInfo(url, sessionId, playerName, interval) {
  function sendInputs(form) {
    const request = new JsonRpcRequest(url + '/play_faster/jsonRpc', "sendInput", [sessionId, playerName, form.attr('requestId'), form.inputs]);
    request.done =
      function (data) {
        console.log(data);
      }
    new JsonRpcClient(request).rpc();
  }

  let newest_id = 0;
  const request = new JsonRpcRequest(url + '/play_faster/jsonRpc', "getPlayInfo", [sessionId, playerName, newest_id]);
  request.delay = interval;
  request.done =
    function (data) {
      if (!data.messages.length == 0) {
        const updated_id = procMessages(data.messages, newest_id);
        newest_id = updated_id != null ? updated_id : newest_id;
        request.params = [sessionId, playerName, newest_id];
      }
      procDecisionRequests(data.decision_requests, sendInputs);
    }
  const client = new JsonRpcClient(request);
  client.schedule();
  return client;
}

