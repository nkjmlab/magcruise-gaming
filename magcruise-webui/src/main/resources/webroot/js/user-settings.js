$(function() {

	$('#btn-update').on('click', function() {

		const userAccount = {
			userId: $('#user-id').val(),
			groupName: $('#group-name').val(),
			nickname: $('#nickname').val(),
			role: $('#role').val(),
		}

		new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), "update", [userAccount], function(data) {
			if (data.error == null) {
				flashSuccess("更新しました");
			} else {
				flashError(data.error.detail.split(":")[1]);
			}
		}, function(data, textStatus, errorThrown) {
			console.error(data);
		})).rpc();
	});

});
