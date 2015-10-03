// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.info.GadgetSettingsInfo;
import com.risevision.common.client.info.GadgetSettingsInfo.GadgetXmlParserError;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerGadgetController implements ViewerControllerInterface {
	private PlaceholderInfo placeholderInfo;
	private PlaylistItemInfo playlistItem;
	private String phName, htmlName, presFrame;
	private boolean isReady = false, isPlaying = false;
	@SuppressWarnings("unused")
	private boolean canPlay, canStop, canPause, canReportReady, canReportDone;
	private Command gadgetReadyCommand, gadgetDoneCommand;
	
	private static final String GADGET_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateGadget('%s1', '%s3', '%s4', '%s5');" +
			"gadgetRequest('%s4', '%s2');" +
			"} catch(err) { parent.writeToLog('updateGadget call - %s3 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	private final String GADGET_HTML = "" +
			"<iframe id=\"if_%id%\" name=\"if_%id%\" allowTransparency=\"true\" " +
			"style=\"display:block;position:absolute;height:100%;width:100%;\" frameborder=0 scrolling=\"no\">" +
			"</iframe>" +
			"";
	
	public ViewerGadgetController(PlaceholderInfo placeholderInfo, PlaylistItemInfo playlistItem, String phName, String htmlName, Command gadgetReadyCommand, Command gadgetDoneCommand) {
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
		if (playlistItem != null && PlaylistItemInfo.TYPE_GADGET.equals(playlistItem.getType())) {
			updateHtmlGadget(presentation);
		}
	}
	
	private void updateHtmlGadget(PresentationInfo presentation) { 
		String transition = "none";
		String xmlUrl = "";
		if (playlistItem != null) {
			if (playlistItem.getObjectData().toLowerCase().contains(".xml")){
				String url = playlistItem.getObjectData();
				
				int queryLocation = url.indexOf(".xml") + 4;

				xmlUrl = url.substring(0, queryLocation);
			}
			else {
				// add URL functionality here
			}
		}
//		else if (placeholderInfo.getUrl().toLowerCase().contains(".xml")) {
//			url = placeholderInfo.getUrl();
//		}
		
		if (placeholderInfo != null) {			
			transition = placeholderInfo.getTransition();
		}
		
		String html = GADGET_HTML
//				.replace("%s0%", url)
				.replace("%id%", htmlName)
				.replace("\n", "")
				.replace("\r", "");
		
		addGadgetScript(presentation, html, xmlUrl, phName, htmlName,
				transition);

	}
	
	public void addGadgetScript(PresentationInfo presentation, String html, String gadgetUrl, String containerName, String htmlName, String transition){
		String tagHtml = GADGET_SCRIPT.replace("%s1", html)
						.replace("%s2", gadgetUrl)
						.replace("%s3", containerName)
						.replace("%s4", htmlName)
						.replace("%s5", transition);
			
		PresentationParser.addScriptTag(presentation, tagHtml);
	}
	
	@SuppressWarnings("unused")
	private String addVersionNumber(String url) {
		int queryLocation = url.indexOf(".xml");
		if (queryLocation != -1) {
			String versionString = "%3Fv%3D" + Random.nextInt(10000);
			queryLocation += ".xml".length();
			
			if (queryLocation < url.length()) {
				url = url.substring(0, queryLocation) + versionString + url.substring(queryLocation, url.length());
			}
			else {
				url = url + versionString;
			}
		}
		else {
			// TODO: handle cases where the Gadget is obtained through an API not a static .XML file.
		}
		return url;
	}
	
	private String updateUrlParams(String urlParams) {
		int height, width;
//		int top = 0, left = 0;
		urlParams = removeIdParameter(urlParams);
//		urlParams = addVersionNumber(urlParams);
		
		if (placeholderInfo.getWidthUnits().equals("%")) {
			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
		}
		else {
			width = (int)placeholderInfo.getWidth();
		}
//			
		if (placeholderInfo.getHeightUnits().equals("%")) {
			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
		}
		else {
			height = (int)placeholderInfo.getHeight();
		}
		
		urlParams += "&up_rsW=" + width;
		urlParams += "&up_rsH=" + height;
		
		//"&pid=test1&up_id=test1"
//		urlParams += "&pid=" + htmlName;
		urlParams += "&up_id=" + htmlName;
//		urlParams += "&parent=" + URL.encodeQueryString(Window.Location.getHref());
//		urlParams += ViewerEntryPoint.getDisplayId().isEmpty() ? "": "&up_displayid=" + ViewerEntryPoint.getDisplayId();
		
		urlParams = urlParams.replace("'", "\\'");
		
		return urlParams;
	}
	
	private String removeIdParameter(String url) {
		while (url.indexOf("up_id=") > 0) {
			int start = url.indexOf("up_id=");
			if (url.charAt(start - 1) == '&') {
				start -= 1;
			}
			int end = url.indexOf("&", url.indexOf("up_id="));
			if (end != -1) {
				url = url.substring(0, start) + url.substring(end, url.length());
			}
			else {
				url = url.substring(0, start);
			}
		}
		
		return url;
	}
	
	public void setLoad(String presFrame, String gadgetXml) {
		try {
			String gadgetHtml;
//			String gadgetDefaultSettings;
			String gadgetUrl = updateUrlParams(playlistItem.getObjectData());

			GadgetSettingsInfo gadgetSettings = new GadgetSettingsInfo(gadgetUrl, null, gadgetXml);
			
			// used for debugging purposes; doesn't use minified js file
//			GadgetSettingsInfo.useLongScript();
			
			gadgetHtml = gadgetSettings.getGadgetHtml();
			
			renderHtml(presFrame, gadgetHtml);
		
		} catch (GadgetXmlParserError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void renderHtml(String presFrame, String gadgetHtml) {
		// render the Gadget in the iframe using (phName, htmlName)
//		String html = GADGET_INNER_HTML.replace("%s0%", urlParams)
//									.replace("%s1%", gadgetDefaultSettings)
//									.replace("%body%", gadgetHtml);
		
		ViewerHtmlUtils.addGadgetHtml(presFrame, "if_" + htmlName, gadgetHtml);
	}

	public void setReady(String presFrame, boolean canPlay, boolean canStop, boolean canPause, boolean canReportReady, boolean canReportDone) {
		this.presFrame = presFrame;
		this.canPause = canPlay;
		this.canStop = canStop;
		this.canPause = canPause;
		this.canReportReady = canReportReady;
		this.canReportDone = canReportDone;
		
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
			if (isReady) {
				ViewerHtmlUtils.playCommand(presFrame, htmlName, show);
			}
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
