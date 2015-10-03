package com.risevision.viewer.client.info;

import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.widgets.MessageCounterLabel;
import com.risevision.viewer.client.widgets.oem.DisplayRegisterWidget;

public enum NotificationType {
	    use_google_chrome("For the Best Viewing Experience Please Use Google Chrome", 10, MessageCounterLabel.ONE_SECOND_DELAY),
	    multiple_display_instances("Multiple Display Instances found in this Browser session"),
	    no_presentation_scheduled("A Presentation has not been assigned to the Schedule."),
	    no_presentation_scheduled_poll("A Presentation has not been assigned to the Schedule. Checking again in "
	    		+ MessageCounterLabel.TIMER_LABEL + " minutes.", 30, MessageCounterLabel.ONE_MINUTE_DELAY),
	    server_connection_failed("An error occurred trying to connect to the Server. Retrying in " 
	    		+ MessageCounterLabel.TIMER_LABEL + " seconds...", 60, MessageCounterLabel.ONE_SECOND_DELAY),
	    		
	    blocked_display("This Display is blocked. See the Display Errors on the Display "
	    		+ "Settings Page for more details."),
	    cookies_disabled("Cookies must be enabled in order for the Display to receive updates"),
	    display_id_not_found("The Display ID, <span style='color:red;font-weight:strong;'>" + DisplayRegisterWidget.DISPLAY_ID_PARAM
	    		+ "</span> was not found."),
	    display_id_duplicate("This Display ID, <span style='color:red;font-weight:strong;'>" + DisplayRegisterWidget.DISPLAY_ID_PARAM
	    		+ "</span> is in use by another Display."),
	    display_id_null("Display ID not found.")
    ;
	
	private String message;
	private int time = 0;
	private int delay = 0;
	private NotificationType(String message) {
		this.message = message;
	}
	
	private NotificationType(String message, int time, int delay) {
		this.message = message;
		this.time = time;
		this.delay = delay;
	}
	
	public String getMessage() {
		return message.replace(DisplayRegisterWidget.DISPLAY_ID_PARAM, ViewerEntryPoint.getDisplayId());
	}
	
	public String getMessage(int count) {
		return getMessage().replace(MessageCounterLabel.TIMER_LABEL, Integer.toString(count));
	}
	
	public int getTime() {
		return time;
	}
	
	public int getDelay() {
		return delay;
	}
	
}
