// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.channel;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.data.ViewerDataParser;
import com.risevision.viewer.client.data.ViewerDataProvider;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ChannelConnectionController extends ChannelConnectionProvider {
	
	private static final int INITIAL_STATE = 0;
	private static final int INACTIVE_STATE = 1;
	private static final int ACTIVE_STATE = 2;
	private static final int RECONNECT_DELAY = 60 * 1000 * 5;
//	private static final int UPDATE_STATE = 3;
//	private static final int CLOSED_STATE = 4;
	
	private static final String REASON_NEW = "new";
	private static final String REASON_NEW_TOKEN = "newtoken";
	private static final String REASON_NOPING = "noping";
	private static final String REASON_ERROR = "error";
	private static final String REASON_RECONNECT = "reconnect";
		
	private static Command channelCommand;
	private static int state = INITIAL_STATE;
	private static String updateTicket;
		
	private static Timer connectionVerificationTimer = new Timer() {
		public void run() {
			state = INACTIVE_STATE;
			
			if (channelCommand != null) {
				channelCommand.execute();
			}
			
			connectChannel(REASON_NOPING, RECONNECT_DELAY);	
		}
	};

	public static void init(Command newChannelCommand) {
		if (state == INITIAL_STATE) {
			channelCommand = newChannelCommand;
			
			if (ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed()) {
				state = INACTIVE_STATE;
				
				connectChannel(REASON_NEW, 0);
			}
		}
	}
	
	public static void setChannelToken(String newChannelToken) {
		channelToken = newChannelToken;

		if (RiseUtils.strIsNullOrEmpty(channelToken)) {
			connectionCancelled();
		}
		else if (state == INACTIVE_STATE) {
			connectChannel(REASON_NEW_TOKEN, 0);
		}
	}
	
	// Static JS callback function
	public static void setChannelMessage(String message) {
		ViewerHtmlUtils.logExternalMessage("channel message", message);
		if (message != null && !message.isEmpty() && state != INITIAL_STATE) {
			if (message.equals(MESSAGE_CONNECT) || message.equals(MESSAGE_PING) /* || message.equals(MESSAGE_AYT) */ ) {
				if (!message.equals(MESSAGE_CONNECT)	|| state != INACTIVE_STATE) {
					state = ACTIVE_STATE;
					
//					if (message.equals(MESSAGE_AYT)) {				
//						sendAytNotification();
//					}
				}
				
				channelConnected();
				
				connectionVerificationTimer.cancel();
				connectionVerificationTimer.schedule((ViewerDataParser.getInstance().getPingInterval() + 3) * 
						ViewerDataController.MINUTE_UPDATE_INTERVAL);
			}
//			else if (message.startsWith(MESSAGE_CONTENT_UPDATED)) {
//				updateTicket = message.substring(MESSAGE_CONTENT_UPDATED.length()).trim();
//				
//				ViewerDataProvider.retrieveData();
//				
////				state = UPDATE_STATE;
//				state = ACTIVE_STATE;
//			}
			else if (message.startsWith(MESSAGE_UPDATED)) {
				updateTicket = message.substring(MESSAGE_UPDATED.length()).trim();
				
				ViewerDataProvider.retrieveData(ViewerDataProvider.Reason.UPDATE_MESSAGE_RECEIVED.toString());
				
//				state = UPDATE_STATE;
				state = ACTIVE_STATE;				
			}
//			else if (message.equals(MESSAGE_HEARTBEAT_STRING)) {
//				connectionVerificationTimer.cancel();
//				connectionVerificationTimer.schedule(VERIFICATION_TIME);
//			}
//			else if (message.equals(INVALID_TOKEN_STRING)) {
//				channelTokenTimer.cancel();
//				
//				connectionCancelled();
//			}
			else if (message.equals(MESSAGE_AYT)) {
				//state = INACTIVE_STATE;
				
				//connectChannel(MESSAGE_AYT);
			}
//			else if (message.equals(CONNECTION_FAILED)) {
//				connectionCancelled();
//
//				state = INACTIVE_STATE;				
//			}
//			else if (message.startsWith(MESSAGE_ERROR) && state != INACTIVE_STATE) {
//				state = INACTIVE_STATE;
//				
//				String [] tokens = message.split(":");
//				if (tokens.length > 1 && !"401".equals(tokens[1])) {
//				}
//				else {
//					channelToken = "";
//					connectionVerificationTimer.cancel();
//				}
//				
//				connectChannel(REASON_ERROR);
//			}
			else {
				return;
			}
			
			if (channelCommand != null) {
				channelCommand.execute();
			}
		}
	}
	
	// Static JS callback function
	public static void setChannelError(int code, String description) {
		if (state != INITIAL_STATE) {
			state = INACTIVE_STATE;

			if (code == 401) {
				channelToken = "";
				connectionVerificationTimer.cancel();
			}
			
                        ViewerHtmlUtils.logExternalMessage("channel error", String.valueOf(code) + " / " + description);
			connectChannel(REASON_RECONNECT, RECONNECT_DELAY + (int)(Math.random() * RECONNECT_DELAY));	
                        ViewerDataController.resetPolling();
				
			if (channelCommand != null) {
				channelCommand.execute();
			}
		}
	}
	
	private static void connectChannel(String reason, int delay) {
		if (ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed() 
				&& state == INACTIVE_STATE) {
			// Token should never be null or empty
			if (!RiseUtils.strIsNullOrEmpty(channelToken)) {
				createChannel(reason, delay);
			}
			else {
				ChannelTokenProvider.retrieveToken();
			}
		}
	}

//	public static void dataUpdated() {
//		if (state == UPDATE_STATE) {
//			state = ACTIVE_STATE;
//		}
//	}
	
	public static void connectionCancelled() {
		channelDisconnected();
		
		connectionVerificationTimer.cancel();
		state = INITIAL_STATE;
	}
	
//	protected static int getState() {
//		return state;
//	}
	
	public static boolean isInactive() {
		return state == INACTIVE_STATE;
	}
	
	@Deprecated
	public static boolean updateRequired() {
//		if (state == UPDATE_STATE) {
//			state = ACTIVE_STATE;
//			
//			return true;
//		}
//		
		return false;
	}
	
	public static String getUpdateTicket() {
		String ticket = updateTicket;
		updateTicket = null;
		
		return ticket;
	}
}
