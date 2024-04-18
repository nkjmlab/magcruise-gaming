$(function() {
	const url = window.location.pathname;
	$(".navbar").find("ul li a").each(function() {
		const str = $(this).attr("href");
		const _str = str.substring(str.lastIndexOf('/'), str.length);
		if (url.indexOf(_str) != -1) {
			if (url.indexOf('settings') != -1) {
				$($(".navbar").find(".dropdown").get(0)).addClass("active");
				return;
			}
			$(this).parent().addClass("active");
		}
	});
});
