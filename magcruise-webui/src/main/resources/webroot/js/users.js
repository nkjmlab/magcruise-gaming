let csvData;

$(function() {
	$("#tbl-users").on(
		'draw.dt',
		function() {
			$('.btn-delete').on(
				'click',
				function() {
					const userId = $(this).parent().attr("data-user-id");
					swalConfirm("削除", "削除してよろしいですか？", "warning", function() {
						const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(),
							"deleteUser", [userId], function(data) {
								location.reload();
							}));
						client.rpc();
					});
				});
			bindCheckAll("#input-check-all", ".input-check-all-target");
			$('#btn-submit').on(
				'click',
				function() {
					swalConfirm("確認", "送信しますか？", "info", function() {
						const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(),
							"uploadUsersCsv", [csvData], function(data) {
								location.reload();
							}));
						client.rpc();
					});
				});
		});
	$("#tbl-users").DataTable({
		"pageLength": 1000,
		order: Array([4, 'desc'])
	});

});

function handleFiles(files) {
	if (files == null || files.length == 0 || files[0] == null) {
		alert("ファイルを取得できませんでした");
		return;
	}
	const file = files[0];
	const fileReader = new FileReader();
	fileReader.onload = function(event) {
		csvData = event.target.result;
		$("#btn-submit").prop("disabled", false);
	};
	// fileReader.readAsText(file);
	fileReader.readAsDataURL(file);
}
