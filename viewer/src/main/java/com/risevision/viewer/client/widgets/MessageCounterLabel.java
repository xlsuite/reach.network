package com.risevision.viewer.client.widgets;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.risevision.viewer.client.info.NotificationType;

public abstract class MessageCounterLabel extends Label {
	
	public static final int ONE_SECOND_DELAY = 1000;
	public static final int ONE_MINUTE_DELAY = ONE_SECOND_DELAY * 60;
	public static final String TIMER_LABEL = "%timer%";
	
	private NotificationType message;
	private int count = 0;
	private Timer countTimer;
	
	public MessageCounterLabel() {
		super();
		
		countTimer = new Timer() {
			
			@Override
			public void run() {
				onTimerTick();
			}
		};
		
		getElement().getStyle().setProperty("textAlign", "center");
		
	}
	
	public void schedule(NotificationType message) {
		this.message = message;
		this.count = message.getTime();
		
		updateCount();
		
		countTimer.cancel();
		countTimer.schedule(message.getDelay());
	}
	
	private void onTimerTick() {
		count--;
		
		if (count > 0) {
			countTimer.schedule(message.getDelay());
		}
		else {
			run();
		}
		
		updateCount();
				
	}
	
	private void updateCount() {
		setText(message.getMessage(count));
	}
	
	abstract void run();

}
