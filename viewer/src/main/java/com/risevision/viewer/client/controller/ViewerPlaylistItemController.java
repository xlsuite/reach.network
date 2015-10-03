// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window.Location;
import com.risevision.common.client.info.ImageItemInfo;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.cache.RiseCacheController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerPlaylistItemController implements ViewerControllerInterface {
	private PlaceholderInfo placeholderInfo;
	private PlaylistItemInfo playlistItem;
	private String phName, htmlName, presFrame;
	private boolean isReady = false, isPlaying = false;
	@SuppressWarnings("unused")
	private boolean canPlay, canStop, canPause, canReportReady, canReportDone;
	private Command gadgetReadyCommand, gadgetDoneCommand;
	
	private static final String EMBED_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateEmbed('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateEmbed call - %s3 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	private static final String TEXT_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateText('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateText call - %s3 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	private static final String IMAGE_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateImage('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateImage call - %s2 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	private final String IMAGE_HTML = "" +
			"<html>" +
			"<head>" +
			"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
			"<title></title>" +
			"<script type=\"text/javascript\" src=\"scripts/imageScripts.js\"></script>" +
			"<style>" +
			"	body { background-color: transparent; -moz-user-select: none; -webkit-user-select: none; -khtml-user-select: none; user-select: none; }" +
			"	#cnt {width:100%; height:100%; background-color: %s4%;}" +
			"	#wrapper {width:100%; height:100%; display:table;}" +
			"	#cell {display:table-cell; vertical-align:middle;}" +
			"	#image {display: block; margin: auto;}" +
			"</style>" +
			"<script type='text/javascript'>" +
			"var id = '%s0%';" +
			"(function() {" +
			"	window.onresize = function(event) {" +
			"		resizeImage();" +
			"	};" +
			"	window.oncontextmenu = function() {" +
			"		return false;" +
			"	};" +
			"}());" +
			"function resizeImage() {" +
			"	if (%s5%) {" +
			"		var width = Math.max(document.body.clientWidth, document.documentElement.clientWidth);" +
			"		var height = Math.max(document.body.clientHeight, document.documentElement.clientHeight);" +
			"		var img = document.getElementById('image');" +
			"		if(img.width/img.height < width/height) {" +
			"			img.style.width = 'auto';" +
			"			img.style.height = height;" +
			"		} else {" +
			"			img.style.width = '100%';" +
			"			img.style.height = 'auto';" +
			"		}" +
			"	}" +			
			"}" +
			"function readyEvent() {" +
			"	var img = document.getElementById('image');" +
//			"	img.onload = function() {" +
//			"		resizeImage();" +
//			"		readyEvent();" +
//			"	};" +
			"	img.onclick = function() {" +
			"		try {" +
			"			parent.onClick(id);" +
			"		}" +
			"		catch (e) {}" +
			"	};" +
			"	img.draggable = false;" +
			"" +
			"	parent.itemReady(id, true, true, true, true, true);" +
			"}" +
			"</script>" +
			"</head>" +
			"<body style=\"margin:0px;\">" +
			"<div id=\"cnt\">" +
			"<div id=\"wrapper\">" +
			"<div id=\"cell\">" +
			"<img id=\"image\" src=\"%s3%\" onload=\"resizeImage(); readyEvent();\" " + /* onclick=\"parent.onClick(id); */ "\">" +
			"</div>" +
			"</div>" +
			"</div>" +
			"</body>" +
			"</html>";
	
	private static final String URL_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateUrl('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateUrl call - %s3 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	public ViewerPlaylistItemController(PlaceholderInfo placeholderInfo, PlaylistItemInfo playlistItem, String phName, String htmlName, Command gadgetReadyCommand, Command gadgetDoneCommand) {
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
		if (playlistItem != null && PlaylistItemInfo.TYPE_PRESENTATION.equals(playlistItem.getType())) {
			updateHtmlEmbed(presentation);
		}
		else if (playlistItem != null && PlaylistItemInfo.TYPE_TEXT.equals(playlistItem.getType()) 
				|| PlaylistItemInfo.TYPE_HTML.equals(playlistItem.getType())) {
			updateHtmlText(presentation);
		}
		else if (playlistItem != null && PlaylistItemInfo.TYPE_VIDEO.equals(playlistItem.getType())) {
			
		}
		else if (playlistItem != null && PlaylistItemInfo.TYPE_IMAGE.equals(playlistItem.getType())) {
			updateHtmlImage(presentation);
		}
		else if (playlistItem != null && PlaylistItemInfo.TYPE_URL.equals(playlistItem.getType())) {
			updateHtmlUrl(presentation);
		}
//		else {
//			updateHtmlGadget(presentation);
//		}
	}
	
	private void updateHtmlEmbed(PresentationInfo presentation) {
		String transition = "none";
		String embedId = playlistItem.getObjectData();
		
		String url = Location.getPath();
		if (!RiseUtils.strIsNullOrEmpty(Location.getParameter("gwt.codesvr")))
			url += "?gwt.codesvr=127.0.0.1:9997&";
		else 
			url += "?";
		url += "type=" + ViewerEntryPoint.getType() + "&id=" + embedId + "&parentId=" + presentation.getId();
		
		if (ViewerEntryPoint.isDisplay()) {
			url += "&displayId=" + ViewerEntryPoint.getDisplayId() 
				+ "&" + ViewerEntryPoint.CACHE_IS_ACTIVE_PARAM + Boolean.toString(RiseCacheController.isActive());
		}
		
//		int height = 0, width = 0;
		
//		if (placeholderInfo.getWidthUnits().equals("%")) {
//			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
//			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
//		}
//		else {
//			height = (int)placeholderInfo.getHeight();
//			width = (int)placeholderInfo.getWidth();
//		}

//		double scale = ViewerUtils.getItemScale(playlistItem.getScale());
		
//		height = (int)(height * scale);
//		width = (int)(width * scale);
		
		transition = placeholderInfo.getTransition();
		
		addEmbedScript(presentation, url, phName, htmlName,
				transition);
	}
	
	private void addEmbedScript(PresentationInfo presentation, String url, String containerName, String htmlName, String transition) {
		String scriptTag = EMBED_SCRIPT.replace("%s1", url)
						.replace("%s2", containerName)
						.replace("%s3", htmlName)
						.replace("%s4", transition);
	
		PresentationParser.addScriptTag(presentation, scriptTag);
	}
	
	private void updateHtmlText(PresentationInfo presentation) {
		String transition = "none";
		String text = playlistItem.getObjectData().replace("\n", "").replace("\r", "");
		
		// Removing comments with regExp
//		String exp = "(?s)<!--.*?-->";
//		text = text.replaceAll("(?s)<!--.*?-->", "");  
//		text = text.replaceAll("?s<!--.*?--\\s*>?gs", "");
//		text = text.replaceAll("(?s)<!--.*?--\\s*>?gs", "");  
//		String exp = "/<!--[\\s\\S]*?-->/g";

		String exp = "<!--.*?-->";
		
		RegExp regExp = RegExp.compile(exp, "gm");
		text = regExp.replace(text, "");

//		int height = 0, width = 0;
		
//		if (placeholderInfo.getWidthUnits().equals("%")) {
//			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
//			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
//		}
//		else {
//			height = (int)placeholderInfo.getHeight();
//			width = (int)placeholderInfo.getWidth();
//		}

//		double scale = ViewerUtils.getItemScale(playlistItem.getScale());
		
//		height = (int)(height * scale);
//		width = (int)(width * scale);
		
		transition = placeholderInfo.getTransition();

		addTextScript(presentation, text, phName, htmlName,
				transition);
	}
	
	private void addTextScript(PresentationInfo presentation, String text, String containerName, String htmlName, String transition) {
		text = text.replace("'", "\\'");
		
		String scriptTag = TEXT_SCRIPT.replace("%s1", text)
					.replace("%s2", containerName)
					.replace("%s3", htmlName)
					.replace("%s4", transition);

		PresentationParser.addScriptTag(presentation, scriptTag);
	}
	
	private void updateHtmlImage(PresentationInfo presentation) {
		String transition = "none";

//		int height = 0, width = 0;
		
//		if (placeholderInfo.getWidthUnits().equals("%")) {
//			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
//			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
//		}
//		else {
//			height = placeholderInfo.getHeight();
//			width = placeholderInfo.getWidth();
//		}

//		double scale = ViewerUtils.getItemScale(playlistItem.getScale());
		
//		height = (int)(height * scale);
//		width = (int)(width * scale);
		
		String html = createImageHtmlString(playlistItem.getObjectData());
		transition = placeholderInfo.getTransition();

		addImageScript(presentation, html, phName, htmlName,
				transition);
	}
		
	private String createImageHtmlString(String objectData) {
		ImageItemInfo imageItem = new ImageItemInfo(objectData);
		
		String htmlString = IMAGE_HTML.replace("%s0%", htmlName);
		htmlString = htmlString.replace("%s3%", imageItem.getUrl());
		htmlString = htmlString.replace("%s4%", imageItem.getBackgroundColor());
		htmlString = htmlString.replace("%s5%", Boolean.toString(imageItem.isScaleToFit()));
		
		return htmlString;
	}
	
	private void addImageScript(PresentationInfo presentation, String html, String containerName, String htmlName, String transition) {
		html = html.replace("'", "\\'");
		
			String scriptTag = IMAGE_SCRIPT.replace("%s1", html)
						.replace("%s2", containerName)
						.replace("%s3", htmlName)
						.replace("%s4", transition);
		
		PresentationParser.addScriptTag(presentation, scriptTag);
	}
	
	private void updateHtmlUrl(PresentationInfo presentation) {
		String transition = "none";
		String url = playlistItem.getObjectData();

//		int height = 0, width = 0;
		
//		if (placeholderInfo.getWidthUnits().equals("%")) {
//			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
//			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
//		}
//		else {
//			height = (int)placeholderInfo.getHeight();
//			width = (int)placeholderInfo.getWidth();
//		}

//		double scale = ViewerUtils.getItemScale(playlistItem.getScale());
		
//		height = (int)(height * scale);
//		width = (int)(width * scale);
		
		transition = placeholderInfo.getTransition();
		
		String scriptTag = URL_SCRIPT.replace("%s1", url)
						.replace("%s2", phName)
						.replace("%s3", htmlName)
						.replace("%s4", transition);
	
		PresentationParser.addScriptTag(presentation, scriptTag);
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
				try {
					ViewerHtmlUtils.playCommand(presFrame, htmlName, show);
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
			ViewerHtmlUtils.stopCommand(presFrame, htmlName, hide);
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
			ViewerHtmlUtils.pauseCommand(presFrame, htmlName, hide);
		}
		catch (Exception e) {

		}
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
