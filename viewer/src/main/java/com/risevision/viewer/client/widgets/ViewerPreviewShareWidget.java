// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.data.ViewerDataController;

public class ViewerPreviewShareWidget extends PopupPanel {
	private static final String EMBED_LINK_CODE = "<a href='%1%'>%2%</a>";	
	private static final String EMBED_HTML_CODE = "<iframe allowtransparency='true' frameborder='0' scrolling='no' " +
			"src='%1%' style='width:%2%px; height:%3%px;'></iframe>";	
	
	private static ViewerPreviewShareWidget instance;
	
	private AbsolutePanel contentPanel = new AbsolutePanel();
	private VerticalPanel sharePanel = new VerticalPanel();
	private Label titleLabel = new Label("Share This Presentation");
	private Label urlLabel = new Label("Share URL:");
	private Label urlDescription = new Label("Copy and paste this URL to share this presentation with " +
			"viewers. For example paste into email or instant messages.");
	private TextArea urlTextBox = new TextArea();
	private Label embedLinkLabel = new Label("Embed Link:");
	private Label embedLinkDescription = new Label("Copy and paste this HTML to embed a link to this " +
			"presentation. For example embed this link in a web page or blog.");
	private TextArea embedLinkTextBox = new TextArea();
	private Label embedLabel = new Label("Embed on your page:");
	private Label embedDescription = new Label("HTML to embed this presentation on your website or blog.");
	private TextArea embedTextBox = new TextArea();
	private HTML closePanel = new HTML("<span style='cursor:pointer;font-size:26px;'>&times;</span>");
	
	public ViewerPreviewShareWidget() {
		super(true, false);
		
		add(contentPanel);
		
		sharePanel.add(titleLabel);
		
		sharePanel.add(new HTML("<span style='line-height:16px;'>&nbsp;</span>"));

		sharePanel.add(urlLabel);
		sharePanel.add(urlDescription);
		sharePanel.add(urlTextBox);

		sharePanel.add(new HTML("<span style='line-height:8px;'>&nbsp;</span>"));

		sharePanel.add(embedLinkLabel);
		sharePanel.add(embedLinkDescription);
		sharePanel.add(embedLinkTextBox);
		
		sharePanel.add(new HTML("<span style='line-height:8px;'>&nbsp;</span>"));

		sharePanel.add(embedLabel);
		sharePanel.add(embedDescription);
		sharePanel.add(embedTextBox);
		
		contentPanel.add(sharePanel, 0, 7);
		contentPanel.add(closePanel, 570, -17);
				
		styleControls();
		initActions();
	}
	
	private void styleControls() {
		contentPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		sharePanel.getElement().getStyle().setPadding(10, Unit.PX);
		sharePanel.setSize("578px", "430px");
		sharePanel.addStyleName("inner-border");
		sharePanel.addStyleName("gradient-overlay-middle");
		
		titleLabel.getElement().getStyle().setFontSize(16, Unit.PX);
		titleLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);

		urlLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		embedLinkLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		embedLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		
		urlTextBox.setSize("420px", "40px");
		embedLinkTextBox.setSize("420px", "60px");
		embedTextBox.setSize("420px", "60px");

		setSize("580px", "440px");
		
		addStyleName("rounded-border");
		addStyleName("gradient-overlay-up");

		getElement().getStyle().setProperty("zIndex", "999");

	}
	
	private void initActions() {
		closePanel.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
	
	public static ViewerPreviewShareWidget getInstance() {
		try {
			if (instance == null)
				instance = new ViewerPreviewShareWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	public void show(String url) {
		super.show();
		super.setPopupPosition((int)(Window.getClientWidth()/2 - 300), 100);
		
		urlTextBox.setText(url);
		urlTextBox.setFocus(true);
		urlTextBox.selectAll();
		
		String embedCode = EMBED_HTML_CODE.replace("%1%", ViewerPreviewWidget.getPreviewUrl() + "&showui=false");
		embedCode = embedCode.replace("%2%", ViewerEntryPoint.getPresentationWidth() + "");
		embedCode = embedCode.replace("%3%", ViewerEntryPoint.getPresentationHeight() + "");
		
		embedTextBox.setText(embedCode);
		
		String linkCode = EMBED_LINK_CODE.replace("%1%", ViewerPreviewWidget.getPreviewUrl() + "&showui=false");
		linkCode = linkCode.replace("%2%", ViewerDataController.getItemName());
		
		embedLinkTextBox.setText(linkCode);
	}
	
}
