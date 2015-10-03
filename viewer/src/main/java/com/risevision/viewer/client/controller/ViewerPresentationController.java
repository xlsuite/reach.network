// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.json.DistributionJsonParser;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerPresentationController {
	private static ArrayList<ViewerPresentationController> instances = new ArrayList<ViewerPresentationController>();
	
	public static final int UNLOADED_STATUS = 1;
	public static final int LOADING_STATUS = 2;
	public static final int ADDED_STATUS = 3;
	public static final int READY_STATUS = 4;
	public static final int ALL_READY_STATUS = 5;
	
	private static final String PRESENTATION_HEADER_SCRIPT = "" +
			"<link type='text/css' rel='stylesheet' href='style/viewer.css'>" +
		    "<script type='text/javascript' src='scripts/jquery-1.7.1.min.js'></script>" +
		    "<script type='text/javascript' src='scripts/jquery.timers-1.2.js'></script>" +
		    
//		    "<script type='text/javascript' src='http://www-open-opensocial.googleusercontent.com/gadgets/js/rpc.js'></script>" +
//			"<script type='text/javascript' src='gadgets/globals.js'></script>" +
//			"<script type='text/javascript' src='gadgets/base.js'></script>" +
//			"<script type='text/javascript' src='gadgets/string.js'></script>" +
//			"<script type='text/javascript' src='gadgets/urlparams.js'></script>" +
//			"<script type='text/javascript' src='gadgets/config.js'></script>" +
//			"<script type='text/javascript' src='gadgets/auth.js'></script>" +
//			"<script type='text/javascript' src='gadgets/auth-init.js'></script>" +
//			"<script type='text/javascript' src='gadgets/json-native.js'></script>" +
//			"<script type='text/javascript' src='gadgets/io.js'></script>" +
//			"<script type='text/javascript' src='gadgets/wpm.transport.js'></script>" +
//			"<script type='text/javascript' src='gadgets/rpc.js'></script>" +
			"<script type='text/javascript' src='gadgets/gadgets.min.js'></script>" +
			
		    "<script type='text/javascript' src='scripts/presentationScripts.js'></script>" +
		    
		    "<script type='text/javascript' src='slicebox/js/jquery.slicebox.js'></script>" +
		    "<script type='text/javascript' src='slicebox/js/jquery.easing.1.3.js'></script>" +
		    
			"<script>" +
			"var presFrame = '%s';" +
		    "</script>";
	
	private int status;
	private boolean isPlaying = false;
	private String containerName, htmlName, iframeName;
	private PresentationInfo presentation;
	private ArrayList<PlaylistItemInfo> items = new ArrayList<PlaylistItemInfo>();
	private ArrayList<ViewerPlaceholderController> placeholders = new ArrayList<ViewerPlaceholderController>();
//	private ArrayList<Command> presentationReadyCommands = new ArrayList<Command>();
	private Command presentationReadyCommand, presentationDoneCommand;

	private Timer readyTimer;
		
	public static ViewerPresentationController getInstance(PlaylistItemInfo item, 
			String htmlName, 
			String containerName, 
			Command presentationReadyCommand,
			Command presentationDoneCommand) {
		for (ViewerPresentationController instance: instances) {
			if ((instance.items.get(0).getType().equals(PlaylistItemInfo.TYPE_PRESENTATION) && instance.items.get(0).getObjectRef().equals(item.getObjectRef()))
					|| (instance.items.get(0).getType().equals(PlaylistItemInfo.TYPE_URL) && instance.items.get(0).getObjectRef().equals(item.getObjectRef()))) {
				instance.items.add(item);
//				instance.presentationReadyCommands.add(presentationReadyCommand);
				return instance;
			}
		}
		ViewerPresentationController instance = new ViewerPresentationController(item, 
				htmlName, 
				containerName, 
				presentationReadyCommand,
				presentationDoneCommand);
		
		instances.add(instance);
		
		return instance;
	}
	
	public static void clearInstances() {
		instances.clear();
	}

	public ViewerPresentationController(PlaylistItemInfo item, 
			String htmlName, 
			String containerName, 
			Command presentationReadyCommand, 
			Command presentationDoneCommand) {
		items.add(item);
		this.htmlName = htmlName;
		this.iframeName = "iFrame_" + htmlName;
		this.containerName = containerName;
		this.presentationReadyCommand = presentationReadyCommand;
		this.presentationDoneCommand = presentationDoneCommand;
//		presentationReadyCommands.add(presentationReadyCommand);
		
		status = LOADING_STATUS;
		
		readyTimer = new Timer() {
			@Override
			public void run() {
				forceDataReady();
			}
		};
		
//		ViewerHtmlUtils.registerPresentation("iFrame_" + this.htmlName, this);
	}

	public void init(boolean load) {	
		if (status == LOADING_STATUS) {
			if (items.get(0).getType().equals(PlaylistItemInfo.TYPE_PRESENTATION)) {
				PresentationInfo result = ViewerDataController.getPresentation(items.get(0).getObjectRef());
				
				if (result != null){
					presentation = result;
		
					initPlaceholders();
					if (load) {
						loadPresentation();
					}
					else {
						status = UNLOADED_STATUS;
						presentationReady();
					}
				}
			} else {
				addPresentation();
				onPlaceholderReady();
			}
		}
		// if this presentation is a multiple, init has already executed for another instance
		else if (load) {
			loadPresentation();
		}
	}
	
	private void initPlaceholders() {
		PresentationParser.parsePresentation(presentation);
		DistributionJsonParser.parseDistributionData(presentation);
		
		String headerScript = PRESENTATION_HEADER_SCRIPT.replace("%s", iframeName);
		PresentationParser.addHeaderScripts(presentation, headerScript);
		
		PresentationParser.removePresentationObject(presentation);
//		if (ViewerEntryPoint.isPresentation()) {
//			PresentationParser.setScrollEnabled(presentation);
//		}
		
		Command placeholderReadyCommand = new Command() {
			@Override
			public void execute() {
				onPlaceholderReady();
			}
		};
		
		Command placeholderDoneCommand = new Command() {
			@Override
			public void execute() {
				onPlaceholderDone();
			}
		};
		
		// [AD] Sets the done placeholder as the first in the list to prevent timing issues
		if (ViewerEntryPoint.isEmbed()) {
			presentation.setDonePlaceholderFirst();
		}
		
		for (PlaceholderInfo ph: presentation.getPlaceholders()){
			String phName = htmlName + "_" + ph.getId();
			if (ph.getType()!= null && ph.getType().equals(PlaceholderInfo.TYPE_PLAYLIST)) {
				if (ph.getDistributionToAll() || ViewerEntryPoint.checkDistribution(ph.getDistribution())) {
//				if (ph.getObjectRef() != null && ViewerDataProvider.getPlaylist(ph.getObjectRef()) != null) {
					
					// if there is no donePlaceholder
					
					// Send done command to the first placeholder
					Command doneCommand = null;
					if (placeholderDoneCommand != null && 
							ViewerEntryPoint.isEmbed() &&
							(RiseUtils.strIsNullOrEmpty(presentation.getDonePlaceholder()) ||
							presentation.getDonePlaceholder().equals(ph.getId()))) {
						doneCommand = placeholderDoneCommand;
						
						// send done for only 1 placeholder
						placeholderDoneCommand = null;
					}
					
					// Send done command to all placeholders
//					Command doneCommand = placeholderDoneCommand;
//					if (!RiseUtils.strIsNullOrEmpty(presentation.getDonePlaceholder()) &&
//							!presentation.getDonePlaceholder().equals(ph.getId())) {
//						doneCommand = null;
//					}
							
					ViewerPlaceholderController placeholder = new ViewerPlaceholderController(ph, 
							iframeName, 
							phName, 
							placeholderReadyCommand,
							doneCommand);
					
					placeholders.add(placeholder);
//				}
				}
				else {
					//hide placeholder
					PresentationParser.hidePlaceholder(presentation, ph.getId());
				}
			}
//			else if (ph.getUrl() != null && ph.getUrl().toLowerCase().contains(".xml")) {
//				ViewerGadgetController gadget = new ViewerGadgetController(ph, null, phName, phName + "_0", null, null);
//				PresentationParser.addGadgetWrapper(presentation, ph.getId(), phName, 				
//						ph.getWidth() + "px",
//						ph.getHeight() + "px");
//				gadget.updateHtml(presentation);
//				gadgets.add(gadget);
//			}
		}
		
		for (ViewerPlaceholderController placeholder : placeholders) {
			placeholder.init();
			placeholder.updateHtml(presentation);
		}

	}
	
	private void loadPresentation() {
		addPresentation();
		
		boolean allReady = true;
		for (ViewerPlaceholderController placeholder: placeholders) {		
			if (placeholder.getStatus() != ViewerPlaceholderController.READY_STATUS) {
				allReady = false;
				break;
			}
		}
		
		// if there are no placeholders
		if (placeholders.size() == 0 || allReady) {
			onPlaceholderReady();
		}
	}

	private void addPresentation(){
		int height = Window.getClientHeight();
		int width = Window.getClientWidth();
		int top = 0, left = 0;
//		double scale = ViewerUtils.getItemScale(item.getScale());
		
//		height = (int)(Window.getClientHeight() * scale);
//		width = (int)(Window.getClientWidth() * scale);
		
//        top = ViewerUtils.getPositionTop(item.getPosition(), 0, Window.getClientHeight(), height);
//        left = ViewerUtils.getPositionLeft(item.getPosition(), 0, Window.getClientWidth(), width);
        
		if (items.get(0).getType().equals(PlaylistItemInfo.TYPE_PRESENTATION)) {
			ViewerHtmlUtils.addPresentation(htmlName, containerName, width, height, top, left, 
					presentation.getLayout(), 
					presentation.getWidth() + presentation.getWidthUnits(), 
					presentation.getHeight() + presentation.getHeightUnits(),
					(presentation.getHidePointer() && ViewerEntryPoint.isDisplay()), !ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed());
			
			// set presentation size in the entry point to make it available for Embed IFrame size
			ViewerEntryPoint.setPresentationSize(presentation.getWidth(), presentation.getHeight());
		}
		else {
			ViewerHtmlUtils.addUrl(htmlName, containerName, width, height, top, left, items.get(0).getObjectRef());	
		}
		
		// Timer that will cut loading to a maximum of 20 seconds.
		readyTimer.schedule(20 * 1000);
		
		status = ADDED_STATUS;
	}
	
//	public void onPresentationLoad() {
//		for (ViewerPlaceholderController placeholder: placeholders) {
//			placeholder.updateGadgets(iframeName);
//		}
//	}
	
	protected void onPlaceholderReady() {
//		if (status != ALL_READY_STATUS) {
//			boolean anyReady = false;
			boolean allReady = true;
			for (ViewerPlaceholderController placeholder: placeholders) {		
				if (placeholder.getStatus() != ViewerPlaceholderController.READY_STATUS) {
					allReady = false;
				}
//				else if (placeholder.getStatus() == ViewerPlaceholderController.READY_STATUS) {
//					anyReady = true;
//				}
			}
			
//			if (anyReady) {
//				status = READY_STATUS;
//			}
			
			if (allReady && status != ALL_READY_STATUS) {
				status = ALL_READY_STATUS;
				readyTimer.cancel();
				presentationReady();
			}
	
			if (status == ADDED_STATUS) {	
				status = READY_STATUS;
				presentationReady();				
			}
			
//			boolean canPlay = false; 
//			for (PlaylistItemInfo item: items) {
//				if (item.getTimeline().canPlay()) {
//					canPlay = true;
//					break;
//				}
//			}
			
//			if (status >= READY_STATUS && isPlaying && (!ViewerEntryPoint.isDisplay() || canPlay)) {
//				for (ViewerPlaceholderController placeholder: placeholders) {		
//					if (placeholder.getStatus() == ViewerPlaceholderController.READY_STATUS) {
//						placeholder.play();
//					}
//				}
//			}
//		}
	}
	
	private void forceDataReady() {
		status = ALL_READY_STATUS;
		presentationReady();
	}
	
	private void presentationReady() {
		if (presentationReadyCommand != null) 
			presentationReadyCommand.execute();
		
//		for (Command command: presentationReadyCommands) {
//			command.execute();
//		}
	}
	
	private void onPlaceholderDone() {
		if (presentationDoneCommand != null)
			presentationDoneCommand.execute();
	}
	
	public void play() {
//		if (!isPlaying) {
			isPlaying = true;
			for (ViewerPlaceholderController placeholder: placeholders) {
				if (!placeholder.play()) {				
//					stop();
					
					onPlaceholderDone();
					
					return;
				}
					
			}
			
			if (isPlaying)
				setVisibility(true);
//		}
	}
	
	public void stop() {
		if (isPlaying) {
			isPlaying = false;
			setVisibility(false);
			
			for (ViewerPlaceholderController placeholder: placeholders) {
				placeholder.stop();
			}
		}
	}
	
	public void pause() {
		if (isPlaying) {
			isPlaying = false;
			
			// AD: Added visibility = false so pause can be called between presentations switching
			setVisibility(false);
			
			for (ViewerPlaceholderController placeholder: placeholders) {
				placeholder.pause();
			}
		}
	}
	
	private void setVisibility(boolean visible) {
		ViewerHtmlUtils.showElement(htmlName, visible);
	}
	
	public int getStatus() {
		return status;
	}
	
	public void load() {
		if (status == UNLOADED_STATUS) {
			loadPresentation();
		}
	}
	
	public void unload() {
		// only unload READY presentations that are NOT playing
		if (status >= READY_STATUS && !isPlaying) {
			// Check if the presentation is a duplicate; if it is check the other timelines (inefficient)
			if (items != null && items.size() > 1) {
				for (PlaylistItemInfo item: items) {
					if (item.getTimeline().canPlay(new Date(new Date().getTime() + ViewerScheduleController.UNLOAD_TIME))) {
						return;
					}
				}
			}
			
			status = UNLOADED_STATUS;
			
			for (ViewerPlaceholderController placeholder: placeholders) {
				placeholder.unload();
			}
			
			ViewerHtmlUtils.destroyElement(htmlName, containerName);
		}
	}
	
}
