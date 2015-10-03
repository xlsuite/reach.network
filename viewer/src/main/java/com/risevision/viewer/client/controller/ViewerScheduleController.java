// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.info.PlaylistItemInfo;
import com.risevision.common.client.info.ScheduleInfo;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.info.NotificationType;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;
import com.risevision.viewer.client.widgets.ViewerNotificationsPanel;

public class ViewerScheduleController {
	private static int count = 0;
	private int thisCount;
	private ScheduleInfo schedule;
	private ArrayList<PlaylistItemInfo> items = new ArrayList<PlaylistItemInfo>();
	private ArrayList<ViewerPresentationController> presentations = new ArrayList<ViewerPresentationController>();
	private int currentItem = -1, nextItem = -1, lastItem;
	private boolean scheduleReady = false;
	private Command scheduleReadyCommand;
	private boolean doneReceived = false;

	private ViewerTimerController timerController;
	private long itemTime = -1;
//	private Timer itemTimer;
	private long scheduleTime = -1;
//	private Timer scheduleTimer;
	
	private Logger logger = Logger.getLogger("");  
	  
	private boolean playing = false;
	
	public static final int UNLOAD_TIME = 10 * 60 * 1000;
	
	private HandlerRegistration resizeHandler;
	
	public ViewerScheduleController(Command scheduleReadyCommand) {
		this.scheduleReadyCommand = scheduleReadyCommand;
		thisCount = count;
		count++;
		
		Command scheduleTimerCommand = new Command() {
			public void execute() {
				timerExecute();
			}
		};
		
		timerController = ViewerTimerController.createInstance();
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
		
		resizeHandler = Window.addResizeHandler(new ResizeHandler() {	
			@Override
			public void onResize(ResizeEvent event) {
				onWindowResize();
			}
		});
	}
	
	public void init() {
		ScheduleInfo schedule = ViewerDataController.getSchedule();
		
		if (schedule != null){
			this.schedule = schedule;
			bindSchedule();
		}
	}
	
	protected void bindSchedule() {
		createContainer();
		
		Command presentationReadyCommand = new Command() {
			@Override
			public void execute() {
				onPresentationReady();
			}
		};
		
		Command presentationDoneCommand = new Command() {
			@Override
			public void execute() {
				onPresentationDone();
			}
		};
		
		// Removes items with blank durations		
		int itemCount = 0;
		for (PlaylistItemInfo item: schedule.getPlayListItems()){
			if (!item.getDuration().isEmpty() && !(RiseUtils.strToInt(item.getDuration(), 0) < 1)){
//				setInheritedValues(item);
		
				ViewerPresentationController presentation = ViewerPresentationController.getInstance(item, 
						"sc" + thisCount + "_pre" + itemCount, 
						"sc" + thisCount, 
						presentationReadyCommand,
						presentationDoneCommand);
				items.add(item);
				presentations.add(presentation);
				itemCount++;
			}
		}
		
		ViewerPresentationController.clearInstances();
		
		for (int i = 0; i < presentations.size(); i++) {
			boolean playable = (!ViewerEntryPoint.isDisplay() || items.get(i).getTimeline().canPlay());
			presentations.get(i).init(playable);
		}
		
		if (presentations.size() == 0) {
			ViewerNotificationsPanel.getInstance().show(NotificationType.no_presentation_scheduled);
			scheduleReadyCommand.execute();
		}
		else if (presentations.size() == 1 && ViewerEntryPoint.isShowingProgressBar() && !ViewerEntryPoint.isEmbed()) {
			timerController.startTimer();
			play();
		}
	}

//	private void setInheritedValues(PlaylistItemInfo item) {
//		//sets #inherited# fields
//		if (item.getTransition().equals(Global.INHERITED))
//			item.setTransition(schedule.getTransition());
//		if (item.getScale().equals(Global.INHERITED))
//			item.setScale(schedule.getScale());
//		if (item.getPosition().equals(Global.INHERITED))
//			item.setPosition(schedule.getPosition());
//	}
	
	public void createContainer() {
//		int height = Window.getClientHeight();
//		int width = Window.getClientWidth();
//		int top = 0, left = 0;
//		double scale = ViewerUtils.getItemScale(schedule.getScale());
		
//		height = (int)(Window.getClientHeight() * scale);
//		width = (int)(Window.getClientWidth() * scale);
		
//        top = ViewerUtils.getPositionTop(schedule.getPosition(), 0, Window.getClientHeight(), height);
//        left = ViewerUtils.getPositionLeft(schedule.getPosition(), 0, Window.getClientWidth(), width);
		
//      ViewerHtmlUtils.createContainer("sc" + thisCount, width, height, top, left);
		ViewerHtmlUtils.createContainer("sc" + thisCount);
	}
	
	protected void onPresentationReady() {
		if (!scheduleReady) {
			// [AD] - All ready signifies all items are ready and if none canPlay(), ready command will execute
			// and the screen will go black
			boolean allReady = true;
			for (int i = 0; i < presentations.size(); i++) {
				if (presentations.get(i).getStatus() == ViewerPresentationController.ALL_READY_STATUS) {
					// [AD] - Added extra check for item.canPlay() or else the ready command is called and 
					// no items would be ready
					if (!ViewerEntryPoint.isDisplay() || items.get(i).getTimeline().canPlay()) {
						scheduleReady = true;
						break;
					}
				}
				else if (presentations.get(i).getStatus() == ViewerPresentationController.UNLOADED_STATUS) {
					;
				}
				else {
					allReady = false;
				}
			}

			if (scheduleReady || allReady) {
				scheduleReady = true;
				scheduleReadyCommand.execute();
			}
		}
		else if (playing && currentItem != -1) {
			ViewerEntryPoint.showBlackScreen(false);

			presentations.get(currentItem).play();
		}
	}
	
	private void onPresentationDone() {
		if (ViewerEntryPoint.isEmbed()) {
//			logger.log(Level.INFO, "Schedule Done received"); 

			if (!doneReceived) {
				doneReceived = true;
				
				ViewerEntryPoint.reportDone();
				
//				stop();
			}
			else {
				// should never happen
				logger.log(Level.WARNING, "Done received multiple times!"); 
			}
		}
		else if (playing && currentItem != -1) {
			ViewerEntryPoint.showBlackScreen(false);

			presentations.get(currentItem).play();
		}
	}
	
	private void timerExecute()	{
		if (itemTime != -1) {
			itemTime--;
		}
		
		// calculate the next item time
		if (itemTime == 0) {
			playNextItem();
		}	
		
		if (scheduleTime != -1) {
			scheduleTime--;
		}
		
		if (scheduleTime == 0) {
			verifySchedule();
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
	
	public void play() {	
//		if (!playing) {
		if (ViewerEntryPoint.isEmbed()) {
//			logger.log(Level.INFO, "Schedule Play sent"); 

			if (!playing)
				doneReceived = false;
			else {
				// should never happen
				logger.log(Level.WARNING, "Play received multiple times!"); 
			}
		}
		
		timerController.startTimer();

		if (ViewerEntryPoint.isDisplay()) {
			verifySchedule();
		}
		else {
			playing = true;
			playNextItem();
			
		}
//		}
	}
	
	private void verifySchedule() {
		if (schedule.getTimeline().canPlay()) {
//			showBlackScreen(false);
			if (!playing) {
				playing = true;
				playNextItem();
			}
			else if (currentItem != -1 && !items.get(currentItem).getTimeline().canPlay()) {
				playNextItem();
			}
		}
		else {
			if (playing) {
				stop();
			}
			ViewerEntryPoint.showBlackScreen(true);
		}
		
		// load if needed
		for (int i = 0; i < items.size();  i++) {
			loadPresentation(i);
		}
		
		setNextScheduleCheck(60);
	}
	
	private void playNextItem() {	
		setNextItem();

		if (nextItem == -1) {
			setNextItemCheck(60);
//			itemTimer.schedule(60 * 1000);
			
			ViewerEntryPoint.showBlackScreen(true);
		}
		else if (ViewerEntryPoint.isDisplay() && !items.get(nextItem).getTimeline().canPlay()) {
			if (nextItem == currentItem) {
				nextItem = -1;
				if (currentItem != -1) {
					presentations.get(currentItem).stop();
				}
				currentItem = -1;
				
				setNextItemCheck(60);
//				itemTimer.schedule(60 * 1000);
				
				ViewerEntryPoint.showBlackScreen(true);
			}
			
			if (presentations.size() == 1) {
				setNextItemCheck(60);
//				itemTimer.schedule(60 * 1000);
				
				ViewerEntryPoint.showBlackScreen(true);
			}
			else {
				unloadPresentation(nextItem);
				
				playNextItem();
			}
		}
		else if (presentations.get(nextItem).getStatus() != ViewerPresentationController.ALL_READY_STATUS && presentations.size() != 1) {
//			playNextItem();
			
			int duration = RiseUtils.strToInt(items.get(nextItem).getDuration(), 0);
			// Schedule the timer to run again in x seconds.
			setNextItemCheck(duration);
			
			currentItem = nextItem;
		}
		else {
			ViewerEntryPoint.showBlackScreen(false);
//			int lastItem = currentItem;
			currentItem = nextItem;
			
			// because the play() command sets all the Placeholders moving, no need to call any more commands 
			// in the timer controller
			timerController.stopPropagation();
			
			if (presentations.size() != 1) {
				int duration = RiseUtils.strToInt(items.get(currentItem).getDuration(), 0);
				// Schedule the timer to run again in x seconds.
				setNextItemCheck(duration);
//				itemTimer.schedule(duration * 1000);
			}
			
			presentations.get(currentItem).play();
			if (lastItem != -1 && currentItem != lastItem && presentations.get(lastItem) != presentations.get(currentItem)){
				// AD: Fixes issues with Gadgets not having the stop command implemented
				presentations.get(lastItem).pause();
			}
			
			lastItem = currentItem;
		}
	}
	
	private void setNextItem() {
		if (presentations.size() == 1) {
			nextItem = 0;
		} else if (nextItem < presentations.size() - 1) {
			nextItem++;
		} else if (currentItem != -1) {
			nextItem = 0;
		} else {
			nextItem = -1;
		}
	}
	
	private void unloadPresentation(int item) {
		if (item != -1 && presentations.get(item).getStatus() != ViewerPresentationController.UNLOADED_STATUS 
					&& !items.get(item).getTimeline().canPlay(new Date(new Date().getTime() + UNLOAD_TIME))) {		
			presentations.get(item).unload();
		}
	}
	
	private void loadPresentation(int item) {
		if (presentations.get(item).getStatus() == ViewerPresentationController.UNLOADED_STATUS 
				&& items.get(item).getTimeline().canPlay(new Date(new Date().getTime() + UNLOAD_TIME))) {
			presentations.get(item).load();
		}
//		else {
//			presentations.get(item).loadPresentation();
//		}
	}

	public void stop() {
		if (playing) {
			playing = false;
			setNextItemCheck(-1);
//			itemTimer.cancel();
			setNextScheduleCheck(-1);
			
			for (ViewerPresentationController presentation: presentations) {
				presentation.stop();
			}
		}
	}
	
	public void pause() {
		if (playing) {
			playing = false;
			setNextItemCheck(-1);
//			itemTimer.cancel();
			setNextScheduleCheck(-1);
			
			for (ViewerPresentationController presentation: presentations) {
				presentation.pause();
			}
		}
	}
	
	private void onWindowResize() {
		int height = Window.getClientHeight();
		int width = Window.getClientWidth();
//		double scale = ViewerUtils.getItemScale(schedule.getScale());
		
//		height = (int)(Window.getClientHeight() * scale);
//		width = (int)(Window.getClientWidth() * scale);
		
//		ViewerHtmlUtils.resizeContainer("sc" + thisCount, width, height);
		for (int i = 0; i < presentations.size(); i++) {
//			ViewerHtmlUtils.resizeContainer("sc" + thisCount + "_pre" + i, width, height);
			
			// [ad] bug fix - automatically re-size URL iframe size as well
			if (items.get(i).getType().equals(PlaylistItemInfo.TYPE_URL)) {
				ViewerHtmlUtils.resizeContainer("iFrame_sc" + thisCount + "_pre" + i, width, height);
			}
		}
	}
	
	public void unload() {
		stop();
		setNextScheduleCheck(-1);
//		scheduleTimer.cancel();
		ViewerTimerController.destroyOldInstance();
		
		// Workaround for HTML5 Video Gadget
		Timer destroyTimer = new Timer() {
			@Override
			public void run() {
				ViewerHtmlUtils.destroyContainer("sc" + thisCount);
			}
		};
		destroyTimer.schedule(10 * 1000);

		if (resizeHandler != null) {
			resizeHandler.removeHandler();
		}
//		ViewerHtmlUtils.destroyContainer("sc" + thisCount);
	}
}
