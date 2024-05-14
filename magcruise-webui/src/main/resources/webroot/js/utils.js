function getServiceUrl() {
	return getBaseUrl(1) + "json/WebUiService";
}

function getBaseUrl(depth) {
	const u = parseUri(document.URL);
	const urlPrefix = u.protocol + "://" + u.authority + "/" + u.directory.split("/")[depth] + "/";
	return urlPrefix;
}

function flashError(msg) {
	$('#flash').empty();
	$('#flash').append($('<div>').addClass('alert alert-danger').text(msg));
}

function flashSuccess(msg) {
	$('#flash').empty();
	$('#flash').append($('<div>').addClass('alert alert-success').text(msg));
}

function swalInput(title, text, inputValue, inputPlaceholder, callback) {
	Swal.fire({
		animation: false,
		title: title,
		input: 'text',
		html: text ? text : null,
		inputPlaceholder: inputPlaceholder,
		inputValue: inputValue,
		inputAttributes: {
			autocapitalize: 'off'
		},
		showCancelButton: true,
	}).then((result) => {
		callback(result.value);
	})
}


function swalConfirm(title, text, type, callback) {
	Swal.fire({
		animation: false,
		title: title,
		html: text ? text : null,
		type: type ? type : null,
		showCancelButton: true
	}).then((e) => {
		if (e.isDismissed) {
			return;
		}
		callback(e);
	});
}


function swalAlert(title, text, type, callback, confirmButtonText) {
	Swal.fire({
		animation: false,
		title: title,
		html: text ? text : null,
		type: type ? type : null,
		confirmButtonText: confirmButtonText ? confirmButtonText : "OK"
	}).then((result) => {
		callback(result);
	})
}

function bindCheckAll(checkAllBtn, checkAllTarget) {
	$(checkAllBtn).on('click', function() {
		$("input:checkbox" + checkAllTarget).prop("checked", this.checked);
	});
	$(checkAllTarget).on(
		'click',
		function() {
			$(checkAllBtn).prop("checked",
				$(checkAllTarget).length === $(checkAllTarget + ":checked").length);
		});
}


window.onerror = function(msg, file, line, col, error) {
	StackTrace.fromError(error)
		.then(function(stackFrames) {
			const errorMsg = msg + '\n';
			errorMsg += stackFrames.map(function(sf) {
				return sf.toString();
			}).join('\n');
			console.error(errorMsg);
			sendLogAux(msg, "ERROR", stackFrames[0]);
		})
		.catch(function(stackFrames) {
			const errorMsg = msg + (stackFrames ? "\n" + stackFrames.toString() : "");
			console.log(errorMsg);
			sendLogAux(errorMsg, "ERROR", "");
		});
};

function sendError(msg) {
	sendLog(msg, "ERROR", 4);
}

function sendWarn(msg) {
	sendLog(msg, "WARN", 4);
}

function sendInfo(msg) {
	sendLog(msg, "INFO", 4);
}

function sendDebug(msg) {
	sendLog(msg, "DEBUG", 4);
}

function sendLog(msg, logLevel, stackNum) {
	const st = StackTrace.getSync();
	sendLogAux(msg, logLevel, st[stackNum]);
}

function getDeviceInfo() {
	const ua = new UAParser().getResult();
	return { browser: ua.browser, os: ua.os, device: ua.device };
}

function sendLogAux(msg, logLevel, stackTrace) {
	setTimeout(function() {
		new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), "sendLog",
			[logLevel, stackTrace, { message: msg, device: getDeviceInfo() }, ""], function(data) {
			})).rpc();
	}, 10);
}
