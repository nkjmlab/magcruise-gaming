$(function() {
	if ($("body").find("input[type=text]").length > 0) {
		$("input[type=text]").get(0).focus();
	}
});

$(function() {
	$(document).on("keypress", "input:not(.allow_submit)", function(event) {
		return event.which !== 13;
	});
});
