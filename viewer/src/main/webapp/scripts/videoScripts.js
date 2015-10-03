// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

var playerError = false;
var isLoading = true;
//var isHTML5Video = false;
var id;
var url;
var type;
var volume, autoHide, carryOn;
var width, height;

var player = null;

function PlayerJW() {
	this.loadVideo = function() {
//		if (autoHide) {
//			// controlbar = { position: "over", idlehide: true }
//			controlbar = "none";
//		} else {
//			controlbar = "bottom";
//		}
		
//		if (url.indexOf("?") == -1) {
//			url = decodeURIComponent(url);
//			url = url.replace(/\ /g, "+");
//		}
		
//		if (type == "mov" || url.indexOf(".mov") != -1) {
//			type = "mp4";
//		}
		
		var mode = "";
		if (url.indexOf(".mov") != -1 || 
				url.indexOf(".m4v") != -1 ||
				url.indexOf(".mp4") != -1
//				|| type == "mov" || type == "m4v"
					) {
			mode = "flash";
		}
		
//		var mode = "flash";
//		if (url.indexOf(".mov") != -1 || url.indexOf(".mp4") != -1) {
//			mode = "html5";
//		}
		
		jwplayer("flash").setup({
//			flashplayer : "mediaplayer/player.swf",
			type : type,
			file : url,
			width : width,
			height : height,
//			controlbar : controlbar,
			controls: !autoHide,
			volume : 0,
			mute : true,
//			icons : false,
			stretching : "uniform",
			primary: mode,
			skin: "/jwplayer/skins/six.xml",
//			wmode : 'transparent',
			events : {
				onReady : function(event) {
					onPlayerReady(event);
				},
				onComplete : function(event) {
					doneEvent();
				},
				onError : function(error) {
					onPlayerError(error);
				},
				onPlay : function(event) {
					onPlay(event);
				}
			}
		});
		
//		jwplayer().onReady(function(event) {
//			onPlayerReady(event);
//		});
//		jwplayer().onComplete(function(event) {
//			doneEvent();
//		});
//		jwplayer().onError(function(error) {
//			onPlayerError(error);
//		});
//		jwplayer().onPlay(function(event) {
//			onPlay(event);
//		});

//		document.getElementById("html5").style.display = "none";	
		document.getElementById("flash").style.visibility = "hidden";
	};
	
	this.configureVideo = function(newHtmlName, volumeParam, autoHideParam, carryOnParam) {
		jwplayer().setVolume(volume);
		jwplayer().setControls(!autoHide);
		
//		if (autoHide) {
//			jwplayer().getPlugin('controlbar').hide();
//		} else {
//			jwplayer().getPlugin('controlbar').show();
//		}
	};
	
	// The only way to tell if the URL is valid is to start playing the video and
	// see if the player throws an error.
	// Applies to JW Player only.
	function onPlayerReady(event) {
		jwplayer().play(true);

//		if (isLoading) {
//			isLoading = false;
//	
//			jwplayer().setVolume(volume);
//			
//			document.getElementById("flash").style.visibility = "hidden";
//			
//			readyEvent();	
//		}
	}
	
	function onPlay(event) {
		if (isLoading) {
			isLoading = false;

			jwplayer().pause(true);
			jwplayer().seek(0);
			// Seek starts video playing again, so pause once again.
			jwplayer().pause(true);

			jwplayer().setMute(false);
			jwplayer().setVolume(volume);
			
			document.getElementById("flash").style.visibility = "hidden";
			
			readyEvent();		
		}
		else if (document.getElementById("flash").style.visibility == "hidden") {
			jwplayer().seek(0);
			jwplayer().pause(true);
		}
	}
	
	function onPlayerError(error) {
		// Video can't be played.
		if (error) {
			document.getElementById("flash").style.display = "none";
			playerError = true;
			errorEvent();
		}
	}
	
	this.play = function() {
		document.getElementById("flash").style.visibility = "visible";
		
		jwplayer().pause(true);
		jwplayer().play(true);
	};
		
	this.pause = function() {
		if (!carryOn) {
			jwplayer().seek(0);
		}
		jwplayer().pause(true);
		
		document.getElementById("flash").style.visibility = "hidden";
	};
	
	this.stop = function() {
		this.pause();
	};
	
	this.remove = function() {
		jwplayer().remove();
	};	
	
	this.getPosition = function() {
		if (jwplayer().getState() == "PLAYING") {
			return jwplayer().getPosition();
		}
		else {
			return -1;
		}
	};
	
	this.resetPosition = function(nextPosition) {
		jwplayer().seek(nextPosition);
		jwplayer().play(true);
	};
};

function loadVideo(idParam, widthParam, heightParam, urlParam, extensionParam, volumeParam, autoHideParam, carryOnParam) {
	var controlbar;
	url = urlParam;
	type = extensionParam;
	id = idParam;
	volume = volumeParam;
	
	autoHide = autoHideParam;
	carryOn = carryOnParam;
	
	// Maximum volume is 100.
	if (volume > 100) {
		volume = 100;
	}
	
	width = widthParam;
	height = heightParam;
	
	if (player) {
		player.loadVideo();
	}
}

function configureVideo(newHtmlName, volumeParam, autoHideParam, carryOnParam) {
	id = newHtmlName;
	volume = volumeParam;
	autoHide = autoHideParam;
	carryOn = carryOnParam;
	
	volume = volumeParam;
	
	if (player) {
		player.configureVideo();
	}
}

function play() {
	if (!playerError) {
		player.play();
	} else { 
		doneEvent(); 
	} 
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

// sends "READY" event to the Viewer
function readyEvent() {
	parent.itemReady(id, true, true, true, true, true);
}

function errorEvent() {
	parent.itemError(id, "videoError");
}

// sends "DONE" event to the Viewer
function doneEvent() {
	parent.itemDone(id);
}

