/* global gadgets, config */

var RiseVision = RiseVision || {};
RiseVision.Video = {};

RiseVision.Video = (function (gadgets) {
  "use strict";

  var _additionalParams;

  var _prefs = null,
    _storage = null,
    _message = null,
    _frameController = null;

  var _playbackError = false,
    _isStorageFile = false,
    _viewerPaused = true;

  var _currentFrame = 0;

  var _separator = "",
    _currentFile = "";

  var displayId;

  var _refreshDuration = 900000,  // 15 minutes
    _refreshIntervalId = null;

  var xlSuitePlaylistRefreshDuration = 5000; //5 sec

  var _noFileTimer = null,
    _noFileFlag = false;

  var _playlistChangeTimer = null;

  var youtubePlayerJS = null;

  /*
   *  Private Methods
   */
  function _clearNoFileTimer() {
    clearTimeout(_noFileTimer);
    _noFileTimer = null;
  }

  function _done() {
    gadgets.rpc.call("", "rsevent_done", null, _prefs.getString("id"));

  }

  function _ready() {
    gadgets.rpc.call("", "rsevent_ready", null, _prefs.getString("id"),
      true, true, true, true, true);
  }

  function _refreshInterval(duration) {
    _refreshIntervalId = setInterval(function videoRefresh() {
      // set new value of non rise-storage url with a cachebuster
      _currentFile = _additionalParams.url + _separator + "cb=" + new Date().getTime();

      // in case refreshed file fixes an error with previous file, ensure flag is removed so playback is attempted again
      _playbackError = false;

    }, duration);
  }

  function _startNoFileTimer() {
    _clearNoFileTimer();

    _noFileTimer = setTimeout(function () {
      // notify Viewer widget is done
      _done();
    }, 5000);
  }

  /*
   *  Public Methods
   */
  function noStorageFile() {
    _noFileFlag = true;
    _currentFile = "";

    _message.show("The selected video does not exist.");

    _frameController.remove(_currentFrame, function () {
      // if Widget is playing right now, run the timer
      if (!_viewerPaused) {
        _startNoFileTimer();
      }
    });
  }

  function onStorageInit(url) {
    _currentFile = url;

    _message.hide();

    if (!_viewerPaused) {
      play();
    }
  }

  function onStorageRefresh(url) {
    _currentFile = url;

    // in case refreshed file fixes an error with previous file, ensure flag is removed so playback is attempted again
    _playbackError = false;
  }

  function pause() {
    var frameObj = _frameController.getFrameObject(_currentFrame);

    _viewerPaused = true;

    if (_noFileFlag) {
      _clearNoFileTimer();
      return;
    }

    if (frameObj) {
      frameObj.pause();
    }
  }

  function play() {
  //  console.log("Play event ");
    var frameObj = _frameController.getFrameObject(_currentFrame);

    _viewerPaused = false;

    if (_noFileFlag) {
      _startNoFileTimer();
      return;
    }

    if (!_playbackError) {
      if (frameObj) {
        frameObj.play();
      } else {

        //if (_currentFile && _currentFile !== "") {
        //  // add frame and create the player
        //  _frameController.add(0);
        //  _frameController.createFramePlayer(0, _additionalParams, _currentFile, config.SKIN, "player.html");
        //}

      }
    } else {
      // This flag only got set upon a refresh of hidden frame and there was an error in setup or video
      // Send Viewer "done"
      _done();
    }
  }

  function playerEnded() {
    _frameController.remove(_currentFrame, function () {
      _done();
    });
  }

  function playerReady() {
    var frameObj;

    // non-storage, check if refresh interval exists yet, start it if not
    if (!_isStorageFile && _refreshIntervalId === null) {
      _refreshInterval(_refreshDuration);
    }

    if (!_viewerPaused) {
      frameObj = _frameController.getFrameObject(_currentFrame);
      frameObj.play();
    }
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

    //if (youtubePlayerJS != null) {
    //  var state = youtubePlayerJS.getPlayerState();
    //  console.log("Timer player state", state);
    //}
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

        _frameController = new RiseVision.Common.Video.FrameController();

        _isStorageFile = false;

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
    var $mainIframe = $('#mainIframe');
    $mainIframe.attr('src', playlistId);
    $mainIframe.attr('width', width);
    $mainIframe.attr('height', height);

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
        'onError': onPlayerError(),
        'onReady': onPlayerReady(),
        'onStateChange': onPlayerStateChange()
      }
    });
  }

  function onPlayerError(event) {
    console.log("Player error code", event);
  //  if (errorCode >= 100) {
    if (youtubePlayerJS && youtubePlayerJS != null) {
      youtubePlayerJS.nextVideo();
      console.log("Error, playing next video", event);
    }
  //  }
  }

  function onPlayerStateChange(event) {
    console.log("On player state change", event);
  }

  function onPlayerReady(event) {
    console.log("On player ready", event);
  }

  function playerError(error) {
    console.debug("video-folder::playerError()", error);

    // flag the video has an error
    _playbackError = true;

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
