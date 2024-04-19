function pollInputRequests(url, pid, playerName, interval, targetSelector) {
	pollInputRequestsAndCall(url, pid, playerName, interval, targetSelector, url);
}

function pollInputRequestsAndCall(url, pid, playerName, interval, targetSelector, callbackUrl) {
	const request = new JsonRpcRequest(url, "getNewRequestsToInput", [pid, playerName]);
	request.delay = interval;
	request.done = function(data) {
		procInputRequests(data.result, pid, playerName, targetSelector, [callbackUrl]);
	}
	const client = new JsonRpcClient(request);
	client.schedule();
	return client;
}

/*
 * requests[{requestId, label, inputs}]; callback({requestId, inputs});
 */

function procInputRequests(requests, pid, playerName, targetSelector, callbackUrls) {

	function sendInputs(form, callbackUrls) {
		callbackUrls.forEach(function(callbackUrl) {
			const request = new JsonRpcRequest(callbackUrl, "sendInput", [pid, playerName,
				form.attr('requestId'), form.attr('roundnum'), form.inputs]);
			request.done = function(data) {
				console.log(request);
				console.log(data);
				$('form input:visible').first().focus();
			}
			new JsonRpcClient(request).retry(1, function(data) {
			}, function(data, textStatus, errorThrown) {
				//sendLog("InputRequestの送信に失敗." + textStatus + ', ' + errorThrown + '.');
				swalAlert("データの送信に失敗しました．再入力して下さい．", "", "info", function() {
					location.reload();
				});
			});
		});
	}

	if (!requests) { return; }
	for (let i = 0; i < requests.length; i++) {
		const request = requests[i];
		if ($(targetSelector + ' [requestId=' + request.id + ']').length != 0) {
			continue;
		}
		const form = makeForm(request, callbackUrls, sendInputs);
		$(targetSelector).append(form);
		form.on("keypress", "input", function(e) {
			if (e.which == 13) {
				if ($("input", form).length != 0 && $("input[simple_submit]", form).length == 0) {
					confirmSendInputs(form, callbackUrls, sendInputs, true);
				} else {
					confirmSendInputs(form, callbackUrls, sendInputs, false);
				}
				return false;
			}
		});
		form.keydown(function(e) {
			if (e.which == 27) {
				$('form input:visible', form).first().focus();
				return false;
			}
		});
		$('form button:visible').first().focus();
		$('form input:visible').first().focus();
	}
}

function confirmSendInputs(form, callbackUrls, callback, confirm) {
	function getInputs(form) {
		const buff = [];
		for (let i = 0; i < form.attr('num_of_params'); i++) {
			let f = form.find(fmt('[order=%d]', i));
			if (!f[0].checkValidity()) {
				form.children('input[type=submit]').click();
				buff = null;
				return buff;
			}
			let val;
			let optionLabel;
			if (f.attr("type") === "text" || f.attr("type") === "textarea") {
				val = f.val();
			} else if (f.attr("type") === "number") {
				val = Number(f.val());
			} else if (f.attr("type") === "radio") {
				f = form.find(fmt('[order=%d]:checked', i));
				val = f.val();
				optionLabel = $(".radio-input-label", f.parent()).html() + " ";
			} else if (f.attr("type") === "checkbox") {
				val = [];
				optionLabel = [];
				form.find(fmt('[order=%d]:checked', i)).each(function() {
					val.push($(this).val());
					optionLabel.push($(this).parent().html() + " ");
				});
			}

			buff.push({
				label: f.closest('.form-group').children('.control-label').text(),
				name: f.attr("name"),
				type: f.attr("type"),
				value: val,
				optionLabel: optionLabel
			});
		}
		return buff;
	}

	form.inputs = getInputs(form);
	if (form.inputs == null) { return; }

	const tmp = $('<div>').append(form.children('.form-label').html());
	form.inputs.forEach(function(i) {
		if (i.optionLabel === undefined) {
			tmp.append($(T
				.p(T.strong(form.find('label[name="' + i.name + '"]').text() + " = " + i.value))));
		} else {
			tmp.append($('<p>').append($('<strong>').append(i.name + " = ")).append(i.optionLabel));
		}
	});

	if (!confirm) {
		callback(form, callbackUrls);
		form.hide();
		return;
	}

	swalConfirm("", tmp, "", function() {
		callback(form, callbackUrls);
		form.hide();
	});

}

function makeForm(request, callbackUrls, callback) {
	function makeFormGroup(order, m) {
		const fg = $(T.div('.form-group')());
		const label = $(T.label({
			className: '.control-label',
			name: m.name
		}));
		label.append(m.label);
		fg.append($('<div>').append(label));

		if (m.type.toUpperCase() === "TEXT") {
			fg.append($(T.input('.form-control')({
				type: 'text',
				order: order,
				name: m.name,
				value: m.value
			})));
		} else if (m.type.toUpperCase() === "NUMBER") {
			const input = $(T.input('.form-control')({
				type: 'number',
				order: order,
				name: m.name,
				value: m.value
			}));
			if (m.attrs.min != null) {
				input.attr('min', m.attrs.min)
			}
			;
			if (m.attrs.max != null) {
				input.attr('max', m.attrs.max)
			}
			;
			fg.append(input);
		} else if (m.type.toUpperCase() === "TEXTAREA") {
			fg.append($(T.textarea('.form-control')({
				type: 'textarea',
				order: order,
				name: m.name
			}, m.value)));
		} else if (m.type.toUpperCase() === "CHECKBOX") {
			for (let i = 0; i < m.attrs.options.length; i++) {
				const input = T.input({
					type: 'checkbox',
					order: order,
					name: m.name,
					value: m.attrs.options[i]
				});
				if (m.value.indexOf(input.value) != -1) {
					$(input).attr('checked', 'checked');
				}
				fg.append($(T.div({
					className: "checkbox-inline"
				}, input, m.attrs.optionLabels[i])));
			}
		} else if (m.type.toUpperCase() === "RADIO") {
			for (let i = 0; i < m.attrs.options.length; i++) {
				const input = T.input({
					type: 'radio',
					order: order,
					name: m.name,
					value: m.attrs.options[i]
				});
				if (m.value === m.attrs.options[i]) {
					$(input).attr('checked', 'checked');
				}
				fg.append($('<div>)').addClass("radio-inline").append($(input)).append(
					$('<label>').addClass('radio-input-label').append(m.attrs.optionLabels[i])));
			}
		} else if (m.type.toUpperCase() === "MENU") {
			const select = $('<select>').attr('order', order).attr('name', m.name);
			for (let i = 0; i < m.attrs.options.length; i++) {
				const option = $(T.option({
					value: m.attrs.options[i]
				}, m.attrs.optionLabels[i]));
				if (m.value == m.attrs.options[i]) {
					option.attr('selected', 'selected');
				}
				select.append(option);
			}
			fg.append(select);
		} else {
			fg.append($('<p>').text("unknown type: " + m.type));
		}
		if (m.attrs.required != null && m.attrs.required) {
			fg.find('input').each(function() {
				$(this).attr("required", "");
			});
		}
		if (m.attrs.simple_submit != null && m.attrs.simple_submit) {
			fg.find('input').each(function() {
				$(this).attr("simple_submit", "");
			});
		}
		return fg;
	}

	const form = $(T.form('.bg-light.card-body')({
		roundnum: request.roundnum,
		requestId: request.id,
		num_of_params: request.inputs.length
	}));

	const label = $(T.div('.form-label')());
	label.append(request.label);
	form.append(label);

	for (let j = 0; j < request.inputs.length; j++) {
		form.append(makeFormGroup(j, request.inputs[j]));
	}
	let btnName;
	if (form.find("input").length == 0) {
		btnName = 'OK';
	} else {
		btnName = 'Submit';
		// 以下の非表示のsubmitボタンはバリデーションメッセージの表示のために存在している
		form.append($(T.input({
			type: 'submit',
			value: 'submit',
			style: {
				display: 'none'
			}
		})));
	}
	form.append($(T.div('.form-group')(T.button({
		className: 'btn btn-primary',
		type: 'button'
	}, btnName))).on('click', function() {
		if ($("input", form).length != 0 && $("input[simple_submit]", form).length == 0) {
			confirmSendInputs(form, callbackUrls, callback, true);
		} else {
			confirmSendInputs(form, callbackUrls, callback, false);
		}
	}));
	return form;
}
