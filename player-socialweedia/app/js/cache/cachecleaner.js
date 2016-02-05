// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvCacheCleaner = function () {
		
	var TIMER_INTERVAL_MS = 24 * 60 * 60 * 1000; //24 hours
	var worker;
	var start = function() {
		console.log('DeleteExpiredJob start');
		onTimer();
	};

	this.forceDeleteExpired = function() {
		deleteExpired();
	};

	var deleteExpired = function() {
		console.log('deleteExpired');
		//it's easier to work with files in worker. Close worker when done;
	    	worker = new Worker("/js/cache/filemanagersync.js");
	    	this.worker = worker;
	    	worker.addEventListener('message', fmEventHandler, false);
	    	this.worker.postMessage({'cmd': 'initFS'});
	};

	var fmEventHandler = function(event) {
		var data = event.data;
		switch (data.cmd) {
			case 'initFS_complete':
				console.log('initFS_complete');
				worker.postMessage({'cmd': 'deleteExpired'});
				break;
			case 'log':
				console.log(data.msg);
				break;
			case 'deleteExpired_complete':
				console.log('deleteExpired_complete');
				break;
			case 'getCachedFiles_complete':
				console.log('getCachedFiles_complete files: ' + data.files);
				
				break;				
			default:
				console.log('Unknown command: ' + data.msg);
		};
	};

	var onTimer = function() {
		try {
			//console.log('on timer');
			deleteExpired();
		} catch (e) {
			console.log('deleteExpired error: ' + e.message);
		} finally {
			setTimeout(onTimer, TIMER_INTERVAL_MS);	
		}
	};
	
	start();

};

