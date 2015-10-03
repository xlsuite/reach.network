// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets.oem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.DisplayRegistrationController;
import com.risevision.viewer.client.player.RisePlayerController;

public class EnterDisplayIdWidget extends DisplayRegisterBaseWidget {
	
	private static EnterDisplayIdWidget instance;
	private HorizontalPanel hpButtons = new HorizontalPanel();
	
	private Label ErrorLabel = new Label(MESSAGE_NOT_VERIFIED);
	private TextBox DisplayIdText = new TextBox();
	
	private Button btApply = new DisplayRegisterButtonWidget("Apply");
	private Button btCancel = new DisplayRegisterButtonWidget("Cancel");
	private Button btHelp = new DisplayRegisterButtonWidget("Help");

	private String newDisplayId;
	
	public EnterDisplayIdWidget() {
		super(false);
						
		topPanel.add(ErrorLabel);

		topPanel.add(DisplayIdText);

		hpButtons.add(btApply);
		hpButtons.add(btCancel);
		hpButtons.add(btHelp);
		buttonPanel.add(hpButtons);
						
		styleControls();
		initActions();
	}
	
	private void styleControls() {

		DisplayIdText.setStyleName("form-control");
		DisplayIdText.getElement().setPropertyString("placeholder", "Enter Display ID");

		ErrorLabel.setVisible(false);
		ErrorLabel.getElement().getStyle().setColor("red");

		hpButtons.setWidth("280px");
		hpButtons.setSpacing(5);
	}
	
	private void initActions() {
		btApply.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stopCountdownTimer();
				//check if Display ID has changed
				newDisplayId = DisplayIdText.getText().trim(); 
				if (newDisplayId.isEmpty()) {
					ErrorLabel.setText(MESSAGE_NOT_VERIFIED);
					ErrorLabel.setVisible(true);
				}
				else {
					if (newDisplayId.equals(ViewerEntryPoint.getDisplayId())) {
						//no need to do anything. Just hide the window.
						hide();
					} else {
						btApply.setEnabled(false);					
						//Call CORE to validate display ID
						DisplayRegistrationController.validateDisplayId(newDisplayId);
						//response is processed in ValidateDisplayIdCallback()
					}
				}
			}
		});
		btCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stopCountdownTimer();
				hide();
			}
		});
		btHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stopCountdownTimer();
				HelpFrameWidget.getInstance().show();
			}
		});
	}
	
	public void ValidateDisplayIdCallback(int errorCode, String errorMessage) {

		resetUI();
		
		boolean isValid = errorCode == 0;
		if (!isValid) {
			ErrorLabel.setText(MESSAGE_NOT_VERIFIED + ". " + errorMessage);
			ErrorLabel.setVisible(true);
		} else {
			RisePlayerController.saveAndRestart(newDisplayId, "");
		}

		
	}

	public static EnterDisplayIdWidget getInstance() {
		try {
			if (instance == null)
				instance = new EnterDisplayIdWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	private void resetUI() {
		ErrorLabel.setVisible(false);
		ErrorLabel.setText(MESSAGE_NOT_VERIFIED);
		btApply.setEnabled(true);
	}

	public void show() {
		resetUI();
		super.show();
		ViewerEntryPoint.getDisplayId();
	}
	
}
