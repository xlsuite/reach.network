// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.risevision.common.client.info.PlaceholderInfo;
import com.risevision.common.client.info.PlaylistInfo;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.PresentationInfo;
import com.risevision.common.client.utils.PresentationParser;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerPlaceholderController {
	public static final int LOADED_STATUS = 1;
	public static final int READY_STATUS = 2; 
	
	private static final String GADGET_WRAPPER_SCRIPT = "<script>try {" +
			"updateGadgetWrapper('%s1', '%s2', %s3, %s4, '%s5');" +
			"} catch(err) { parent.writeToLog('updateGadgetWrapper call - %s2 - ' + err.message); }" +
			"</script>";

	private PlaceholderInfo placeholder;
	private String presFrame;
	private String htmlName;
	private int status;
	private Command placeholderReadyCommand, placeholderDoneCommand;
	
//	private PlaylistItemInfo scheduleItem;
	private ArrayList<ViewerControllerInterface> gadgets = new ArrayList<ViewerControllerInterface>();
	PlaceholderVideoController videoController = null;

	private int currentItem = -1, nextItem = -1, lastItem = -1;
	
	private ViewerTimerController timerController = ViewerTimerController.getInstance();
	private long itemTime = -1;
//	private Timer itemTimer;
	private long scheduleTime = -1;
//	private Timer scheduleTimer;
	
	private boolean isPlaying = false;
	
	public ViewerPlaceholderController(PlaceholderInfo placeholderInfo,
				String presFrame,
				String htmlName,
				Command placeholderReadyCommand,
				Command placeholderDoneCommand) {
//		this.scheduleItem = scheduleItem;
		this.placeholder = placeholderInfo;
		this.placeholderReadyCommand = placeholderReadyCommand;
		this.placeholderDoneCommand = placeholderDoneCommand;
		this.presFrame = presFrame;
		this.htmlName = htmlName;

		Command scheduleTimerCommand = new Command() {
			public void execute() {
				timerExecute();
			}
		};
		
		timerController.addTimerCommand(scheduleTimerCommand);
	
//		itemTimer = new Timer() {
//			@Override
//			public void run() {
//				playNextItem();
//			}
//		};
		
//		scheduleTimer = new Timer() {
//			@Override
//			public void run() {
//				verifySchedule();
//			}
//		};
	}
	
	public void init() {
		if (placeholder.getObjectRef() != null && !placeholder.getObjectRef().isEmpty()){
			PlaylistInfo result = ViewerDataController.getPlaylist(placeholder.getObjectRef());
			
			if (result != null){
				placeholder.setPlaylist(result);
			}
		}
		else {
			// Assume the playlist info is already in placeholder
		}
		
//		setInheritedValues();
		status = LOADED_STATUS;
		bindPlaylist();
	}
	
//	private void setInheritedValues() {
//		if (placeholder.getTransition().equals(Global.INHERITED))
//			placeholder.setTransition(scheduleItem.getTransition());
//		if (placeholder.getScale().equals(Global.INHERITED))
//			placeholder.setScale(scheduleItem.getScale());
//		if (placeholder.getPosition().equals(Global.INHERITED))
//			placeholder.setPosition(scheduleItem.getPosition());
//	}

	protected void bindPlaylist() {
		Command gadgetReadyCommand = new Command() {
			
			@Override
			public void execute() {
				executeGadgetReady();
			}
		};
		
		Command gadgetDoneCommand = new Command() {
			
			@Override
			public void execute() {
				executeGadgetDone();
			}
		};
		
		// Removes items with blank durations
		int count = 0;
		boolean gadgetReady = false;
		if (placeholder.getPlaylistItems() != null) {
			for (PlaylistItemInfo item: placeholder.getPlaylistItems()){
				if (!item.getDuration().isEmpty() && !(RiseUtils.strToInt(item.getDuration(), 0) < 1)
						&& (item.getDistributionToAll() || ViewerEntryPoint.checkDistribution(item.getDistribution()))){
//					setInheritedValues(item);
					
					ViewerControllerInterface gadget;
					if (item != null && PlaylistItemInfo.TYPE_VIDEO.equals(item.getType())) {
						if (videoController == null) {
							videoController = new PlaceholderVideoController(placeholder, presFrame, htmlName);
						}
						gadget = new ViewerVideoController(item, htmlName + "_" + count, gadgetReadyCommand, gadgetDoneCommand, videoController);
					}
					else if (item != null && PlaylistItemInfo.TYPE_GADGET.equals(item.getType())) {
						gadget = new ViewerGadgetController(placeholder, item, htmlName, htmlName + "_" + count, gadgetReadyCommand, gadgetDoneCommand);
					}
					else if (item != null && PlaylistItemInfo.TYPE_WIDGET.equals(item.getType())) {
						gadget = new ViewerWidgetController(placeholder, item, htmlName, htmlName + "_" + count, gadgetReadyCommand, gadgetDoneCommand);
					}
					else {
						gadget = new ViewerPlaylistItemController(placeholder, item, htmlName, htmlName + "_" + count, gadgetReadyCommand, gadgetDoneCommand);
					}
					
					gadgets.add(gadget);
					count++;
					
					// Gadget should never be ready before being added
//					if (gadget.isReady()) {
//						gadgetReady = true;
//					}
				}
			}
		}
		
		if (gadgets.size() == 0 || gadgetReady) {
			executeGadgetReady();
		}
	}
	
//	private void setInheritedValues(PlaylistItemInfo item) {
//		if (item.getTransition().equals(Global.INHERITED))
//			item.setTransition(placeholder.getTransition());
//		if (item.getScale().equals(Global.INHERITED))
//			item.setScale(placeholder.getScale());
//		if (item.getPosition().equals(Global.INHERITED))
//			item.setPosition(placeholder.getPosition());
//	}
	
	public void updateHtml(PresentationInfo presentation) {
		if (gadgets.size() != 0) {
			// load placeholder as hidden (show only if empty)
			PresentationParser.hidePlaceholder(presentation, placeholder.getId());
			
			addGadgetWrapper(presentation, placeholder.getId(), htmlName, 					
//					placeholder.getWidth(),
//					placeholder.getHeight(), 
					0, 0,
					placeholder.getTransition());
			
			for (ViewerControllerInterface gadget: gadgets) {
				gadget.updateHtml(presentation);
			}
			
			if (videoController != null) {
				videoController.updateHtml(presentation);
			}
		}
	}
	
	private void addGadgetWrapper(PresentationInfo presentation, String containerName, String htmlName, int width, int height, String transition){
		String modifiedGadgetScript = GADGET_WRAPPER_SCRIPT.replace("%s1", containerName)
						.replace("%s2", htmlName)
						.replace("%s3", Integer.toString(width))
						.replace("%s4", Integer.toString(height))
						.replace("%s5", transition);
		
		PresentationParser.addScriptTag(presentation, modifiedGadgetScript);
	}
	
//	public void updateGadgets(String presFrame) {
//		if (gadgets.size() != 0) {
//			for (ViewerControllerInterface gadget: gadgets) {
//				if (gadget instanceof ViewerWidgetController) {
//					((ViewerWidgetController) gadget).renderHtml(presFrame);
//				}
//			}
//		}
//	}
	
	protected void executeGadgetReady() {
		if (status != READY_STATUS) {			
			status = READY_STATUS;
			placeholderReadyCommand.execute();
		}
	
		// if no Gadget was selected as the Current item, but the playlist is supposed to play
		// play the ready gadget
//		if (isPlaying && currentItem == -1) {
//			for (int i = 0; i < gadgets.size(); i++) {
//				if (gadgets.get(i).isReady() && gadgets.get(i).getItem().getTimeline().canPlay()) {
//					currentItem = i;
//					
//					if (!gadgets.get(currentItem).getItem().isPlayUntilDone()) {
//						int duration = RiseUtils.strToInt(gadgets.get(currentItem).getItem().getDuration(), 0);
//						// Schedule the timer to run once in x seconds.
//						setNextItemCheck(duration);
////						itemTimer.schedule(duration * 1000);
//					}
//					else {
//						// cancel item check if PUD
//						setNextItemCheck(-1);
//					}
//					
//					break;
//				}
//			}
//		}

		// if lastItem == -1 then we haven't played through any
		if (isPlaying && lastItem == -1) {
			if (currentItem != -1 && gadgets.get(currentItem).isReady()) {
				showPlaceholder(true);
//				nextItem = currentItem;
				gadgets.get(currentItem).play(true);
				lastItem = currentItem;
			}
			else if (currentItem == -1) {
				setNextItemCheck(-1);
				
				playNextItem(true);
			}
		}
	}
	
	private void executeGadgetDone() {
		if ((/* gadgets.size() == 1 || */ !gadgets.get(currentItem).getItem().isPlayUntilDone()) && 
				lastItem == currentItem) {
			gadgets.get(currentItem).stop(false);
			gadgets.get(currentItem).play(false);
		}
		else {
			playNextItem(true);
		}
	}
	
	private void timerExecute()	{
		if (itemTime != -1) {
			itemTime--;
		}
		
		// calculate the next item time
		if (itemTime == 0) {
			playNextItem(true);
		}	
		
		if (scheduleTime != -1) {
			scheduleTime--;
		}
		
		if (scheduleTime == 0) {
			verifySchedule(true);
		}
	}
	
	private void setNextItemCheck(int seconds) {
		itemTime = seconds;
//		if (seconds == -1) {
//			itemTime = -1;
//		}
//		else {
//			itemTime = timerController.getCurrentTime() + (seconds * 1000);
//		}
	}
	
	private void setNextScheduleCheck(int seconds) {
		scheduleTime = seconds;
//		if (seconds == -1) {
//			scheduleTime = -1;
//		}
//		else {
//			scheduleTime = timerController.getCurrentTime() + (seconds * 1000);
//		}
	}
	
	public boolean play() {
		// [AD] play should execute even if there are no gadgets to show the placeholder if it's empty
		if (placeholder.isVisible() && /* status == READY_STATUS && */ gadgets.size() > 0 && !isPlaying) {
			if (ViewerEntryPoint.isDisplay()) {
				verifySchedule(false);
			}
			else {
				isPlaying = true;
				playNextItem(false);
			}
		}
		
		return isPlaying || placeholderDoneCommand == null;
	}
	
	private void verifySchedule(boolean executeDoneCommand) {
		if (placeholder.getTimeline().canPlay()) {
			if (!isPlaying) {
				isPlaying = true;
				playNextItem(executeDoneCommand);
			}
			else if (currentItem != -1 && !gadgets.get(currentItem).getItem().getTimeline().canPlay()) {
				playNextItem(executeDoneCommand);
			}
		}
		else {
			if (isPlaying) {
				stop();
			}
			showPlaceholder(false);
			
			if (placeholderDoneCommand != null && executeDoneCommand) {
				placeholderDoneCommand.execute();
			}
		}

		setNextScheduleCheck(60);
	}

	private void playNextItem(boolean executeDoneCommand) {	
		// only send Done if an item actually played
		if (placeholderDoneCommand != null && nextItem == gadgets.size() - 1) {
			nextItem = -1;
			isPlaying = false;
			
			if (executeDoneCommand) {
				placeholderDoneCommand.execute();
			}

			return;
		}
		
		// signifies Done was sent right after Play
		if (isPlaying && lastItem != currentItem && currentItem != -1 && gadgets.get(currentItem).isReady()) {
			gadgets.get(currentItem).stop(true);

			// we haven't played through any items yet
			if (lastItem == -1) {
				currentItem = -1;
			}
		}
		
		setNextItem();
		
		if (nextItem == -1) {
			setNextItemCheck(60);
			
			showPlaceholder(false);
		}
		else if (ViewerEntryPoint.isDisplay() && !gadgets.get(nextItem).getItem().getTimeline().canPlay()) {
			showPlaceholder(false);

			if (nextItem == currentItem) {
				nextItem = -1;
				if (currentItem != -1) {
					gadgets.get(currentItem).stop(true);
				}
				currentItem = -1;
				
				setNextItemCheck(60);
			}
			else if (gadgets.size() == 1) {
				setNextItemCheck(60);
			}
			else {
				playNextItem(executeDoneCommand);
			}
		}
		else if (!gadgets.get(nextItem).isReady() && gadgets.get(nextItem).getItem().isPlayUntilDone()) {
			// if the item is PUD but not ready, skip it
			if (currentItem == nextItem) {
				nextItem = -1;
				currentItem = -1;
				
				setNextItemCheck(60);
			}
			else if (gadgets.size() != 1) {
				playNextItem(executeDoneCommand);
			}
			else {
				currentItem = nextItem;
			}
		}
		else if (!gadgets.get(nextItem).isReady()) {
			if (gadgets.size() != 1){
//				playNextItem();
				
				int duration = RiseUtils.strToInt(gadgets.get(nextItem).getItem().getDuration(), 0);
				// Schedule the timer to run again in x seconds.
				setNextItemCheck(duration);
			}
//			else {
//				isPlaying = false;
//			}
			
			currentItem = nextItem;
		}
		else {
			showPlaceholder(true);
//			int lastItem = currentItem;
			currentItem = nextItem;
//			isPlaying = true;

			if (gadgets.size() != 1) {
				if (!gadgets.get(currentItem).getItem().isPlayUntilDone()) {
					int duration = RiseUtils.strToInt(gadgets.get(currentItem).getItem().getDuration(), 0);
					// Schedule the timer to run once in x seconds.
					setNextItemCheck(duration);
//					itemTimer.schedule(duration * 1000);
				}
			}
//			else if (gadgets.size() == 1 && gadgets.get(currentItem).getItem().isPlayUntilDone()) {
//				gadgets.get(currentItem).stop();
//				gadgets.get(currentItem).play();
//			}
			
			gadgets.get(currentItem).play(true);
			if (lastItem != -1 && currentItem != lastItem){
				gadgets.get(lastItem).pause(true);
			}
			
			lastItem = currentItem;

		}

	}
	
	private void setNextItem() {
		if (gadgets.size() == 1) {
			nextItem = 0;
		} else if (nextItem < gadgets.size() - 1) {
			nextItem++;
		} else if (currentItem != -1) {
			nextItem = 0;
		} else {
			nextItem = -1;
		}
	}
	
	public void stop() {
		isPlaying = false;
		
		setNextItemCheck(-1);
		setNextScheduleCheck(-1);
		if (currentItem != -1) {
			gadgets.get(currentItem).stop(true);
			
			// [AD] Prevent done next time the placeholder is played
			if (nextItem == gadgets.size() - 1) {
				nextItem = -1;
			}
		}
	}
	
	public void pause() {
		isPlaying = false;
		
		setNextItemCheck(-1);
		setNextScheduleCheck(-1);
		if (currentItem != -1) {
			// AD: Added hide = true so pause can be called between presentations switching 
			gadgets.get(currentItem).pause(true);
			
			// [AD] Prevent done next time the placeholder is played
			if (nextItem == gadgets.size() - 1) {
				nextItem = -1;
			}
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	private void showPlaceholder(boolean show) {
		ViewerHtmlUtils.showFrameElement(presFrame, placeholder.getId(), show);
	}
	
	public void unload() {
		for (ViewerControllerInterface item: gadgets) {
			item.setReady(false);
		}
		
		status = LOADED_STATUS;
	}
}
