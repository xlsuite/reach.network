// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.info;

import java.util.ArrayList;

import com.risevision.common.client.info.PlaylistInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.info.ScheduleInfo;

public class ViewerDataInfo {
	private int statusCode = -2;
	private String statusMessage;
	private ScheduleInfo schedule;
	private ArrayList<PresentationInfo> presentations;
	private ArrayList<PlaylistInfo> playlists;
	
	private String contentDescriptor;
	
	public ScheduleInfo getSchedule() {
		return schedule;
	}
	
	public void setSchedule(ScheduleInfo schedule) {
		this.schedule = schedule;
	}
	
	public ArrayList<PresentationInfo> getPresentations() {
		return presentations;
	}
	
	public void setPresentations(ArrayList<PresentationInfo> presentations) {
		this.presentations = presentations;
	}
	
	public ArrayList<PlaylistInfo> getPlaylists() {
		return playlists;
	}
	
	public void setPlaylists(ArrayList<PlaylistInfo> playlists) {
		this.playlists = playlists;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setContentDescriptor(String contentDescriptor) {
		this.contentDescriptor = contentDescriptor;
	}

	public String getContentDescriptor() {
		return contentDescriptor;
	}

}
