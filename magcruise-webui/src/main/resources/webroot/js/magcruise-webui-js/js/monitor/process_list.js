function pollActiveProcessList(url, interval, field) {
  const request = new JsonRpcRequest(url, "listActiveProcesses", []);
  request.delay = interval;
  request.done =
    function (data) {
      const f = $(field);
      f.text(JSON.stringify(data.result, null, "    "));
      f.scrollTop(f[0].scrollHeight - f.height());
    }
  const client = new JsonRpcClient(request);
  client.schedule();
  return client;
}
