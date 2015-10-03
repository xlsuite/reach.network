// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginHelpWidget extends PopupPanel {
	private static LoginHelpWidget instance;
	
	//UI pieces
	private AbsolutePanel mainPanel = new AbsolutePanel();
	private VerticalPanel innerPanel = new VerticalPanel();

	private HTML closePanel = new HTML("<span style='cursor:pointer;font-size:26px;'>&times;</span>");

	private Image loginButton = new Image("images/wide-login-button.png");
	private Label infoLabel = new Label("New or existing User? Doesn't matter, just click the " +
			"big blue button below and login with your Google Account to get started.");
	private Image loginImage = new Image("images/google-login-info.png");
	
	private String loginUrl;
	
	public LoginHelpWidget() {
		super(true, true); //set auto-hide and modal
		add(mainPanel);
		
		innerPanel.add(infoLabel);
		innerPanel.add(new HTML("&nbsp;"));
		innerPanel.add(loginButton);
		innerPanel.add(new HTML("&nbsp;"));
		innerPanel.add(loginImage);
		
		mainPanel.add(innerPanel, -1, 7);
		mainPanel.add(closePanel, 652, -17);
		
		styleControls();

		initActions();		
	}
	
	public static LoginHelpWidget getInstance() {
		try {
			if (instance == null)
				instance = new LoginHelpWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instance;
	}
	
	private void styleControls() {		
    	setSize("660px", "452px");
		getElement().getStyle().setProperty("zIndex", "999");

		addStyleName("rounded-border");
		addStyleName("gradient-overlay-up");
		
		loginButton.getElement().getStyle().setCursor(Cursor.POINTER);
		loginButton.setPixelSize(366, 79);
		
    	loginImage.setPixelSize(390, 280);
		
    	mainPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);

    	innerPanel.getElement().getStyle().setPadding(10, Unit.PX);
//		mainPanel.setSize("275px", "389px");
    	innerPanel.addStyleName("inner-border");
    	innerPanel.addStyleName("gradient-overlay-middle");
    	
		innerPanel.setCellHorizontalAlignment(loginButton, HasHorizontalAlignment.ALIGN_CENTER);
		innerPanel.setCellHorizontalAlignment(loginImage, HasHorizontalAlignment.ALIGN_CENTER);
	}
	
	private void initActions() {
		closePanel.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open(loginUrl, "_blank", "");
			}
		});
	}

	
	public void show(String loginUrl) {
		this.loginUrl = loginUrl;
		
		super.show();
		center();
	}
}
