function pollStdIO(url, pid, interval, method, field) {
  const request = new JsonRpcRequest(url, method, [pid]);
  request.delay = interval;
  request.done = function (data) {
    const t = data.result;
    const f = $(field);
    if (t != null && t != f.text()) {
      f.text(t);
      f.scrollTop(f[0].scrollHeight - f.height());
    }
  }
  const client = new JsonRpcClient(request);
  client.setAuth(basicAuthUserId, basicAuthPassword);
  client.rpc();
  return client;
}

function pollStdOut(url, pid, interval, targetSelector) {
  return pollStdIO(url, pid, interval, "getStdOut", targetSelector);
}

function pollStdErr(url, pid, interval, targetSelector) {
  return pollStdIO(url, pid, interval, "getStdErr", targetSelector);
}
