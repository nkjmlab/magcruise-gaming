function pollProgress(url, pid, interval, targetSelector) {
	const request = new JsonRpcRequest(url, "getProgresses", [pid]);
	request.delay = interval;
	request.done = function(data) {
		if (data.result.length == 0) { return; }
		$(targetSelector + '>li').remove();
		for (let i = 0; i < data.result.length; i++) {
			const progress = data.result[i];
			const t = '<span>' + toFormatedDate(progress.id) + '</span> Round(' + progress.roundnum + ") "
				+ progress.type + ': ' + progress.message;
			$(targetSelector).prepend($('<li>').addClass('message').html(t));
		}
	}
	const client = new JsonRpcClient(request);
	client.schedule();
	return client;
}
