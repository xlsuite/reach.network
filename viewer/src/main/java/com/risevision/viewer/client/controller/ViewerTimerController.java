// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.controller;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

public class ViewerTimerController {
	private static ViewerTimerController instance, oldInstance;
	private ArrayList<Command> timerCommands = new ArrayList<Command>();
	private Timer timer;
	private boolean timerRunning = false;
	private boolean stopPropagation = false;
//	private long currentTime;
	
	public static ViewerTimerController getInstance() {
		try {
			if (instance == null)
				instance = new ViewerTimerController();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public static ViewerTimerController createInstance() {
		oldInstance = instance;
		try {
			instance = new ViewerTimerController();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public static void destroyOldInstance() {
		oldInstance.timer.cancel();
		oldInstance = null;
	}

	public ViewerTimerController() {
		timer = new Timer() {
			public void run() {
				onTimerExecute();
			}
		};
	}
	
	public void addTimerCommand(Command command) {
		timerCommands.add(command);
	}
	
	public void startTimer() {
		if (!timerRunning) {
			onTimerExecute();
			
			timer.scheduleRepeating(1000);
		}
	}
	
	private void onTimerExecute() {
//		currentTime = new Date().getTime();
		for (Command command: timerCommands) {
			if (stopPropagation) {
				stopPropagation = false;
				break;
			}
			
			command.execute();
		}
	}
	
	public void stopPropagation() {
		stopPropagation = true;
	}
	
//	public long getCurrentTime() {
//		return currentTime;
//	}
	
}
