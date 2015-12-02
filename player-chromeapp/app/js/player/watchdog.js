// Copyright � 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvWatchdog = function () {

	var HEARTBEAT_TIMER_INTERVAL_MS = 60 * 1000;
	var MAX_HEARTBEAT_GAP_MS = 3 * HEARTBEAT_TIMER_INTERVAL_MS;
	
	var lastHearbeat;
	var callback;
	
	this.start = function(callback_function) {
		console.log('Watchdog start');
		callback = callback_function;
		this.poke();
		onTimer();
	};

	this.poke = function() {
		//console.log('poke');
		lastHearbeat = new Date();
	};

	var onTimer = function() {
		try {
			//console.log('on timer');
			var now = new Date();
			if ((lastHearbeat.getTime() + MAX_HEARTBEAT_GAP_MS) < now.getTime()) {
				HEARTBEAT_TIMER_INTERVAL_MS = (HEARTBEAT_TIMER_INTERVAL_MS * 2) + (Math.random() * 10000);
				if (HEARTBEAT_TIMER_INTERVAL_MS > 18000000) {
					HEARTBEAT_TIMER_INTERVAL_MS = 18000000 + (Math.random() * 10000);
				}
				if (callback) {
					console.log('watchdog triggered at ' + now);
					callback();
				}
				return;
			}
			HEARTBEAT_TIMER_INTERVAL_MS = 60 * 1000;
		} catch (e) {
			console.log('watchdog error: ' + e.message);
		} finally {
			setTimeout(onTimer, HEARTBEAT_TIMER_INTERVAL_MS);
		}
	};

};

