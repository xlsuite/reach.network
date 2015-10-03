// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.risevision.common.client.info.PlaylistInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.info.ScheduleInfo;
import com.risevision.common.client.info.TimeLineInfo;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.info.ViewerDataInfo;

public abstract class ViewerDataControllerBase {
	protected static ViewerDataParser dataParser = ViewerDataParser.getInstance();
	
	protected static Command dataReadyCommand;

	private static ViewerDataInfo viewerData;
	private static String itemName, itemId, itemCompanyId;
	private static boolean template;
	
	protected static void updateViewerData(ViewerDataInfo newViewerData) {
		boolean newData = false;
		
		if (newViewerData != null && (!ViewerEntryPoint.isDisplay() || newViewerData.getContentDescriptor() != null 
				&& !newViewerData.getContentDescriptor().isEmpty()) /* && newViewerData.getStatusCode() != -1 */) {
			if (viewerData == null) {
				viewerData = newViewerData;
				newData = true;
			}
			else {
				if (!newViewerData.getContentDescriptor().equals(viewerData.getContentDescriptor())) {
					viewerData = newViewerData;
					newData = true;
				}
			}
		}
		
		if (newData) {
			dataReadyCommand.execute();
		}
	}
	
	public static JavaScriptObject getEmbedData(String id, String parentId) {
		if (id != null && viewerData != null && viewerData.getPresentations() != null) {
			for (PresentationInfo presentation: viewerData.getPresentations()) {
				if (id.equals(presentation.getId())) {
					return dataParser.getEmbedData(parentId);
				}
			}
		}
		
		return null;
	}
	
	public static ScheduleInfo getSchedule() {
		if (viewerData != null) {
			if (viewerData.getSchedule() != null) {
				itemName = viewerData.getSchedule().getName();
				itemCompanyId = viewerData.getSchedule().getCompanyId();
				return viewerData.getSchedule();
			}
			else
				return getDummySchedule();
		}

		return null;
	}
	
	private static ScheduleInfo getDummySchedule() {
		ScheduleInfo schedule = new ScheduleInfo();
		
		schedule.setId(ViewerEntryPoint.SCHEDULE);
//		schedule.setPosition("Centered");
//		schedule.setTransition("none");
//		schedule.setScale("fit");
		schedule.setTimeline(new TimeLineInfo());
		
		ArrayList<PlaylistItemInfo> itemList = new ArrayList<PlaylistItemInfo>();

		if (viewerData.getPresentations() != null && viewerData.getPresentations().size() > 0) {
			int index = 0;
			for(PresentationInfo presentation: viewerData.getPresentations()) {
				if (presentation != null && presentation.getId().equals(ViewerEntryPoint.getId())) {
					index = viewerData.getPresentations().indexOf(presentation);
					break;
				}
			}
			
			PlaylistItemInfo item = new PlaylistItemInfo();
			item.setObjectRef(viewerData.getPresentations().get(index).getId());
			item.setType(PlaylistItemInfo.TYPE_PRESENTATION);
			item.setTimeline(new TimeLineInfo());
			itemList.add(item);
			
			itemName = viewerData.getPresentations().get(index).getName();
			itemId = viewerData.getPresentations().get(index).getId();
			itemCompanyId = viewerData.getPresentations().get(index).getCompanyId();
			template = viewerData.getPresentations().get(index).isTemplate();
		}
		else {
			return null;
		}
		
		schedule.setPlayListItems(itemList);		
		
		return schedule;
	}
	
	public static PresentationInfo getPresentation(String id) {
		if (viewerData != null && viewerData.getPresentations() != null) {
			for (PresentationInfo presentation: viewerData.getPresentations()) {
				if (id.equals(presentation.getId())) {
					return presentation;
				}
			}
		}
		return null;
	}
	
	public static PlaylistInfo getPlaylist(String id) {
		if (viewerData != null && viewerData.getPlaylists() != null) {
			for (PlaylistInfo playlist: viewerData.getPlaylists()) {
				if (id.equals(playlist.getId()) && playlist.getPlaylistItems().size() != 0) {
					return playlist;
				}
			}
		}
		return null;
	}
	
	public static String getItemName() {
		return itemName;
	}
	
	public static String getItemId() {
		return itemId;
	}
	
	public static String getItemCompanyId() {
		return itemCompanyId;
	}
	
	public static boolean isTemplate() {
		return template;
	}

	public static String getSig() {
		return viewerData != null ? viewerData.getContentDescriptor() : "";
	}
	
	public static boolean hasData() {
		return viewerData != null;
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
