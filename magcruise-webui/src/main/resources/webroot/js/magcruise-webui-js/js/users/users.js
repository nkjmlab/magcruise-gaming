$(function () {
  let deleteAllEnabled = false;

  function clickTarget() {
    if ($(".checkTarget:checked").length != 0) {
      deleteAllEnabled = true;
    } else {
      deleteAllEnabled = false;
    }
    $("#delete_all_btn").attr("disabled", !deleteAllEnabled);
  }

  $("#check_all").on('click', function () {
    $("input:checkbox").prop("checked", this.checked);
    clickTarget();
  });

  $(".checkTarget").on('click', clickTarget);

  clickTarget();

});
