// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.channel;

import com.google.gwt.user.client.Timer;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.info.Global;

public class ChannelTokenProvider {
	
	private static final int IDLE_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int WAITING_STATE = 2;
	private static final int CONTENT_STATE = 3;
	
	private static int state = IDLE_STATE;

	private static Timer apiTimer = new Timer() {
		public void run() {
			if (state == WAITING_STATE) {
//				ViewerNotificationsWidget.getInstance().show("Server connection failed. Retrying...");
				
				state = CONTENT_STATE;
			}	
						
			// signifies last attempt to call the Viewer API failed, so we will inform the user of it.
			if (/* ViewerEntryPoint.isDisplay() && */ state == CONTENT_STATE) {
//				if (ViewerEntryPoint.isDisplay()) {
//					reportDataReady(getLocalStorageData(), true);
//				}
				
				state = IDLE_STATE;
			
				retrieveToken();
			}
			else {
				state = IDLE_STATE;
			}
		}
	};
	
	public static void retrieveToken() {
		if (ChannelConnectionController.isInactive() && !ViewerDataController.isBlocked()) {
			if (state == IDLE_STATE) {
				String url = Global.CHANNEL_SERVER_URL + 
								ViewerEntryPoint.getDisplayId() + "/channel?" + Global.VIEWER_URL_IDENTIFIER + "&callback=?";
				
		    	state = WAITING_STATE;
		    	
				// start 60 second timer for timeout of data retrieval
				apiTimer.schedule(ViewerDataController.MINUTE_UPDATE_INTERVAL);
				
                                ViewerHtmlUtils.logExternalMessage("channel token retrieval", null);
				retrieveChannelTokenNative(url);

			}
			else {
				state = CONTENT_STATE;
			}
		}
	}
	
	private static void setToken(String newToken) {
		state = ACTIVE_STATE;

		ChannelConnectionController.setChannelToken(newToken);
	}
	
	private static native void retrieveChannelTokenNative(String url) /*-{
		$wnd.writeToLog("Retrieving channel token.");
		
		$wnd.$.getJSON(url,
			{
				format: 'json'
			},
			function(data) {
				if (data.token && data.token != 'null') {
//					debugger;
					$wnd.writeToLog("Retrieving channel token - OK.");
					@com.risevision.viewer.client.channel.ChannelTokenProvider::setToken(Ljava/lang/String;)(data.token);
				}
				else {
					$wnd.writeToLog("Invalid channel token - null.");
					@com.risevision.viewer.client.channel.ChannelTokenProvider::setToken(Ljava/lang/String;)(null);
				}
			}
		);
	}-*/;
	
}
