$(function() {
	$("#btn-login").attr("disabled", true);

	if (parseUri(location).anchor === "logout") {
		new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), "logout", [], function() {
			location.href = "login.html";
			return;
		})).rpc();
	}

	$('#btn-login').on(
		'click',
		function() {
			for (let i = 0; i < $('input').length; i++) {
				if (!$('input')[i].checkValidity()) {
					$('#submit-for-validation').trigger("click");
					return;
				}
			}
			const userId = $('#user-id').val();
			const passwordSha = new jsSHA("SHA-256", "TEXT");
			passwordSha.update($('#password').val());

			const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), "login", [userId,
				passwordSha.getHash("HEX")], function(data) {
					if (data.result) {
						location.href = "open-sessions.html";
						return;
					} else {
						flashError(data.error.detail.split(":")[1]);
					}
				}, function(data, textStatus, errorThrown) {
					flashError("Fail to login.");
				}));
			client.rpc();
		});

	checkDevice("#os-browser-check-result", function() {
		checkWebsocket();
	});

});
