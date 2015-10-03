// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.info;

public class ViewerPlayerInfo {
	public static final String ORIENTATION = "orientation";

	private String restartRequired;
	private String rebootRequired;
	private String rebootTime;
	private String rebootEnabled;
	private String updateRequired;
	private int orientation;
	
	public String getRestartRequired() {
		return restartRequired;
	}
	
	public void setRestartRequired(String restartRequired) {
		this.restartRequired = restartRequired;
	}
	
	public String getRebootRequired() {
		return rebootRequired;
	}
	
	public void setRebootRequired(String rebootRequired) {
		this.rebootRequired = rebootRequired;
	}
	
	public String getRebootTime() {
		return rebootTime;
	}
	
	public void setRebootTime(String rebootTime) {
		this.rebootTime = rebootTime;
	}
	
	public String getRebootEnabled() {
		return rebootEnabled;
	}
	
	public void setRebootEnabled(String rebootEnabled) {
		this.rebootEnabled = rebootEnabled;
	}
	
	public String getUpdateRequired() {
		return updateRequired;
	}

	public void setUpdateRequired(String updateRequired) {
		this.updateRequired = updateRequired;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
}
