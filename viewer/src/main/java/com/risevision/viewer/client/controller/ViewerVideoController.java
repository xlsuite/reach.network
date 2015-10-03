// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import com.google.gwt.user.client.Command;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.info.VideoItemInfo;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerVideoController implements ViewerControllerInterface {
	private PlaylistItemInfo playlistItem;
	private String htmlName;
	private boolean isReady = false, isPlaying = false;
	private Command gadgetReadyCommand, gadgetDoneCommand;
	private VideoItemInfo videoInfo;
	private PlaceholderVideoController videoController;
	
	public ViewerVideoController(PlaylistItemInfo playlistItem, String htmlName, Command gadgetReadyCommand, Command gadgetDoneCommand, PlaceholderVideoController videoController) {
		this.playlistItem = playlistItem;
		this.htmlName = htmlName;
				
		// add first letter of item type to the name
		if (playlistItem.getType() != null && playlistItem.getType().length() > 1)
			this.htmlName = htmlName + playlistItem.getType().substring(0, 1);
		
		this.gadgetReadyCommand = gadgetReadyCommand;
		this.gadgetDoneCommand = gadgetDoneCommand;
		this.videoController = videoController;
		
		ViewerHtmlUtils.registerGadget(this.htmlName, this);
		
		videoInfo = new VideoItemInfo(playlistItem.getObjectData());
		
		videoController.addVideo(this.htmlName, this);
	}
	
	public void updateHtml(PresentationInfo presentation) {

	}
	
	public void setReady(String presFrame, boolean canPlay, boolean canStop, boolean canPause, boolean canReportReady, boolean canReportDone) {
//		videoController.setPresFrame(presFrame);
//		videoController.reportReady(this);
		
		if (!isReady) {
			isReady = true;
			if (gadgetReadyCommand != null) {
				gadgetReadyCommand.execute();	
			}
			// if GadgetReadyCommand is null, than this is a single Gadget 
			// in placeholder (no ready command, just Start playing right away)
			else if (!isPlaying) {
				play(true);
			}
		}
	}
	
	public void setError(String presFrame, String reason) {
//		videoController.setPresFrame(presFrame);
		videoController.reportError(this, reason);
	}
	
	public void setDone() {
		if (isPlaying) {
			if (gadgetDoneCommand != null) {
				isPlaying = false;
				gadgetDoneCommand.execute();
			}
			else {
				stop(false);
				play(false);
			}
		}
	}
	
	public void play(boolean show) {
		if (!isPlaying) {
			isPlaying = true;
			if (isReady) {
				try {
					videoController.play(this, show);
				}
				catch (Exception e) {

				}
			}
		}
	}
	
	public void stop(boolean hide) {
//		if (isPlaying) {
		isPlaying = false;
		try {
			videoController.stop(this, hide);
		}
		catch (Exception e) {

		}
//		}
	}
	
	public void pause(boolean hide) {
		// removed isPlaying check (since pause can be called on a "paused" item to hide it)
//		if (isPlaying) {
		isPlaying = false;
		try {
			videoController.pause(this, hide);
		}
		catch (Exception e) {

		}
//		}
	}
	
	public boolean isReady() {
		if (!isReady) {
			videoController.readyRequested(this);
		}
		
		return isReady;
	}
	
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public PlaylistItemInfo getItem() {
		return playlistItem;
	}
	
	public VideoItemInfo getVideoInfo() {
		return videoInfo;
	}
}
