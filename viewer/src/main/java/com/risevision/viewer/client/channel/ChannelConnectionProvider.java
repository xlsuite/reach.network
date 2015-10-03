// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.channel;

import com.google.gwt.user.client.Timer;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.info.Global;

public abstract class ChannelConnectionProvider {

	protected static final String MESSAGE_CONNECT = "connect";
	protected static final String MESSAGE_ERROR = "error";
	protected static final String MESSAGE_CLOSE = "close";
//	protected static final String MESSAGE_INVALID_TOKEN = "invalid token";
	
	@Deprecated
	protected static final String MESSAGE_CONTENT_UPDATED = "content updated";
	protected static final String MESSAGE_UPDATED = "updated";
	protected static final String MESSAGE_PING = "ping";
	protected static final String MESSAGE_AYT = "ayt";
	
	protected static final String CONNECTION_FAILED = "connection failed";
	
	private static final String HTML_STRING = "" +
			"<html>\n" +
			"<head>\n" +
			"<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>\n" +
			"<script type='text/javascript' src='" + Global.SERVER_URL + "/_ah/channel/jsapi'></script>\n" +
			"<script>\n" +
			"var socket;\n" +
			"\n" +
			"function onOpen() {\n" +
			"	parent.writeToLog('Channel opened.');\n" +			
			"	parent.channelMessage('" + MESSAGE_CONNECT + "');\n" +
			"}\n" +
			"function onMessage(message) {\n" +
			"	parent.writeToLog('Channel message: ' + message.data);\n" +
			"	parent.channelMessage(message.data);\n" +
			"}\n" +
			"function onError(error) {\n" +
			"	parent.writeToLog('Channel error:' + error.description + ' code=' + error.code);\n" +
			"	parent.channelError(error.code, error.description);\n" +
			"}\n" +
			"function onClose() {\n" +
			"	parent.writeToLog('Channel closed, attempting to reconnect.');\n" +
			"	parent.channelMessage('" + MESSAGE_CLOSE + "');\n" +
//			"	connect();\n" +
			"}\n" +
			"function connect(reason, token) {\n" +
			"\n" +
			"	parent.writeToLog('Channel Connecting - reason: ' + reason);\n" +
			"	channel = new goog.appengine.Channel(token);\n" +
			"	socket = channel.open({\n" +
			"		onopen: onOpen,\n" +
			"		onmessage: onMessage,\n" +
			"		onerror: onError,\n" +
			"		onclose: onClose\n" +
			"	});\n" +
			"}\n" +
			"function disconnect() {\n" +
			"	try {\n" +
			"		if (socket) {\n" +
			"			socket.close();\n" +
			"			parent.writeToLog('Channel socket closed.');\n" +
			"		}\n" +
			"	}\n" +
			"	catch (err) {" +
			"		parent.writeToLog('Channel socket close error.');\n" +			
			"	}\n" +
			"}\n" +
			"</script>\n" +
			"</head>\n" +
			"<body onload='connect(\"%reason%\", \"%token%\");'>\n" +
			"</body>\n" +
			"</html>";
	
	private static final String REASON_RECONNECT = "reconnect";
		
	protected static String channelToken;
	
	private static final int IDLE_STATE = 0;
	private static final int ACTIVE_STATE = 1;
	private static final int WAITING_STATE = 2;
	
	private static int state = IDLE_STATE;

	private static Timer apiTimer = new Timer() {
		public void run() {
			// signifies last attempt failed 
			if (state == WAITING_STATE) {
				state = IDLE_STATE;
			
				createChannel(REASON_RECONNECT, 0);
			}
			else {
				state = IDLE_STATE;
			}
		}
	};
	
	protected static void createChannel(String reason, int delay) {
		// Token should never be null or empty
		if (!RiseUtils.strIsNullOrEmpty(channelToken)) {

			if (state == IDLE_STATE) {
				final String html = HTML_STRING.replace("%reason%", reason).replace("%token%", channelToken);
	
		    	state = WAITING_STATE;
		    	
                                ViewerHtmlUtils.logExternalMessage("channel creation", reason + " - " + (delay / 1000) + "s delay");
				destroyChannelNative();
				new Timer() {
                                  public void run() {
                                    apiTimer.schedule(ViewerDataController.MINUTE_UPDATE_INTERVAL);
                                    createChannelNative(html);
                                  }
                                }.schedule(delay);
			}
			else {
				state = WAITING_STATE;
			}
		}
	}
	
	protected static void channelConnected() {
		if (state == WAITING_STATE) {
			state = ACTIVE_STATE;
		}
	}
	
	protected static void channelDisconnected() {
		destroyChannelNative();
	}

//	protected static void sendAytNotification() {
//		String url = Global.SERVER_URL + "/viewer/checkin?displayId=" + 
//				ViewerEntryPoint.getDisplayId();
//		
//		sendAytNotificationNative(url);
//	}
	
	protected static native void destroyChannelNative() /*-{
//		debugger; 
		
		var myFrame = $wnd.document.getElementById('channelFrame');
		
		if (myFrame) {
			myFrame.contentWindow.disconnect();
			$wnd.destroyElement('channelFrame', 'channelDiv');
		}
		
	}-*/;
	
	protected static native void createChannelNative(String html) /*-{
//		debugger; 
		
		myFrame = $wnd.createNewElement('channelFrame', 'iFrame', 0, 0, 0, 0);
		
		if (myFrame) {
			$wnd.document.getElementById('channelDiv').appendChild(myFrame);
			$wnd.populateIframe('channelFrame', html);
		}
//		$wnd.channelConnect();
	}-*/;

//	private static native void connectChannelNative(String reason, String channelToken) /*-{
//		$wnd.document.getElementById('channelFrame').contentWindow.disconnect();
//		$wnd.document.getElementById('channelFrame').contentWindow.connect(reason, channelToken);
//	}-*/;

//	private static native void sendAytNotificationNative(String url) /*-{
//		$wnd.writeToLog("Sending AYT notification.");
//		
//		$wnd.$.ajax(url);
//	}-*/;
	
}
