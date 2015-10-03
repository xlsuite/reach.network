// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets.oem;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class HelpFrameWidget extends PopupPanel {
		
	private static HelpFrameWidget instance;
	private AbsolutePanel outerPanel = new AbsolutePanel();
	private HTML closePanel = new HTML("<span style='cursor:pointer;font-size:26px;'>&times;</span>");
	private Frame helpFrame = new Frame("http://help.risevision.com/#/user/player/register-player");  /*TODO (Reach.Network): change help URL*/
	private int w;
	private int h;
	
	public HelpFrameWidget() {
		super(true, false);

		w = Window.getClientWidth() - 40;
		h = Window.getClientHeight() - 40;
		
		styleControls();

		add(outerPanel);		
		
		outerPanel.add(helpFrame, 0, 7);
		outerPanel.add(closePanel, w-10, -17);
				
		initActions();
	}
	
	private void styleControls() {
		
		outerPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		helpFrame.setSize(w + "px", h + "px");
		
		setSize((w + 2) + "px", (h + 10) + "px");
		
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
	
	public static HelpFrameWidget getInstance() {
		try {
			if (instance == null)
				instance = new HelpFrameWidget();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	public void show() {
		super.show();
		super.setPopupPosition(10, 10);
	}
	
}
