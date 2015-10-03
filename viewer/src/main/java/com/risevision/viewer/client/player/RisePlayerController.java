// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.player;

import java.util.Date;

import com.google.gwt.user.client.Timer;
import com.risevision.viewer.client.info.ViewerPlayerInfo;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class RisePlayerController {
	private static boolean isActive = false; 
	private static final String baseUrl = "http://localhost:9449/";
	private static final String cmdRestart = "restart";
	private static final String cmdShutdown = "shutdown";
	private static final String cmdSave = "save_property";
	private static final String cmdSetProperty = "set_property";
	private static final String cmdHeartbeat = "heartbeat";
	
	// TODO: Not used?
//	private static final String paramDisplayId = "display_id";
//	private static final String paramClaimId = "claim_id";
	
	private static final String paramRestartRequired = "restart_required";
	private static final String paramRebootRequired = "reboot_required";
	private static final String paramRebootTime = "reboot_time";
	private static final String paramRebootEnabled = "reboot_enabled";
	private static final String paramUpdateRequired = "update_required";
	private static final String paramOrientation = "orientation";
	
	private static final String displayCommand = "display_command";
	
	private static final int HEARTBEAT_TIME = 1 * 60 * 1000;
	private static Timer playerHeartbeatTimer = new Timer() {
		public void run() {
			pingPlayer();
		}
	};
		
	public static void restart() {
		String url = baseUrl + cmdRestart + "?" + getTimestampParam();
		callUrlNative(url, cmdRestart);
	}

	public static void shutdown() {
		String url = baseUrl + cmdShutdown + "?" + getTimestampParam();
		callUrlNative(url, cmdShutdown);
	}

	private static String getTimestampParam() {
		return "timestamp=" + String.valueOf(new Date().getTime());
	}

	public static void saveAndRestart(String displayId, String claimId) {
		String url = baseUrl + cmdSave + "?restart_viewer=true&display_id=" + displayId + "&claim_id=" + claimId + "&" + getTimestampParam();
		callUrlNative(url, cmdSave);
	}
	
	public static void setPlayerInfo(ViewerPlayerInfo playerInfo) {
		if (isActive) {
			String url = baseUrl + cmdSetProperty + 
					"?" + paramRestartRequired + "=" + playerInfo.getRestartRequired() +
					"&" + paramRebootRequired + "=" + playerInfo.getRebootRequired() +
					"&" + paramRebootTime + "=" + playerInfo.getRebootTime() +
					"&" + paramRebootEnabled + "=" + playerInfo.getRebootEnabled() +
					"&" + paramUpdateRequired + "=" + playerInfo.getUpdateRequired() +
					"&" + paramOrientation + "=" + playerInfo.getOrientation() +
					"&" + getTimestampParam();
			
                        try {
                          String command = "";
                          if (playerInfo.getUpdateRequired() == "true") {command = "update required";}
                          if (playerInfo.getRebootRequired() == "true") {command = "reboot required";}
                          if (playerInfo.getRestartRequired() == "true") {command = "restart required";}
                          if (command != "") {ViewerHtmlUtils.logExternalMessage("player command", command);}
                        } catch(Exception e) {}

			callUrlNative(url, cmdSetProperty);
		}
	}
	
	public static void setDisplayCommand(boolean isOff) {
		if (isActive) {
			String params = displayCommand + "=" + (isOff ? "off" : "on");
			String url = baseUrl + cmdSetProperty + "?" + 
					params;
			
			callUrlNative(url, params);
		}
	}
		
	private static void pingPlayer() {
		if (isActive) {
			String url = baseUrl + cmdHeartbeat + "?callback=?";
			callUrlNative(url, "ping");
		
			playerHeartbeatTimer.schedule(HEARTBEAT_TIME);
		}
	}

	private static void pingResponseStatic() {
		//this is not necessary as we detect Player's presence from "player=true" parameter in Viewer's URL
//		isActive = true; 
	}
	
	private static native void callUrlNative(String url, String action) /*-{
	    try {
	    	$wnd.writeToLog("Rise Player request - " + action);
	    	
			$wnd.$.getJSON(url,
				{
					format: 'json'
				},
				function() {
		    	    try { 
	//	    	    	debugger;
		    	    	
			        	$wnd.writeToLog("Rise Player response - active");
		    	    	
		    	    	@com.risevision.viewer.client.player.RisePlayerController::pingResponseStatic()();
		    	    }
		    	    catch (err) {
		    	    	$wnd.writeToLog("Rise Player request failed - " + url + " - " + err.message);
		    	    }
				}
			);
	    }
	    catch (err) {
	    	$wnd.writeToLog("Rise Player request error - " + url + " - " + err.message);
	    }
	}-*/;

	public static void setIsActive(String value) {
		if ("true".equalsIgnoreCase(value))
			isActive = true;
		
		pingPlayer();
	}

	public static boolean getIsActive() {
		return isActive;
	}

}
