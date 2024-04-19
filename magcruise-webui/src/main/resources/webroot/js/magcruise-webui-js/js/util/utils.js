$(function() {
	// sprintf.js isRequired.
	fmt = sprintf;
	vfmt = vsprintf;
});

function appendLineAndScrollBottom(v, m) {
	v.append(getFormatedDate() + m + "\n");
	if (v[0] && v[0].scrollHeight) {
		v.scrollTop(v[0].scrollHeight - v.height());
	}
}

function getFormatedDate() {
	return toFormatedDate(Date.now());
}

function toFormatedDate(milliseconds) {
	function padding(str) {
		return ('0' + str).slice(-2);
	}

	const date = new Date(milliseconds);
	let str = [date.getFullYear(), padding(date.getMonth() + 1),
	padding(date.getDate())].join('-');
	str += ' ';
	str += [padding(date.getHours()), padding(date.getMinutes()),
	padding(date.getSeconds())].join(':');
	return "[" + str + "] ";

}

function changeProtocolIfNeeded(url) {
	const currentUrl = parseUri(location);
	if (url.startsWith("http:") && currentUrl.protocol.indexOf("https") != -1) {
		return url.replace("http:", "https:");
	} else if (url.startsWith("ws:")
		&& currentUrl.protocol.indexOf("https") != -1) {
		return url.replace("ws:", "wss:");
	}
	return url;
}
