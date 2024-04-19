function validatePassword() {
  const password = document.getElementById("password");
  const confirm_password = document.getElementById("confirm-password");

  if (password.value != confirm_password.value) {
    confirm_password.setCustomValidity("Passwords don't Match");
  } else {
    confirm_password.setCustomValidity('');
  }
}

$(function() {
  $('#btn-update').on('click', function() {
    validatePassword();
    for (let i = 0; i < $('input').length; i++) {
      if (!$('input')[i].checkValidity()) {
        $('#submit-for-validation').trigger("click");
        return;
      }
    }
    if ($('#password').val().length < 4) {
      swalAlert("パスワードが短すぎます", "パスワードは4文字以上にしてください", "warning");
      return;
    }

    const uri = new parseUri(location);
    const userId = uri.queryKey['userId'];

    const passwordSha = new jsSHA("SHA-256", "TEXT");
    passwordSha.update($('#password').val());
    const newEncryptedPassword = passwordSha.getHash("HEX");

    const methodName = currentUser.admin ? "updatePasswordByAdmin" : "updatePassword";

    let args;

    if (methodName === "updatePasswordByAdmin") {
      args = [userId, newEncryptedPassword]
    } else {
      const oldPasswordSha = new jsSHA("SHA-256", "TEXT");
      oldPasswordSha.update($('#old-password').val());
      const oldEncryptedPassword = oldPasswordSha.getHash("HEX");
      args = [userId, oldEncryptedPassword, newEncryptedPassword];
    }

    new JsonRpcClient(new JsonRpcRequest(getServiceUrl(), methodName, args, function(data) {
      if (data.error == null) {
        location.href = document.referrer;
      } else {
        flashError(data.error.detail.split(":")[1]);
      }
    }, function(data, textStatus, errorThrown) {
      console.error(data);
    })).rpc();

  });
});
