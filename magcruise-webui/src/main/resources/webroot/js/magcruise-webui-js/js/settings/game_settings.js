$(function () {
  let allEnabled = false;

  function clickTarget() {
    if ($(".checkTarget:checked").length != 0) {
      allEnabled = true;
    } else {
      allEnabled = false;
    }
    $(".all_target").attr("disabled", !allEnabled);
  }

  $("#check_all").on('click', function () {
    $("input:checkbox").prop("checked", this.checked);
    clickTarget();
  });

  $(".checkTarget").on('click', clickTarget);

  clickTarget();

});
