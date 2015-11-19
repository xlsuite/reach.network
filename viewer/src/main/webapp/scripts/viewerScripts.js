// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

var is_chrome;

(function() {
	try {
		is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

		window.onload = function() {
		    // Initialize page style...
		    document.body.style["MozUserSelect"] = "none";
		    document.body.style["KhtmlUserSelect"] = "none";
		    document.body.style["WebkitUserSelect"] = "none";
		    document.body.style["UserSelect"] = 'none';
		    
//		    document.body.onmousedown = function(e){if(e.button==1)return false};
		    
		    window.oncontextmenu = function() {
				return false;
			};

            document.onkeypress = function(event) {
                console.log("Document on KeyPress", event);
            };
		};
		
	} catch (err) {
    	parent.writeToLog("Viewer init failed - " + err.message);
	}
}());

function startJSONCall(url) {
    try {
//    	var viewerWidth = $(window).width();
//    	var viewerHeight = $(window).height();
//    	
//    	var fullUrl = url + "?sig=" + dataString + "&width=" + viewerWidth + "&height=" + viewerHeight + "&callback=?";

    	writeToLog("startJSONCall - Getting Data");
    	
    	jQuery.getJSON(url, function(result) {
    	    try { 
    	    	if (url.indexOf("display") != -1 && supportsHtml5Storage()) {
    	    		if (result && result.content) {
    	    			storeViewerResponse(result);
    	    		}
    	    	}
    	    
	        writeToLog("reportDataReady - Status Message - " + result.status.message);
    	    	
	    	reportDataReady(result);
    	    }
    	    catch (err) {
    	    	writeToLog("reportDataReady - " + result + " - " + err.message);
    	    }
		}, "json");
//    	writeToLog("startJSONCall - " + url + " - Started successfully");
    }
    catch (err) {
    	writeToLog("startJSONCall - " + url + " - " + err.message);
    }
}

function supportsHtml5Storage() {
	try {
		return 'localStorage' in window && window['localStorage'] !== null;
	} catch (e) {
		return false;
	}
}

function storeViewerResponse(response) {
	// Put the object into storage
	localStorage.setItem('viewerAPI', JSON.stringify(response));
}

function retreiveViewerResponse() {
	try {
		return localStorage.getItem('viewerAPI') && JSON.parse(localStorage.getItem('viewerAPI'));
	} catch (e) {
		return null;
	}
}

function onPresentationLoad(presFrame) {
	writeToLog("Presentation ID=" + presFrame + " is loaded.");
    
//	reportPresentationLoadEvent(presFrame);
}

function getParentData(id, parentId) {
    try {
    	if (parent) {
    		return parent.getEmbedData(id, parentId);
    	}
    }
    catch (err) {
    	writeToLog("getParentData - " + id + " - " + err.message);
    }
}

//function receivePreviewData(event) {
//	if (event.origin !== "http://rdn-test.appspot.com" && event.origin !== "http://rvauser.appspot.com" && event.origin !== "http://127.0.0.1:8888")
//		return;
//
//	setPreviewData(event.data);
//	// event.source is window.opener
//}

//function getPreviewData() {
//    try {
//        var d = window.opener || window.parent;
//        if (d) {
//           	d.parent.postMessage("ready", "*");
//    		
//    		window.addEventListener("message", receivePreviewData, false);
//    	}
//    }
//    catch (err) {
//    	writeToLog("getPreviewData - " + err.message);
//    }
//}

function getEmbedData(id, parentId) {
	return getEmbedItemData(id, parentId);
}

function embedReady() {
    try {
    	var id = window.name;
    	if (window.name.indexOf("if_") == 0) {
    		id = id.substr(3, id.length);
    	}
    	
    	if (parent) {
    		return parent.itemReady(id, true, true, true, true, true);
    	}
    }
    catch (err) {
    	writeToLog("embedReady - " + id + " - " + err.message);
    }
}

function embedDone() {
    try {
    	var id = window.name;
    	if (window.name.indexOf("if_") == 0) {
    		id = id.substr(3, id.length);
    	}
    	
    	if (parent) {
    		return parent.itemDone(id);
    	}
    }
    catch (err) {
    	writeToLog("embedDone - " + id + " - " + err.message);
    }
}

function itemReady(presFrame, id, canPlay, canStop, canPause, canReportReady, canReportDone) {
	writeToLog("Item ID=" + id + " is ready.");
    
    reportReadyEvent(presFrame, id, canPlay, canStop, canPause, canReportReady, canReportDone);
}

function itemError(presFrame, id, reason) {
	writeToLog("Item ID=" + id + " threw an error. Reason - " + reason);
	
	reportErrorEvent(presFrame, id, reason);
}

function itemDone(presFrame, id) {
	writeToLog("Item ID=" + id + " is finished playing.");
	
	reportDoneEvent(presFrame, id);
}

function playCmd(presFrame, id) {
    document.getElementById(presFrame).contentWindow.playCmd(id);
}

function pauseCmd(presFrame, id, hide) {
    document.getElementById(presFrame).contentWindow.pauseCmd(id);
}

function stopCmd(presFrame, id) {
    document.getElementById(presFrame).contentWindow.stopCmd(id);
}

//gadgets.rpc.register("rsevent_ready", gadgetReady);
//gadgets.rpc.register("rsevent_done", gadgetDone);

//function getParam(param) {
//	return getParameter(param);
//}

function getPlaceholderIFrameIds(presFrame, id) {
	return requestPlaceholderIFrameIds(presFrame, id);
}

function updateVideo(presFrame, html, containerName, htmlName, transition) {
    document.getElementById(presFrame).contentWindow.updateVideo(html, containerName, htmlName, transition);
}

function configureVideo(presFrame, htmlName, newHtmlName, volumeParam, autoHideParam, carryOnParam) {
	document.getElementById(presFrame).contentWindow.configureVideo(htmlName, newHtmlName, volumeParam, autoHideParam, carryOnParam);
}

function destroyFrameElement(presFrame, elementName, containerName) {
	document.getElementById(presFrame).contentWindow.destroyElement(elementName, containerName);
}

function showFrameElement(presFrame, elementName, show) {
//	if (document.getElementById(presFrame).contentWindow.setVisible) {
		document.getElementById(presFrame).contentWindow.setVisible(elementName, show);
//	}
}

function writeToLog(logEntry) {
    var log = document.getElementById('log');  
//    log.innerHTML += logEntry + " --- ";
    
	var ts = new Date();
	var txt = document.createTextNode(ts.toString() + ' - ' + logEntry + '');
	log.appendChild(txt);
}

function createContainer(containerName /*, width, height, top, left */) {
    try {
        myFrame = getElement(containerName);
        if (myFrame == null) {
//          var myFrame = createNewElement(containerName, 'div', width, height, top, left);
        	var myFrame = createNewElement(containerName, 'div', "100%", "100%", 0, 0);
            myFrame.style["overflow"] = "hidden";
            
            document.getElementById("mainDiv").appendChild(myFrame);
        }
    }
    catch (err) {
        writeToLog("createContainer - " + frameName + " - " + err.message);
    }
}

function createPresentation(frameName, containerName, width, height, top, left, html, presentationWidth, presentationHeight, hidePointer, enableScroll) {
    try {   	
        myFrame = getElement(frameName);
        if (myFrame == null) {       	
            var myDiv = createNewElement(frameName, 'div', '100%', '100%', 0, 0);
//            var myFrame = createNewElement('iFrame_' + frameName, 'iFrame', height + scrolly, width + scrollx, 0, 0);
            var myFrame = createNewElement('iFrame_' + frameName, 'iFrame', presentationWidth, presentationHeight, 0, 0);

            if (myDiv != null) {
            	if (enableScroll) {
            		myDiv.style["overflow"] = "auto";
            	}
            	else {
            		myDiv.style["overflow"] = "hidden";
            	}
                myDiv.style["visibility"] = "hidden";
                myDiv.style["opacity"] = 0;
//            	myDiv.style["display"] = "none";

                if (myFrame != null) {
                    myFrame.setAttribute("allowTransparency", true);
                    myFrame.setAttribute("frameBorder", "0");
                    myFrame.setAttribute("scrolling", "no");
                    
                    myFrame.style["position"] = "static";
//                    myFrame.style['margin'] = '-' + scrolly + 'px 0 0 ' + '-' + scrollx + 'px';

                    document.getElementById(containerName).appendChild(myDiv);
                    document.getElementById(frameName).appendChild(myFrame);
                    
                    html = stripMetaRefresh(html);
                    populateIframe('iFrame_' + frameName, html);
                }
            }
            
            if (hidePointer) {
            	var pointerDiv = createNewElement(frameName + '_pointer', 'div', '100%', '100%', 0, 0);

            	if (pointerDiv != null) {
            		pointerDiv.style.zIndex = "999";
            		document.getElementById(frameName).appendChild(pointerDiv);
            	}
            }
        }
    }
    catch (err) {
    	writeToLog("createPresentation - " + frameName + " - " + err.message);
    }
}

function stripMetaRefresh(html) {
  html = html.replace(/meta[ ]+http-equiv=.refresh./gi, 'meta http-equiv="strippedrefresh"');
  return html;
}

function populateIframe(frameName, html) {
	try {
	    var myFrame = getElement(frameName);
	    myFrame = (myFrame.contentWindow) ? myFrame.contentWindow : (myFrame.contentDocument.document) ? myFrame.contentDocument.document : myFrame.contentDocument;
	    myFrame.document.open();
	    myFrame.document.write(html);
	    myFrame.document.close();
	}
	catch (err) {
		writeToLog("populateIframe - " + frameName + " - " + err.message);
	}
}

function getContentWindow(myContainer){
    try {
    	var myContainerFrame = (myContainer.contentWindow) ? myContainer.contentWindow : (myContainer.contentDocument.document) ? myContainer.contentDocument.document : myContainer.contentDocument;

        return myContainerFrame;
    }
    catch (err) {
        return null;
    }
}

function createURL(frameName, containerName, width, height, top, left, scrollx, scrolly, url) {
    try {   	
        myFrame = getElement(frameName);
        if (myFrame == null) {
            var myDiv = createNewElement(frameName, 'div', width, height, top, left);
            var myFrame = createNewElement('iFrame_' + frameName, 'iFrame', width + scrollx, height + scrolly, 0, 0);

            if (myDiv != null) {
                myDiv.style["overflow"] = "hidden";

                if (myFrame != null) {
                    myFrame.style["position"] = "static";
                    myFrame.style['margin'] = '-' + scrolly + 'px 0 0 ' + '-' + scrollx + 'px';
                    
					// Add http:// if no protocol parameter exists
					if (url.indexOf("://") == -1) {
						url = "http://" + url;
					}
                    
                    myFrame.setAttribute("src", url);
                    
                    myFrame.setAttribute("allowTransparency", true);
                    myFrame.setAttribute("frameBorder", "0");
                    myFrame.setAttribute("scrolling", "no");

                    myDiv.style["visibility"] = "hidden";
                    myDiv.style["opacity"] = 0;
//                	myDiv.style["display"] = "none";

                    document.getElementById(containerName).appendChild(myDiv);
                    document.getElementById(frameName).appendChild(myFrame);
                }
            }
        }
    }
    catch (err) {
    	writeToLog("createURL - " + frameName + " - " + err.message);
    }
}

function createNewElement(frameName, type, width, height, top, left) {
    try {
        var myFrame = document.createElement(type);

        if (myFrame != null) {
            myFrame.setAttribute('id', frameName);

            myFrame.style["position"] = "absolute";

            myFrame.style["left"] = setUnit(left);
            myFrame.style["top"] = setUnit(top);
            myFrame.style["width"] = setUnit(width);
            myFrame.style["height"] = setUnit(height);
        }
        return myFrame;
    }
    catch (err) {
    	writeToLog("createNewElement - " + frameName + " - " + err.message);
    }
}

function setUnit(value) {
	if (isNumber(value)) {
		return value + "px";
	}
	return value;
}

function isNumber(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

function getElement(elementName) {
    try {
        var myElement = document.getElementById(elementName);
        return myElement;
    }
    catch (err) {
        return null;
    }
}

function showElement(elementName) {
	showElementAttempt(elementName, 0);
}

function showElementAttempt(elementName, attempt) {
    try {
        var myElement = document.getElementById(elementName);

        if (myElement && myElement.style["visibility"] != "visible") {
	        myElement.style["visibility"] = "visible";
	        myElement.style["opacity"] = 1;
        }
//		if (myElement && myElement.style["display"] != "inline") {
//			myElement.style["display"] = "inline";
//		}
        else if (!myElement && attempt < 3) {
        	setTimeout("showElementAttempt('" + elementName + "', " + attempt++ + ");", 500);
        }
    }
    catch (err) {
    	writeToLog("showElement - " + elementName + " - " + err.message);
    }
}

function hideElement(elementName) {
    try {
        var myElement = document.getElementById(elementName);

        myElement.style["visibility"] = "hidden";
        myElement.style["opacity"] = 0;
//        myElement.style["display"] = "none";
    }
    catch (err) {
    	writeToLog("hideElement - " + elementName + " - " + err.message);
    }
}

// TODO: Deprecated after Lazy Load release
//function destroyContainer(containerName) {
//	destroyElement(containerName, "main");
//}

function destroyElement(elementName, containerName) {
    try {
        var myElement = document.getElementById(elementName);

        document.getElementById(containerName).removeChild(myElement);
    }
    catch (err) {
    	writeToLog("destroyElement - " + elementName + " - " + err.message);
    }
}

function setBackground(elementName, color) {
    try {
//    	$("#mainDiv").css("width", $(window).width());
//    	$("#mainDiv").css("height", $(window).height());

        $("#" + elementName).css("backgroundColor", color);
    }
    catch (err) {
    }
}

function resizeContainer(elementName, width, height) {
    try {
    	$("#" + elementName).css("width", width);
    	$("#" + elementName).css("height", height);
    }
    catch (err) {
    }
}

function startBitlyJSONCall(requestUrl, param) {
    try {
    	var encodedParam = encodeURIComponent(param);
    	var fullUrl = requestUrl + encodedParam + "&format=json&callback=?";

    	jQuery.getJSON(fullUrl, function(result) {
    	    try {   	    	
	    		bitlyResponse(result);
//	        	writeToLog("bitlyResponse - " + result + " - Received successfully");
    	    }
    	    catch (err) {
    	    	writeToLog("bitlyResponse - " + result + " - " + err.message);
    	    }
		}, "json");
//    	writeToLog("startBitlyJSONCall - " + url + " - Started successfully");
    }
    catch (err) {
    	writeToLog("startBitlyJSONCall - " + fullUrl + " - " + err.message);
    }
}

//function loadScriptAsync(url) {
//    var s = document.createElement('script');
//    s.type = 'text/javascript';
//    s.async = true;
//    s.src = 'url';
//    var x = document.getElementsByTagName('script')[0];
//    x.parentNode.insertBefore(s, x);
//}
