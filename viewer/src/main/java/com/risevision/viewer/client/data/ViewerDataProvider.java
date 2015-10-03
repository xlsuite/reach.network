// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.channel.ChannelConnectionController;
import com.risevision.viewer.client.info.Global;
import com.risevision.viewer.client.info.NotificationType;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.widgets.ViewerNotificationsPanel;

public class ViewerDataProvider {
	private static final int IDLE_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int WAITING_STATE = 2;
	private static final int CONTENT_STATE = 3;
	
	private static int state = IDLE_STATE;
        public enum Reason {
          RETRIEVAL_TIMEOUT, UPDATE_MESSAGE_RECEIVED, VIEWER_INIT, POLLING_TIMER, NO_CACHE, AFTER_CACHE, PREVIEW
        }
	
	private static Timer apiTimer = new Timer() {
		@Override
		public void run() {
			if (state == WAITING_STATE) {
				if (!ViewerDataController.hasData()) {
					ViewerNotificationsPanel.getInstance().show(NotificationType.server_connection_failed);
				}
				
				state = CONTENT_STATE;
			}	
						
			// signifies last attempt to call the Viewer API failed, so we will inform the user of it.
			if (/* ViewerEntryPoint.isDisplay() && */ state == CONTENT_STATE) {
//				if (ViewerEntryPoint.isDisplay()) {
//					reportDataReady(getLocalStorageData(), true);
//				}
				
				state = IDLE_STATE;
			
				retrieveData(Reason.RETRIEVAL_TIMEOUT.toString());
			}
			else {
				state = IDLE_STATE;
			}
		}
	};
	
	public static void retrieveData(String reason) {
		if (state == IDLE_STATE) {
			// [AD] moved this function to after the data is retrieved - workaround for Core bug 
			// where the Viewer API and Channel Token calls can't be made at the same time 
//			channelController.init(channelCommand);
			
			String url;
			
			url = Global.DATA_SERVER_URL.replace("{0}", ViewerEntryPoint.getType());
			url = url.replace("{1}", ViewerEntryPoint.getId());	
			
	    	int viewerWidth = Window.getClientWidth();
	    	int viewerHeight = Window.getClientHeight();
	    	
	    	String sysInfo = ViewerEntryPoint.getSysInfo();
	    	JsArrayString browserInfo = ViewerHtmlUtils.getBrowserVersion();
	    	
	    	String updateTicket = ChannelConnectionController.getUpdateTicket();
	    	updateTicket = updateTicket == null ? "" : "&ticket=" + updateTicket;
	    	
	    	String fullUrl = url + 
	    			"?sig=" + ViewerDataController.getSig() + 
	    			updateTicket + 
	    			"&" + Global.VIEWER_URL_IDENTIFIER + 
	    			(RiseUtils.strIsNullOrEmpty(sysInfo) ? "" : "&" + URL.decodeQueryString(sysInfo)) + 
	    			"&cn=" + browserInfo.get(0) +
	    			"&cv=" + browserInfo.get(1) +
	    			"&width=" + viewerWidth + 
	    			"&height=" + viewerHeight + 
	    			"&callback=?";
			
	
	    	state = WAITING_STATE;
	    	
			// start 60 second timer for timeout of data retrieval
			apiTimer.schedule(ViewerDataController.MINUTE_UPDATE_INTERVAL);
                        ViewerHtmlUtils.logExternalMessage("viewer data retrieval", reason);
			getDataNative(fullUrl);
		}
		else {
			state = CONTENT_STATE;
		}
	}
	
	private static void reportDataReady(JavaScriptObject jso) {
          state = ACTIVE_STATE;
          ChannelConnectionController.init(ViewerDataController.channelCommand);
          ViewerDataController.reportDataReady(jso);
	}

	private static native void getDataNative(String url) /*-{
//		$wnd.startJSONCall(url);
		
		try {
	    	$wnd.writeToLog("Retrieving Viewer Data");
	    	
	    	$wnd.jQuery.getJSON(url, function(result) {
	    	    try { 
	    	    	if (url.indexOf("display") != -1 && $wnd.supportsHtml5Storage()) {
	    	    		if (result && result.content) {
                                        var idMatch = /display\/([0-9A-Za-z-]+)/.exec(url);
                                        if (idMatch && result.display) {
                                          result.display.id = idMatch[1];
	    	    			  $wnd.storeViewerResponse(result);
                                        }
	    	    		}
	    	    	}
	    	    
		        	$wnd.writeToLog("Viewer Data - Status Message - " + result.status.message);
	    	    	
	    	    	@com.risevision.viewer.client.data.ViewerDataProvider::reportDataReady(Lcom/google/gwt/core/client/JavaScriptObject;)(result);
	    	    }
	    	    catch (err) {
	    	    	$wnd.writeToLog("Error Parsing Viewer Data - " + result + " - " + err.message);
                        @com.risevision.viewer.client.utils.ViewerHtmlUtils::logExternalMessage(Ljava/lang/String;Ljava/lang/String;)("viewer data parse error", result + " - " + err.message);
	    	    }
			}, "json");
	    }
	    catch (err) {
	    	$wnd.writeToLog("Error Retrieving Viewer Data - " + url + " - " + err.message);
                @com.risevision.viewer.client.utils.ViewerHtmlUtils::logExternalMessage(Ljava/lang/String;Ljava/lang/String;)("viewer data retrieval error", url + " - " + err.message);
	    }
	}-*/;

}
