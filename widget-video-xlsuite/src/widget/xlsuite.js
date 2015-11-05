'use strict';

function loadVideoLinkFromXLSuite(callback) {
  var apiKey = "2ae45fc2-72b9-45ce-9771-caf0fabf9c97";

  //todo: get display key from settings
  var displayKey = "12345";

  $.ajax({
    url: "https://rn.xlsuite.com/admin/api/liquids/call?api_key=" + apiKey + "&tag=load_screen&display_key=" + displayKey
  }).then(function (data) {
    var chanelUrl = data.screen.channel_url;  //https://www.youtube.com/embed/videoseries?list=PLn56VbxOS77fd-qbZw0mvnS2Pm__tvSHZ

    //todo: save channel URL to some settings / variable
    console.log("Loaded channel URL", chanelUrl);

    $('#mainIframe').attr('src', chanelUrl
    + "&amp;autoplay=1&amp;controls=0&amp;showinfo=0 frameborder=0");

    callback(chanelUrl);
  });

}
