// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window.Location;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.cache.RiseCacheController;
import com.risevision.viewer.client.controller.ViewerScheduleController;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.player.RisePlayerController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.widgets.ViewerPreviewWidget;
import com.risevision.viewer.client.widgets.oem.ReachNetworkDisplayWidget;

import java.util.ArrayList;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ViewerEntryPoint implements EntryPoint {
	public static final String DISPLAY = "display";
	public static final String SCHEDULE = "schedule";
	public static final String PRESENTATION = "presentation";
//	public static final String EMBED = "embed";
//	public static final String PREVIEW = "preview";
	
	private static final String DEMO_ID = "BQHM8GRCU7KQ";
	
	private static final String TYPE_PARAM = "type=";
	private static final String ID_PARAM = "id=";
	private static final String CLAIM_ID_PARAM = "claimId=";
	private static final String PARENT_ID_PARAM = "parentId=";
	private static final String DISPLAY_ID_PARAM = "displayId=";
	private static final String AUTH_KEY_PARAM = "CompanyAuthKey=";
	private static final String DISPLAY_ADDRESS_PARAM = "DisplayAddress=";
	private static final String SHOW_UI_PARAM = "showUi=";
	private static final String PLAYER_PARAM = "player=";
	private static final String SYS_INFO_PARAM = "sysInfo=";
	public static final String CACHE_IS_ACTIVE_PARAM = "cacheIsActive=";

	private static String queryString;
	private static String type;
	private static String id;
	private static String claimId;
	private static String parentId;
	private static boolean showUi = true;
	
	private static boolean isEmbed = false;
//	private static boolean isPlayer = false;
	private static String sysInfo;
	
	private static int width, height;

	private static ViewerScheduleController scheduleController, newScheduleController;
	private static Command dataReadyCommand;
	
	private static boolean isShowingBlack = false, isShowingProgressBar = true;

	@Override
	public void onModuleLoad() {
		// loads static methods to be called through JSNI
		ViewerHtmlUtils.exportStaticMethods();

		updateParameters();
		
//		showBlackScreen(true);

		//Showing dialog on Ctrl+Q
		Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
			public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
				NativeEvent ne = event.getNativeEvent();
				if (ne.getCtrlKey() && ne.getKeyCode() == KeyCodes.KEY_Q) {
					ReachNetworkDisplayWidget.getInstance().show();
				}
			}
		});

		ViewerHtmlUtils.logExternalMessage("startup", null);
		
		//check if no Display ID - commented due we want to show display ID
//		if (RisePlayerController.getIsActive() && isDisplay() && !isEmbed() && DEMO_ID.equals(id)) {
//			if (claimId != null && !claimId.isEmpty()) {
//				EnterClaimIdWidget.getInstance(false).show(false);
//			} else {
//				DisplayRegisterWidget.getInstance().show(NotificationType.display_id_null);
//			}
//		} else {
//			loadPresentation();
//		}

		loadPresentation();
	}

	public static void loadPresentation(){
		initCommands();
		
		if (isDisplay()) {
			RiseCacheController.pingCache();
		}
		
		if (id != null && isValidType(type)){
			ViewerDataController.init(dataReadyCommand, type, id);
		}
	}

	private void updateParameters(){
		//example: queryString = "?type=display&id=1d3b23d3-31f6-4ad8-b8b1-c180231b9919";
		//example: queryString = "?type=display&id=1d3b23d3-31f6-4ad8-b8b1-c180231b9919&CompanyAuthKey=";
		//example: queryString = "?type=preview"; **** DEPRECATED
		//example: queryString = "?type=presentation&id=1d3b23d3-31f6-4ad8-b8b1-c180231b9919&showui=false";
		queryString = Location.getQueryString();
		
		type = RiseUtils.getFromQueryString(queryString, TYPE_PARAM);
		if (type == null) {
			type = DISPLAY;
		}
		
		id = RiseUtils.getFromQueryString(queryString, ID_PARAM);
		if (id == null || id.isEmpty()) {
			id = DEMO_ID;
		}
		
		parentId = RiseUtils.getFromQueryString(queryString, PARENT_ID_PARAM);
		if (parentId != null) {
			isEmbed = true;
		}

		claimId = RiseUtils.getFromQueryString(queryString, CLAIM_ID_PARAM);

		showUi = RiseUtils.getFromQueryString(queryString, SHOW_UI_PARAM) == null || !"false".equals(RiseUtils.getFromQueryString(queryString, SHOW_UI_PARAM).toLowerCase());
		
//		isPlayer = RiseUtils.getFromQueryString(queryString, playerParam) != null && "true".equals(RiseUtils.getFromQueryString(queryString, playerParam)) ;
		RisePlayerController.setIsActive(RiseUtils.getFromQueryString(queryString, PLAYER_PARAM));
		
		sysInfo = RiseUtils.getFromQueryString(queryString, SYS_INFO_PARAM);
		
		RiseCacheController.setActive(Boolean.parseBoolean(RiseUtils.getFromQueryString(queryString, CACHE_IS_ACTIVE_PARAM)));
	}
	
	private static void initCommands() {
		
		final Command scheduleReadyCommand = new Command() {
			@Override
			public void execute() {
				initSchedule();
			}
		};

		dataReadyCommand = new Command() {
			@Override
			public void execute() {
				if (newScheduleController != null) {
					newScheduleController.unload();
				}
				
				newScheduleController = new ViewerScheduleController(scheduleReadyCommand);
				newScheduleController.init();
				
//				if (type.equals(PREVIEW)) {
//					id = ViewerDataProvider.getItemId();
//				}
				
				// init Analytics for Displays and Preview
				ViewerHtmlUtils.initAnalytics();

				if (isPreview()) {
					if (showUi) {
						ViewerPreviewWidget.getInstance().show();
					}

//					ViewerHtmlUtils.initAnalytics();
					ViewerHtmlUtils.trackAnalyticsEvent("Preview", "Load", showUi ? "Showing" : "Not Showing");

					if (ViewerDataController.isTemplate()) {
						ViewerHtmlUtils.trackAnalyticsEvent("Preview", "Template", ViewerDataController.getItemName());
					}
				}
			}
		};
	}
	
	private static void initSchedule()	 {
		ViewerScheduleController oldScheduleController = scheduleController;

		scheduleController = newScheduleController;
		
		// Reset the newScheduleController parameter
		newScheduleController = null;
		
		if (isEmbed) {
			reportReady();
		}
		else {
			playSchedule();
		}
		
		if (oldScheduleController != null) {
			oldScheduleController.unload();
		}
	}
	
	private static void playSchedule() {
		if (isShowingProgressBar) {
			ViewerHtmlUtils.showElement("progress", false);
//			ViewerHtmlUtils.showElement("main", true);
			
			isShowingProgressBar = false;
		}
		
		scheduleController.play();					
	}

	public static String getType() {
		return type;
	}
	
	private static boolean isValidType(String type) {
		if (type != null)
			if (type.equals(DISPLAY) || type.equals(PRESENTATION) || type.equals(SCHEDULE)
//					|| type.equals(EMBED) 
//					|| type.equals(PREVIEW)
					)
				return true;
		return false;
	}
	
	public static boolean isDisplay() {
		return DISPLAY.equals(type);
	}
	
	public static boolean isPresentation() {
		return PRESENTATION.equals(type);
	}
	
	public static boolean isPreview() {
		return (PRESENTATION.equals(type) || SCHEDULE.equals(type)) && !isEmbed;
	}
	
//	public static boolean isPreview() {
//		if (type != null && showUi && (type.equals(PRESENTATION) || type.equals(SCHEDULE) 
////					|| type.equals(PREVIEW)
//					)) {
//			return true;
//		}
//		return false;
//	}
	
	public static boolean isEmbed() {
		return isEmbed;
	}
	
	public static String getDisplayId() {
		if (isDisplay()) {
			if (isEmbed) {
				return RiseUtils.getFromQueryString(queryString, DISPLAY_ID_PARAM);
			}
			else {
				return id;
			}
		}
		else return "";
	}
	
	public static String getId() {
		return id;
	}
	
	public static String getParentId() {
		return parentId;
//		return getFromQueryString(parentIdParam);
	}

	public static String getClaimId() {
		return claimId;
	}

	public static String getSysInfo() {
		return sysInfo == null ? "" : sysInfo;
	}
	
	public static boolean getShowUi(){
		return showUi;
	}
	
	public static boolean checkDistribution(ArrayList<String> distribution) {
		if (!isDisplay()) {
			return true;
		}
		
		String displayId = getDisplayId();
		for (String display: distribution) {
			if (display.equals(displayId)) {
				return true;
			}
		}
		return false;
	}
	
	public static void showBlackScreen(boolean show) {
		if (isDisplay() && isShowingBlack != show) {
			isShowingBlack = show;
			ViewerHtmlUtils.setBackground("mainDiv", show ? "black" : "transparent");
			
			RisePlayerController.setDisplayCommand(show);
		}
	}
	
	public static boolean isShowingProgressBar() {
		return isShowingProgressBar;
	}
	
	// CompanyAuthKey used for Data Mining gadgets
	public static String getAuthKey() {
		return RiseUtils.getFromQueryString(queryString, AUTH_KEY_PARAM);
	}
	
	public static String getDisplayAddress() {
		return RiseUtils.getFromQueryString(queryString, DISPLAY_ADDRESS_PARAM);
	}
	
	private static void reportReady() {
		ViewerHtmlUtils.embedReady();
	}
	
	public static void reportDone() {
		ViewerHtmlUtils.embedDone();
	}
	
	public static void embedPlay() {
		playSchedule();
	}
	
	public static void embedPause() {
		scheduleController.pause();
	}
	
	public static void embedStop() {
		embedPause();
	}
	
	public static void setPresentationSize(int newWidth, int newHeight) {
		width = newWidth;
		height = newHeight;
	}
	
	public static int getPresentationWidth() {
		return width;
	}
	
	public static int getPresentationHeight() {
		return height;
	}
}
