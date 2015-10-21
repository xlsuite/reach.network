"use strict";

describe("Unit Testing - Player init() method", function() {

  var params, url, skin;

  beforeEach(function () {
    params = {width: 1024, height: 768, video: {scaleToFit: true, volume: 50, controls: true, autoplay: true, pause: 10}};

    url = "https://storage.googleapis.com/risemedialibrary-abc123/Widgets%2Ftest.webm";

    skin = "";
  });

  it("should create an instance of PlayerJW and assign it to player variable", function () {
    expect(player).to.be.null;

    init(params, url, skin);

    expect(player).to.exist;
    expect(player).to.be.an("object");
  });

  it("should correctly apply autoPlay value with consideration to params.video.controls", function () {
    init(params, url, skin);
    expect(autoPlay).to.be.true;

    params.video.controls = false;
    params.video.autoplay = false;
    init(params, url, skin);
    expect(autoPlay).to.be.true;
  });

  it("should correctly apply stretching value based on params.video.scaleToFit", function () {
    init(params, url, skin);
    expect(stretching).to.equal("uniform");

    params.video.scaleToFit = false;
    init(params, url, skin);
    expect(stretching).to.equal("none");
  });


});

describe("Unit Testing - PlayerJW object", function() {

  var player;

  beforeEach(function() {
    player = new PlayerJW();
  });

  it("should return correct HTML5 video file type calling getVideoFileType()", function () {
    var baseUrl = "https://storage.googleapis.com/risemedialibrary-abc123/Widgets%2Ftest";

    expect(player.getVideoFileType(baseUrl + ".webm")).to.equal("webm");
    expect(player.getVideoFileType(baseUrl + ".mp4")).to.equal("mp4");
    expect(player.getVideoFileType(baseUrl + ".ogv")).to.equal("ogg");
    expect(player.getVideoFileType(baseUrl + ".ogg")).to.equal("ogg");
  });

  it("should return null as the HTML5 video file type calling getVideoFileType()", function () {
    var baseUrl = "https://storage.googleapis.com/risemedialibrary-abc123/Widgets%2Ftest";

    expect(player.getVideoFileType(baseUrl + ".flv")).to.be.null;
    expect(player.getVideoFileType(baseUrl + ".mov")).to.be.null;
    expect(player.getVideoFileType(baseUrl + ".avi")).to.be.null;
    expect(player.getVideoFileType(baseUrl + ".mpg")).to.be.null;
    expect(player.getVideoFileType(baseUrl + ".wmv")).to.be.null;
  });

});
