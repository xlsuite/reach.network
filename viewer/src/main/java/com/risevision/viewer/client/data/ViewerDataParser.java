// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.JsArray;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.info.ScheduleInfo;
import com.risevision.common.client.json.JSOModel;
import com.risevision.common.client.json.PlaylistItemJsonParser;
import com.risevision.common.client.json.TimelineJsonParser;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.core.api.attributes.DisplayAttribute;
import com.risevision.core.api.attributes.PresentationAttribute;
import com.risevision.core.api.attributes.ScheduleAttribute;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.info.ViewerDataInfo;
import com.risevision.viewer.client.info.ViewerPlayerInfo;
import com.risevision.viewer.client.player.RisePlayerController;
import com.risevision.viewer.client.widgets.AdsenseBannerWidget;
import com.risevision.viewer.client.widgets.TutorialWidget;

public class ViewerDataParser {
	private static ViewerDataParser instance;
	
	private JSOModel object;
	
	private String displayAddress;
	private String authKey;
	
	private HashMap<String, String> socialConnections;
	
	private String tutorialUrl;
	
	private int pollInterval = 30;
	private int pingInterval = 30;
	
	private int blockRemaining = 0;
	
	public static ViewerDataParser getInstance() {
		try {
			if (instance == null)
				instance = new ViewerDataParser();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public JSOModel getEmbedData(String id) {
		try {		
			// make a copy of the object
			String s = object.getAsString();
			JSOModel newObject = JSOModel.fromJson(s);
			
			JSOModel content = newObject.getObject("content");
			
			if (content == null) {
				content = newObject;
			}
			
			if (content != null) {
				content.set("schedule", (JSOModel) null);
	
				JsArray<JSOModel> jsPresentations = content.getArray("presentations");
				
				if (jsPresentations != null) {
					for (int i = 0; i < jsPresentations.length(); i++) {
						if (jsPresentations.get(i).get(PresentationAttribute.ID, "").equals(id)) {
							jsPresentations.set(i, null);
						}
					}
				}			
			}
			
			return newObject;
		}
		catch (Exception e) {
			return object;
		}
	}
	
	public ViewerDataInfo populateDataProvider(JSOModel newObject) {
		if (newObject == null) {
			return null;
		}
		object = newObject;
		
		ViewerDataInfo viewerData = new ViewerDataInfo();
		try {		
			JSOModel status = object.getObject("status");
			if (status != null) {
				viewerData.setStatusCode(status.getInt("code", 0));
				viewerData.setStatusMessage(status.get("message"));
			}
			
			JSOModel connection = object.getObject("connection");
			if (connection != null) {
				pollInterval = connection.getInt("pollInterval", 5);
				pingInterval = connection.getInt("pingInterval", 5);
				blockRemaining = connection.getInt("blockRemaining", 0);
			}
			
			ViewerPlayerInfo playerInfo = new ViewerPlayerInfo();
			
			JSOModel player = object.getObject("player");
			if (player != null) {
				playerInfo.setRestartRequired(player.get("restartRequired", ""));
				playerInfo.setRebootRequired(player.get("rebootRequired", ""));
				playerInfo.setUpdateRequired(player.get("updateRequired", ""));	
			}
			
			JSOModel display = object.getObject("display");
			if (display != null) {
//				viewerData.setDisplayAddress(display.get("displayAddress"));
				try {
					JSOModel jsDisplayAddress = display.getObject("displayAddress");
					if (jsDisplayAddress != null) {
						displayAddress = jsDisplayAddress.getAsString();
					}
				}
				catch(Exception e) {
					
				}
				
				authKey = display.get("authKey");
				
				playerInfo.setRebootTime(display.get(DisplayAttribute.RESTART_TIME, ""));
				playerInfo.setRebootEnabled(display.get(DisplayAttribute.RESTART_ENABLED, ""));
				playerInfo.setOrientation(RiseUtils.strToInt(display.get(ViewerPlayerInfo.ORIENTATION, ""), 0));
			}

			RisePlayerController.setPlayerInfo(playerInfo);
			
			JSOModel company = object.getObject("company");
			if (company != null) {
//				viewerData.setAuthKey(company.get("authKey"));
				
				AdsenseBannerWidget.getInstance().init(company.get(AdsenseBannerWidget.BANNER_URL), 
						company.get(AdsenseBannerWidget.BANNER_TARGET_URL),
						company.get(AdsenseBannerWidget.ADSENSE_SERVICE_ID),
						company.get(AdsenseBannerWidget.ADSENSE_SERVICE_SLOT));
				
				tutorialUrl = company.get(TutorialWidget.TUTORIAL_URL);
//				ViewerPreviewWidget.getInstance().setTutorialUrl(company.get(TutorialWidget.TUTORIAL_URL));
			}
			
			JsArray<JSOModel> social = object.getArray("social");
			if (social != null) {
				try {
					socialConnections = new HashMap<String, String>();
					
					for (int i = 0; i < social.length(); i++) {
						JSOModel socialConnection = social.get(i);
						
						socialConnections.put(socialConnection.get("network"), socialConnection.getAsString());
					}
				}
				catch (Exception e) {
					
				}
			}
			
			viewerData.setContentDescriptor(object.get("signature", ""));
			
			JSOModel content = object.getObject("content");
			
			if (content == null) {
				content = object;
			}
			
			if (content != null) {
				JSOModel jsSchedule = content.getObject("schedule");
				
				if (jsSchedule != null) {
					ScheduleInfo schedule = parseJsSchedule(jsSchedule);
					viewerData.setSchedule(schedule);
					
//					viewerData.addDataTypeInfo(new ViewerDataTypeInfo(schedule.getId(), jsSchedule.get(CommonAttribute.CHANGE_DATE)));
				}
				
				JsArray<JSOModel> jsPresentations = content.getArray("presentations");
				
				if (jsPresentations != null) {
					ArrayList<PresentationInfo> presentations = new ArrayList<PresentationInfo>();
					for (int i = 0; i < jsPresentations.length(); i++) {
						if (jsPresentations.get(i) != null) {
							PresentationInfo presentation = parseJsPresentation(jsPresentations.get(i));
							presentations.add(presentation);
						}
						
//						viewerData.addDataTypeInfo(new ViewerDataTypeInfo(presentation.getId(), jsPresentations.get(i).get(CommonAttribute.CHANGE_DATE)));
					}
					
					viewerData.setPresentations(presentations);
				}			
	
				JSOModel jsPresentation = content.getObject("presentation");
				
				if (jsPresentation != null) {
					ArrayList<PresentationInfo> presentations = new ArrayList<PresentationInfo>();
					PresentationInfo presentation = parseJsPresentation(jsPresentation);
					presentations.add(presentation);
					
//					viewerData.addDataTypeInfo(new ViewerDataTypeInfo(presentation.getId(), jsPresentation.get(CommonAttribute.CHANGE_DATE)));
					
					viewerData.setPresentations(presentations);
				}
				
				/*
				JsArray<JSOModel> jsPlaylists = content.getArray("playlists");
				
				if (jsPlaylists != null) {
					ArrayList<PlaylistInfo> playlists = new ArrayList<PlaylistInfo>();
					for (int i = 0; i < jsPlaylists.length(); i++) {
						PlaylistInfo playlist = parseJsPlaylist(jsPlaylists.get(i));
						
						if (playlist.getDistributionToAll() || ViewerEntryPoint.checkDistribution(playlist.getDistribution())) {
							playlists.add(playlist);
							
//							viewerData.addDataTypeInfo(new ViewerDataTypeInfo(playlist.getId(), jsPlaylists.get(i).get(CommonAttribute.CHANGE_DATE)));
						}
					}
					
					viewerData.setPlaylists(playlists);
				}
				*/
			}
		}
		catch (Exception e) {
			return null;
		}
		
		return viewerData;
	}

	private static ScheduleInfo parseJsSchedule(JSOModel jsSchedule) {
		ScheduleInfo schedule = new ScheduleInfo();
		
//		"changeDate":"22032010192527631",
		
		schedule.setDistributionToAll(jsSchedule.getBoolean(ScheduleAttribute.DISTRIBUTE_TO_ALL));
		schedule.setDistribution(jsSchedule.getArrayList(ScheduleAttribute.DISTRIBUTION));
		schedule.setName(jsSchedule.get(ScheduleAttribute.NAME));
		schedule.setId(jsSchedule.get(ScheduleAttribute.ID));
		schedule.setCompanyId(jsSchedule.get("companyId"));
//		schedule.setPosition(jsSchedule.get(ScheduleAttribute.POSITION));
//		schedule.setScale(jsSchedule.get(ScheduleAttribute.SCALE));
//		schedule.setTransition(jsSchedule.get(ScheduleAttribute.TRANSITION));

		schedule.setTimeline(TimelineJsonParser.parseTimeline(jsSchedule));
				
		JsArray<JSOModel> jsScheduleItems = jsSchedule.getArray("items");
		
		if (jsScheduleItems != null) {
			ArrayList<PlaylistItemInfo> scheduleItems = new ArrayList<PlaylistItemInfo>();
			for (int i = 0; i < jsScheduleItems.length(); i++) {
				PlaylistItemInfo playlistItem = new PlaylistItemInfo();
				PlaylistItemJsonParser.parseJsPlaylistItem(playlistItem, jsScheduleItems.get(i));
				scheduleItems.add(playlistItem);
			}
			
			schedule.setPlayListItems(scheduleItems);
		}
		
		return schedule;
	}
	
	private static PresentationInfo parseJsPresentation(JSOModel jsPresentation) {
		PresentationInfo presentation = new PresentationInfo();
		
//		"changeDate":"22032010192527631",
		
		presentation.setId(jsPresentation.get(PresentationAttribute.ID));
		presentation.setCompanyId(jsPresentation.get("companyId"));
		presentation.setName(jsPresentation.get(PresentationAttribute.NAME));
		presentation.setLayout(jsPresentation.get(PresentationAttribute.LAYOUT));
		presentation.setTemplate(jsPresentation.getBoolean(PresentationAttribute.TEMPLATE));
//		presentation.setPublishType(jsPresentation.getInt(PresentationAttribute.PUBLISH));
		
		try {
			JSOModel jsDistribution = jsPresentation.getObject("distribution");
			if (jsDistribution != null) {
				presentation.setDistributionString(jsDistribution.getAsString());
			}
		}
		catch(Exception e) {
			
		}
			
//		JsArray<JSOModel> jsPlaceholders = jsPresentation.getArray("placeholders");
//		
//		if (jsPlaceholders != null) {
//			ArrayList<PlaceholderInfo> placeholders = new ArrayList<PlaceholderInfo>();
//			for (int i = 0; i < jsPlaceholders.length(); i++) {
//				placeholders.add(parseJsPlaceholder(jsPlaceholders.get(i)));
//			}
//			
//			presentation.setPlaceholders(placeholders);
//		}
		
		return presentation;
	}
	
	/*
	private static PlaceholderInfo parseJsPlaceholder(JSOModel jsItem) {
		PlaceholderInfo placeholder = new PlaceholderInfo();
		
//		"changeDate":"22032010192830055",
		
		placeholder.setId(jsItem.get(PlaceholderAttribute.ID));
		
		if (placeholder.getId() == null || placeholder.getId().isEmpty() || placeholder.getId().equals("undefined"))
			placeholder.setId("ph" + jsItem.get("index"));
		
		placeholder.setType(jsItem.get(PlaceholderAttribute.TYPE));
//		placeholder.setUrl(jsItem.get(PlaceholderAttribute.OBJECT_DATA));
		placeholder.setObjectRef(jsItem.get(PlaceholderAttribute.OBJECT_REFERENCE));
		
		return placeholder;
	}
	*/
	
	/*
	private static PlaylistInfo parseJsPlaylist(JSOModel jsPlaylist) {
		PlaylistInfo playlist = new PlaylistInfo();
		
//		"changedDate": "26022010200104617",
		
		playlist.setDistributionToAll(jsPlaylist.getBoolean(ScheduleAttribute.DISTRIBUTE_TO_ALL));
		playlist.setDistribution(jsPlaylist.getArrayList(ScheduleAttribute.DISTRIBUTION));
		playlist.setId(jsPlaylist.get(ScheduleAttribute.ID));
		playlist.setPosition(jsPlaylist.get(ScheduleAttribute.POSITION));
		playlist.setScale(jsPlaylist.get(ScheduleAttribute.SCALE));
		playlist.setTimeline(parseTimeLine(jsPlaylist));
		playlist.setTransition(jsPlaylist.get(ScheduleAttribute.TRANSITION));
		
		JsArray<JSOModel> jsPlaylistItems = jsPlaylist.getArray("items");
		
		if (jsPlaylistItems != null) {
			ArrayList<PlaylistItemInfo> playlistItems = new ArrayList<PlaylistItemInfo>();
			for (int i = 0; i < jsPlaylistItems.length(); i++) {
				PlaylistItemInfo item = parseJsPlaylistItem(jsPlaylistItems.get(i));
				
				if (item.getDistributionToAll() || ViewerEntryPoint.checkDistribution(item.getDistribution())) {
					playlistItems.add(item);
				}
			}
			
			playlist.setPlaylistItems(playlistItems);
		}
		
		return playlist;
	}

	private static PlaylistItemInfo parseJsPlaylistItem(JSOModel jsItem) {
		PlaylistItemInfo playlistItem = new PlaylistItemInfo();
		
//		"changeDate":"22032010192830055",
		
		playlistItem.setDistributionToAll(jsItem.getBoolean(PlaylistItemAttribute.DISTRIBUTE_TO_ALL));
		playlistItem.setDistribution(jsItem.getArrayList(PlaylistItemAttribute.DISTRIBUTION));
		playlistItem.setId(jsItem.get(PlaylistItemAttribute.INDEX));
//		playlistItem.setPosition(jsItem.get(PlaylistItemAttribute.POSITION));
//		playlistItem.setScale(jsItem.get(PlaylistItemAttribute.SCALE));
		playlistItem.setTimeline(parseTimeLine(jsItem));
//		playlistItem.setTransition(jsItem.get(PlaylistItemAttribute.TRANSITION));
		
		playlistItem.setDuration(jsItem.get(PlaylistItemAttribute.DURATION));
		playlistItem.setPlayUntilDone(jsItem.getBoolean(PlaylistItemAttribute.PLAY_UNTIL_DONE));
		playlistItem.setType(jsItem.get(PlaylistItemAttribute.TYPE));
		playlistItem.setObjectData(jsItem.get(PlaylistItemAttribute.URL));
		playlistItem.setObjectRef(jsItem.get(PlaylistItemAttribute.OBJECT_REFERENCE));
		
		return playlistItem;
	}
	
	private static TimeLineInfo parseTimeLine(JSOModel jsItem) {
		TimeLineInfo t = new TimeLineInfo();
		try {
			t.setUseSchedule(jsItem.getBoolean(PlaylistAttribute.TIME_DEFINED, false));

			t.setStartDate(RiseUtils.stringToDate(jsItem.get(PlaylistAttribute.START_DATE)));
			t.setEndDate(RiseUtils.stringToDate(jsItem.get(PlaylistAttribute.END_DATE)));
			t.setNoEndDate(t.getEndDate() == null);

			t.setStartTime(RiseUtils.stringToDate(jsItem.get(PlaylistAttribute.START_TIME)));
			t.setEndTime(RiseUtils.stringToDate(jsItem.get(PlaylistAttribute.END_TIME)));
			t.setAllDay(t.getStartTime() == null);

			t.setRecurrenceType(parseRecurrenceType(jsItem));
			t.setRecurrenceFrequency(jsItem.getInt(PlaylistAttribute.RECURRENCE_FREQUENCY, 1));
			t.setRecurrenceIsAbsolute(jsItem.getBoolean(PlaylistAttribute.RECURRENCE_ABSOLUTE, true));
        	t.setRecurrenceDayOfWeek(jsItem.getInt(PlaylistAttribute.RECURRENCE_DAY_OF_WEEK, 0));
			t.setRecurrenceDayOfMonth(jsItem.getInt(PlaylistAttribute.RECURRENCE_DAY_OF_MONTH, 1));
			t.setRecurrenceWeekOfMonth(jsItem.getInt(PlaylistAttribute.RECURRENCE_WEEK_OF_MONTH, 0));
			t.setRecurrenceMonthOfYear(jsItem.getInt(PlaylistAttribute.RECURRENCE_MONTH_OF_YEAR, 0));

			if (TimeLineInfo.RecurrenceType.Weekly.equals(t.getRecurrenceType())) {
				String wd = jsItem.get(PlaylistAttribute.RECURRENCE_DAYS_OF_WEEK);
				wd = (wd == null) ? "" : wd;
				t.setRecurrenceSunday(wd.contains(DayOfWeek.SUNDAY));
				t.setRecurrenceMonday(wd.contains(DayOfWeek.MONDAY));
				t.setRecurrenceTuesday(wd.contains(DayOfWeek.TUESDAY));
				t.setRecurrenceWednesday(wd.contains(DayOfWeek.WEDNESDAY));
				t.setRecurrenceThursday(wd.contains(DayOfWeek.THURSDAY));
				t.setRecurrenceFriday(wd.contains(DayOfWeek.FRIDAY));
				t.setRecurrenceSaturday(wd.contains(DayOfWeek.SATURDAY));
			}

		} catch (Exception e) {
		}
		return t;
	}
	
	private static TimeLineInfo.RecurrenceType parseRecurrenceType(JSOModel jsItem) {
		TimeLineInfo.RecurrenceType res = TimeLineInfo.RecurrenceType.Daily;
		String s = jsItem.get(PlaylistAttribute.RECURRENCE_TYPE);
		if (RecurrenceType.WEEKLY.equals(s))
			res = TimeLineInfo.RecurrenceType.Weekly;
		else if (RecurrenceType.MONTHLY.equals(s))
			res = TimeLineInfo.RecurrenceType.Monthly;
		else if (RecurrenceType.YEARLY.equals(s))
			res = TimeLineInfo.RecurrenceType.Yearly;
		return res;
	}
	*/

	public String getTutorialUrl() {
		return tutorialUrl;
	}

	
	public String getDisplayAddress() {
		if (ViewerEntryPoint.getDisplayAddress() != null) {
			return ViewerEntryPoint.getDisplayAddress();
		}
		else {
			if (displayAddress != null) {
				return displayAddress;
			}
		}
		return "";
	}
	
	public String getAuthKey() {
		if (ViewerEntryPoint.getAuthKey() != null) {
			return ViewerEntryPoint.getAuthKey();
		}
		else {
			if (authKey != null) {
				return authKey;
			}
		}
		return "";
	}
	
//	public HashMap<String, String> getSocialConnections() {
//		return socialConnections;
//	}
	
	public String getSocialConnection(String network) {
		if (socialConnections != null) {
			return socialConnections.get(network);
		}
		return "";
	}
	
	public int getPollInterval() {
		return pollInterval;
	}

	public int getPingInterval() {
		return pingInterval;
	}
	
	public int getBlockRemaining() {
		return blockRemaining + 2;
	}
	
}
