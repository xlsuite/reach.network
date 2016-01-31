/* global gadgets, config */

var tag = document.createElement('script');
tag.src = "https://www.youtube.com/iframe_api";

var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

var RiseVision = RiseVision || {};
RiseVision.Video = {};

RiseVision.Video = (function (gadgets) {
  "use strict";

  var _additionalParams;
  var _prefs = null;

  var _currentFile = "";
  var displayId;

  var xlSuitePlaylistRefreshDuration = 5000; //5 sec

  var _playlistChangeTimer = null;

  var youtubePlayerJS = null;

  function _done() {
    gadgets.rpc.call("", "rsevent_done", null, _prefs.getString("id"));
  }

  function _ready() {
    gadgets.rpc.call("", "rsevent_ready", null, _prefs.getString("id"), true, true, true, true, true);
  }

  /*
   *  Public Methods
   */
  function noStorageFile() {
  }

  function onStorageInit(url) {
  }

  function onStorageRefresh(url) {
  }

  function pause() {
  }

  function play() {
  }

  function playerEnded() {
  }

  function playerReady() {
  }

  function startPlaylistChangeTimer() {
    clearTimeout(_playlistChangeTimer);

    _playlistChangeTimer = setInterval(function () {
      console.log("Interval check");

      loadVideoLinkFromXLSuite(displayId, function (videoUrl) {
        if (videoUrl != _currentFile) {
          _currentFile = videoUrl;
          initYTPlayer(_currentFile, _additionalParams.width, _additionalParams.height);
        }
      });

    }, xlSuitePlaylistRefreshDuration);
  }

  function setAdditionalParams(names, values) {
    console.log("additional params values", values);
    if (Array.isArray(names) && names.length > 0 && names[0] === "additionalParams") {
      if (Array.isArray(values) && values.length > 0) {
        _additionalParams = JSON.parse(values[0]);
        _prefs = new gadgets.Prefs();

        if (!_additionalParams) {
          _additionalParams = {};
          console.log("additional params recreated");
        }

        document.getElementById("videoContainer").style.height = _prefs.getInt("rsH") + "px";

        _additionalParams.width = _prefs.getInt("rsW");
        _additionalParams.height = _prefs.getInt("rsH");

        if (_prefs.getString("displayId")) {
          displayId = _prefs.getString("displayId");
          console.log("displayId set from chromeApp param", displayId);
        } else {
          displayId = _additionalParams.displayId;
          console.log("displayId set from default settings", displayId);
        }

        loadVideoLinkFromXLSuite(displayId, startXLSuitePlayer, function() {
          if (displayId != _prefs.getString("displayId")) {
            displayId = _additionalParams.displayId;
            console.log("Trying to use default displayId", displayId);
            loadVideoLinkFromXLSuite(displayId, startXLSuitePlayer);
          }
        });
      }
    }
  }

  function startXLSuitePlayer(videoUrl) {
    _currentFile = videoUrl;
    initYTPlayer(_currentFile, _additionalParams.width, _additionalParams.height);
    startPlaylistChangeTimer();

    _ready();
  }

  function initYTPlayer(playlistId, width, height) {
    console.log("Video url, w, h", playlistId, width, height);

    youtubePlayerJS = new YT.Player('mainIframe', {
      width: width,
      height: height,
      playerVars: {
        listType:'playlist',
        list: playlistId,
        'autoplay': 1,
        'controls': 0,
        'loop': 1,
        'showinfo': 0
      },
      events: {
        'onError': onPlayerError,
        'onReady': onPlayerReady,
        'onStateChange': onPlayerStateChange
      }
    });
  }

  function onPlayerError(event) {
    console.log("Player error code", event);
    if (youtubePlayerJS && youtubePlayerJS != null && event && event.data >= 100) {
      youtubePlayerJS.nextVideo();
      console.log("Error, playing next video", event);
    }
  }

  function onPlayerStateChange(event) {
  //  console.log("On player state change", event);
  }

  function onPlayerReady(event) {
//    console.log("On player ready", event);
  }

  function playerError(error) {
    console.debug("video-folder::playerError()", error);

    // act as though video has ended
    playerEnded();
  }

  function stop() {
    pause();
  }

  return {
    "noStorageFile": noStorageFile,
    "onStorageInit": onStorageInit,
    "onStorageRefresh": onStorageRefresh,
    "pause": pause,
    "play": play,
    "setAdditionalParams": setAdditionalParams,
    "playerEnded": playerEnded,
    "playerReady": playerReady,
    "playerError": playerError,
    "stop": stop
  };

})(gadgets);
