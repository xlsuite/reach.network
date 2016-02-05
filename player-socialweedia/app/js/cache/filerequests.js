// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvFileRequestInfo = function(url, downloadComplete) {
	
	this.url = url;
	this.lastRequested = new Date(2000, 1, 2, 3, 4, 5, 6); //set it to previous date so beginRequest() returns true on first call
	this.downloadComplete = downloadComplete;

	this.setDownloadComplete = function(value) {
		this.downloadComplete = value;
		if (!value) {
			this.lastRequested = new Date();
		}
	};

};

rvFileRequests = function() {
	
	var REQ_OFFSET_MINUTES = 15*60*1000; //15 minutes threshold to check if file is modified.
	var map = new rvHashTable();

	this.register = function(url) {
		var fr = map.get(url);
		if (!fr) {
			fr = new rvFileRequestInfo(url, true);
			map.put(url, fr);
		}
	};

	this.beginRequest = function(url) {
		var res = false;
		var fr = map.get(url);
		if (!fr) {
			//this should never happen because register() is always called first
			fr = new rvFileRequestInfo(url, false);
			map.put(url, fr);
			res = true;
		} else {
			//check if enough time (15 minutes) past since last request and if it's not downloading
			if (fr.downloadComplete) {
				var frOffsetM = new Date().getTime() - REQ_OFFSET_MINUTES;
				if (fr.lastRequested.getTime() < frOffsetM) {
					res = true;
					fr.setDownloadComplete(false);
				}
			} 
		}
		return res;
	};

	this.endRequest = function(url) {
		var fr = map.get(url);
		if (fr != null) {
			fr.setDownloadComplete(true);
		}
	};
	
};
