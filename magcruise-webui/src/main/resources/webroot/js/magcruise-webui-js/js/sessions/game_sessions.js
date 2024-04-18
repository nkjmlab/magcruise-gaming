$(function() {
	function clickTarget() {
		let stopAllEnabled = false;
		let deleteAllEnabled = false;

		$(".checkTarget:checked").each(function() {
			if ($(this).data('status') != '2') {
				stopAllEnabled = true;
				deleteAllEnabled = false;
				return false;
			} else {
				stopAllEnabled = false;
				deleteAllEnabled = true;
			}
		});

		$("#game_sessions_btns input[name=stop_all]").attr("disabled", !stopAllEnabled);
		$("#game_sessions_btns input[name=delete_all]").attr("disabled", !deleteAllEnabled);
	}

	$("#check_all").on('click', function() {
		$("input:checkbox").prop("checked", this.checked);
		clickTarget();
	});

	$(".checkTarget").on('click', clickTarget);
	$("#game_sessions_btns input[type=submit]").attr("disabled", true);
});
