// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

importScripts("/js/hashtable.js");

rvFileManagerSync = function () {

	//constants
	var HEADER_IF_NONE_MATCH = "If-None-Match";
	var HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	var HEADER_CONTENT_LENGTH = "Content-Length";
	var HEADER_CONTENT_TYPE = "Content-Type";
	var HEADER_LAST_MODIFIED = "Last-Modified";
	var HEADER_ETAG = "ETag";
	var HEADER_FILE_URL = "File-URL";
	var FILE_EXT_DATA = "dat";
	var FILE_EXT_HEADERS = "txt";
	var CACHE_MINIMUM_LIMIT = 512*1024*1024; //512MB
	var FILE_KEEP_IN_CACHE_DURATION_DAYS = 7;
	var FILE_KEEP_IN_CACHE_DURATION_MS = FILE_KEEP_IN_CACHE_DURATION_DAYS * 24 * 60 * 60 * 1000; //milliseconds

	self.requestFileSystem = self.webkitRequestFileSystem || self.requestFileSystem;
	self.directoryEntry = self.webkitDirectoryEntry || self.directoryEntry;
	var fs; //file system 
	var fsCache; //cache folder
	
	this.init = function() {
		// Creating a filesystem
		//we request 5GB, but we get unlimited as specified in manifest
		self.requestFileSystem(self.PERSISTENT, 5*1024*1024*1024 /*5GB*/, onInitFs, errorHandler);
		
	};
	
	function onInitFs(_fs) {
		fs = _fs;
		console.log('Opened file system: ' + fs.name);		
		fs.root.getDirectory("cache", {create : true}, function(dirEntry) {
			console.log('Opened Cache Directory: ' + dirEntry.name);		
			fsCache = dirEntry;
			self.postMessage({'cmd': 'initFS_complete', 'id': id});
		});
	}	

	function errorHandler(e) {
	  var msg = '';
	
	  switch (e.code) {
	    case FileError.QUOTA_EXCEEDED_ERR:
	      msg = 'QUOTA_EXCEEDED_ERR';
	      break;
	    case FileError.NOT_FOUND_ERR:
	      msg = 'NOT_FOUND_ERR';
	      break;
	    case FileError.SECURITY_ERR:
	      msg = 'SECURITY_ERR';
	      break;
	    case FileError.INVALID_MODIFICATION_ERR:
	      msg = 'INVALID_MODIFICATION_ERR';
	      break;
	    case FileError.INVALID_STATE_ERR:
	      msg = 'INVALID_STATE_ERR';
	      break;
	    default:
	      msg = 'Unknown Error';
	      break;
	  };
	
	  log('Error: ' + msg);
	}

	this.fileUrlToFileName = function(str) {
		//get string hash of fileUrl
	    var hash = 0xFFFFFFFF, i, char;
	    hash = hash - hash;
	    if (str.length == 0) return hash;
	    for (var i = 0; i < str.length; i++) {
	        char = str.charCodeAt(i);
	        hash = ((hash<<5)-hash)+char;
	        hash = hash & hash; // bitwise operation converts big number to 32bit integer
	    }
	    hash = intToHex8(hash);
	    return hash;
	};

	var intToHex8 = function(value) {
	    //convert signed integer to unsigned integer
		if (value < 0) {
			value = value >>> 0;
		}

	    //format output to HEX
		var res = value.toString(16).toUpperCase();
	    //make sure the result is 8 char long
		res = "00000000".substr(0,8-res.length) + res;
		
		return res;
		
	};
	
	var getCurrentVersion = function(name, callbackFunc) {
		var maxVersion = -1;


		var dirReader = fsCache.createReader();
		dirReader.readEntries(function(entries) {
			for (var i = 0, entry; entry = entries[i]; ++i) {
				if (entry.isFile && startsWith(entry.name, name) && endsWith(entry.name, FILE_EXT_HEADERS)) {
					var version = extractFileVersionAsInt(entry.name);
					//if (versions) versions.push(version);
					if (version > maxVersion)
						maxVersion = version;
				}
			}
			callbackFunc(maxVersion);
			return;
		}, function(e) {
			callbackFunc(maxVersion);
		});
	};

	this.getFile = function(fileUrl, remainingBytes) {
		
		var retVal = {}; //helper object to return function results (headers) by reference 
		try {
			var fileName = this.fileUrlToFileName(fileUrl);		
			
			getCurrentVersion(fileName, function(version) {
				if (version == -1) {
					log("File not found. Starting download...");
					if(remainingBytes < CACHE_MINIMUM_LIMIT ) {
				        	log("getFile: avaiable space is " + (+remainingBytes - +CACHE_MINIMUM_LIMIT) + ". Skipping download.");
						self.postMessage({'cmd': 'getFile_complete', 'id': id, 'fileUrl': fileUrl, 'file': null, 'headers': null});	
						return;
				        }
				        else {
						download(fileName, fileUrl, retVal, null, remainingBytes, function(file) {
							log("getFile:download complete");
							self.postMessage({'cmd': 'getFile_complete', 'id': id, 'fileUrl': fileUrl, 'file': file, 'headers': retVal.headers});	
							return;
						});
					}
					
				} else {
					log("File is found in cache.");
					readAllHeaders(formatFileName(fileName, version, FILE_EXT_HEADERS), true, function(headers) {
						retVal.headers = headers
						fsCache.getFile(formatFileName(fileName, version, FILE_EXT_DATA), {create: false}, function(fileEntry) {
							fileEntry.file(function(file) {
								self.postMessage({'cmd': 'getFile_complete', 'id': id, 'fileUrl': fileUrl, 'file': file, 'headers': retVal.headers});	
								return;
							});
							
						}, function(e) {
							log('getFile:getCurrentVersion:readAllHeaders:getFile error : ' + e.toString());
							self.postMessage({'cmd': 'getFile_complete', 'id': id, 'fileUrl': fileUrl, 'file': null, 'headers': retVal.headers});	
							return;
						});
						
					}, function(e) {
						log('getFile:getCurrentVersion:readAllHeaders error : ' + e.toString());
						self.postMessage({'cmd': 'getFile_complete', 'id': id, 'fileUrl': fileUrl, 'file': null, 'headers': null});	
						return;
					});
					
				}
				
			});
		} catch (e) {
			log("get error: "  + e.message);
		}
	};

	var fileIsExpired = function(file) {
		var today = new Date().setHours(0,0,0,0); 
		var fileDate = file.lastModifiedDate.setHours(0,0,0,0);
		return (today - fileDate >= FILE_KEEP_IN_CACHE_DURATION_MS);
	};

	var deleteExpired = function() {
		try {
			var dirReader = fsCache.createReader();
			dirReader.readEntries(function(entries) {
				for (var i = 0, entry; entry = entries[i]; ++i) {
					if (entry.isFile && endsWith(entry.name, FILE_EXT_HEADERS)) {
						entry.file(function(file) {
							if (fileIsExpired(file)) {
								fsCache.getFile(file.name, {create: false}, function(headerFileEntry) {
									log("DeleteExpired file: " + headerFileEntry.name );
									headerFileEntry.remove(function() {
										var datFileName = file.name.substring(0,18) + FILE_EXT_DATA;
										fsCache.getFile(datFileName, {create: false}, function(datFileEntry) {
											datFileEntry.remove(function() {
											}); //remove data file.
										});	
									}); //remove header file.
								});	
							}    
						  });	
					}
				}
			});

		} catch (e) {
			log("deleteExpired worker error: "  + e.message);
		}
	};

	this.deleteAllDuplicates = function() {
		try {
			var dirReader = fsCache.createReader();
			dirReader.readEntries(function(entries) {
				var names = new rvHashTable();
				//find the latest versions of all files
				for (var i = 0, entry; entry = entries[i]; ++i) {
					if (entry.isFile && endsWith(entry.name, FILE_EXT_DATA)) {
						var name = extractFileName(entry.name);
						var version = extractFileVersionAsInt(entry.name);
						var savedVersion = names.get(name);
						if (savedVersion === null || savedVersion < version){
							names.put(name, version);
						}
					}
				}
				//remove duplicates - files with older versions
				for (var i = 0, entry; entry = entries[i]; ++i) {
					if (entry.isFile && (endsWith(entry.name, FILE_EXT_DATA) || endsWith(entry.name, FILE_EXT_HEADERS))) {
						var name = extractFileName(entry.name);
						var version = extractFileVersionAsInt(entry.name);
						if (version !== names.get(name)) {
							log("DeleteAllDuplicates deleting file: " + entry.name);
							entry.remove(function() {
							}); //remove header file.
						}
					}
				}
				deleteExpired();	
			}, function(e) {
				log("DeleteAllDuplicates readEntries error");
			});
		} catch (e) {
			log("DeleteAllDuplicates worker error: "  + e.message);
		}
	};

	this.clearCache = function() {
		try {
			var dirReader = fsCache.createReader();
			dirReader.readEntries(function(entries) {
				for (var i = 0, entry; entry = entries[i]; ++i) {
						entry.remove(function(){});
				}
				self.postMessage({'cmd': 'clearCache_complete', 'id': id});
				return;
			}, function(e) {
				log('clearCache:dirReader.readEntries error : ' + e.toString());
				self.postMessage({'cmd': 'clearCache_complete', 'id': id});
				return;
			});
		} catch (e) {
			log("clearCache worker error: "  + e.message);
		}
	};

	this.getCachedFiles = function() {	
		var cachedFiles = [];
		try {
			var dirReader = fsCache.createReader();
			dirReader.readEntries(function(entries) {
				log("entries.lenght:" + entries.length)
				if(entries.length == undefined || entries.length == 0) {
					self.postMessage({'cmd': 'getCachedFiles_complete', 'id': id, 'files': cachedFiles});
					return;
				}
				for (var i = 0, entry; entry = entries[i]; ++i) {
					entry.file(function(file) {
						cachedFiles.push({name: file.name, size: file.size});		    
						if( cachedFiles.length == entries.length )
							self.postMessage({'cmd': 'getCachedFiles_complete', 'id': id, 'files': cachedFiles});
					  }, function(e) {
					  	log("getCachedFiles error");
						self.postMessage({'cmd': 'getCachedFiles_complete', 'id': id, 'files': cachedFiles});
					  });
				}
			}, function(e) {
				log('getCachedFiles:dirReader.readEntries error : ' + e.toString());
				self.postMessage({'cmd': 'getCachedFiles_complete', 'id': id, 'files': cachedFiles});
				return;
			});
			
		} catch (e) {
			log("clearCache worker error: "  + e.message);
			self.postMessage({'cmd': 'getCachedFiles_complete', 'id': id, 'files': cachedFiles});
		}
	};

	var readAllHeaders = function(fileName, needToUpdateFileLastAccessedTime, callbackFunc) {
		log("readAllHeaders: fileName=" + fileName);
		fsCache.getFile(fileName, {create: false}, function(fileEntry) {
			log("readAllHeaders:fsCache.getFile");
			fileEntry.file(function(file) {
				log("readAllHeaders:fsCache.getFile:fileEntry.file");
				var reader = new FileReader();
			 	reader.onloadend = function(e) {
			 		log("readAllHeaders:fsCache.getFile:fileEntry.file:reader.onloadend needToUpdateFileLastAccessedTime=" + needToUpdateFileLastAccessedTime);
					var headers = this.result;       
					log("readAllHeaders:fsCache.getFile:fileEntry.file:reader.onloadend headers=" + headers);
					if (needToUpdateFileLastAccessedTime) {
						//update file last modified so we know last time it waas accessed
						var today = new Date().setHours(0,0,0,0); 
						var fileDate = file.lastModifiedDate.setHours(0,0,0,0);
						// setHours() returns milliseconds (int type) which is easy to compare
						if (today !== fileDate) {
							//fileEntry.createWriter().write(strToBlob(headers));
							fileEntry.createWriter(function(fileWriter) {
							      fileWriter.onwriteend = function(e) {
							        log('readAllHeaders: Write completed.');
							        callbackFunc(headers);
							        return;
							      };
							
							      fileWriter.onerror = function(e) {
							        log('readAllHeaders:fileEntry.file:reader:fileWriter:onerror failed: ' + e.toString());
							        callbackFunc(headers);
							        return;
							      };
							
							      // Create a new Blob and write it to log.txt.
							      log("readAllHeaders:fileEntry.file:reader:fileWriter writting to file");
							      fileWriter.write(strToBlob(headers));
							
							}, function(e) {
								console.log('readAllHeaders:fileEntry.file:reader:fileWriter failed: ' + e.toString());
				      				callbackFunc(headers);
							});
						} else {
							callbackFunc(headers);
						}
					} else {
						callbackFunc(headers);
					}
					
				};
				 
				reader.onerror = function(e) {
				      console.log('Write failed: ' + e.toString());
				      callbackFunc(null);
				      return;
				};
				reader.readAsText(file);
			}, function(e) {
				log('readAllHeaders:fileEntry.file error: '  + e.message);
				callbackFunc(null);
				return;
			});
		}, function(e) {
			log('readAllHeaders:getFile error: '  + e.message);
			callbackFunc(null);
			return;
		});
		
	};
	
	var strToBlob = function(txt) {
		return new Blob([ txt ], {type : "text/plain;charset=UTF-8"});
	};

	var blobToStr = function(blob) {
		var reader = new FileReaderSync();
        return reader.readAsText(blob);
	};

	var download = function(fileName, fileUrl, retVal, condHeader, remainingBytes, callbackFunc) {
		var file = null;
		try {
		    var xhr = new XMLHttpRequest();
		    xhr.responseType = "blob";
		    xhr.open('HEAD', fileUrl, false); //async=FALSE
		    if (condHeader) {
			    xhr.setRequestHeader(condHeader.name, condHeader.value);
		    }
		    xhr.send();
		    
		    log(" --- on download. xhr.readyState="+xhr.readyState+" | xhr.status="+xhr.status); //DO NOT DELETE!!! This line somehow magically fixes InvalidState error.
		    if (xhr.status >= 200 && xhr.status < 300) {
		    	var contentLength = xhr.getResponseHeader(HEADER_CONTENT_LENGTH);
		    	if( (+contentLength + +CACHE_MINIMUM_LIMIT) > remainingBytes ) {
				log("download: requested file size " + contentLength + " is greater than available space " + (+remainingBytes - +CACHE_MINIMUM_LIMIT) + ". Skipping download.");
				callbackFunc(null);
				return;
			}
			//check if entire file has been downloaded. It happens when 
			// - HEAD requests are treated as GET on server
			// - Chrome replaces HEAD by GET when it follows 301 redirect
			if (xhr.response && xhr.response.size > 0) {
				//response includes data - skip GET request and just save data
				log('download: File is modified. HEAD request returned data (oh-oh!). Saving... fileName=' + fileName);
				saveResponseData(fileName, fileUrl, xhr, retVal, function(file) {
					callbackFunc(file);		
		    			return;
				});
			}
		    } else {
		    	callbackFunc(null);
			return;
		    }
		} catch (e) {
			log("download error: " + e.message);
		}
		
		try {
		    var xhr = new XMLHttpRequest();
		    xhr.responseType = "blob";
		    xhr.open('GET', fileUrl, false); //async=FALSE
		    if (condHeader) {
			    xhr.setRequestHeader(condHeader.name, condHeader.value);
		    }
		    xhr.send();
		} catch (e) {
			log("download error: " + e.message);
		}

		try {
			log(" --- on download. xhr.readyState="+xhr.readyState+" | xhr.status="+xhr.status); //DO NOT DELETE!!! This line somehow magically fixes InvalidState error.
			if (xhr.status >= 200 && xhr.status < 300) {
				log("file download is complete.");
				saveResponseData(fileName, fileUrl, xhr, retVal, function(file) {
					callbackFunc(file);		
					return;
				});
			} else {
				log("download status code: " + xhr.status);
				callbackFunc(file);
				return;
			}
		} catch (e) {
			log("save file error: " + e.message);
			callbackFunc(file);
			return;
		}
	};

	this.downloadIfModified = function(fileUrl, remainingBytes) {	
		//send HEAD request
		var fileName = this.fileUrlToFileName(fileUrl);
		getCurrentVersion(fileName, function(version) {
			log("filename:" + fileName + " , version: " + version);
			readAllHeaders(formatFileName(fileName, version, FILE_EXT_HEADERS), false, function(headersStr) {
				var headers = headersStrToObj(headersStr);
				if (!(headers && (headers.ETag || headers.LastModified))) {
					//if neither ETag nor LastModified exist, then there is no way to check if file is modified
					self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': fileUrl});
					return;
				}		
		    		var condHeader = null; //conditional header
				try {
					var xhr = new XMLHttpRequest();
					xhr.responseType = "blob";
					xhr.open('HEAD', fileUrl, false); //async=FALSE
					if (headers.ETag) {
					    xhr.setRequestHeader(HEADER_IF_NONE_MATCH, headers.ETag);
					    condHeader = {"name": HEADER_IF_NONE_MATCH, "value": headers.ETag};
					} else {
					    xhr.setRequestHeader(HEADER_IF_MODIFIED_SINCE, headers.LastModified);
					    condHeader = {"name": HEADER_IF_MODIFIED_SINCE, "value": headers.LastModified};
					}
					log("sending xhr request fileUrl:" + fileUrl);
					xhr.send();
				
				
					//expected responses: "200 OK" or "304 Not Modified"	
					log(" --- on downloadIfModified. xhr.readyState="+xhr.readyState+" | xhr.status="+xhr.status); //DO NOT DELETE!!! This line somehow magically fixes InvalidState error.
					if (xhr.status >= 200 && xhr.status < 300) {
						//check if entire file has been downloaded. It happens when 
						// - HEAD requests are treated as GET on server
						// - Chrome replaces HEAD by GET when it follows 301 redirect
						if (xhr.response && xhr.response.size > 0) {
							//response includes data - skip GET request and just save data
							log('File is modified. HEAD request returned data (oh-oh!). Saving... fileName=' + fileName);
							var contentLength = xhr.getResponseHeader(HEADER_CONTENT_LENGTH);
							if( (+contentLength + +CACHE_MINIMUM_LIMIT) > remainingBytes ) {
								log("downloadIfModified: requested file size " + contentLength + " is greater than available space " + (+remainingBytes - +CACHE_MINIMUM_LIMIT) + ". Skipping download.");
								self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': data.fileUrl});
								return;
							}
							saveResponseData(fileName, fileUrl, xhr, retVal, function(file) {
								self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': data.fileUrl});
								return;
							});
						} else {
							log('File is modified. Re-downloading... fileName=' + fileName);
							download(fileName, fileUrl, null, condHeader, remainingBytes, function() {
								self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': fileUrl});
								return;
							});
						}
					} else {
						self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': fileUrl});
						return;
					}
				} catch (e) {
					log("checkIfModiifed download error: " + e.message);
					self.postMessage({'cmd': 'downloadIfModified_complete', 'id': id, 'fileUrl': fileUrl});
					return;
				}
			});
		});
	};
	
	var saveResponseData = function(fileName, fileUrl, xhr, retVal, callbackFunc) {
		var headers = extractHeaders(xhr, fileUrl);
		getCurrentVersion(fileName, function(version) {
			version++;
			//save data file
			var dataFileName = formatFileName(fileName, version, FILE_EXT_DATA);
	
			saveFile(dataFileName, xhr.response, function(file) {
				//try to recover from errors like InvalidStateError i.e. "state had changed" message.
				//try saving second time if first time fails. 		
				
				if (file) {
					//save headers
					saveFile(formatFileName(fileName, version, FILE_EXT_HEADERS), strToBlob(headers), function(headerFile) {
						if (retVal) retVal.headers = headers;
						callbackFunc(file);
						return;
					});	
				} else {
					log("Failed to save file");
					callbackFunc(file);
					return;
				}
			});
		}) ;

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
				}
			}
		}
		return res;
	};
	
	var formatFileName = function(name, version, ext) {
		return name + "." + intToHex8(version) + "." + ext;
	};
	
	var extractHeaders = function(xhr, fileUrl) {
		var headers = [];
		
		var v = xhr.getResponseHeader(HEADER_ETAG);
		if (v) {
			headers.push(HEADER_ETAG + ": " + v);
		}
		v = xhr.getResponseHeader(HEADER_LAST_MODIFIED);
		if (v) {
			headers.push(HEADER_LAST_MODIFIED + ": " + v);
		}
		headers.push(HEADER_CONTENT_TYPE + ": " + xhr.response.type);
		headers.push(HEADER_CONTENT_LENGTH + ": " + xhr.response.size);
		headers.push(HEADER_FILE_URL + ": " + fileUrl);
		
		log(headers);
		
		return headers.join("\n");
	};
	
	var saveFile = function(fileName, data, callbackFunc) {
		var file = null;
		try {
			fsCache.getFile(fileName, {create: true}, function(fileEntry) {
				fileEntry.createWriter(function(fileWriter) {
				      fileWriter.onwriteend = function(e) {
				        log('saveFile: Write completed.');
				        fileEntry.file(function(file) {
						callbackFunc(file);
						return;
					}, function(e) {
						log("saveFile:fileEntry.file error: "  + e.message);
						callbackFunc(file);
						return;
					});
				      };
				
				      fileWriter.onerror = function(e) {
				        log('saveFile:Write failed: ' + e.toString());
				        callbackFunc(file);
				        return;
				      };
				      log("saveFile:getFile writting filename: " + fileName );
				      fileWriter.write(data);
				
				}, function(e) {
					log("saveFile:fsCache.getFile error: "  + e.message);
					callbackFunc(file);
					return;
				});
				
				
			}, function(e) {
				log("saveFile:fsCache.getFile error: "  + e.message);
				callbackFunc(file);
				return;
			});
			
		} catch (e) {
			log("saveFile:catch error: "  + e.message);
			callbackFunc(file);
			return;
		}
	};

	var extractFileName = function(name) {
		return name.substring(0, 8);
	};

	var extractFileVersion = function(name) {
		return name.substring(9, 17);
	};

	var extractFileVersionAsInt = function(name) {
		return parseInt(extractFileVersion(name), 16);
	};

	var startsWith = function(str, suffix) {
	    return str.indexOf(suffix) == 0
	};
	
	var endsWith = function(str, suffix) {
	    return str.indexOf(suffix, str.length - suffix.length) !== -1;
	};
	
};

function messageHandler(event) {
	try {
	    var data = event.data ? event.data : {};
	    //"self" is the property of the HTML5 Worker object. No need to declare it.
		self.id = data.id;
		log('New message cmd=' + data.cmd);

		if (!self.fm) {
		    self.fm = new rvFileManagerSync();
		}

		//self.fm.init();
		switch (data.cmd) {
		case 'initFS':
			self.fm.init();
			break;
		case 'getFile':
			self.fm.getFile(data.fileUrl, data.remainingBytes);
			break;
		case 'downloadIfModified':
			self.fm.downloadIfModified(data.fileUrl, data.remainingBytes)
			break;
		case 'deleteExpired':
			self.fm.deleteAllDuplicates();
			break;
		case 'clearCache':
			self.fm.clearCache();
			break;
		case 'getCachedFiles':
			self.fm.getCachedFiles();
			break;
		case 'stop':
			break;
		default:
			log('Unknown command: ' + data.cmd);
		};
	} catch (e) {
		   self.postMessage({'cmd': 'log', 'id': self.id, 'msg': 'error' + e.message});
	}

};


//Defining the callback function raised when the main page will call us
this.addEventListener('message', messageHandler, false);

var log = function(msg) {
    self.postMessage({'cmd': 'log', 'id': self.id, 'msg': msg});
};	

var returnFile = function(fileUrl, fileLocalUrl) {
    self.postMessage({'cmd': 'log', 'id': self.id, 'fileUrl': fileUrl, 'fileLocalUrl': fileLocalUrl});
};	
