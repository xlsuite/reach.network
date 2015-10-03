// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.risevision.common.client.json.JSOModel;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.data.ViewerDataParser;
import com.risevision.viewer.client.info.Global;
import com.risevision.viewer.client.utils.ViewerHtmlUtils;

public class ViewerPreviewWidget extends PopupPanel implements ClickHandler {
	private static final String EMAIL_TEXT = "Check out this Presentation %1% on Reach.Network - a web platform for digital signage - %2%";
//	private static final String TWEET_TEXT = "Check out %1% on Rise Vision --";
//	private static final String TWEET_END_TEXT = "http://t.co/onos9km via @risevision";
//	private static final int TWEET_NAME_LENGTH = 135 - TWEET_TEXT.length() - TWEET_END_TEXT.length();
	
//	private static final String LIKE_BUTTON_HTML = "<fb:like href=\"%1%\" send=\"false\" layout=\"button_count\" " +
//			"width=\"120\" show_faces=\"false\" font=\"\"></fb:like>";
//	private static final String TWEET_BUTTON_HTML = "<iframe allowtransparency=\"true\" frameborder=\"0\" scrolling=\"no\" " +
//			"src=\"http://platform.twitter.com/widgets/tweet_button.html?text=%1%&via=risevision&related=risevision\" " +
//			"style=\"width:120px; height:20px;\"></iframe>";
	private static final String EMBED_HTML_CODE = "<iframe allowtransparency='true' frameborder='0' scrolling='no' " +
			"src='%1%' style='width:%2%px; height:%3%px;'></iframe>";	

	private static ViewerPreviewWidget instance;
	
	private AbsolutePanel contentPanel = new AbsolutePanel();
	private HTML logoDiv = new HTML();
	private Label nameLabel = new Label();
	
//	private VerticalPanel socialPanel = new VerticalPanel();
	
//	private HorizontalPanel zoomControlPanel = new HorizontalPanel();
	
//	private Label loginLabel = new Label("Login / FREE Sign Up");
	
	private Widget hidePanel, showPanel;
	
//	private Widget sharePanel;
	private HorizontalPanel sharePanel = new HorizontalPanel();
	
//	private Widget postButton;
	private Anchor shareButton = new Anchor("Share");
	private Anchor tutorialButton = new Anchor("<span style='white-space:nowrap;'>Take the Tour</span>", true);
	private Anchor emailButton = new Anchor("Email");
	private Anchor templateButton = new Anchor("<span style='white-space:nowrap;'>Copy Template</span>", true);
	private Anchor loginButton = new Anchor("Login");
	
	private String shortenedUrl;
	
	private AdsenseBannerWidget bannerWidget = AdsenseBannerWidget.getInstance();
	
	public ViewerPreviewWidget() {
		super(false, false);
		
		add(contentPanel);
		
//		contentPanel.add(logoDiv, 0, 0);
		
		contentPanel.add(nameLabel, 10, 40);
//		contentPanel.add(nameLabel, 200, 40);
		
		nameLabel.setText(ViewerDataController.getItemName());

//		contentPanel.add(zoomControlPanel, 490, 10);
		
//		sharePanel = createMenuItem("Share", "../images/speech-bubble-icon.png", 34, 28);
		
		sharePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		sharePanel.add(loginButton);
		
		if (ViewerDataController.isTemplate()) {		
			sharePanel.add(templateButton);
		}
		
		sharePanel.add(shareButton);
		sharePanel.add(emailButton);
		sharePanel.add(tutorialButton);
		
		updateTutorialUrl();
			
//		String itemName = ViewerDataProvider.getItemName();
//		if (itemName.length() > TWEET_NAME_LENGTH) {
//			itemName = itemName.substring(0, TWEET_NAME_LENGTH) + "...";
//		}
//		String messageString = TWEET_TEXT.replace("%1%", itemName);
//		messageString = messageString.replace("%2%", "");
//		socialPanel.add(new HTML(TWEET_BUTTON_HTML.replace("%1%", URL.encode(messageString).replace("'", "\'"))));
//		socialPanel.add(new HTML("<div style='padding-top:8px;'></div>"));
//		socialPanel.add(postButton = new HTML(LIKE_BUTTON_HTML.replace("%1%", getPreviewUrl())));
		
		if (ViewerEntryPoint.getId() != null && !ViewerEntryPoint.getId().isEmpty()) {
			contentPanel.add(sharePanel, 0, 10);
//			contentPanel.add(sharePanel, 180, 10);
			
//			contentPanel.add(socialPanel, 370, 10);
		}

//		contentPanel.add(loginLabel, 740, 39);
//		contentPanel.add(signupLabel, 760, 35);
		
//		contentPanel.add(bannerWidget, 500, 5);
		contentPanel.add(bannerWidget, 380, 5);
		
//		contentPanel.add(hidePanel = createMenuItem("Hide", "../images/eye-icon.png", 47, 29), 980, 10);
		contentPanel.add(hidePanel = createMenuItem("Hide", "../images/eye-icon.png", 47, 29), 880, 10);
		
		contentPanel.add(showPanel = createMenuItem("Show", "../images/eye-icon.png", 47, 29), 0, 10);
		showPanel.setVisible(false);
		
		styleControls();
		initHandlers();
		
//		initFb();
	}
	
	private void styleControls() {
		resizeLarge();
		
		contentPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		addStyleName("rounded-border");
		addStyleName("gradient-overlay-up");

		this.getElement().getStyle().setBackgroundColor("#065ac0");		
		this.getElement().getStyle().setProperty("zIndex", "999");
		
		logoDiv.getElement().addClassName("logo-style");
		logoDiv.getElement().getStyle().setCursor(Cursor.POINTER);
		
		nameLabel.getElement().getStyle().setFontSize(20, Unit.PX);
		nameLabel.getElement().getStyle().setColor("#ffffff");
		nameLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		nameLabel.addStyleName("overflow-ellipsis");
		nameLabel.setWidth("350px");
		
//		zoomControlPanel.getElement().addClassName("zoom-icons-style");
		
//		loginLabel.getElement().getStyle().setFontSize(14, Unit.PX);
//		loginLabel.getElement().getStyle().setColor("#00cd66");
//		loginLabel.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
//		loginLabel.getElement().addClassName("text-drop-shadow");
//		loginLabel.getElement().getStyle().setCursor(Cursor.POINTER);

//		signupLabel.getElement().getStyle().setFontSize(14, Unit.PX);
//		signupLabel.getElement().getStyle().setColor("#00cd66");
//		signupLabel.getElement().getStyle().setFontWeight(FontWeight.BOLDER);
//		signupLabel.getElement().addClassName("text-drop-shadow");
//		signupLabel.getElement().getStyle().setCursor(Cursor.POINTER);	
		
//		postButton.getElement().getStyle().setWidth(120, Unit.PX);
//		postButton.getElement().getStyle().setHeight(20, Unit.PX);
		
		shareButton.addStyleName("link-action");
		tutorialButton.addStyleName("link-action");
		emailButton.addStyleName("link-action");
		loginButton.addStyleName("link-action");
		templateButton.addStyleName("link-action");
	}
	
	private Widget createMenuItem(String label, String imageURL, int width, int height) {
		VerticalPanel itemPanel = new VerticalPanel();
		HTML itemIcon = new HTML();
		Label itemLabel = new Label(label);
		
		itemPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		itemIcon.getElement().getStyle().setBackgroundImage("url('" + imageURL + "')");
		itemIcon.getElement().getStyle().setProperty("backgroundRepeat", "no-repeat");
		itemIcon.setSize(width + "px", height + "px");

		itemLabel.getElement().getStyle().setFontSize(14, Unit.PX);
		itemLabel.getElement().getStyle().setColor("#ffffff");
		itemLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);		
		
		itemPanel.add(itemIcon);
		itemPanel.setCellWidth(itemIcon, "45px");		
		itemPanel.add(itemLabel);
		
		itemPanel.getElement().getStyle().setCursor(Cursor.POINTER);
		
		itemPanel.addDomHandler(this, ClickEvent.getType());
		
		return itemPanel;
	}
	
	private void resizeLarge() {
//		setSize((Window.getClientWidth() - 80) + "px", "65px");
//		hidePanel.getElement().getStyle().setLeft(Window.getClientWidth() - 140, Unit.PX);
		
//		setSize("1030px", "65px");
		setSize("930px", "65px");
		this.getElement().getStyle().setOpacity(1);
	}
	
	private void initHandlers() {
		logoDiv.addClickHandler(this);

//		loginLabel.addClickHandler(this);
//		signupLabel.addClickHandler(this);
		
		shareButton.addClickHandler(this);
		tutorialButton.addClickHandler(this);
		loginButton.addClickHandler(this);
		templateButton.addClickHandler(this);
		emailButton.addClickHandler(this);
		
//		Window.addResizeHandler(new ResizeHandler() {
//			@Override
//			public void onResize(ResizeEvent event) {
//				if (hidePanel.isVisible()) {
//					resizeLarge();
//				}
//			}
//		});
	}
	
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();

		if (sender instanceof Anchor) {
			trackEvent("Click", ((Anchor) sender).getText());
			
			if (sender == loginButton /*sender == loginLabel*/) {
				String param = "";
				if (ViewerEntryPoint.getId() != null && !ViewerEntryPoint.getId().isEmpty()) {
					if (ViewerEntryPoint.isPresentation())
						param = "#PRESENTATION_MANAGE/id=" + ViewerEntryPoint.getId() + "/company=" + ViewerDataController.getItemCompanyId();
					else 
						param = "#SCHEDULE_MANAGE/id=" + ViewerEntryPoint.getId() + "/company=" + ViewerDataController.getItemCompanyId();
				}
//				Window.open(Global.RVA_APP_URL + "?parentId=" + ViewerDataProvider.getItemCompanyId() + param, "_blank", "");
				
				LoginHelpWidget.getInstance().show(Global.RVA_APP_URL + "?parentId=" + ViewerDataController.getItemCompanyId() + param);
			}
			else if (sender == templateButton) {
				String param = "";
				if (ViewerEntryPoint.getId() != null && !ViewerEntryPoint.getId().isEmpty())
					param = "#PRESENTATION_MANAGE/id=" + ViewerEntryPoint.getId() + "/fromcompanyid=" + ViewerDataController.getItemCompanyId();
//				Window.open(Global.RVA_APP_URL + "?parentId=" + ViewerDataProvider.getItemCompanyId() + param, "_blank", "");
				
				LoginHelpWidget.getInstance().show(Global.RVA_APP_URL + "?parentId=" + ViewerDataController.getItemCompanyId() + param);
			}
//			else if (sender == signupLabel) {
//				Window.open("http://www.risevision.com/login", "_blank", "");
//			}
			else if (sender == emailButton) {
				String messageString = EMAIL_TEXT.replace("%1%", ViewerDataController.getItemName());
				messageString = messageString.replace("%2%", shortenedUrl);
				
				Window.open("mailto:?subject=Reach.Network Presentation&body=" + messageString, "_blank", "");
			}
			else if (sender == shareButton) {
				ViewerPreviewShareWidget.getInstance().show(shortenedUrl);
			}
			else if (sender == tutorialButton) {
				TutorialWidget.getInstance().show();
			}
		}
		else if (sender == hidePanel) {
			trackEvent("Hide", "");

			showElements(false);
			this.setSize("50px", "55px");
			this.getElement().getStyle().setOpacity(0.7);			
		}
		else if (sender == showPanel) {
			trackEvent("Show", "");

			showElements(true);
			resizeLarge();
		}
		else if (sender == logoDiv) {
			trackEvent("Click", "Logo");
			
			Window.open("http://reach.network/", "_blank", "");
		}
	}
	
	private void showElements(boolean show) {
		logoDiv.setVisible(show);
		nameLabel.setVisible(show);
		sharePanel.setVisible(show);
		bannerWidget.setVisible(show);

//		loginLabel.setVisible(show);
//		signupLabel.setVisible(show);
		
//		socialPanel.setVisible(show);
		
		hidePanel.setVisible(show);
		showPanel.setVisible(!show);
		
		if (!show) {
			ViewerPreviewShareWidget.getInstance().hide();
			TutorialWidget.getInstance().hide();
		}
	}
	
	public static ViewerPreviewWidget getInstance() {
		try {
			if (instance == null)
				instance = new ViewerPreviewWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	private void updateTutorialUrl() {
		String tutorialUrl = ViewerDataParser.getInstance().getTutorialUrl();
		if (RiseUtils.strIsNullOrEmpty(tutorialUrl)) {
			tutorialButton.setVisible(false);
		}
		else {
			TutorialWidget.getInstance().setVideoUrl(tutorialUrl);			
		}

	}

	public void show() {
		super.show();
		super.setPopupPosition(10, -10);
	
		getShortenedUrl();
		bannerWidget.renderItem();
	}
	
	private void trackEvent(String action, String label) {
		ViewerHtmlUtils.trackAnalyticsEvent("PreviewBar", action, label);
	}
	
//	private native void initFb() /*-{
//		try {
//			$wnd.FB.init({status: true, cookie: false, xfbml: true});
//		}
//		catch (e) {}
//	}-*/;
	
	private void setShortenedUrl(String shortenedUrl) {
		this.shortenedUrl = shortenedUrl;
		
//		((HTML) postButton).setHTML(LIKE_BUTTON_HTML.replace("%1%", URL.encode(shortenedUrl)));
//		initFb();
	}
	
	private void getShortenedUrl() {	
		String requestUrl = "http://api.bitly.com/v3/shorten?login=risevision&apiKey=R_e90a186204ec281a65f7f53ae375f28d&longUrl=";

		getBitlyUrl(requestUrl, getPreviewUrl());
	}
	
	public static String getPreviewUrl() {
		String url = Window.Location.getHref();
//		if (url.toLowerCase().contains(ViewerEntryPoint.PREVIEW)) {
//			url = "http://" + Window.Location.getHost() + "/Viewer.html?type=presentation&id=" + ViewerEntryPoint.getId();
//		}
		return url;
	}
	
	private native void getBitlyUrl(String requestUrl, String param) /*-{
		$wnd.startBitlyJSONCall(requestUrl, param);
	}-*/;
	
	private static void bitlyResponse(JavaScriptObject jsObject) {
		JSOModel jsModel = (JSOModel) jsObject;
		/*
		{
		    "status_code": 200, 
		    "data": {
		        "url": "http://bit.ly/cmeH01", 
		        "hash": "cmeH01", 
		        "global_hash": "1YKMfY", 
		        "long_url": "http://betaworks.com/", 
		        "new_hash": 0
		    }, 
		    "status_txt": "OK"
		}
		 */
		
		if (("200").equals(jsModel.get("status_code")) && jsModel.getObject("data") != null) {
			instance.setShortenedUrl(jsModel.getObject("data").get("url", ""));
		}
	}
}
