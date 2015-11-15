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

    var chanelUrl = data.screen.channel_url;

    //todo: save channel URL to some settings / variable
    console.log("Loaded channel URL", chanelUrl);

    var fullUrl = chanelUrl + "&amp;autoplay=1&amp;controls=0&amp;showinfo=0 frameborder=0";

    successCallback(fullUrl);

    //RN - https://www.youtube.com/embed/videoseries?list=PLn56VbxOS77fd-qbZw0mvnS2Pm__tvSHZ
    //my - https://www.youtube.com/embed/videoseries?list=PL48ZGwCpwPyFViELgsnvUknRzJyo2gOhA
  });

}
