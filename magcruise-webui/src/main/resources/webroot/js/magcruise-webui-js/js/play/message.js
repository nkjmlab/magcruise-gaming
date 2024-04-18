(function(window) {
	function procMessages(messages, targetSelector) {
		function procMsg(messages, index) {
			if (messages.length === index) { return; }
			const msg = messages[index];
			$(targetSelector)
				.each(
					function(i, val) {
						if ($(val).find('li[messageid=' + msg.id + ']').length == 0) {
							if ($(val).attr("message-area") === undefined
								|| $(val).attr("message-area") == msg.messageArea) {
								if (msg.message.attrs !== null) {
									for (let j = 0; j < msg.message.attrs.length; j++) {
										if (msg.message.attrs[j].name == 'message-area'
											&& msg.message.attrs[j].value == 'no-show') {
											$(val).prepend($(T.li('.message-no-show')({
												messageid: msg.id
											})).append(msg.message.message).fadeIn(500));
											return procMsg(messages, index + 1);
										}
									}
								}
								const msgContent = msg.message.message;
								if (msgContent.indexOf('<script>') != -1
									|| msgContent.indexOf('<style>') != -1) {
									$('body').append(msgContent);
								} else {
									$(val).prepend(
										$(T.li('.message')({
											messageid: msg.id
										}, T.div({
											className: "arrived_date"
										}, T.span(toFormatedDate(new Date(msg.id))), " "))).append(
											msgContent).fadeIn(500));
								}
							}
						}
						procMsg(messages, index + 1);
					});
		}
		procMsg(messages, 0);
		return messages[messages.length - 1].id;
	}

	function pollMessages(url, sessionId, playerId, interval, targetSelector) {
		const request = new JsonRpcRequest(url, "getMessages", [sessionId, playerId]);
		request.delay = interval;
		request.done = function(data) {
			if (data.result.length == 0) { return; }
			procMessages(data.result, targetSelector);
			request.params = [sessionId, playerId];
		}
		const client = new JsonRpcClient(request);
		client.schedule();
		return client;
	}
	window.procMessages = procMessages;
	window.pollMessages = pollMessages;
})(window);
