'use strict';

function loadVideoLinkFromXLSuite(callback) {
  var apiKey = "2ae45fc2-72b9-45ce-9771-caf0fabf9c97";
  var displayKey = "12345";

  callback("https://www.youtube.com/watch?v=P5_GlAOCHyE");

  //$.ajax({
  //  url: "http://reach.network/admin/api/liquids/call?api_key=" + apiKey + "&tag=load_screen&display_key=" + displayKey
  //}).then(function (data) {
  //  var chanelUrl = data.screen.channel_url;
  //  callback("https://www.youtube.com/watch?v=P5_GlAOCHyE");
  //  //todo: set to some settings
  //});

  /*
   {
   "screen": {
   "display_key": "12345",
   "description": null,
   "name": "Test screen",
   "channel_url": "https://www.youtube.com/embed/videoseries?list=PLn56VbxOS77fd-qbZw0mvnS2Pm__tvSHZ"
   }
   }
   */

}
