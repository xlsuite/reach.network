// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

"use strict";

var playerError = false;
//var isLoading = true;

//var player = null;

function loadVideo(url, type) {

 // player = new PlayerJW();

  if (type == "mov" || url.indexOf(".mov") != -1) {
    type = "mp4";
  }

  jwplayer("flash").setup({
    type : type,
    file : url,
    width : "100%",
    height : "100%",
    controls: false,
    volume : 0,
    mute : true,
    stretching : "uniform",
    primary: "flash",
    skin: "skin/RVSkin.xml",
    events : {
      onReady : function(event) {
        onPlayerReady(event);
      },
      onComplete : function(event) {

      },
      onError : function(error) {
        onPlayerError(error);
      },
      onPlay : function(event) {
        onPlay(event);
      }
    }
  });

}

// The only way to tell if the URL is valid is to start playing the video and
// see if the player throws an error.
// Applies to JW Player only.
function onPlayerReady(event) {
  jwplayer().play(true);
}

function onPlay(event) {
//	if (isLoading) {
//		isLoading = false;
//		jwplayer().pause(true);
//		jwplayer().seek(0);
//	}
  // Seek starts video playing again, so pause once again.
  jwplayer().pause(true);
}

function onPlayerError(error) {
  // Video can't be played.
  if (error) {
    document.getElementById("flash").style.display = "none";
    playerError = true;
  }
}
