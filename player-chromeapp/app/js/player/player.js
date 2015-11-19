// Copyright � 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvPlayer = function () {
	
	var w;
	var PLAYER_SERVER_PORT = 9449;

	var ws;
	var watchdog;
	var configPage;
	var dcPage;
	var apilog = ""; //log available through API
	var _connectionId = -1;
	var restartTimer = 0;
	var portString;

	var setDisplayControlConfig = function() {
		chrome.serial.getDevices(function(ports) {
			
			//log("total serial ports:" + ports.length);
			portString="";
			for (var i=0; i<ports.length; i++) {
				if(i>0) portString=portString+",";
				portString = portString+"\""+ports[i].path+"\"";
				//log(ports[i]);
			}
			configPage = new rvPlayerConfigPage();
			configPage.init();	
		});
	};
		
	this.init = function() {	
	   try {	
		ws = new rvWebServer(PLAYER_SERVER_PORT, onRequest);
		ws.start();
		
		watchdog = new rvWatchdog();
		watchdog.start(reloadViewer);
		
		setDisplayControlConfig();

		
		dcPage = new rvPlayerDCPage();
		dcPage.init();
		
		startTimer_CheckForUpdate();
		
		if (!$rv.debug) {
			startViewerWithDelay();
		}
	   } catch (e) {
	        console.log("rvplayer:init error" + e.message);
	   } 
	};


	this.close = function() {
		ws.stop();
		if (_connectionId > -1) {
			serialDisconnect();
		}
	};

	var log = function(msg) {
		console.log(msg);
	};

	var logToAPi = function(msg) {
		if (apilog.length > 1000) {
			apilog = apilog.substr(-500);
		}
		apilog += "<br>" + new Date().toLocaleString() + " | " + msg;
	};

	var onRequest = function(socketId, cmd, qs, keepAlive) {
		//log("Player request: cmd=" + cmd + " | qs=" + qs);
		
		try {
	        if (cmd === "/ping") {
	        	var cb = ws.getUrlParam(qs, "callback");
				cb = cb ? cb + "();" : "";
				ws.writeTextResponse(socketId, cb, keepAlive, ws.CONTENT_TYPE_JAVASCRIPT);

	        } else if (cmd === "/heartbeat") {
				watchdog.poke();
	        	var cb = ws.getUrlParam(qs, "callback");
				cb = cb ? cb + "();" : "";
				ws.writeTextResponse(socketId, cb, keepAlive, ws.CONTENT_TYPE_JAVASCRIPT);
	        	
	        } else if (cmd === "/set_property") {
	    		if ("true" === (ws.getUrlParam(qs, "reboot_required"))) {
	    			log("reboot_required received");
                                $rv.extLogger.log("reboot_required received", reboot);
	    		} else if ("true" === (ws.getUrlParam(qs, "restart_required"))) {
	    			log("restart_required received");
                                $rv.extLogger.log("restart_required received", restart);
	    		} else if ("true" === (ws.getUrlParam(qs, "update_required"))) {
	    			log("update_required received");
                                $rv.extLogger.log("update_required received", checkForUpdate);
	    		} else	if ("true" === (ws.getUrlParam(qs, "reboot_enabled"))) {
	    			var rebootTime = ws.getUrlParam(qs, "reboot_time");
	    			if (rebootTime) {
		    			setRestartTime(rebootTime); //time to restart (not reboot)
	    			} else {
	    				console.warn("reboot_time param is missing (reboot_enabled=true)");
	    			}
	    		} else if ("off" === (ws.getUrlParam(qs, "display_command"))) {
	    			if($rv.config.dcStatus === "on") {
		    			log("display displayStandby off received");
                                        $rv.extLogger.log("display_standby_off received");
		    			displayStandby(false);
	    			}
	    		} else if ("on" === (ws.getUrlParam(qs, "display_command"))) {
	    			if($rv.config.dcStatus === "on") {
		    			log("display displayStandby on received");
                                        $rv.extLogger.log("display_standby_on received");
		    			displayStandby(true);
		    		}
	    		}
	    		var rotation = ws.getUrlParam(qs, "orientation");
	    		if (rotation != undefined) {
	    			log("setting rotation to " + rotation);
	    			setOrientation(rotation);
	    		}
				ws.writeTextResponse(socketId, "", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
	        } else if (cmd === "/save_property") {
	            
	    		var displayId = ws.getUrlParam(qs,"display_id");
	    		var claimId = ws.getUrlParam(qs,"claim_id");
	    		var server = ws.getUrlParam(qs,"server");
	    		var serverUrl = ws.getUrlParam(qs,"server_url");
	    		var restartViewerParam = ws.getUrlParam(qs,"restart_viewer");
	    		
	    		$rv.config.displayId = (displayId != null) ? displayId : $rv.config.displayId;
    			$rv.config.claimId = (claimId != null) ? claimId : $rv.config.claimId;
    			$rv.config.server = (server != null) ? server : $rv.config.server;
    			$rv.config.serverUrl = (serverUrl != null) ? decodeURIComponent(serverUrl) : $rv.config.serverUrl;

	    		$rv.config.saveDisplayProperties(function() {
		    		if ("true" === restartViewerParam) {
		    			restartViewer();
		    		}
	    		});

				var indicator = document.querySelector(".indicator");
				if (!$rv.config.displayId) {
					indicator.innerText = "";
				}

				ws.writeTextResponse(socketId, "", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
	        } else if (cmd === "/save_dc") {
	            
				var onStr = ws.getUrlParam(qs,"onStr");
	    		var offStr = ws.getUrlParam(qs,"offStr");
	    		var status = ws.getUrlParam(qs,"status");
				var port = ws.getUrlParam(qs,"port");
			    		
			log("dc save status=" + status);
	    		$rv.config.onStr = (onStr != null) ? onStr : $rv.config.onStr;
    			$rv.config.offStr = (offStr != null) ? offStr : $rv.config.offStr;
    			$rv.config.dcStatus = (status != null) ? status : $rv.config.dcStatus;
			$rv.config.port = (port != null) ? port : $rv.config.port;
			
	    		$rv.config.saveDCProperties();
			ws.writeTextResponse(socketId, "", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);	    		
			serialDisconnect();
	        } else if (cmd === "/clear") {
	    		$rv.config.clearStorage();
				ws.writeTextResponse(socketId, "", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
			} else if (cmd === "/restart") {
				log("restart command received");
                                $rv.extLogger.log("restart command received", restart);
			} else if (cmd === "/reboot") {
				log("reboot command received");
                                $rv.extLogger.log("reboot command received", reboot);
	        } else if (cmd === "/shutdown") {
				log("shutdown command received");
                                $rv.extLogger.log("shutdown command received", shutdown);
				//ws.writeTextResponse(socketId, "", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
	        } else if (cmd === "/config") {
	        	//var configHTML = rvGetConfigPageHtml($rv.config.displayId, $rv.config.claimId, $rv.config.server)
	        	var configHTML = configPage.get($rv.config.displayId, $rv.config.claimId, $rv.config.server, $rv.config.viewerServer);
				ws.writeTextResponse(socketId, configHTML, keepAlive, ws.CONTENT_TYPE_TEXT_HTML);
	        } else if (cmd === "/dc") {
	        	console.log("ports string:"+portString);
	        	var configHTML = dcPage.get($rv.config.port, portString, $rv.config.dcStatus, $rv.config.onStr, $rv.config.offStr);
				ws.writeTextResponse(socketId, configHTML, keepAlive, ws.CONTENT_TYPE_TEXT_HTML);
	        } else if (cmd === "/log") {
				ws.writeTextResponse(socketId, apilog, keepAlive, ws.CONTENT_TYPE_TEXT_HTML);
	        } else if (cmd === "/version") {
				ws.writeTextResponse(socketId, $rv.config.appVersion, keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
	        } else {
				ws.writeErrorResponse(socketId, ws.HTTP_BAD_REQUEST_TEXT, keepAlive);
	        }
		} catch (e) {
    			log("rvplayer:onRequest error" + e.message);
                        $rv.extLogger.log("onRequest error");
			ws.writeErrorResponse(socketId, keepAlive, ws.HTTP_BAD_REQUEST_TEXT);
		}
        
	};

	var checkForUpdate = function() {
		log("checking for update...");
		chrome.runtime.requestUpdateCheck(function(status, details) {
			logToAPi("status=" + status);
			if (status == "update_found") {
				log("update pending... Version=" + (details ? details.version : "n/a"));
				//reboot is implemented in onUpdateAvailable in main.js
			} else if (status == "no_update") {
				log("no update found");
			} else if (status == "throttled") {
				log("asking too frequently");
			}
		});
	};
	
	var setOrientation = function(rotation) {
		if(launchData.os == "cros") {
	                chrome.system.display.getInfo(function(displays) {
				console.log("Current: " + displays[0].rotation);
				if(rotation != displays[0].rotation) {
					var info = {rotation:0};
					info.rotation = rotation % 360;
					console.log("New: " + info.rotation);
					chrome.system.display.setDisplayProperties(displays[0].id, info, function(displays) {
						//
					});
				}
			});
		}
	};

////////// SERIAL CONNECT     /////////////////////

	var displayStandby = function(displayOn) {
		var _displayOn = displayOn;
		var onConnectComplete = function(connectionInfo) {
			if (!connectionInfo) {
			    console.log("Serial Connection failed.");
    				return;
  			}
   			// The serial port has been opened. Save its id to use later.
  			_connectionId = connectionInfo.connectionId;
  			console.log("Serial Connected successfully, _connectionId " + _connectionId);  			
  			sendCommand();
		};

		
		var sendCommand = function() {
  			var hexValue;
  			if(_displayOn)
  				hexValue = $rv.config.onStr; // "01302a30413043024332303344363030303103730D";
  			else
  				hexValue = $rv.config.offStr; // "01302a30413043024332303344363030303103730D";
  				
			//hexValue = hexValue.toString (16);
  			console.log("Serial _connectionId:" + _connectionId + ", sending:" + hexValue);  			

			var buf=new ArrayBuffer(hexValue.length/2);
			var bufView=new Uint8Array(buf);
			for (var i=0; i<hexValue.length; i+=2) {
				bufView[((i+2)/2)-1]=parseInt(hexValue.substr(i,2),16);
			}
			console.log("sending "+bufView.length+" bytes");
  			chrome.serial.send(_connectionId, buf, onSendComplete);
		};
		
		var onSendComplete = function(sendInfo) {
   			//if (!(typeof sendInfo.error === "undefined")) {
 				console.log("send complete status:"+sendInfo.error);
			//}
   			chrome.serial.flush(_connectionId, onFlush);
		};

		var onFlush = function(result) {
   			console.log("flush result:"+result);
			if (_connectionId > -1) {
				serialDisconnect();
			}
		};

		var serialDisconnect = function() {
			var onDisconnect = function(result) {
				_connectionId = -1;
	   			console.log("disconnect status:"+result);
			};
	
			console.log("Serial disconnecting");
			if (_connectionId < 0) {
				throw 'Invalid connection';
			}
			chrome.serial.disconnect(_connectionId, onDisconnect);
		};
		
		
		if(_connectionId < 1) {
			console.log("Serial connecting");
			chrome.serial.connect($rv.config.port, {bitrate: 9600}, onConnectComplete);
		} else {
			console.log("Serial already connected sending command");
			sendCommand();
		}
	};
	
			
	var onGetDevices = function(ports) {
	  for (var i=0; i<ports.length; i++) {
	    console.log(ports[i].path);
	  }
	};

	// Convert string to ArrayBuffer
	var convertStringToArrayBuffer=function(str) {
		console.log("string length is :"+str.length/2);
		var buf=new ArrayBuffer(str.length/2);
		var bufView=new Uint8Array(buf);
		for (var i=0; i<str.length; i+=2) {
			bufView[((i+2)/2)-1]=parseInt(str.substr(i,2),16);
		}
		return buf;
	};

	
	var shutdown = function() {
		ws.stop();
		$rv.close();
	};

	var startViewerWithDelay = function() {
		setTimeout(startViewer, 3000);
	};

	var startViewer = function() {
		var viewerUrl = $rv.config.getViewerUrl();
		log("[start Viewer] URL: " + viewerUrl);
		$rv.browser.src = viewerUrl;
	};

	var restartViewer = function() {
		if (!$rv.debug) {
			var viewerUrl = $rv.config.getViewerUrl();
			log("[restart Viewer] URL: " + viewerUrl);
			if ($rv.browser.src === viewerUrl) {
				reloadViewer();
			} else {
				$rv.browser.src = viewerUrl;
			}
		}
	};

	var stopViewer = function() {
		log("[stop Viewer]");
		$rv.browser.stop();
	};

	var reloadViewer = function () {
		log("[reload Viewer]");
		$rv.extLogger.log("reloading viewer", function () {
			$rv.browser.reload();
		});
	};

	var restart = function() {
		//restart command needs to clear browser cache
		
		//do we need to navigate to other page first?
		var onPageLoad = function() {
			$rv.browser.removeEventListener('contentload', onPageLoad);
			//now we can clear cache
			//clearData is still in Dev channel, so using try-catch
			try {
				$rv.cache.workers.clearCache();
				$rv.browser.clearData({since: 0}, {
					appcache: true,
					//cache: true,
					cookies: true,
					//downloads: true,
					//fileSystems: true,
					//formData: true,
					//history: true,
					indexedDB: true,
					localStorage: true,
					//serverBoundCertificates: true,
					//pluginData: true,
					//passwords: true,
					webSQL: true
				}, function () {
					console.log("Cache cleared successfully.");
					restartViewer();
				});
			} catch (e) {
				console.warn("cache clearing failed. Error: " + e.message);
				restartViewer();
			}
		};
		$rv.browser.addEventListener('contentload', onPageLoad);
		$rv.browser.src = "data:text/plain,reloading..."; //or $rv.browser.src="about:blank"
	};

	var reboot = function () {
		chrome.runtime.getPlatformInfo(function (info) {
			if (info.os == "cros") {
				log("[reboot]");
				chrome.runtime.restart();
				//if the app is in non-kiosk mode then as per documentation restart is no-op, and in this case next line restart viewer will be executed.
				restartViewer();
			} else {
				log("[reboot - not supported on " + info.os + ", instead restarting viewer]");
				restartViewer();
			}
		});
	};
	
	var startTimer_CheckForUpdate = function() {
		setTimeout(onTimer_CheckForUpdate,  60 * 60 * 1000); // 1 hour interval
	};

	var onTimer_CheckForUpdate = function() {
		startTimer_CheckForUpdate();
		checkForUpdate();
	};
	
	var setRestartTime = function(restartTimeAsText) {
		var timeDiffMS = parseRestartTime(restartTimeAsText);
		if (timeDiffMS > 0) {
			clearTimeout(restartTimer);
			restartTimer =	setTimeout(reboot, timeDiffMS);
		}
	};
	
	var parseRestartTime = function(restartTime) {
		//returns difference in ms between current time and restartTime
		var res = 0;
		try {
			var da = restartTime.split(":");
			if (da.length == 2) {
				var dt = new Date();
				dt.setHours(da[0]);
				dt.setMinutes(da[1]);
				dt.setSeconds(0);
				//make sure this time has not passed yet
				res = dt.getTime() - new Date().getTime();
				if (res < 0) {
					res += 24*60*60*1000; // add 1 day
				}
			}
		} catch (e) {
		}
		return res;
	}

};

