// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).
'use strict';

var $rv = $rv || {}; //Rise Vision namespace
console.log(launchData);

$rv.debug = launchData.debugMode;

onload = function (e) {
    console.log('body.onload is called');

    var w = chrome.app.window.current();
    w.setBounds({
        left: launchData.windowOptions.left,
        top: launchData.windowOptions.top,
        width: launchData.windowOptions.width,
        height: launchData.windowOptions.height
    });

    w.onBoundsChanged.addListener(updateDisplayProperties);
    $rv.browser = document.querySelector('#viewer');

	var indicator = document.querySelector(".indicator");

	$rv.browser.addEventListener("loadstart", function() {
		indicator.innerText = "";
	});

	$rv.browser.addEventListener("loadstop", function() {
		if ($rv.config.displayId) {
			indicator.innerText = "";
		} else {
			indicator.innerText = "Press Ctrl+Q to enter Display ID; Ctrl+W to exit";
		}
	});

    $rv.browser.addEventListener('exit', function(e) {
    	console.log("[WebView.exit] reason: " + e.reason);
		if (e.reason === 'crash' || e.reason === 'crashed') {
			//$rv.browser.src = 'data:text/plain,Browser crashed. It will restart shortly!';
			//TODO: restart WebView ASAP
		}
	});

    $rv.browser.addEventListener('permissionrequest', function(e) {
	    console.log('Permission '+e.permission+' requested by webview');
    	  if ( e.permission === 'geolocation' ) {
    	    e.request.allow();
    	  }
    	  else if ( e.permission === 'loadplugin' ) {
    	    e.request.allow();
    	  } else {
    	    e.request.deny();
    	  }
    });

	if ($rv.debug) {
		window.onresize = resizeBrowser;

//      document.querySelector('#btClose').onclick = function() {
//    	window.close();
//    };

		var btRefresh = document.querySelector('#btRefresh');
		if (btRefresh) {
			btRefresh.style.display = "inline";
			btRefresh.onclick = function () {
				if ($rv.browser) {
					$rv.browser.reload();
				}
			};
		}

		var btCacheStatus = document.querySelector('#btCacheStatus');
		if (btCacheStatus) {
			btCacheStatus.style.display = "inline";
			btCacheStatus.onclick = function () {
				$rv.cache.printStats();
			};
		}
	}

    try {
        $rv.config = new rvConfig();
        $rv.config.init(onConfigLoad);
        resizeBrowser();
    } catch (e) {
        console.log("onload error" + e.message);
    }
};

var onConfigLoad = function () {

	try {
		$rv.extLogger = new rvExtLogger();
		$rv.extLogger.log("launch from " + $rv.config.launchSource);

		$rv.cache = new rvCache();
		$rv.cache.init($rv.config);

		$rv.player = new rvPlayer();
		$rv.player.init($rv.config);
		$rv.extLogger.log("player initialized");

		resizeBrowser();
	} catch (e) {
		console.log("onConfigLoad error" + e.message);
	}
};

function resizeBrowser() {
	$rv.heightOffset = $rv.debug ? 50 : 0;
	if ($rv.browser) {
		var windowWidth = launchData.windowOptions.width;
		var windowHeight = launchData.windowOptions.height - $rv.heightOffset;
		$rv.browser.style.width = windowWidth + "px";
		$rv.browser.style.height = windowHeight + "px";
		//$rv.config.saveScreenSize(windowWidth, windowHeight);
		console.log('resize to screenWidth = ' + windowWidth + ' | screenHeight = ' + windowHeight);
	}
}

function updateDisplayProperties() {
	console.log('updateDisplayProperties event called');
	chrome.system.display.getInfo(function (displays) {
		console.log(displays[0].bounds.left);
		var f_mon = 0;
		var f_left = 0;
		var f_top = 0;
		var max_width = 0;
		var max_height = 0;
		var total_width = 0;
		var total_height = 0;
		for (var i = 0; i < displays.length; i++) {
			var display = displays[i];
			if (launchData.os == "win") {
				if (display.bounds.left > f_left) {
					f_left = display.bounds.left;
					f_mon = i;
				}
				if (display.bounds.top > f_top) {
					f_top = display.bounds.top;
					f_mon = i;
				}
				if (display.bounds.width > max_width) {
					max_width = display.bounds.width;
				}
				if (display.bounds.height > max_height) {
					max_height = display.bounds.height;
				}
			} else {
				if (display.bounds.left === 0 && display.bounds.top === 0) {
					total_width = display.bounds.width;
					total_height = display.bounds.height;
				}
			}
		}

		if (launchData.os == "win") {
			var display = displays[f_mon];
			launchData.windowOptions.width = display.bounds.left + max_width;
			launchData.windowOptions.height = display.bounds.top + max_height;
		} else {
			launchData.windowOptions.width = total_width;
			launchData.windowOptions.height = total_height;
		}

		if (launchData.os != "win" || displays.length === 1) {
			launchData.windowOptions.state = launchData.debugMode ? 'normal' : 'fullscreen';
		}
		resizeBrowser();
	});
}


$rv.close = function () {
	window.close();
};

$rv.onSocketCreated = function (socketId) {
	launchData.sockets.push(socketId);
};
