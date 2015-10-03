// Copyright © 2010 - May 2015 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvWorkers = function (onFileReady, onDownloadIfModified, onClearCache, onGetCachedFiles) {
	
	var WORKER_STATUS_READY = 0;
	var WORKER_STATUS_BUSY = 1;
	var FILE_MANAGER_PATH =  "/js/cache/filemanagersync.js";
	var lastId = 0;
	var fmWorkers = []; //array of file manager workers
	var WorkerItem = function(worker, id) {
		this.worker = worker;
		this.id = id;
		this.status = WORKER_STATUS_READY; 
	}
	
	var log = function(msg) {
		console.log(msg);
	};
	
	var initFS = function() {
		var wi = getWorkerItem(fmWorkers, FILE_MANAGER_PATH);
		wi.status == WORKER_STATUS_BUSY;
		wi.worker.postMessage({'cmd': 'initFS', 'id': wi.id});
	};
	
	this.getFile = function(fileUrl) {
		navigator.webkitPersistentStorage.queryUsageAndQuota ( 
		    function(usedBytes, grantedBytes) {  
		    	var remainingBytes = grantedBytes - usedBytes;
		        console.log('we are using ', usedBytes, ' and remainingBytes ', remainingBytes, 'bytes');
		        var wi = getWorkerItem(fmWorkers, FILE_MANAGER_PATH);
			wi.status == WORKER_STATUS_BUSY;
			wi.worker.postMessage({'cmd': 'getFile', 'id': wi.id, 'fileUrl': fileUrl, 'remainingBytes': remainingBytes});
		    }, 
		    function(e) { console.log('Error', e);  }
		);  
	};

	this.downloadIfModified = function(fileUrl) {
		navigator.webkitPersistentStorage.queryUsageAndQuota ( 
		    function(usedBytes, grantedBytes) {  
		    	var remainingBytes = grantedBytes - usedBytes;
		        console.log('we are using ', usedBytes, ' and remainingBytes ', remainingBytes, 'bytes');
		    	var wi = getWorkerItem(fmWorkers, FILE_MANAGER_PATH);
			wi.status == WORKER_STATUS_BUSY;
			wi.worker.postMessage({'cmd': 'downloadIfModified', 'id': wi.id, 'fileUrl': fileUrl, 'remainingBytes': remainingBytes});
		    }, 
		    function(e) { console.log('Error', e);  }
		);  
	};

	this.clearCache = function() {
		var wi = getWorkerItem(fmWorkers, FILE_MANAGER_PATH);
		wi.status == WORKER_STATUS_BUSY;
		wi.worker.postMessage({'cmd': 'clearCache', 'id': wi.id});
	};

	this.getCachedFiles = function() {
		var wi = getWorkerItem(fmWorkers, FILE_MANAGER_PATH);
		wi.status == WORKER_STATUS_BUSY;
		wi.worker.postMessage({'cmd': 'getCachedFiles', 'id': wi.id});
	};

	var fmEventHandler = function(event) {
		var data = event.data;
		switch (data.cmd) {
			case 'initFS_complete':
				log('WORKER ' + data.id + ': initFS_complete');
				changeWorkerStatus(fmWorkers, data.id, WORKER_STATUS_READY);
				break;
			case 'log':
				log('WORKER ' + data.id + ': ' + data.msg);
				break;
			case 'getFile_complete':
				log('WORKER ' + data.id + ': getFile_complete');
				changeWorkerStatus(data.id, WORKER_STATUS_READY);
				if (onFileReady) {
					onFileReady(data.fileUrl, data.file, data.headers);
				}
				break;
			case 'downloadIfModified_complete':
				log('WORKER ' + data.id + ': downloadIfModified_complete');
				changeWorkerStatus(fmWorkers, data.id, WORKER_STATUS_READY);
				if (onDownloadIfModified) {
					onDownloadIfModified(data.fileUrl);
				}
				break;
			case 'clearCache_complete':
				log('WORKER ' + data.id + ': clearCache_complete');
				changeWorkerStatus(fmWorkers, data.id, WORKER_STATUS_READY);
				if (onClearCache) {
					onClearCache();
				}
				break;
			case 'getCachedFiles_complete':
				log('WORKER ' + data.id + ': getCachedFiles_complete');
				changeWorkerStatus(fmWorkers, data.id, WORKER_STATUS_READY);
				if (onGetCachedFiles) {
					onGetCachedFiles(data.files);
				}
				break;
			default:
				log('Unknown command: ' + data.msg);
		};
	};

	var getWorkerItem = function(workers, workerFileName) {
		var wi;
	    for (var i = 0;  i < workers.length; ++i) {
	    	if (workers[i].status == WORKER_STATUS_READY) {
	    		wi = workers[i];
	    		return wi;
	    	}
	    }
	    
	    if (workers.length >= 10) {
	    	throw "Too many open workers: " + workers.length;
	    }
	    	    
	    wi = new WorkerItem(new Worker(workerFileName), lastId++);
	    wi.worker.addEventListener("message", fmEventHandler, false);
		workers.push(wi);
		return wi;
	};
	
	var changeWorkerStatus = function(workers, workerId, status) {
	    for (var i = 0;  i < workers.length; ++i) {
	    	if (workers[i].id === workerId) {
	    		workers[i].status = status;
	    	}
	    }
	};
initFS();
};

