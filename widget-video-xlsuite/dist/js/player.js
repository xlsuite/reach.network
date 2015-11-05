'use strict';

var file;
var controls, volume, autoPlay, stretching;
var width, height, skin;

var player = null;

function init(params, url, skinVal) {
  window.oncontextmenu = function() {
    return false;
  };

  width = params.width;
  height = params.height;
  skin = skinVal;
  controls = params.video.controls;
  volume = params.video.volume;
  stretching = (params.video.scaleToFit) ? "uniform" : "none";
  file = url;

  // ensure autoPlay is true if controls value is false, otherwise use params value
  autoPlay = true;//(!controls) ? true : params.video.autoplay;

  player = new PlayerJW();
}

function load() {
  player.loadVideo();
}

function doneEvent() {
  if (window.parent !== window.top) {
    parent.RiseVision.Video.playerEnded();
  }
}

function readyEvent() {
  if (window.parent !== window.top) {
    parent.RiseVision.Video.playerReady();
  }
}

function errorEvent(data) {
  if (window.parent !== window.top) {
    parent.RiseVision.Video.playerError(data);
  }
}

function playYoutubeEmbedded() {
  var $mainIframe = document.getElementById("mainIframe");//$('#mainIframe');
  //$mainIframe.attr('src', file);
  //$mainIframe.attr('width', width);
  //$mainIframe.attr('height', height);

  $mainIframe.setAttribute('src', file);
  $mainIframe.setAttribute('width', width);
  $mainIframe.setAttribute('height', height);
}

function play() {
 // playYoutubeEmbedded();
  player.play();
}

function pause() {
  player.pause();
}

function stop() {
  player.stop();
}

function remove() {
  player.remove();
}

function getPlaybackData() {
  return {
    duration: player.getDuration(),
    position: player.getPosition()
  }
}

function PlayerJW() {

  function onVideoComplete() {
    doneEvent();
  }

  function onPlayerError(error) {
    if (error) {
      errorEvent({
        type: "video",
        message: error.message
      });
    }
  }

  function onSetupError(error) {
    if (error) {
      errorEvent({
        type: "setup",
        message: error.message
      });
    }
  }

  this.getVideoFileType = function (url) {
    var extensions = [".mp4", ".webm", ".ogg", ".ogv"],
      urlLowercase = url.toLowerCase(),
      type = "",
      i;

    for (i = 0; i <= extensions.length; i += 1) {
      if (urlLowercase.indexOf(extensions[i]) !== -1) {
        type = extensions[i].substr(extensions[i].lastIndexOf(".") + 1);
        break;
      }
    }

    if (type === "ogv") {
      type = "ogg";
    }

    return type;
  };

  this.loadVideo = function() {
    jwplayer("player").setup({
      "file":"",
      "type":"mp4",
      width : 0,
      height : 0,
      controls: true,
      stretching : "uniform",
      primary: "player",
      skin: skin
    });

    jwplayer().onSetupError(function (error) {
      onSetupError(error);
    });

    jwplayer().onReady(function () {
      var elements = document.getElementById("player").getElementsByTagName("*"),
        total = elements.length,
        i;

      // Workaround for Chrome App Player <webview> not handling CSS3 transition
      for (i = 0; i < total; i += 1) {
        elements[i].className += " notransition";
      }

      document.getElementById("player").className += " notransition";

      // Bugfix - issue #36 (JWPlayer context menu)
      document.getElementById("player_menu").className += " disable-context-menu";


      jwplayer().onComplete(function () {
        onVideoComplete();
      });

      jwplayer().onError(function (error) {
        onPlayerError(error);
      });

      jwplayer().setVolume(volume);

      //if (controls && !autoPlay) {
      //  jwplayer().setControls(true);
      //}

      readyEvent();

    });
  };

  this.play = function() {
    if (autoPlay) {
      if (controls && !jwplayer().getControls()) {
        // Will be first time player is being told to play so doing this here and not in setup so that controls
        // aren't visible upon playing for the first time.
  //      jwplayer().setControls(true);
      }

//      jwplayer().play();

      if (controls) {
        // workaround for controls remaining visible, turn them off and on again
        //jwplayer().setControls(false);
        //jwplayer().setControls(true);
      }
    }
  };

  this.pause = function() {
    jwplayer().pause();
  };

  this.stop = function() {
    this.pause();
  };

  this.remove = function() {
    jwplayer().remove();
  };

  this.getDuration = function () {
    return jwplayer().getDuration();
  };

  this.getPosition = function () {
    return jwplayer().getPosition();
  }

}
