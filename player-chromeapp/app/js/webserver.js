rvWebServer = function(int_port, requestCallback) {

	this.CONTENT_TYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";
	this.CONTENT_TYPE_TEXT_XML = "text/xml; charset=UTF-8";
	this.CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	this.CONTENT_TYPE_TEXT_HTML = "text/html";
	this.CONTENT_TYPE_VIDEO_MP4 = "video/mp4";

	this.HTTP_VERSION = "HTTP/1.1";
	this.HTTP_OK_TEXT = "200 OK";
	this.HTTP_PARTIAL_CONTENT_TEXT = "206 Partial Content";
// this.HTTP_MOVED_TEMP_TEXT = "302 Found. Rise Cache.";
	this.HTTP_BAD_REQUEST_TEXT = "400 Bad Request";
	this.HTTP_NOT_FOUND_TEXT = "404 Not Found";
//  this.HTTP_CONNECTION_REFUSED_TEXT = "404 Connection refused.Rise Cache.";
//  this.HTTP_CLIENT_TIMEOUT_TEXT = "408 Request Timeout. Rise Cache.";
//  this.HTTP_INSUFFICIENT_SPACE_TEXT = "500 Internal Server Error. Insufficient Space. Rise Cache.";
//  this.HTTP_INTERNAL_ERROR_TEXT = "500 Internal Server Error. Rise Cache.";

	
	var socket = chrome.socket;
	var socketInfo;
	var port = int_port || 9999;
	var requestCallback = requestCallback;
	var useOptimistingAccept = true;
	this.openSockets = new rvHashTable(); // key = socket id


	var stringToUint8Array = function(string, returnBuffer) {
		var buffer = new ArrayBuffer(string.length);
		var view = new Uint8Array(buffer);
		for ( var i = 0; i < string.length; i++) {
			view[i] = string.charCodeAt(i);
		}
		if (returnBuffer)
			return buffer;
		else
			return view;
	};

	var arrayBufferToString = function(buffer) {
		var str = '';
		var uArrayVal = new Uint8Array(buffer);
		for ( var s = 0; s < uArrayVal.length; s++) {
			str += String.fromCharCode(uArrayVal[s]);
		}
		return str;
	};

	var logToScreen = function(log) {
		logger.textContent += log + "\n";
	};

	var acceptNext = function() {
	  try {	
		if (!useOptimistingAccept) {
			socket.accept(socketInfo.socketId, onAccept);
		}
	  } catch (e) {
		console.error(e);
	  }
	};
	
	var acceptNext_Optimistic = function() {
	  try {
		if (useOptimistingAccept) {
			socket.accept(socketInfo.socketId, onAccept);
		}
	  } catch (e) {
		console.error(e);
	  }
	};

	var onAccept = function(acceptInfo) {
	  try {
		console.log("ACCEPT", acceptInfo);
		acceptNext_Optimistic(); // continue accepting connections
		if (acceptInfo.resultCode === 0) {
			readFromSocket(acceptInfo.socketId);
		}
	  } catch (e) {
		console.error(e);
	  }
	};
	
	var readFromSocket = function(socketId) {
	  try {
	  	// Read in the data
		socket.read(socketId, function(readInfo) {
		  try {
			console.log("READ", readInfo);
		        if (readInfo.resultCode < 0) {
		        	console.error("socket is dead");
		                socket.destroy(socketId);
		    		acceptNext();
		                return;
		        }
			// Parse the request.
			var data = arrayBufferToString(readInfo.data);
			if (data.indexOf("GET ") == 0) {
				var headers = data.toLowerCase().split("\n");
				console.log(headers);
				var keepAlive = (headers.indexOf("connection: keep-alive") != -1);
				var range = getRanges(headers);
				
				// we can only deal with GET requests
				var uriEnd = data.indexOf(" ", 4);
				if (uriEnd < 0) { /* throw a wobbler */
					return;
				}
				var uri = data.substring(4, uriEnd);
				// strip query string
				var qs = "";
				var qi = uri.indexOf("?");
				if (qi != -1) {
					qs = uri.substring(qi + 1);
					uri = uri.substring(0, qi);
				}

				var headersObj = headersStrToObj(data.toLowerCase());
				var cmd = uri;
				requestCallback(socketId, cmd, qs, keepAlive, range, headersObj.ifRange, headersObj.ifNoneMatch );

			} else {
				console.warn("invalid request: " + data);
				socket.destroy(socketId);
				//this.openSockets.remove(socketId);
			}
		  } catch (e) {
			console.error(e);
		  }
		});
	  } catch (e) {
		console.error(e);
	  }
	};

	var getRanges = function (headers) {
		var res = null;
		
		try {
			for ( var i = 0; i < headers.length; i++) {
				if (headers[i].indexOf("range: bytes=") == 0) {
					var str = headers[i].substr("range: bytes=".length);
					var ranges = str.split("-");
					if (ranges.length > 0) {
						res = [parseInt(ranges[0])]
						if (ranges.length > 1 && !isNaN(parseInt(ranges[1]))) {
							res.push(parseInt(ranges[1]));
						}
					}
					console.log("requested ranges: " + res);
					break;
				}
			}
		} catch (e) {
			console.error(e);
		}
		
		return res;

	};
	
	this.getUrlParam = function(queryStr, paramName) {
		var res = null;
		queryStr = "&" + queryStr;
		var i = queryStr.indexOf("&" + paramName + "=");
		if (i != -1) {
			var j = queryStr.indexOf("&", i + paramName.length + 2);
			if (j == -1)
				res = queryStr.substr(i + paramName.length + 2);
			else
				res = queryStr.substring(i + paramName.length + 2, j);
		}

		return res;
	};
	
	this.getCommonHeaders = function(keepAlive) {
		return "\nAccess-Control-Allow-Origin: *"
			+ "\nDate: " + (new Date().toUTCString())
			+ "\nServer: Rise Cache"
			+ "\nCache-Control: no-cache"
			+ "\nPragma: no-cache"
			+ "\nExpires: -1"
			+ (keepAlive ? "\nConnection: keep-alive" : ""); 
	};
	
	this.writeTextResponse = function(socketId, msg, keepAlive, contentType, httpCode) {
	  try {
		httpCode = httpCode ? httpCode : this.HTTP_OK_TEXT;

		var ws = this;
		var file = {
			size : msg.length
		};
				
		var header = stringToUint8Array(this.HTTP_VERSION + " " + httpCode
				+ "\nContent-length: " + file.size 
				+ "\nContent-type: " + contentType
				+ this.getCommonHeaders(keepAlive)
				+ "\n\n");
		var outputBuffer = new ArrayBuffer(header.byteLength + file.size);
		var view = new Uint8Array(outputBuffer)
		view.set(header, 0);
		view.set(new stringToUint8Array(msg), header.byteLength);
		socket.write(socketId, outputBuffer, function(writeInfo) {
			console.log("[writeTextResponse] done. writeInfo: ", writeInfo);
			ws.writeResponse_End(socketId, keepAlive);
		});
	  } catch (e) {
		console.error(e);
	  }	
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
					res.ifNoneMatch = value;
				}
			}
		}
		return res;
	};
	
	this.writeResponse_Headers = function(socketId, fileSize, headers, keepAlive, range, ifRange, ifNoneMatch) {
	  try {
		//Content type and length headers may come from cached headers file
		var httpCode = this.HTTP_OK_TEXT;
		
		if (!headers) {
			headers = "Content-length: " + fileSize
				+ "\nContent-type: " + this.CONTENT_TYPE_TEXT_PLAIN;
		}

		if (range && range.length == 2) {
			var addRangeHeader = false;
			if(ifRange) {
				var headersObj = headersStrToObj(headers);
				if(ifRange == headersObj.ETag) {
					addRangeHeader = true;
				}
			} else if(ifNoneMatch) {
				var headersObj = headersStrToObj(headers);
			 	if(ifNoneMatch == headersObj.ETag) {
					addRangeHeader = true;
				}
			} else {
				addRangeHeader = true;
			}
			
			if(addRangeHeader) {
				console.log("[writeTextResponseHeader] adding range header");
				//replace Content-length
				var ha = headers.split("\n");
				for ( var i = 0; i < ha.length; i++) {
					if (ha[i].toLowerCase().indexOf("content-length:") == 0) {
						ha[i] = "Content-length: " + (range[1] - range[0] + 1);
						break;
					}
				}
				headers = ha.join("\n");
				
				httpCode = this.HTTP_PARTIAL_CONTENT_TEXT;
				headers += "\nContent-Range: bytes " + range[0] + "-" + range[1] + "/" + fileSize;
			}
		}
		
		headers += this.getCommonHeaders(keepAlive) + "\nAccept-Ranges: bytes";
		
		
		
		var outputBuffer = stringToUint8Array(this.HTTP_VERSION + " " + httpCode +  "\n" + headers + "\n\n", true);
		socket.write(socketId, outputBuffer, function(writeInfo) {
			console.log("WRITE_HEADERS socketId=" + socketId + " | bytesWritten=" + writeInfo.bytesWritten);
		});
	  } catch (e) {
		console.error(e);
	  }		
	};
	
	this.writeResponse_Body_File = function(socketId, fileSize, headers, keepAlive, range, ifRange, ifNoneMatch, file) {
	  try {	
		var ws = this;
		var chunkSize = 10*1024*1024;
		var startPos = 0;
		var endPos = fileSize-1;
		
		
		if (range && range.length == 2) {
			var addRangeHeader = false;
			if(ifRange) {
				var headersObj = headersStrToObj(headers);
				if(ifRange == headersObj.ETag) {
					addRangeHeader = true;
				}
			} else if(ifNoneMatch) {
				var headersObj = headersStrToObj(headers);
			 	if(ifNoneMatch == headersObj.ETag) {
					addRangeHeader = true;
				}
			} else {
				addRangeHeader = true;
			}
			
			if(addRangeHeader) {
				console.log("[writeResponse_Body_File] using range");
				startPos = range[0];
				endPos = range[1];
			}
		}

		if (startPos > endPos) {
			console.log("startPos > endPos");
			ws.writeResponse_End(socketId, keepAlive);
			return;
		}
		
		if (startPos + chunkSize > endPos + 1) {
			chunkSize = endPos - startPos + 1;
		}
		
		var blobBuffer = file.slice(startPos, chunkSize, ws.CONTENT_TYPE_VIDEO_MP4); //buffer - blob type
		if (blobBuffer.size = 0) {
			// 0 bytes read? error?
			console.log("file.slice returned 0");
			ws.writeResponse_End(socketId, keepAlive);
			return;
		}
		startPos += blobBuffer.size;
		
		var fileReader = new FileReader();
		
		fileReader.onload = function(e) {
			socket.write(socketId, e.target.result, function(writeInfo) {
			  try {	
				if (writeInfo.bytesWritten < 0) {
					console.log("WRITE_BODY socketId=" + socketId + " | error code=" + writeInfo.bytesWritten);
					ws.writeResponse_End(socketId, false);
				} else {
					console.log("WRITE_BODY socketId=" + socketId + " | bytesWritten=" + (writeInfo.bytesWritten) + " | total=" + startPos);
					if (startPos + chunkSize > endPos + 1) {
						chunkSize = endPos - startPos + 1;
					}
					blobBuffer = file.slice(startPos, startPos + chunkSize, ws.CONTENT_TYPE_VIDEO_MP4);
					startPos += blobBuffer.size;
					if (blobBuffer.size > 0) {
						fileReader.readAsArrayBuffer(blobBuffer);
					} else {
						ws.writeResponse_End(socketId, keepAlive);
					}
				}
			  } catch (e) {
			  	console.error(e);
	  		  }	
			});
		};
		
		fileReader.readAsArrayBuffer(blobBuffer);
	  } catch (e) {
		console.error(e);
	  }	
	};

	this.writeResponse_End = function(socketId, keepAlive) {
	  try {
		console.log("WRITE_END socketId=" + socketId + " | " + socketInfo.socketId + " | keepAlive=" + keepAlive);
		if (keepAlive) {
			readFromSocket(socketId);
		} else {
			socket.destroy(socketId);
			acceptNext();
			if (socketId == socketInfo.socketId) {
				console.error("socketId === socketInfo.socketId");
			}
			this.openSockets.remove(socketId);
		}
	  } catch (e) {
		console.error(e);
	  }
	};

	this.writeErrorResponse = function(socketId, errorCodeText, keepAlive) {
		this.writeTextResponse(socketId, errorCodeText, keepAlive, this.CONTENT_TYPE_TEXT_PLAIN, errorCodeText);
	};
	
	this.start = function() {
		console.log("webserver.start()");

		if (!requestCallback) {
			console.error("requestCallback is not defined");
			return;
		}

		socket.create("tcp", {}, function(_socketInfo) {
			console.log("socket created:", _socketInfo);
			socketInfo = _socketInfo;
			$rv.onSocketCreated(socketInfo.socketId);
			socket.listen(socketInfo.socketId, "127.0.0.1", port, 50, function(result) {
                                if (chrome.runtime.lastError) {
                                  $rv.extLogger.log("socket listen error");
                                  console.log("socket listen error: " + chrome.runtime.lastError.message);

								  //todo: remove error details after fixing the issue
                                  $rv.messageWindow("The player could not listen on port " + port +
                                  ". A player or other application may already be using that port." +
								  "\nError was: " + chrome.runtime.lastError.message);
                                  setTimeout(function() {window.close();}, 5500);
                                  return;
                                }
				console.log("LISTENING:", result);
				//Accept the first response
				socket.accept(socketInfo.socketId, onAccept);
			});
		});
	};

	this.stop = function() {
		//socket.disconnect(socketInfo.socketId); - don't do that
		socket.destroy(socketInfo.socketId);
	};

	this.reset = function() {
		var ws=this;
		socket.getInfo(socketInfo.socketId, function(info) {
			if (!info.connected) {
				ws.stop();
				ws.start();
			}
		})
	};

	this.printSocketInfo = function() {
		socket.getInfo(socketInfo.socketId, function(info) {
			console.log("SocketInfo=",info);
		});
	};
	
};
