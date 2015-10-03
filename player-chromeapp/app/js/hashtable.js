// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

rvHashTable = function() {
	
	this.map = {};
	this.length = 0;

	this.exist = function(key) {
		return typeof this.map[key] != "undefined";
	};

	this.put = function(key, val) {
		if (!this.exist(key)) {
			this.length++;
		}
		this.map[key] = val;
	};

	this.get = function(key) {
		if (this.exist(key)) {
			return this.map[key];
		}

		return null;
	};

	this.remove = function(key) {
		if (this.exist(key)) {
			this.length--;
			delete this.map[key];
		}
	};

	this.clear = function() {
		this.map = {};
		this.length = 0;
	};

	this.size = function() {
		return this.length;
	};

	this.keySet = function() {
		var keys = new Array();
		for ( var key in this.map) {
			keys.push(key);
		}

		return keys;
	};

	this.valSet = function() {
		var vals = new Array();
		for ( var key in this.map) {
			vals.push(this.map[key]);
		}

		return vals;
	};
	
	this.valExists = function(value) {
		for (var key in this.map) {
			if (value === this.map[key]) {
				return true;
			}
		}
		return false;
	};
};
