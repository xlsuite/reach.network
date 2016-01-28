'use strict';

function loadVideoLinkFromXLSuite(displayKey, successCallback, errorCallback) {
  var apiKey = "2ae45fc2-72b9-45ce-9771-caf0fabf9c97";
  console.log("displayKey param", displayKey);

  $.ajax({
    url: "https://rn.xlsuite.com/admin/api/liquids/call?api_key=" + apiKey + "&tag=load_screen&display_key=" + displayKey
  }).then(function (data) {
    if (!data || !data.screen) {
      console.log("Error - Returned data is empty", data);
      if (errorCallback) {
        errorCallback();
      }
      return;
    }

    //todo: save channel URL to some  settings / variable
    var chanelUrl = data.screen.channel_url;
    var channelId = chanelUrl.substring(chanelUrl.indexOf("?list=") + 6);

    console.log("Loaded channel URL, channel ID", chanelUrl, channelId);
    successCallback(channelId);

    //RN - https://www.youtube.com/embed/videoseries?list=PLn56VbxOS77fd-qbZw0mvnS2Pm__tvSHZ
    //my - https://www.youtube.com/embed/videoseries?list=PL48ZGwCpwPyFViELgsnvUknRzJyo2gOhA

    //todo: fix embedded errors
    //https://www.drupal.org/node/1887818
    //http://stackoverflow.com/questions/12522291/pausing-youtube-iframe-api-in-javascript
    //http://stackoverflow.com/questions/8205179/youtube-api-handling-videos-that-have-been-removed-by-youtube

  });

}
