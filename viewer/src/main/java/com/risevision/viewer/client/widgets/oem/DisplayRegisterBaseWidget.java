// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets.oem;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.player.RisePlayerController;

public class DisplayRegisterBaseWidget extends PopupPanel {
		
	protected final String MESSAGE_NOT_VERIFIED = "Not Verified";
	
	protected HTMLPanel outerPanel = new HTMLPanel("");
	protected HTMLPanel topContainer = new HTMLPanel("");
	protected VerticalPanel topPanel = new VerticalPanel();

	protected HTMLPanel buttonContainer = new HTMLPanel("");
	protected VerticalPanel buttonPanel = new VerticalPanel();
	private Label countdownLabel = new Label("Presentation will begin in 30 seconds.");

	private int countdownSeconds;
	private Timer timer;

	protected Boolean showCountdown = false;
	protected boolean hasParent = true;
	
	public DisplayRegisterBaseWidget(boolean showCountdown) {
		super(false, true);
		
		this.showCountdown = showCountdown;

		styleControls();

		add(outerPanel);
		outerPanel.add(topContainer);
		topContainer.add(topPanel);
		
		outerPanel.add(buttonContainer);
		buttonContainer.add(buttonPanel);
				
		if (showCountdown)
			topPanel.add(countdownLabel);

		initActions();
	}
	
	private void styleControls() {
		
//		innerPanel.getElement().getStyle().setPadding(10, Unit.PX);
//		innerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		

		outerPanel.setSize("450px", "350px");
		
		outerPanel.setStyleName("content-box");

		topContainer.addStyleName("navbar navbar-default navbar-static-top");
		topContainer.getElement().getStyle().setPadding(20, Unit.PX);
		topContainer.getElement().getStyle().setHeight(180, Unit.PX);
		topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);		
		topPanel.getElement().getStyle().setProperty("margin", "0 auto");
		topPanel.getElement().getStyle().setWidth(100, Unit.PCT);
		topPanel.getElement().getStyle().setHeight(100, Unit.PCT);
		topPanel.setSpacing(5);
		
		buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		buttonPanel.getElement().getStyle().setProperty("margin", "0 auto");

		getElement().getStyle().setProperty("zIndex", "999");

	}
	
	private void initActions() {
	}
	
//	public static DisplayRegisterBaseWidget getInstance(Boolean showCountdown) {
//		try {
//			if (instance == null)
//				instance = new DisplayRegisterBaseWidget(showCountdown);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return instance;
//	}

	public void show() {
		show(true);
	}

	public void show(boolean hasParent) {
		super.show();
		super.setPopupPosition((int)(Window.getClientWidth()/2 - 200), 100);
		
		this.hasParent = hasParent;
		
		if (showCountdown) {
			startCountdownTimer();
			countdownLabel.setVisible(true);
		}
	}
	
	protected void startCountdownTimer() {
		timer = new Timer() {
			@Override
			public void run() {
				countdownSeconds -=1;
				countdownLabel.setText("Presentation will begin in " + countdownSeconds + " seconds.");
				if (countdownSeconds == 0) {
					closeAndStartPresentation();
				}
					
			}
		};
		
		countdownSeconds = 30;
		timer.scheduleRepeating(1000);
	}
	
	protected void stopCountdownTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			countdownLabel.setVisible(false);
		}
	}

	protected void closeAndStartPresentation() {
		stopCountdownTimer();
		hide();
		ViewerEntryPoint.loadPresentation();
	}

	protected void closeAndRestartViewer() {
		stopCountdownTimer();
		//hide();
		RisePlayerController.restart();
		//TODO: show error if Player is not responding
	}

}
