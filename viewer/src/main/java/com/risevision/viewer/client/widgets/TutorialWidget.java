// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class TutorialWidget extends PopupPanel {
	private static TutorialWidget instance;
	
	private AbsolutePanel mainPanel = new AbsolutePanel();
    private Frame videoFrame = new Frame();
	private HTML closePanel = new HTML("<span style='cursor:pointer;font-size:26px;'>&times;</span>");
//	private Image startVideoImage = new Image("images/help-video.jpg");
	
	// Default video url not actually used - Tutorial link is hidden if the value is missing in the PNO settings 
//	private String videoUrl = "http://www.youtube.com/embed/1QJygspi8MA?autoplay=0";
	private String videoUrl = "http://www.youtube.com/embed/fgPqkDfcLx4?autoplay=0";

	
	public final static String TUTORIAL_URL = "tutorialURL";
	
	public static TutorialWidget getInstance() {
		try {
			if (instance == null)
				instance = new TutorialWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
    public TutorialWidget() {
    	super(false, false);
		
		mainPanel.add(videoFrame, 0, 7);
//		mainPanel.add(startVideoImage, 285, 9);
		mainPanel.add(closePanel, 635, -10);
		
		add(mainPanel);
		
        styleControls();
        initActions();
        
    }
    
    private void styleControls() {  
    	videoFrame.setSize("640px", "385px");
    	videoFrame.getElement().setPropertyInt("frameBorder", 0);
    	videoFrame.getElement().getStyle().setBackgroundColor("black");
    	
//    	startVideoImage.setSize("640px", "385px");
//    	startVideoImage.getElement().getStyle().setCursor(Cursor.POINTER);
    	
    	mainPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    	
    	setSize("645px", "398px");

		addStyleName("rounded-border");
		addStyleName("gradient-overlay-up");
    }
    
	private void initActions() {
		closePanel.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				videoFrame.setUrl("");

				hide();
			}
		});
	}
	
	public void setVideoUrl(String videoUrl) {
		if (!RiseUtils.strIsNullOrEmpty(videoUrl)) {
			this.videoUrl = videoUrl;
		}
	}
	
	public void show() {
		super.show();
		center();
		
		trackEvent("Show", "");
		videoFrame.setUrl(videoUrl);
		
	}
	
	public void hide() {
		trackEvent("Hide", "");
		
		super.hide();
	}
	
	private void trackEvent(String action, String label) {
		ViewerHtmlUtils.trackAnalyticsEvent("Tour", action, label);
	}
	
}
