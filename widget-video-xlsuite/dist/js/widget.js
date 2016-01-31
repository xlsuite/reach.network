/* global config: true */
/* exported config */
if (typeof config === "undefined") {
  var config = {
    SKIN: "skin/RVSkin.xml",
    STORAGE_ENV: "prod"
  };

  if (typeof angular !== "undefined") {
    angular.module("risevision.common.i18n.config", [])
      .constant("LOCALES_PREFIX", "locales/translation_")
      .constant("LOCALES_SUFIX", ".json");
  }
}

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

/* global config */

var RiseVision = RiseVision || {};
RiseVision.Video = RiseVision.Video || {};

RiseVision.Video.Storage = function (data) {
  "use strict";

  var _initialLoad = true;

  /*
   *  Public Methods
   */
  function init() {
    var storage = document.getElementById("videoStorage");

    if (!storage) {
      return;
    }

    storage.addEventListener("rise-storage-response", function(e) {
      if (e.detail && e.detail.url) {

        if (_initialLoad) {
          _initialLoad = false;

          RiseVision.Video.onStorageInit(e.detail.url);
        }
        else {
          // check for "changed" property and ensure it is true
          if (e.detail.hasOwnProperty("changed") && e.detail.changed) {
            RiseVision.Video.onStorageRefresh(e.detail.url);
          }
        }
      }
    });

    storage.addEventListener("rise-storage-no-file", function() {
      RiseVision.Video.noStorageFile();
    });

    storage.setAttribute("folder", data.storage.folder);
    storage.setAttribute("fileName", data.storage.fileName);
    storage.setAttribute("companyId", data.storage.companyId);
    storage.setAttribute("env", config.STORAGE_ENV);
    storage.go();
  }

  return {
    "init": init
  };
};

var RiseVision = RiseVision || {};
RiseVision.Common = RiseVision.Common || {};

RiseVision.Common.Video = RiseVision.Common.Video || {};

RiseVision.Common.Video.FrameController = function () {
  "use strict";

  var PREFIX = "if_";

  function getFrameContainer(index) {
    return document.getElementById(PREFIX + index);
  }

  function getFrameObject(index) {
    var frameContainer = getFrameContainer(index),
      iframe;

    iframe = frameContainer.querySelector("iframe");

    if (iframe) {
      return (iframe.contentWindow) ? iframe.contentWindow :
        (iframe.contentDocument.document) ? iframe.contentDocument.document : iframe.contentDocument;
    }

    return null;
  }

  function _clear(index) {
    var frameContainer = getFrameContainer(index),
      frameObj = getFrameObject(index),
      iframe;

    if (frameObj) {
      iframe = frameContainer.querySelector("iframe");
      frameObj.remove();
      iframe.setAttribute("src", "about:blank");
    }
  }

  function add(index) {
    var frameContainer = getFrameContainer(index),
      iframe = document.createElement("iframe");

    iframe.setAttribute("allowTransparency", true);
    iframe.setAttribute("frameborder", "0");
    iframe.setAttribute("scrolling", "no");

    frameContainer.appendChild(iframe);
  }

  function createFramePlayer(index, params, files, skin, src) {
    var frameContainer = getFrameContainer(index),
      frameObj = getFrameObject(index),
      iframe;

    if (frameObj) {
      iframe = frameContainer.querySelector("iframe");

      iframe.onload = function () {
        iframe.onload = null;

        // initialize and load the player inside the iframe
        frameObj.init(params, files, skin);
        frameObj.load();
      };

      iframe.setAttribute("src", src);
    }

  }

  function hide(index) {
    var frameContainer = getFrameContainer(index);

    frameContainer.style.visibility = "hidden";
  }

  function remove(index, callback) {
    var frameContainer = document.getElementById(PREFIX + index);

    _clear(index);

    setTimeout(function () {
      // remove the iframe by clearing all elements inside div container
      while (frameContainer.firstChild) {
        frameContainer.removeChild(frameContainer.firstChild);
      }

      if (callback && typeof callback === "function") {
        callback();
      }
    }, 200);
  }

  function show(index) {
    var frameContainer = getFrameContainer(index);

    frameContainer.style.visibility = "visible";
  }

  return {
    add: add,
    createFramePlayer: createFramePlayer,
    getFrameContainer: getFrameContainer,
    getFrameObject: getFrameObject,
    hide: hide,
    remove: remove,
    show: show
  };
};

var RiseVision = RiseVision || {};
RiseVision.Common = RiseVision.Common || {};

RiseVision.Common.Message = function (mainContainer, messageContainer) {
  "use strict";

  var _active = false;

  function _init() {
    try {
      messageContainer.style.height = mainContainer.style.height;
    } catch (e) {
      console.warn("Can't initialize Message - ", e.message);
    }
  }

  /*
   *  Public Methods
   */
  function hide() {
    if (_active) {
      // clear content of message container
      while (messageContainer.firstChild) {
        messageContainer.removeChild(messageContainer.firstChild);
      }

      // hide message container
      messageContainer.style.display = "none";

      // show main container
      mainContainer.style.visibility = "visible";

      _active = false;
    }
  }

  function show(message) {
    var fragment = document.createDocumentFragment(),
      p;

    if (!_active) {
      // hide main container
      mainContainer.style.visibility = "hidden";

      messageContainer.style.display = "block";

      // create message element
      p = document.createElement("p");
      p.innerHTML = message;
      p.setAttribute("class", "message");
      p.style.lineHeight = messageContainer.style.height;

      fragment.appendChild(p);
      messageContainer.appendChild(fragment);

      _active = true;
    } else {
      // message already being shown, update message text
      p = messageContainer.querySelector(".message");
      p.innerHTML = message;
    }
  }

  _init();

  return {
    "hide": hide,
    "show": show
  };
};

/* global gadgets, RiseVision */

(function (window, gadgets) {
  "use strict";

  var prefs = new gadgets.Prefs(),
    id = prefs.getString("id");

  // Disable context menu (right click menu)
  window.oncontextmenu = function () {
    return false;
  };

  function play() {
    RiseVision.Video.play();
  }

  function pause() {
    RiseVision.Video.pause();
  }

  function stop() {
    RiseVision.Video.stop();
  }

  function polymerReady() {
    window.removeEventListener("WebComponentsReady", polymerReady);

    if (id && id !== "") {
      gadgets.rpc.register("rscmd_play_" + id, play);
      gadgets.rpc.register("rscmd_pause_" + id, pause);
      gadgets.rpc.register("rscmd_stop_" + id, stop);

      gadgets.rpc.register("rsparam_set_" + id, RiseVision.Video.setAdditionalParams);
      gadgets.rpc.call("", "rsparam_get", null, id, ["additionalParams"]);
    }
  }

  window.addEventListener("WebComponentsReady", polymerReady);

})(window, gadgets);



/* jshint ignore:start */
var _gaq = _gaq || [];

_gaq.push(['_setAccount', 'UA-57092159-2']);
_gaq.push(['_trackPageview']);

(function() {
  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();
/* jshint ignore:end */
