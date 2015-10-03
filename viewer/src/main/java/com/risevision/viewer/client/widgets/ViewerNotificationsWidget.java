// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.risevision.viewer.client.info.NotificationType;

public abstract class ViewerNotificationsWidget extends VerticalPanel {

	private Image errorIcon = new Image("../images/icons-error.png");

	private SimplePanel messageContainer = new SimplePanel();
	
	private MessageCounterLabel messageCounterLabel;
	private HTML messageLabel = new HTML();

	public ViewerNotificationsWidget() {		
		add(errorIcon);
		setCellHorizontalAlignment(errorIcon, HasHorizontalAlignment.ALIGN_CENTER);
		setCellVerticalAlignment(errorIcon, HasVerticalAlignment.ALIGN_MIDDLE);
		add(messageContainer);
				
		messageCounterLabel = new MessageCounterLabel() {
			
			@Override
			void run() {
				hide();
			}
		};
				
		styleControls();
	}
	
	private void styleControls() {
		errorIcon.setPixelSize(64, 64);
		
		messageLabel.getElement().getStyle().setProperty("paddingTop", "20px");
		messageLabel.getElement().getStyle().setProperty("paddingBottom", "40px");

		messageLabel.getElement().getStyle().setProperty("textAlign", "center");
		
	}

	public void setNotification(NotificationType notification) {
		if (notification.getTime() != 0) {
			messageCounterLabel.schedule(notification);
			
			messageContainer.clear();
			messageContainer.add(messageCounterLabel);
						
		}
		else {
			setText(notification.getMessage());
		}
	}
	
	public void setText(String text) {
		messageLabel.setHTML(text);
		
		messageContainer.clear();
		messageContainer.add(messageLabel);
		
	}
		
	protected abstract void hide();
	
}
