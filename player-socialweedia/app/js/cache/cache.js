// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

var $rv = $rv || {}; //Rise Vision namespace

rvCache = function () {

	var CACHE_SERVER_PORT = 9494;

	var ws; //web server
	var workers; //list of file manager workers
	var cacheCleaner; //helper object to remove expired files every 24h
	var cacheDir; //cache folder
	var socketRequests = new rvHashTable(); // key = socket; value = request URL
	var fileRequests = new rvFileRequests();  //info about file last modified time
	
	this.init = function() {
	    try {
		ws = new rvWebServer(CACHE_SERVER_PORT, onRequest);
		ws.start();
		
		workers = new rvWorkers(onFileReady, onDownloadIfModified, onClearCache, onGetCachedFiles);
		this.workers = workers;
		cacheCleaner = new rvCacheCleaner();
	    } catch (e) {
	        console.log("rvCache:init error" + e.message);
	    } 
	};
	
	var log = function(msg) {
		console.log(msg);
	}
	
	var onRequest = function(socketId, cmd, qs, keepAlive, range, ifRange, ifNoneMatch) {
		ws.openSockets.put(socketId, null)
		try {
			log("Cache request: cmd=" + cmd + " | socketId=" + socketId + " | qs=" + qs);
	        
	    	var url = ws.getUrlParam(qs, "url");
	    	//var isVideo = cmd && (cmd.toLowerCase().indexOf("/video") === 0);
	    	//var isImage = cmd && (cmd.toLowerCase().indexOf("/image") === 0);
	        
	        if (cmd === "/ping") {
	        	var cb = ws.getUrlParam(qs, "callback");
				cb = cb ? cb + "();" : "";
				ws.writeTextResponse(socketId, cb, keepAlive, ws.CONTENT_TYPE_JAVASCRIPT);
	        } else if (cmd === "/clear_cache") {
	    		socketRequests.put(socketId, {"url": "cmd:clear_cache", "keepAlive": keepAlive});
				workers.clearCache();
	        } else if (cmd === "/get_cached_files") {
	    		socketRequests.put(socketId, {"url": "cmd:get_cached_files", "keepAlive": keepAlive});
			workers.getCachedFiles();
	        } else if (url) {
		    	url = decodeURIComponent(url);
	        	processRequest_GetVideo(socketId, url, keepAlive, range, ifRange, ifNoneMatch);
	        } else {
				log("Unrecognized request. Returning " + ws.HTTP_BAD_REQUEST_TEXT);
				ws.writeErrorResponse(socketId, ws.HTTP_BAD_REQUEST_TEXT, keepAlive);
	        }
		} catch (e) {
			log("Cache onRequest error: " + e.message);
		}
	};

	var processRequest_GetVideo = function(socketId, fileUrl, keepAlive, range, ifRange, ifNoneMatch) {
		//append displayid if available
		if($rv.config.displayId != null) {
			fileUrl = fileUrl + (fileUrl.indexOf('?') > -1 ? (fileUrl.slice(-1) == "&" ? "":"&") : "?" ) + "displayid=" + $rv.config.displayId;
		}
		fileRequests.register(fileUrl);
		var urlIsAlreadyRequested = socketRequests.valExists(fileUrl);
		socketRequests.put(socketId, {"url": fileUrl, "keepAlive": keepAlive, "range": range, "ifRange": ifRange, "ifNoneMatch": ifNoneMatch});
		if (!urlIsAlreadyRequested) {
			workers.getFile(fileUrl);
		}
	};

	var onFileReady = function(requestUrl, file, headers) {
		var socketIds = socketRequests.keySet();
		for (var i = 0; i < socketIds.length; i++) {
			var socketId = socketIds[i];
			var socketInfo = socketRequests.get(socketId);
			if (requestUrl == socketInfo.url) {
				var keepAlive = socketInfo.keepAlive;
				var range = socketInfo.range;
				var ifRange = socketInfo.ifRange;
				var ifNoneMatch = socketInfo.ifNoneMatch;
				socketRequests.remove(socketId);
				socketId = parseInt(socketId, 10);
				log("onFileReady: socketId=" + socketId + " | url=" + requestUrl + " | range=" + range + " | if-Range=" + ifRange + " | If-None-Match=" + ifNoneMatch);	
				
				var headersObj = headersStrToObj(headers);
				if (file) {
					if (range && range.length == 1) {
						range.push(file.size - 1);
					}
					ws.writeResponse_Headers(socketId, file.size, headers, keepAlive, range, ifRange, ifNoneMatch);
					if (file.size) {
						ws.writeResponse_Body_File(socketId, file.size, headers, keepAlive, range, ifRange, ifNoneMatch,  file);
					} else {
						ws.writeResponse_End(socketId, keepAlive);
					}
				} else {
					ws.writeErrorResponse(socketId, ws.HTTP_NOT_FOUND_TEXT, keepAlive);
				}
			}
		}
		if (file) downloadIfModified(requestUrl);
	};
	var headersStrToObj = function(headersStr) {
		var res = null;
		if (headersStr) {
			res = {};
			var arr = headersStr.split("\n");
			for (var i = 0; i < arr.length; i++) {
				var key = arr[i].split(":", 1)[0]; //use split to get the key only because there might be more than one ":"
				key = key ? key.toLowerCase() : "";
				var value = arr[i].substring(key.length+1);
				value = value ? value.trim() : "";
				if ("content-type" == key) {
					res.ContentType = value;
				} else if ("content-length" == key) {
					res.ContentLength = value;
				} else if ("last-modified" == key) {
					res.LastModified = value;
				} else if ("etag" == key) {
					res.ETag = value;
				} else if ("if-range" == key) {
					res.ifRange = value;
				} else if ("if-none-match" == key) {
					res.ETag = value;
				}
				
			}
		}
		return res;
	};
	var onClearCache = function() {
		var socketIds = socketRequests.keySet();
		for (var i = 0; i < socketIds.length; i++) {
			var socketId = socketIds[i];
			var socketInfo = socketRequests.get(socketId);
			if ("cmd:clear_cache" == socketInfo.url) {
				var keepAlive = socketInfo.keepAlive;
				socketRequests.remove(socketId);
				socketId = parseInt(socketId, 10);
				log("onClearCache: socketId=" + socketId);
				ws.writeTextResponse(socketId, "All cached files have been removed.", keepAlive, ws.CONTENT_TYPE_TEXT_PLAIN);
			}
		}
	};

	var onGetCachedFiles = function(files) {
		var socketIds = socketRequests.keySet();
		for (var i = 0; i < socketIds.length; i++) {
			var socketId = socketIds[i];
			var socketInfo = socketRequests.get(socketId);
			if ("cmd:get_cached_files" == socketInfo.url) {
				var keepAlive = socketInfo.keepAlive;
				socketRequests.remove(socketId);
				socketId = parseInt(socketId, 10);
				log("onGetCachedFiles: socketId=" + socketId);
				var htmlStr = JSON.stringify(files);
				console.log(htmlStr);
				ws.writeTextResponse(socketId, htmlStr, keepAlive, ws.CONTENT_TYPE_JAVASCRIPT);
			}
		}
	};
	
	var downloadIfModified = function(fileUrl) {
		//TODO: optimize - use one worker to download modified files
		if (fileRequests.beginRequest(fileUrl)) {
			workers.downloadIfModified(fileUrl);
		}
	};

	var onDownloadIfModified = function(fileUrl) {
		fileRequests.endRequest(fileUrl);
	};
	
	this.printStats = function() {
		log("Open sockets "  + ws.openSockets.keySet());
		ws.printSocketInfo();
		//ws.reset();
	};


};

