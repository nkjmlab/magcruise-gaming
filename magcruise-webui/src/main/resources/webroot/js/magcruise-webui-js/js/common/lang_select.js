$(document).ready(function() {

	function replaceLang(_href, lang) {
		const href = _href;
		if (!href) {
			return "";
		}
		if (href.match(/(.*?)([\?|&]lang=)(..)(.*)/)) {
			href = RegExp.$1 + RegExp.$2 + lang + RegExp.$4;
		} else if (href.indexOf("?") > 0) {
			href += "&lang=" + lang;
		} else {
			href += "?lang=" + lang;
		}
		return href;
	}

	$("#lang_select").change(function() {
		const lang = $(this).children("option:selected").val();
		const href = replaceLang(window.location.href, lang);
		window.location.href = href;
	});

	if (!window.location.href.match(/[\?|&]lang=../)) {
		return;
	}
	const lang = window.location.href.match(/([\?|&]lang=)(..)/)[2];
	$("#lang_select").val(lang);
	$("a").each(function() {
		const href = $(this).attr("href");
		if (!href || !href.indexOf("?") > 0) {
			return true;
		}
		$(this).attr("href", replaceLang(href, lang));
	});
});
