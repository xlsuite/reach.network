/*jshint expr:true */
"use strict";

describe("Unit Tests - Settings Controller", function () {

  var defaultSettings, scope, ctrl;

  beforeEach(module("risevision.widget.video.settings"));

  beforeEach(inject(function($injector, $rootScope, $controller, _commonSettings_) {
    defaultSettings = $injector.get("defaultSettings");
    scope = $rootScope.$new();
    ctrl = $controller('videoSettingsController', {
      $scope: scope,
      commonSettings: _commonSettings_
    });

    scope.settingsForm = {
      $setValidity: function () {
        return;
      },
      videoUrl: {
        $valid: true
      }
    };

    scope.settings = {
      additionalParams: defaultSettings.additionalParams
    };

  }));

  it("should define defaultSettings", function (){
    expect(defaultSettings).to.be.truely;
    expect(defaultSettings).to.be.an("object");
  });

  it('should define additionalParams.storage with valid storage url', function() {
    var url = "https://storage.googleapis.com/risemedialibrary-abc123/Widgets%2Ftest.webm";

    // make a valid storage folder url entry
    scope.settings.additionalParams.url = url;
    scope.$digest();

    expect(scope.settings.additionalParams.storage).to.deep.equal({
      "companyId": "abc123",
      "folder": "Widgets/",
      "fileName": "test.webm"
    });
  });

  it('should reset additionalParams.storage with invalid storage folder url', function() {
    var url = "http:/ww";

    // make an invalid storage folder url entry
    scope.settings.additionalParams.url = url;
    scope.settingsForm.videoUrl.$valid = false;
    scope.$digest();

    expect(scope.settings.additionalParams.storage).to.deep.equal({});
  });

  it('should reset additionalParams.storage with invalid url', function() {
    var url = "https://storage.googleapis.com/risemedialibrary-abc123/Widgets%2Ftest.webm";

    // make an initial correct entry
    scope.settings.additionalParams.url = url;
    scope.$digest();

    // make an invalid url entry
    scope.settings.additionalParams.url = "http:/ww";
    scope.settingsForm.videoUrl.$valid = false;
    scope.$digest();

    expect(scope.settings.additionalParams.storage).to.deep.equal({});
  });


});
