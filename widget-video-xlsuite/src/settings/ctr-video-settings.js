angular.module("risevision.widget.video.settings")
  .controller("videoSettingsController", ["$scope", "$log", "commonSettings",
    function ($scope, $log, commonSettings) {

    }])
  .value("defaultSettings", {
    params: {},
    additionalParams: {
      url: "",
      displayId: "12345",
      storage: {},
      video: {
        scaleToFit: true,
        volume: 50,
        controls: true,
        autoplay: true
      }
    }
  });
