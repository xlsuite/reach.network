// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.risevision.common.client.json.JSOModel;
import com.risevision.core.api.types.ViewerStatus;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.channel.ChannelConnectionController;
import com.risevision.viewer.client.info.NotificationType;
import com.risevision.viewer.client.info.ViewerDataInfo;
import com.risevision.viewer.client.player.RisePlayerController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.widgets.ViewerNotificationsPanel;
import com.risevision.viewer.client.widgets.oem.DisplayRegisterWidget;

public class ViewerDataController extends ViewerDataControllerBase {

	private static final int INITIAL_STATE = 0;
//	private static final int CONTENT_STATE = 1;
//	private static final int POLLING_STATE = 2;
	private static final int BLOCKED_STATE = 3;
	private static final int CLOSED_STATE = 4;
	
	private static int state = INITIAL_STATE;
	
	private static Timer pollingTimer;
	private static boolean pollingTimerActive = false;
	
	public static int MINUTE_UPDATE_INTERVAL = 60 * 1000;

	public static Command channelCommand;

	public static void init(Command newDataReadyCommand, String type, String id) {
		dataReadyCommand = newDataReadyCommand;
		initObjects();

		if (ViewerEntryPoint.isEmbed()) {
			reportDataReady(getEmbedDataNative(ViewerEntryPoint.getId(), ViewerEntryPoint.getParentId()));
//		}
//		else if (type.equals(ViewerEntryPoint.PREVIEW)) {
//			getPreviewDataNative();
//		}
		} else {
			// [AD] moved this function to after the data is retrieved - workaround for Core bug 
			// where the Viewer API and Channel Token calls can't be made at the same time 
//			channelController.init(channelCommand);

			if (ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed()) {
				ViewerInstanceController.init();
			}

			ViewerDataProvider.retrieveData(ViewerDataProvider.Reason.VIEWER_INIT.toString());
		}
	}
	
	private static void initObjects() {
		pollingTimer = new Timer() {
			@Override
			public void run() {
				// if the timer is running, it means the channel is disconnected.
//				channelController.connectChannel(ChannelConnectionController.REASON_RECONNECT);
				
				// reset BLOCKED_STATE
				if (isBlocked()) {
					state = INITIAL_STATE;
				}
				
				pollingTimerActive = false;
				
				ViewerDataProvider.retrieveData(ViewerDataProvider.Reason.POLLING_TIMER.toString());
			}
		};
		
		channelCommand = new Command() {
			@Override
			public void execute() {
				onChannelCommand();
			}
		};
	}
	
	public static void setDuplicateInstance() {
		state = CLOSED_STATE;

		ChannelConnectionController.connectionCancelled();
		
		// Do not show notification
		// TODO: Relay message to server
//		ViewerNotificationsWidget.getInstance().show(NotificationType.multiple_display_instances, true);
	}
	
	private static void onChannelCommand() {
		// if channel connected, stop the update timer
//		if (ChannelConnectionController.updateRequired()) {
//			ViewerDataProvider.retrieveData();
//		}
//		else 
		if (hasData() && !ChannelConnectionController.isInactive()) {
			stopPolling();
		}
		else if (!isBlocked()) {
			startPolling(0);
//			retrieveData();
		}

	}
	
	public static void reportDataReady(JavaScriptObject jso) {
		reportDataReady(jso, false);
	}
	
	public static void reportDataReady(JavaScriptObject jso, boolean cached) {
		JSOModel jsoModel = (JSOModel) jso;

		ViewerDataInfo newViewerData = dataParser.populateDataProvider(jsoModel);
		
		if (newViewerData != null) {
			if (cached) {
				newViewerData.setContentDescriptor("cached");
			}
			setNewViewerData(newViewerData);
		}
	}
	
	private static void setNewViewerData(ViewerDataInfo newViewerData) {
		if (state == CLOSED_STATE) {
			return;
		}
		
//		boolean pollForUpdate = false;
		stopPolling();
		
		switch (newViewerData.getStatusCode()) {
//		case ViewerStatus.NO_CHANGES:

		case ViewerStatus.OK:
//			state = CONTENT_STATE;

			if (!ViewerHtmlUtils.isChrome() && ViewerEntryPoint.isPreview() && ViewerEntryPoint.getShowUi()) {
				ViewerNotificationsPanel.getInstance().show(NotificationType.use_google_chrome);
			}
			else {
				ViewerNotificationsPanel.getInstance().hide();
			}
			
			break;
		case ViewerStatus.BLOCKED:
			state = BLOCKED_STATE;

			startPolling(ViewerDataParser.getInstance().getBlockRemaining());
			if (!hasData()) {
				ViewerNotificationsPanel.getInstance().show(NotificationType.blocked_display);
			}
			
			break;
		case ViewerStatus.NO_COOKIE:
			showNotification(NotificationType.cookies_disabled.getMessage());
			break;
			
		case ViewerStatus.CONTENT_NOT_FOUND:
			showNotification(newViewerData.getStatusMessage());
			
			break;
		case ViewerStatus.ID_SHARING_VIOLATION:
			//do nothing due we are allowing display sharing
		    //showDisplayRegistration(NotificationType.display_id_duplicate);

			break;
		case ViewerStatus.DISPLAY_NOT_FOUND:
			showDisplayRegistration(NotificationType.display_id_not_found);
			
			break;
		default:
			break;
		}
		
		updateViewerData(newViewerData);
		
//		if (!pollForUpdate) {
//			ChannelConnectionController.dataUpdated();
//		}
		
		if (ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed() 
				&& ChannelConnectionController.isInactive()) {
			startPolling(0);
		}
	}

	private static void showDisplayRegistration(NotificationType notificationType) {
		setClosedState();
		
		if (RisePlayerController.getIsActive() && ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed()) {
			DisplayRegisterWidget.getInstance().show(notificationType);
		}
		else {
			ViewerNotificationsPanel.getInstance().show(notificationType.getMessage());
		}		
	}
	
	private static void showNotification(String notification) {
		setClosedState();
		
		if (!hasData()) {
			ViewerNotificationsPanel.getInstance().show(notification);
		}
	}

	private static void setClosedState() {
		state = CLOSED_STATE;

		ChannelConnectionController.connectionCancelled();
		
	}
	
	private static void startPolling(int interval) {
		if (!pollingTimerActive) {
			pollingTimerActive = true;
			
			if (interval == 0) {
				interval = ViewerDataParser.getInstance().getPollInterval();
			}
			else {
				interval += 2;
			}
			
			pollingTimer.schedule(interval * MINUTE_UPDATE_INTERVAL);
		}
	}

        public static void resetPolling() {
          int interval = ViewerDataParser.getInstance().getPollInterval();
          pollingTimer.schedule(interval * MINUTE_UPDATE_INTERVAL);
        }
	
	private static void stopPolling() {
		pollingTimerActive = false;
		
		pollingTimer.cancel();
	}
	
	public static boolean isBlocked() {
		return state == BLOCKED_STATE;
	}

	public static native JavaScriptObject getLocalStorageData() /*-{
		return $wnd.retreiveViewerResponse();
	}-*/;
	
	private static native JavaScriptObject getEmbedDataNative(String id, String parentId) /*-{
//		return $wnd.getParentData(id, parentId);
	
	    try {
	    	if ($wnd.parent) {
	    		return $wnd.parent.getEmbedData(id, parentId);
	    	}
	    }
	    catch (err) {
	    	$wnd.writeToLog("getParentData - " + id + " - " + err.message);
	    }

	}-*/;

}
