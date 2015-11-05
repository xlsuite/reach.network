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

  var _refreshDuration = 900000,  // 15 minutes
    _refreshIntervalId = null;

  var _noFileTimer = null,
    _noFileFlag = false;

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
    console.log("Play event ");
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

        if (_currentFile && _currentFile !== "") {
          // add frame and create the player
          _frameController.add(0);
          _frameController.createFramePlayer(0, _additionalParams, _currentFile, config.SKIN, "player.html");
        }

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

  function setAdditionalParams(names, values) {
    var str;

    if (Array.isArray(names) && names.length > 0 && names[0] === "additionalParams") {
      if (Array.isArray(values) && values.length > 0) {
        _additionalParams = JSON.parse(values[0]);
        _prefs = new gadgets.Prefs();

        document.getElementById("videoContainer").style.height = _prefs.getInt("rsH") + "px";

        _additionalParams.width = _prefs.getInt("rsW");
        _additionalParams.height = _prefs.getInt("rsH");

        _frameController = new RiseVision.Common.Video.FrameController();

        _isStorageFile = false;

        str = _additionalParams.url.split("?");

        // store this for the refresh timer
        _separator = (str.length === 1) ? "?" : "&";
        _currentFile = _additionalParams.url;

        loadVideoLinkFromXLSuite(function (videoUrl) {
          _currentFile = videoUrl;

          var $mainIframe = $('#mainIframe');
          $mainIframe.attr('src', _currentFile);
          $mainIframe.attr('width', _additionalParams.width);
          $mainIframe.attr('height', _additionalParams.height);

          _ready();
        });
      }
    }
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
