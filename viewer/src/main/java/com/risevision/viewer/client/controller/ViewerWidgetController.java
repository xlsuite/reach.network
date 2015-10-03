// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerWidgetController implements ViewerControllerInterface {
	private PlaceholderInfo placeholderInfo;
	private PlaylistItemInfo playlistItem;
	private String phName, htmlName, presFrame;
	private boolean isReady = false;
	private boolean isPlaying = false;
//	@SuppressWarnings("unused")
//	private boolean canPlay, canStop, canPause, canReportReady, canReportDone;
	private Command gadgetReadyCommand;
	private Command gadgetDoneCommand;

	private static final String WIDGET_SCRIPT = "<script><!--\n" +
			"try {" +
			"	updateWidget('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateWidget call - %s2 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	public ViewerWidgetController(PlaceholderInfo placeholderInfo, PlaylistItemInfo playlistItem, String phName, String htmlName, Command gadgetReadyCommand, Command gadgetDoneCommand) {
		this.placeholderInfo = placeholderInfo;
		this.playlistItem = playlistItem;
		this.phName = phName;
		
		this.htmlName = htmlName;
		
		// add first letter of Gadget type to the name
		if (playlistItem != null && playlistItem.getType() != null && playlistItem.getType().length() > 1)
			this.htmlName = htmlName + playlistItem.getType().substring(0, 1);
		
		this.gadgetReadyCommand = gadgetReadyCommand;
		this.gadgetDoneCommand = gadgetDoneCommand;
		
		ViewerHtmlUtils.registerGadget(this.htmlName, this);
	}
	
	public void updateHtml(PresentationInfo presentation) {
		if (playlistItem != null && PlaylistItemInfo.TYPE_WIDGET.equals(playlistItem.getType())) {
			updateHtmlWidget(presentation);
		}
	}

	private void updateHtmlWidget(PresentationInfo presentation) { 
		String transition = "none";

		String widgetUrl = updateUrlParams(playlistItem.getObjectData());
		
		if (placeholderInfo != null) {			
			transition = placeholderInfo.getTransition();
		}
		
		addWidgetScript(presentation, widgetUrl, phName, htmlName, transition);
	}
	
	public static void addWidgetScript(PresentationInfo presentation, String url, String containerName, String htmlName, String transition){
		String tagHtml = WIDGET_SCRIPT.replace("%s1", url)
						.replace("%s2", containerName)
						.replace("%s3", htmlName)
						.replace("%s4", transition);
		
		PresentationParser.addScriptTag(presentation, tagHtml);
		
	}
	
	private String updateUrlParams(String urlParams) {
		int height, width;
//		int top = 0, left = 0;
//		urlParams = removeIdParameter(urlParams);
//		urlParams = addVersionNumber(urlParams);
		
		if (placeholderInfo.getWidthUnits().equals("%")) {
			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
		}
		else {
			width = (int)placeholderInfo.getWidth();
		}
		
		if (placeholderInfo.getHeightUnits().equals("%")) {
			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
		}
		else {
			height = (int)placeholderInfo.getHeight();
		}
		
		//"&pid=test1&up_id=test1"
//		urlParams += "&pid=" + htmlName;
		urlParams += urlParams.contains("?") ? "&" : "?";
		urlParams += "up_id=" + htmlName;
		urlParams += "&parent=" + URL.encodeQueryString(Window.Location.getHref());
		urlParams += "&up_rsW=" + width;
		urlParams += "&up_rsH=" + height;
		
		urlParams = urlParams.replace("'", "\\'");
		
		return urlParams;
			
	}
	
	public void setReady(String presFrame, boolean canPlay, boolean canStop, boolean canPause, boolean canReportReady, boolean canReportDone) {
		this.presFrame = presFrame;
//		this.canPause = canPlay;
//		this.canStop = canStop;
//		this.canPause = canPause;
//		this.canReportReady = canReportReady;
//		this.canReportDone = canReportDone;
//		
		if (!isReady) {
			isReady = true;
			if (gadgetReadyCommand != null) {
				gadgetReadyCommand.execute();	
			}
//			// if GadgetReadyCommand is null, than this is a single Gadget 
//			// in placeholder (no ready command, just Start playing right away)
//			else if (!isPlaying) {
//				play(true);
//			}
		}
	}
	
	public void setError(String presFrame, String reason) {
		// catch gadget error (not implemented)
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
//			if (isReady) {
				ViewerHtmlUtils.playCommand(presFrame, htmlName, show);
//			}
		}
	}
	
	public void stop(boolean hide) {
//		if (isPlaying) {
			isPlaying = false;
			ViewerHtmlUtils.stopCommand(presFrame, htmlName, hide);
//		}
	}
	
	public void pause(boolean hide) {
		// removed isPlaying check (since pause can be called on a "paused" item to hide it)
//		if (isPlaying) {
			isPlaying = false;
			ViewerHtmlUtils.pauseCommand(presFrame, htmlName, hide);
//		}
	}
	
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public PlaylistItemInfo getItem() {
		return playlistItem;
	}
}
