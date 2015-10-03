window.addEventListener("load", function() {
  var message = window.location.search.split("?message=")[1];
  message = decodeURIComponent(message);
  document.getElementsByTagName("div")[0].innerHTML = message;

  setTimeout(function() {
    window.close();
  }, 5000);
});
