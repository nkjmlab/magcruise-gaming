$(function() {
	if (parseUri(location).anchor === "logout") {
		new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), "logout", [], function() {
		})).rpc();
	}
	$("#tbl-scripts").on(
		'draw.dt',
		function() {
			$('#start-game-modal').on(
				'show.bs.modal',
				function(event) {
					const scriptId = $(event.relatedTarget).data('game-script-id');
					const script = gameScripts[scriptId].script;
					const additionalScript = gameScripts[scriptId].additionalScript;
					const className = gameScripts[scriptId].className;

					const modal = $(this);
					modal.find('.input-script-id').val(scriptId);
					modal.find('.input-script').text(script);
					modal.find('.input-additional-script').text(additionalScript);
					modal.find('.input-class-name').val(className);
					$('#btn-ok-exec').on(
						'click',
						function() {
							const rootBrokerUrl = $('.input-root-broker-url').val();
							$(this).prop("disabled", true);

							let methodName = "";
							const className = modal.find('.input-class-name').val();
							if (currentUser.userId == "demo") {
								methodName = "sendStartDemoGameSession";
							} else if (className || className.trim().length != 0) {
								methodName = "invokeMain";
							} else {
								methodName = "sendStartGameSession";
							}

							const args = [{
								rootBrokerUrl: rootBrokerUrl,
								scriptId: scriptId,
								additionalScript: modal.find('.input-additional-script').val(),
								className: className,
							}];

							const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(),
								methodName, args, function(data) {
									if (data.result) {
										if (currentUser.role != "RESTRICTED_OPERATOR") {
											location.href = "active-sessions.html";
										} else {
											let timerInterval;
											Swal.fire({
												title: "セッション開始中",
												html: "このままお待ちください<br>残り<b></b>ミリ秒",
												timer: 15000,
												onOpen: () => {
													Swal.showLoading()
													timerInterval = setInterval(() => {
														const content = Swal.getContent()
														if (content) {
															const b = content.querySelector('b')
															if (b) {
																b.textContent = Swal.getTimerLeft()
															}
														}
													}, 200)
												},
												onClose: () => {
													clearInterval(timerInterval)
												}
											}).then((result) => {
												location.href = "open-sessions.html";
											}
											);
										}
									} else {
										swalAlert("Error", data.error.detail.split(":")[1],
											"error", function() {
												location.reload();
											});
									}
								}, function(data, textStatus, errorThrown) {
									swalAlert("Error", data.error().responseText, "error",
										function() {
											location.reload();
										});
								}));
							client.setAuth(basicAuthUserId, basicAuthPassword);
							client.rpc();
						});

				});

			$('#edit-script-modal').on('show.bs.modal', function(event) {
				let scriptId = "";
				let script = "";
				let additionalScript = "";
				let description = "";
				let scriptName = "";
				let userId = currentUser.userId;
				let sourceUrl = "";
				let className = "";
				const modal = $(this);

				function updateScriptView() {
					scriptId = $(event.relatedTarget).data('game-script-id');
					script = origGameScripts[scriptId].script;
					additionalScript = origGameScripts[scriptId].additionalScript;
					description = origGameScripts[scriptId].description;
					scriptName = origGameScripts[scriptId].name;
					sourceUrl = origGameScripts[scriptId].sourceUrl;
					userId = currentUser.userId;
					className = origGameScripts[scriptId].className;
					modal.find('.input-script-id').prop("readonly", true);
				}

				function addScriptView() {
					modal.find('.input-script-id').prop("readonly", false);
				}

				let methodName = $(event.relatedTarget).data('method-name');

				if (methodName == "updateScript") {
					updateScriptView();
				} else {
					addScriptView();
				}

				modal.find('.input-script-id').val(scriptId);
				modal.find('.input-script-name').val(scriptName);
				modal.find('.input-script').text(script);
				modal.find('.input-additional-script').text(additionalScript);
				modal.find('.input-description').text(description);
				modal.find('.input-source-url').val(sourceUrl);
				modal.find('.input-class-name').val(className);

				$('#btn-ok-update').on('click', function() {
					$(this).prop("disabled", true);

					const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), methodName, [{
						id: modal.find('.input-script-id').val(),
						name: modal.find('.input-script-name').val(),
						description: modal.find('.input-description').val(),
						script: modal.find('.input-script').val(),
						additionalScript: modal.find('.input-additional-script').val(),
						userId: userId,
						className: modal.find('.input-class-name').val()
					}], function(data) {
						if (data.result) {
							swalAlert("Success", methodName, "info", function() {
								location.reload();
							});
						} else {
							flashError(data.error.detail.split(":")[1]);
						}
					}, function(data, textStatus, errorThrown) {
						flashError("Fail to create a game session.");
					}));
					client.rpc();
				});
			});

			$('.btn-delete').on(
				'click',
				function() {
					const scriptId = $(this).parent().attr("data-game-script-id");
					swalConfirm("削除", "削除してよろしいですか？", "warning", function() {
						const client = new JsonRpcClient(new JsonRpcRequest(getServiceUrl(),
							"deleteScript", [scriptId], function(data) {
								location.reload();
							}));
						client.rpc();
					});
				});
		});
	$("#tbl-scripts").DataTable({
		"pageLength": 100,
		order: Array([1, 'asc'])
	});

});
