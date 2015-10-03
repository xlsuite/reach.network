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

public class EnterClaimIdWidget extends DisplayRegisterBaseWidget {
	
	private static EnterClaimIdWidget instance;

	private HorizontalPanel hpButtons = new HorizontalPanel();
		
	private Label ErrorLabel = new Label(MESSAGE_NOT_VERIFIED);

	private TextBox ClaimIdText = new TextBox();
	private TextBox DisplayNameText = new TextBox();
	
	private Button btApply = new DisplayRegisterButtonWidget("Apply");
	private Button btCancel = new DisplayRegisterButtonWidget("Cancel");
	private Button btHelp = new DisplayRegisterButtonWidget("Help");

	private String newClaimId;

	public EnterClaimIdWidget(boolean showCountdown) {
		super(showCountdown);

		topPanel.add(ErrorLabel);

		topPanel.add(ClaimIdText);
		topPanel.add(DisplayNameText);

		hpButtons.add(btApply);
		hpButtons.add(btCancel);
		hpButtons.add(btHelp);
		buttonPanel.add(hpButtons);
						
		styleControls();
		initActions();
	}
	
	private void styleControls() {

		ClaimIdText.setStyleName("form-control");
		DisplayNameText.setStyleName("form-control");

		ClaimIdText.getElement().setPropertyString("placeholder", "Enter Claim ID");
		DisplayNameText.getElement().setPropertyString("placeholder", "Enter Display Name");

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
				registerDisplayId();
			}
		});
		btCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stopCountdownTimer();
				hide();
				if (!hasParent)
					ViewerEntryPoint.loadPresentation();
			}
		});
		btHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				stopCountdownTimer();
				HelpFrameWidget.getInstance().show();
			}
		});
		
		if (showCountdown) {
			ClickHandler stopCountdownClickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					stopCountdownTimer();
				}
			};
			
			ClaimIdText.addClickHandler(stopCountdownClickHandler);
			DisplayNameText.addClickHandler(stopCountdownClickHandler);
		}
		
	}

	public void RegisterDisplayCallback(int errorCode, String errorMessage, String newDisplayId) {

		resetUI();

		boolean isValid = errorCode == 0;
		if (!isValid) {
			ErrorLabel.setText(MESSAGE_NOT_VERIFIED + ". " + errorMessage);
			ErrorLabel.setVisible(true);
		} else {
			RisePlayerController.saveAndRestart(newDisplayId, newClaimId);
		}
	}

	public static EnterClaimIdWidget getInstance(boolean showCountdown) {
		try {
			if (instance == null)
				instance = new EnterClaimIdWidget(showCountdown);
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

	public void show(boolean hasParent) {
		resetUI();
		super.show(hasParent);
		ClaimIdText.setText(ViewerEntryPoint.getClaimId());
	}

	private void registerDisplayId() {
		newClaimId = ClaimIdText.getText().trim(); 
		if (newClaimId.isEmpty())
			ErrorLabel.setVisible(true);
		else {
			btApply.setEnabled(false);
			String displayName = DisplayNameText.getText().trim(); 
			//call CORE to generate new Display ID and validate Claim ID
			DisplayRegistrationController.registerDisplay(newClaimId, displayName);
			//response is processed in RegisterDisplayCallback()
		}
	}

	protected void closeAndStartPresentation() {
		stopCountdownTimer();
		registerDisplayId();
	}

}
