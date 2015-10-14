// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import java.util.ArrayList;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.info.VideoItemInfo;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.cache.RiseCacheController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class PlaceholderVideoController {
	private PlaceholderInfo placeholderInfo;
	private String phName, presFrame;
	private int currentVideo = -1, nextVideo = -1, previousVideo = -1;
//	int height, width;
	private ArrayList<String> videoItems = new ArrayList<String>();
	private ArrayList<ViewerVideoController> videoItemObjects = new ArrayList<ViewerVideoController>();
	private ArrayList<Boolean> videoItemLoaded = new ArrayList<Boolean>();
	private ArrayList<Integer> videoErrorCount = new ArrayList<Integer>();
	
	private double lastPosition = -1;
	private int nudgeCounter = 0;
	private Timer checkPlayTimer = new Timer() {
		
		@Override
		public void run() {
			verifyPosition();
		}
	};
	
	private final String LOAD_ERROR = "loadError";
	
	private static final String VIDEO_SCRIPT = "<script><!--\n" +
			"try {" +
			"updateVideo('%s1', '%s2', '%s3', '%s4');" +
			"} catch(err) { parent.writeToLog('updateVideo call - %s2 - ' + err.message); }" +
			"\n//-->" +
			"</script>";
	
	private final String HTML_JWPLAYER = "" +
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
		"<title></title>" +
//		"<script type=\"text/javascript\" src=\"mediaplayer/BrowserDetect.js\"></script>" +
		"<script type=\"text/javascript\" src=\"jwplayer/jwplayer.js\"></script>" + 	
		"<script>jwplayer.key=\"xBXyUtAJ+brFzwc2kNhDg/Sqk8W7rmktAYliYHzVgxo=\"</script>" +
		"<script type=\"text/javascript\" src=\"scripts/videoScripts.js\"></script>" +
		"<style>" +
		"	body { " +
		"		background-color: transparent; " +
		"		-moz-user-select: none; " +
		"		-webkit-user-select: none; " +
		"		-khtml-user-select: none; " +
		"		user-select: none; " +
		"	}" +
		"</style>" +
		"</head>" +
		"<body style=\"margin:0px;\">" +
		"<div id=\"flash\" style=\"visibility:hidden\">Loading the player...</div>" +
		"<script language=\"javascript\">" +
		"	window.oncontextmenu = function() {" +
		"		return false;" +
		"	};" +
		"	try {" +
		"		player = new PlayerJW();" +
		"		loadVideo(\"%s1\", \"%s2\", \"%s3\", \"%s4\", \"%s5\", %s6, %s7, %s8);" +
		"	} catch (e) {" +
		"		console.log(e.message);" +
		"		parent.itemError(\"%s1\", \"" + LOAD_ERROR + "\");" +
		"	}" +
		"</script>" +
		"</body>" +
		"</html>";
	
	private final String HTML_VIDEOJS = "" +
		"<html>" +
		"<head>" +
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
		"<title></title>" +
//		"<script>" +
//		"	window.onerror = function (error) {	parent.itemError(\"%s1\"); }" +
//		"</script>" +
		"<link href=\"videojs/video-js.css\" rel=\"stylesheet\">" +
			//todo (IL): use minified video.min.js
		"<script type=\"text/javascript\" src=\"videojs/video.js\"></script>" +
		"<script type=\"text/javascript\" src=\"scripts/videoScripts.js\"></script>" +
		"<style>" +
		"	body { background-color: transparent; -moz-user-select: none; -webkit-user-select: none; -khtml-user-select: none; user-select: none; }" +
		"	.vjs-default-skin .vjs-loading-spinner, .vjs-default-skin .vjs-big-play-button { visibility: hidden !important; }" +
		"   .video-js { height:100% !important; width:100% !important; }" +
		"</style>" +
		"</head>" +
		"<body style=\"margin:0px;\">" +
		"<div id=\"playerjs\" style=\"visibility:hidden\"><video id=\"videoPlayer\" preload=\"auto\" controls class=\"video-js vjs-default-skin\"></video></div>" +
//		"<div id=\"html5\" style=\"visibility:hidden;display:none;width:%s2;height:%s3;\">" +
//		"<video id=\"html5_video\" src=\"\" style=\"width:%s2;height:%s3;\"></video>" +
		"</div>" +
		"<script language=\"javascript\">" +
		"try {	" +
		"	player = new PlayerJs();" +
		// Video JS doesn't need file extension (%s5)
		"	loadVideo(\"%s1\", \"%s2\", \"%s3\", \"%s4\", %s6, %s7, %s8);" +
		"} catch (e) {" +
		"	console.log(e.message);" +
		"	parent.itemError(\"%s1\", \"" + LOAD_ERROR + "\");" +
		"}" +
		"</script>" +
		"</body>" +
		"</html>";
	
	public PlaceholderVideoController(PlaceholderInfo placeholderInfo, String presFrame, String phName) {
		this.placeholderInfo = placeholderInfo;
		this.presFrame = presFrame;
		this.phName = phName;
		
//		if (placeholderInfo.getWidthUnits().equals("%")) {
//			width = (int)((placeholderInfo.getWidth() / 100.0) * Window.getClientWidth());
//			height = (int)((placeholderInfo.getHeight() / 100.0) * Window.getClientHeight());
//		}
//		else {
//			height = placeholderInfo.getHeight();
//			width = placeholderInfo.getWidth();
//		}
	}
	
	public void addVideo(String itemName, ViewerVideoController videoController) {
		if (nextVideo == -1 && (!ViewerEntryPoint.isDisplay() || videoController.getItem().getTimeline().canPlay())) {
			nextVideo = videoItems.size();
		}
		
		videoItems.add(itemName);
		videoItemObjects.add(videoController);
		videoItemLoaded.add(false);
		videoErrorCount.add(0);
	}
	
	public void updateHtml(PresentationInfo presentation) {		
		if (nextVideo != -1) {			
			addVideoScript(presentation, createHtmlString(nextVideo), phName, videoItems.get(nextVideo),
//					width + "px",
//					height + "px",
//					0 + "px", 0 + "px",
					placeholderInfo.getTransition());
			
			videoItemLoaded.set(nextVideo, true);
		}
	}
	
	private void addVideoScript(PresentationInfo presentation, String html, String containerName, String htmlName, String transition) {
		html = html.replace("'", "\\'");
		
		String scriptTag = VIDEO_SCRIPT.replace("%s1", html)
						.replace("%s2", containerName)
						.replace("%s3", htmlName)
						.replace("%s4", transition);
		
		PresentationParser.addScriptTag(presentation, scriptTag);
	}
	
//	public void setPresFrame(String presFrame) {
//		this.presFrame = presFrame;
//	}
	
//	public void reportReady(ViewerVideoController videoController) {
//		int readyVideo = videoItemObjects.indexOf(videoController);
//		
//		videoErrorCount.set(readyVideo, 0);
//	}
	
	public void reportError(ViewerVideoController videoController, String reason) {
		int errorVideo = videoItemObjects.indexOf(videoController);
		
		destroyVideo(errorVideo);
		videoItemLoaded.set(errorVideo, false);
		
		if (!LOAD_ERROR.equals(reason)) {
//			videoErrorCount.set(errorVideo, videoErrorCount.get(errorVideo) + 1);
			
			// error from Video - skip video and load the next one
			if (errorVideo == nextVideo) {
				getNextPlayableVideo();
				
				if (nextVideo != errorVideo) {
					loadNextVideo();
				}
			}
			else if (errorVideo == currentVideo) {
				videoController.setDone();
			}
		}
	}
	
	public void readyRequested(ViewerVideoController videoController) {
		int requestedVideo = videoItemObjects.indexOf(videoController);

		if (currentVideo == -1 && nextVideo == -1 && !videoItemLoaded.get(requestedVideo)) {
			getNextPlayableVideo();
			
			loadNextVideo();
		}
	}
	
	public void play(ViewerVideoController videoController, boolean show) {
		int playVideo = videoItemObjects.indexOf(videoController);

		destroyPreviousVideo();

		if (playVideo == nextVideo) {
			if (getPlayableVideoCount() > 3) {
				previousVideo = currentVideo;
			}
			
			compareNextVideo(currentVideo);
			
			currentVideo = nextVideo;
			
			getNextPlayableVideo();
			playCurrentVideo(show);
		}
		else if (playVideo == currentVideo) {
			nextVideo = currentVideo;
			getNextPlayableVideo();
			
			playCurrentVideo(show);
		}
		else if (videoItemLoaded.get(playVideo)) {
			nextVideo = currentVideo;
			currentVideo = playVideo;
			
			playCurrentVideo(show);
		}
		else {
			nextVideo = -1;
			currentVideo = playVideo;
			
			playCurrentVideo(show);
		}
		
		// load the next video
		loadNextVideo();
	}
	
	private void playCurrentVideo(boolean show) {
		try {
			ViewerHtmlUtils.playCommand(presFrame, videoItems.get(currentVideo), show);
			
			checkPlayTimer.scheduleRepeating(10000);
			checkPlayTimer.run();
		}
		catch (Exception e) {

		}
	}
	
	private void loadNextVideo() {
		// next video not ready yet?
//		if (videoItemLoaded.get(nextVideo) && !videoItemObjects.get(nextVideo).isReady()) {
//			destroyVideo(nextVideo);
//		}
		
		if (!videoItemLoaded.get(nextVideo) && videoErrorCount.get(nextVideo) < 3) {
			if (currentVideo != -1
					&& videoItemObjects.get(currentVideo).getVideoInfo().getVideoUrl().equals(videoItemObjects.get(nextVideo).getVideoInfo().getVideoUrl())) {
				videoItemLoaded.set(nextVideo, true);
				videoItemObjects.get(nextVideo).setReady(presFrame, true, true, true, true, true);
			}
			else if (!compareNextVideo(previousVideo)) {
				videoItemLoaded.set(nextVideo, true);

				createVideo(nextVideo);
			}
		}
	}
	
	private boolean compareNextVideo(int thisVideo) {
		VideoItemInfo nextVideoInfo = videoItemObjects.get(nextVideo).getVideoInfo();
		if (thisVideo != -1 && videoItemObjects.get(thisVideo).getVideoInfo().getVideoUrl().equals(nextVideoInfo.getVideoUrl())) {
			if (thisVideo != nextVideo) {
				configureVideoNative(presFrame, videoItems.get(thisVideo), videoItems.get(nextVideo), nextVideoInfo.getVideoVolume(), nextVideoInfo.isAutoHide(), nextVideoInfo.isCarryOn());
				
				videoItemLoaded.set(nextVideo, true);
				videoItemObjects.get(nextVideo).setReady(presFrame, true, true, true, true, true);
	
				videoItemLoaded.set(thisVideo, false);
				videoItemObjects.get(thisVideo).setReady(false);
			}
			
			if (thisVideo == previousVideo) {
				ViewerHtmlUtils.pauseCommand(presFrame, videoItems.get(nextVideo), true);
			}
			previousVideo = -1;
			
			return true;
		}
		else {
			return false;
		}
	}
	
	private void getNextPlayableVideo() {
		do {
			if (nextVideo < videoItemObjects.size() - 1) {
				nextVideo++;
			} else {
				nextVideo = 0;
			}
		}
		while (nextVideo != currentVideo && ((ViewerEntryPoint.isDisplay() 
				&& !videoItemObjects.get(nextVideo).getItem().getTimeline().canPlay())
				|| !(videoErrorCount.get(nextVideo) < 3)));
	}
	
	private int getPlayableVideoCount() {
		int count = 0;
		
		for (ViewerVideoController videoItem: videoItemObjects) {
			if (!ViewerEntryPoint.isDisplay() || videoItem.getItem().getTimeline().canPlay()) {
				count++;
			}
		}
		
		return count;
	}
	
	public void stop(ViewerVideoController videoController, boolean hide) {
		if (previousVideo != -1) {
			videoItemObjects.get(previousVideo).setReady(false);
		}
		
		pausePlayTimer(videoController);
		
		try {
			int itemIndex = videoItemObjects.indexOf(videoController);
			if (videoItemLoaded.get(itemIndex)) {
				ViewerHtmlUtils.stopCommand(presFrame, videoItems.get(itemIndex), hide);
			}
		}
		catch (Exception e) {

		}
	}
	
	public void pause(ViewerVideoController videoController, boolean hide) {
		if (previousVideo != -1) {
			videoItemObjects.get(previousVideo).setReady(false);
		}

		pausePlayTimer(videoController);
		
		try {
			int itemIndex = videoItemObjects.indexOf(videoController);
			if (videoItemLoaded.get(itemIndex)) {
				ViewerHtmlUtils.pauseCommand(presFrame, videoItems.get(itemIndex), hide);
			}
		}
		catch (Exception e) {

		}
	}
	
	private void pausePlayTimer(ViewerVideoController videoController) {
		if (videoItemObjects.indexOf(videoController) == currentVideo) {
			checkPlayTimer.cancel();
			lastPosition = -1;
		}
	}
	
	private void verifyPosition() {
		if (currentVideo != -1) {
			double d = checkVideoPosition(presFrame, videoItems.get(currentVideo));
			
			if (d != -1) {
				if (lastPosition != -1) {
					if (d == lastPosition) {
						if (nudgeCounter < 3) {
							nudgeVideo(presFrame, videoItems.get(currentVideo), lastPosition + 0.01);
							nudgeCounter++;
							d = -1;
						}
						else {
							Window.Location.reload();
						}
					}
					else {
						nudgeCounter = 0;
					}
				}
				lastPosition = d;
			}
		}
	}
	
	private void createVideo(int videoId) {	
		createVideoNative(presFrame, createHtmlString(videoId), phName, videoItems.get(videoId),
//				width + "px",
//				height + "px",
				placeholderInfo.getTransition());
	}
	
	private void destroyPreviousVideo() {
		destroyVideo(previousVideo);
	}

	private void destroyVideo(int videoItem) {
		if (videoItem != -1 && videoItemLoaded.get(videoItem)) { 
			ViewerHtmlUtils.destroyFrameElement(presFrame, videoItems.get(videoItem), phName);
			videoItemLoaded.set(videoItem, false);
			videoItemObjects.get(videoItem).setReady(false);
		}
	}
	
	private String createHtmlString(int videoId) {
		VideoItemInfo videoItem = videoItemObjects.get(videoId).getVideoInfo();
		
		boolean isHTML5Video = false;
//		String[] extensions = {".webm", 
//				".ogv", 
//				".ogg", 
////				".mov", 
////				".mp4"
//				};
	
//		String url = videoItem.getVideoUrl();
//		String extension = "";
//		if (url.lastIndexOf('.') != -1) {
//			extension = url.substring(url.lastIndexOf('.')).toLowerCase();
//		}
	
//		for (int i = 0; i < extensions.length; i++) {
//			if (extension.equals(extensions[i])) {
//				isHTML5Video = true;
//	
//				break;
//			}
//		}

		String videoUrl = videoItem.getVideoUrl();
		// Append Display id to Video URL
		if (ViewerEntryPoint.isDisplay() && videoUrl.contains("commondatastorage.googleapis.com")) {
			videoUrl += "?displayId=" + ViewerEntryPoint.getDisplayId();
		}
		String url = RiseCacheController.getCacheVideoUrl(videoUrl, videoItem.getVideoExtension());
		
		String htmlString = isHTML5Video ? HTML_VIDEOJS : HTML_JWPLAYER;
		htmlString = htmlString.replace("%s1", videoItems.get(videoId));
		htmlString = htmlString.replace("%s2", "100%");
		htmlString = htmlString.replace("%s3", "100%");
//		htmlString = htmlString.replace("%s2", width + "px");
//		htmlString = htmlString.replace("%s3", height + "px");
		htmlString = htmlString.replace("%s4", url.trim());
		htmlString = htmlString.replace("%s5", videoItem.getVideoExtension());
		htmlString = htmlString.replace("%s6", Integer.toString(RiseUtils.strToInt(videoItem.getVideoVolume(), 0)));
		htmlString = htmlString.replace("%s7", Boolean.toString(videoItem.isAutoHide()));
		htmlString = htmlString.replace("%s8", Boolean.toString(videoItem.isCarryOn()));
		
		return htmlString;
	}
	
	private static native void createVideoNative(String presFrame, String html, String containerName, String htmlName,
			String transition) /*-{
		try {
			$wnd.updateVideo(presFrame, html, containerName, htmlName, transition);
		} catch (err) {$wnd.writeToLog("updateVideo - " + htmlName + " - " + err);}
	}-*/;
	
	private static native void configureVideoNative(String presFrame, String htmlName, String newHtmlName,
			String volumeParam, boolean autoHideParam, boolean carryOnParam) /*-{
		try {
			$wnd.configureVideo(presFrame, htmlName, newHtmlName, volumeParam, autoHideParam, carryOnParam);
		} catch (err) {$wnd.writeToLog("configureVideo - " + htmlName + " - " + err);}
	}-*/;
	
	private static native double checkVideoPosition(String presFrame, String htmlName) /*-{
		try {
//			debugger; 
			
			var presElement = $wnd.document.getElementById(presFrame);
			if (presElement) {
				var videoElement = presElement.contentWindow.document.getElementById("if_" + htmlName);
				if (videoElement) {
					return videoElement.contentWindow.player.getPosition();
					
//					if (!videoElement.contentWindow.isHTML5Video) {
//						return videoElement.contentWindow.jwplayer().getPosition();
//					}
//					else {
//						return videoElement.contentWindow.document.getElementById("html5_video").currentTime;
//					}
				}
			}
			return -1;
		} catch (err) {$wnd.writeToLog("checkVideoPlayback - " + htmlName + " - " + err);}
	}-*/;
	
	private static native double nudgeVideo(String presFrame, String htmlName, double nextPosition) /*-{
		try {
//			debugger; 
			
			var presElement = $wnd.document.getElementById(presFrame);
			if (presElement) {
				var videoElement = presElement.contentWindow.document.getElementById("if_" + htmlName);
				if (videoElement) {
					videoElement.contentWindow.player.resetPosition(nextPosition);
					
//					if (!videoElement.contentWindow.isHTML5Video) {
//						if (videoElement.contentWindow.jwplayer().getPosition() >= videoElement.contentWindow.jwplayer().getDuration() - 1) {
////							videoElement.contentWindow.jwplayer().seek(0);
//							videoElement.contentWindow.doneEvent();
//						}
//						else {
//							videoElement.contentWindow.jwplayer().seek(nextPosition);
//							videoElement.contentWindow.jwplayer().play(true);
//						}
//					}
//					else {
//						videoElement.contentWindow.document.getElementById("html5_video").currentTime = nextPosition;
//						videoElement.contentWindow.document.getElementById("html5_video").play();
//					}
				}
			}
			return -1;
		} catch (err) {$wnd.writeToLog("nudgeVideo - " + htmlName + " - " + err);}
	}-*/;
	
}
