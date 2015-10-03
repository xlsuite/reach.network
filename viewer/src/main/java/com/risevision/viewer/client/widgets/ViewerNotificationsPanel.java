package com.risevision.viewer.client.widgets;

import com.google.gwt.user.client.ui.PopupPanel;
import com.risevision.viewer.client.info.NotificationType;

public class ViewerNotificationsPanel extends PopupPanel {
	private static ViewerNotificationsPanel instance;
	
	private ViewerNotificationsWidget notificationsWidget;

	public ViewerNotificationsPanel() {
		super(true, true);

		notificationsWidget = new ViewerNotificationsWidget() {
			
			@Override
			protected void hide() {
				hidePanel();
			}
		};
		
		add(notificationsWidget);

		styleControls();
	}
	
	private void styleControls() {
		setSize("450px", "250px");

		setStyleName("content-box");
		
		this.getElement().getStyle().setProperty("zIndex", "999");

	}
	
	public static ViewerNotificationsPanel getInstance() {
		try {
			if (instance == null)
				instance = new ViewerNotificationsPanel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	public void show(NotificationType notificationType) {
		notificationsWidget.setNotification(notificationType);
		
		show();
	}
	
	public void show(String notification) {
		notificationsWidget.setText(notification);
		
		show();
	}
	
	@Override
	public void show() {
//		setAutoHideEnabled(!persist);		

		super.show();
		super.center();
		super.setPopupPosition(super.getPopupLeft(), 120);
		
	}
	
	private void hidePanel() {
		hide();
	}
}
