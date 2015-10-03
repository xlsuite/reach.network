// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

function rvPlayerDCPage() {

	var pageHTML = "";
	
	this.get = function(port, ports, dcStatus, onStr, offStr) {
		var res = pageHTML.replace("[PORT]", port);
		res = res.replace("[PORTS]", ports);
		res = res.replace("[Status]", dcStatus);
		res = res.replace("[onStr]", onStr);
		res = res.replace("[offStr]", offStr);
		return res;
	}
	
	this.init = function() {
		download(chrome.runtime.getURL("display_page.html"));
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