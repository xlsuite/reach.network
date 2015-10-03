// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

/*
	<div id="cnt">
	    <img id="image" src="" onload="readyEvent();">
	</div>
*/

//var id;
//var url;
//var scaleToFit;

// Initialize the gadget.
//function loadImage(idParam, width, height, urlParam, backgroundColor, scaleToFitParam) {
//	id = idParam;
//	url = urlParam;
//	scaleToFit = scaleToFitParam;
//	var image = document.getElementById("image");
//
//	if (backgroundColor != "") {
//		document.body.style.background = backgroundColor;
//	}
//
//	var settings = {
//		url : url,
//		rsW : width,
//		rsH : height,
//		callback : function(newWidth, newHeight) {
//			if (newWidth && newHeight) {
//				image.style.width = newWidth;
//				image.style.height = newHeight;
//				
//				document.getElementById("cnt").style.marginLeft = -(newWidth / 2) + "px";
//				document.getElementById("cnt").style.marginTop = -(newHeight / 2) + "px";
//			}
//
//			image.setAttribute("src", this.url);
//
//			// Notify the Presentation that the image was clicked.
//			image.onclick = function() {
//				try {
//					parent.onClick(id);
//				}
//				catch (e) {
//					
//				}
//			}
//		},
//		onerror : function(image) {
//			// Do nothing.
//		}
//	};
//
//	scaleImage(settings);
//}

// Creates an HTML image map from JSON.
function addImageMap(map) {
	var image = document.getElementById("image");
	var imageMap = document.createElement("map");

	if (map.name) {
		image.useMap = "#" + map.name;
		imageMap.setAttribute("name", map.name);
	}

	if (map.areas) {
		for ( var i = 0; i < map.areas.length; i++) {
			var area = document.createElement("area");

			if (map.areas[i].shape) {
				area.setAttribute("shape", map.areas[i].shape);
			}

			if (map.areas[i].coords) {
				area.setAttribute("coords", map.areas[i].coords);
			}

			if (map.areas[i].href) {
				area.setAttribute("href", map.areas[i].href);
			} else {
				area.setAttribute("href", "javascript:onImageClicked('"
						+ map.areas[i].title + "');");
			}

			if (map.areas[i].title) {
				area.setAttribute("title", map.areas[i].title);
			}

			if (map.areas[i].alt) {
				area.setAttribute("alt", map.areas[i].alt);
			}

			imageMap.appendChild(area);
		}
	}

	document.body.appendChild(imageMap);
}

function onImageClicked(tab) {
	try {
		parent.onImageClicked(id, tab);
	}
	catch (e) {
		
	}
}

// Send "READY" event to the Viewer.
//function readyEvent() {
//	parent.itemReady(id, true, true, true, true, true);
//}

// Get the dimenions of an image that will cause it to scale to fit within a
// placeholder.
//function scaleImage(settings) {
//	var objImage = new Image();
//
//	// Use an Image object in order to get the actual dimensions of the image.
//	objImage.onload = function() {
//		var imageWidth, imageHeight, ratioX, ratioY, scale, newWidth, newHeight;
//
//		imageWidth = objImage.width;
//		imageHeight = objImage.height;
//
//		if (scaleToFit && imageWidth > 0 && imageHeight > 0) {
//			// Calculate scale ratios.
//			ratioX = settings.rsW / imageWidth;
//			ratioY = settings.rsH / imageHeight;
//			scale = ratioX < ratioY ? ratioX : ratioY;
//
//			// Calculate and set new image dimensions.
//			newWidth = parseInt(imageWidth * scale, 10);
//			newHeight = parseInt(imageHeight * scale, 10);
//			
//			// Call the callback function and pass the new dimensions.
//			settings.callback(newWidth, newHeight);
//		}
//		else {
//			settings.callback(imageWidth, imageHeight);
//		}
//	}
//
//	// Call the error handler if the image could not be loaded.
//	objImage.onerror = function() {
//		settings.onerror(objImage);
//	}
//
//	objImage.setAttribute("src", settings.url);
//}