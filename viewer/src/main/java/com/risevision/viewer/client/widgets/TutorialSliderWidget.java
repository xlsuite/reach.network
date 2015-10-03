// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class TutorialSliderWidget extends PopupPanel {
//	private static final String OVERVIEW_VIDEO = "NlrH1dp1W2o";
	private static final String OVERVIEW_VIDEO = "QagsiDIpT8k";

	private static TutorialSliderWidget instance;
	
	private AbsolutePanel mainPanel = new AbsolutePanel();
	private VerticalPanel linksPanel = new VerticalPanel();
    private Frame videoFrame = new Frame();
	private HTML closePanel = new HTML("<span style='cursor:pointer;font-size:26px;'>&times;</span>");
	private Image startVideoImage = new Image("images/help-video.jpg");
	
	public static TutorialSliderWidget getInstance() {
		try {
			if (instance == null)
				instance = new TutorialSliderWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
    public TutorialSliderWidget() {
    	super(true, false);
		
		linksPanel.add(createAndStyleLabel("Take The Tour"));
		linksPanel.add(createAndStyleLink("The Two Minute Overview", OVERVIEW_VIDEO, 0));
		linksPanel.add(new HTML("<span style='padding-top: 5px;'></span>"));
		
		linksPanel.add(createAndStyleLabel("Be One With Your Network"));
		linksPanel.add(createAndStyleLink("Create a Presentation from a Template", "ybA8H4bTZrA", 1));
		linksPanel.add(createAndStyleLink("Create a Presentation from Scratch", "GgIYYtj6umI", 2));
		linksPanel.add(createAndStyleLink("Add a Display", "-dwV_PnVkjA", 3));
		linksPanel.add(createAndStyleLink("Schedule a Presentation", "mYkKdnd2K6Q", 4));
		linksPanel.add(createAndStyleLink("Setup your Company and Users", "eAzYcZJLRuQ", 5));
		linksPanel.add(new HTML("<span style='padding-top: 5px;'></span>"));

		linksPanel.add(createAndStyleLabel("Rule The World!"));
		linksPanel.add(createAndStyleLink("Be a Network Operator", "34QErw4_wdY", 6));
		linksPanel.add(createAndStyleLink("Create Multi-Site Presentations", "yiolDjxshFw", 7));
		linksPanel.add(createAndStyleLink("Build your own Gadgets", "ePUOdgt7Zw0", 8));
		linksPanel.add(new HTML("<span style='padding-top: 5px;'></span>"));

//		linksPanel.add(closePanel);
//		mainPanel.setCellHorizontalAlignment(closePanel, HasHorizontalAlignment.ALIGN_RIGHT);
		
		mainPanel.add(linksPanel, -1, 7);
		mainPanel.add(videoFrame, 283, 7);
		mainPanel.add(startVideoImage, 285, 9);
		mainPanel.add(closePanel, 915, -17);
		
		add(mainPanel);
		
        styleControls();
        initActions();
    }
    
    private Label createAndStyleLabel(String text) {
    	Label label = new Label(text);
    	label.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
    	
    	return label;
    }
    
    private Anchor createAndStyleLink(String text, final String videoId, final int videoIndex) {
    	Anchor link = new Anchor(text);
    	link.getElement().getStyle().setMarginLeft(8, Unit.PX);
    	
    	link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				playVideo(videoId);
			}
		});
    	
    	return link;
    }
    
    private void styleControls() {  
    	linksPanel.getElement().getStyle().setPadding(10, Unit.PX);
    	linksPanel.setSize("275px", "389px");
    	linksPanel.addStyleName("inner-border");
    	linksPanel.addStyleName("gradient-overlay-middle");
    	
    	videoFrame.setSize("640px", "385px");
    	videoFrame.getElement().setPropertyInt("frameBorder", 0);
    	videoFrame.getElement().getStyle().setBackgroundColor("black");
    	
    	startVideoImage.setSize("640px", "385px");
    	startVideoImage.getElement().getStyle().setCursor(Cursor.POINTER);
    	
    	mainPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
    	
    	setSize("928px", "398px");
		getElement().getStyle().setProperty("zIndex", "999");

		addStyleName("rounded-border");
		addStyleName("gradient-overlay-up");
    }
    
	private void initActions() {
		closePanel.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		startVideoImage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				playVideo(OVERVIEW_VIDEO);
			}
		});
	}
	
	private void playVideo(String videoId) {
		trackEvent("Play", videoId);
		startVideoImage.setVisible(false);
    	videoFrame.setUrl("http://www.youtube.com/embed/" + videoId + "?autoplay=1");
	}
	
	public void show() {
		super.show();
		center();
		
		trackEvent("Show", "");
		startVideoImage.setVisible(true);
		
//		videoFrame.setUrl("http://www.youtube.com/embed/p/444CCAE310D039D2?autoplay=0&enablejsapi=1");
//		setPopupPosition(Window.getClientWidth() - getOffsetWidth()+ 5, 85);
	}
	
	public void hide() {
		if (isShowing()) {
			videoFrame.setUrl("");
		
			trackEvent("Hide", "");
		}
		super.hide();
	}
	
	private void trackEvent(String action, String label) {
		ViewerHtmlUtils.trackAnalyticsEvent("Tour", action, label);
	}
}
