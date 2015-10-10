// Copyright � 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvConfig = function () {

	var PLAYER_NAME = "ReachNetworkPlayerPackagedApp";
	
	var VIEWER_SERVER_PRODUCITON = "http://reachtvnetwork.appspot.com";
	var VIEWER_SERVER_TEST = "http://reachtvnetwork.appspot.com";
	var VIEWER_PATH = "/Viewer.html?player=true&type=display&id=";

	this.displayId = "";
	this.claimId = "";
	this.screenWidth = 0;
	this.screenHeight = 0;
	this.osName = "";
	this.appVersion = "";
	this.dcStatus = "off";
	this.port = "COM1";
	this.onStr = "";
	this.offStr = "";
        this.ipAddress = "";
        this.launchSource = "";
	
	var onInitPrereq = ["loadDisplayProperties", "getPlatformInfo", "getIPAddress", "getLaunchSource"]; //prerequisites to complete before firing onInit
	
	this.server = "production";
	this.serverUrl = VIEWER_SERVER_PRODUCITON;
	this.viewerServer = VIEWER_SERVER_PRODUCITON;
	
	this.init = function(onInitCallback) {
		
		this.getPlatformInfo(onInitCallback); //get OS name

		this.loadDisplayProperties(onInitCallback);

                this.getIPAddress(onInitCallback);

                this.getLaunchSource(onInitCallback);
	};

	this.fireOnInit = function(onInitCallback, completedPrereq) {
		
		for ( var i = 0; i < onInitPrereq.length; i++) {
			if (onInitPrereq[i] === completedPrereq) {
				onInitPrereq.splice(i, 1);
			}
		}
		
		if (onInitPrereq.length == 0 && onInitCallback) {
			onInitCallback();
		}
	};

	this.getViewerUrl = function() {
		this.setViewerServer();
		return this.viewerServer + VIEWER_PATH + this.displayId + "&sysinfo=" + this.getSysInfo();
	};

	this.setViewerServer = function() {
		if (this.server == "test") {
			this.viewerServer = VIEWER_SERVER_TEST;
		} else if (this.server == "url") {
			this.viewerServer = this.serverUrl;
		} else {
			this.viewerServer = VIEWER_SERVER_PRODUCITON;
		}
	};

	this.saveDisplayProperties = function(callback) {
		// use Chrome extension storage API.
		var self = this;
		chrome.storage.local.set({
			'displayId' : this.displayId,
			'claimId' : this.claimId,
			'server' : this.server,
			'serverUrl' : this.serverUrl
		}, function() {
			if (chrome.runtime.lastError) {
			    console.error('saveDisplayProperties error ' + chrome.runtime.lastError);
			}
			if (callback) {
				self.setViewerServer();
				callback();
			}
		});
	};

	this.saveDCProperties = function(callback) {
		// use Chrome extension storage API.
		var self = this;
		chrome.storage.local.set({
			'port' : this.port,
			'dcStatus' : this.dcStatus,
			'onStr' : this.onStr,
			'offStr' : this.offStr
		}, function() {
			if (chrome.runtime.lastError) {
			    console.error('saveDCProperties error ' + chrome.runtime.lastError);
			}
			if (callback) {
				callback();
			}
		});
	};
		
	this.clearStorage = function() {
		this.displayId = "";
		this.claimId = "";
		this.server = "production";
		this.serverUrl = "";
		this.setViewerServer();
		chrome.storage.local.clear(function() {
			if (chrome.runtime.lastError) {
			    console.error('clearStorage error ' + chrome.runtime.lastError);
			}
		});
	};
	
	this.saveScreenSize = function(w,h) {
		this.screenWidth = w;
		this.screenHeight = h;
		chrome.storage.local.set({
			'screenWidth' : this.screenWidth,
			'screenHeight' : this.screenHeight
		}, function() {
			if (chrome.runtime.lastError) {
			    console.error('saveScreenSize error ' + chrome.runtime.lastError);
			}
		});
	};

	this.loadDisplayProperties = function(onInitCallback) {
		var self = this;
		chrome.storage.local.get(['displayId','claimId','server','serverUrl','screenWidth','screenHeight','port','dcStatus','onStr','offStr'], function(items) {
			if (chrome.runtime.lastError) {
			    console.error('loadDisplayProperties error ' + chrome.runtime.lastError);
			} else {
				self.displayId = items.displayId ? items.displayId : "";
				self.claimId = items.claimId ? items.claimId : "";
				self.server = items.server ? items.server : "production";
				self.serverUrl = items.serverUrl ? items.serverUrl : VIEWER_SERVER_PRODUCITON;
				self.screenWidth = items.screenWidth ? items.screenWidth : 600;
				self.screenHeight = items.screenHeight ? items.screenHeight : 400;
				self.port = items.port ? items.port : "COM1";
				self.dcStatus = items.dcStatus ? items.dcStatus : "off";
				self.onStr = items.onStr ? items.onStr : "";
				self.offStr = items.offStr ? items.offStr : "";
				self.setViewerServer();
				console.log('Display ID: ' + self.displayId + ' | Claim ID: ' + self.claimId  + ' | Server: ' + self.server + ' | Server URL: ' + self.serverUrl);
				console.log('screenWidth: ' + self.screenWidth + ' | screenHeight: ' + self.screenHeight);
			}
			
			self.fireOnInit(onInitCallback, "loadDisplayProperties")
		});
	};

	this.getPlatformInfo = function(onInitCallback) {
		var self = this;
		chrome.runtime.getPlatformInfo(function(platformInfo) {
			self.osName = platformInfo.os + "/" + platformInfo.arch;
			self.fireOnInit(onInitCallback, "getPlatformInfo");
		});
    	var appInfo = chrome.runtime.getManifest();
    	this.appVersion = appInfo.version;
	};

        this.getIPAddress = function(onInitCallback) {
          var self = this;
          var xhr = new XMLHttpRequest();
          xhr.responseType = "text";
          xhr.open("GET", "http://ident.me", true);
          xhr.onloadend = function() {
            if (xhr.responseText) {
              self.ipAddress = xhr.responseText;
            } else {
              console.log("Could not get ip address");
            }

            self.fireOnInit(onInitCallback, "getIPAddress");
          };
          xhr.send();
        };

        this.getLaunchSource = function(onInitCallback) {
          var self = this;
          chrome.storage.local.get(["launchSource"], function(resp) {
            self.launchSource = resp.launchSource;
            self.fireOnInit(onInitCallback, "getLaunchSource");
          });
        };

	this.getSysInfo = function() {
		//example: "os={OS}&pn={PlayerName}&iv={InstallerVersion}&jv={CurrentJavaVersion}&ev={CurrentRiseCacheVersion}&pv={CurrentRisePlayerVersion}";

		var res = "os=" + this.osName + "&pn=" + PLAYER_NAME + "&pv=" + this.appVersion;
		return encodeURIComponent(res); 
	};
	
};

