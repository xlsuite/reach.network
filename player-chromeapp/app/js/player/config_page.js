// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

function rvPlayerConfigPage() {

	var pageHTML = "";
	
	this.get = function(displayId, claimId, server, serverUrl) {
		var res = pageHTML.replace("[DISPLAY_ID]", displayId);
		res = res.replace("[CLAIM_ID]", claimId);
		res = res.replace("[SERVER]", server);
		res = res.replace("[SERVER_URL]", serverUrl);
		return res;
	}
	
	this.init = function() {
		download(chrome.runtime.getURL("config_page.html"));
	}
	
	var download = function(fileUrl) {
	    var xhr = new XMLHttpRequest();
	    xhr.responseType = "text";
	    //xhr.onerror = ???;
	    xhr.onload = function(xhrProgressEvent) {
	    	pageHTML = xhrProgressEvent.target.responseText;
	    }
	    xhr.open('GET', fileUrl, true); //async=true
	    xhr.send();
	};


	
}