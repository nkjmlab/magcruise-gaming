$(function() {
	const password = document.getElementById("password"), confirm_password = document
		.getElementById("confirm-password");

	function validatePassword() {
		if (password.value != confirm_password.value) {
			confirm_password.setCustomValidity("Passwords don't Match");
		} else {
			confirm_password.setCustomValidity('');
		}
	}

	password.onchange = validatePassword;
	confirm_password.onkeyup = validatePassword;
});

$(function() {
	const uri = new parseUri(location);
	let method = "signup";

	if (uri.anchor === "addUser") {
		document.title = "ユーザ追加 | MAGCruise";
		$("h2").text("ユーザ追加");
		$("#notice").hide();
		method = "register";
	}

	$('#sign-up-btn').on('click', function() {
		for (let i = 0; i < $('input').length; i++) {
			if (!$('input')[i].checkValidity()) {
				$('#submit-for-validation').trigger("click");
				return;
			}
		}

		if ($('#password').val().length < 8) {
			swalAlert("パスワードが短すぎます", "パスワードは8文字以上にしてください", "warning");
			return;
		}
		const passwordSha = new jsSHA("SHA-256", "TEXT");
		passwordSha.update($('#password').val());

		const userAccount = {
			userId: $('#user-id').val(),
			groupName: $('#group-name').val(),
			nickname: $('#nickname').val(),
			role: $('#role').val(),
			encryptedInputPassword: passwordSha.getHash("HEX")
		}

		new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), method, [userAccount], function(data) {
			if (data.error == null) {
				location.href = "open-sessions.html";
			} else {
				flashError(data.error.detail.split(":")[1]);
			}
		}, function(data, textStatus, errorThrown) {
			console.error(data);
		})).rpc();
	});
});
