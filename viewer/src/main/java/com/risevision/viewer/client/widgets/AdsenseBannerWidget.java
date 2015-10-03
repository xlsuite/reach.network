// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.widgets;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;
import com.risevision.common.client.utils.RiseUtils;

public class AdsenseBannerWidget extends SimplePanel {
	private static AdsenseBannerWidget instance;
	
	private final int BANNER_WIDTH = 468;
	private final int BANNER_HEIGHT = 60;
	private final String RISE_BANNER_ID = "ca-pub-2013654478569194";
	private final String RISE_BANNER_SLOT = "RVA_Preview";
	private final String BANNER_HTML = "" +
			"<html>" +
			"<head>" +
			"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
			"<title></title>" +
			"<script type='text/javascript' src='http://partner.googleadservices.com/gampad/google_service.js'></script>" +
			"<script type='text/javascript'>" +
			"GS_googleAddAdSenseService('%s1');" +
			"GS_googleEnableAllServices();" +
			"</script>" +
			"<script type='text/javascript'>" +
			"GA_googleAddSlot('%s1', '%s2');" +
			"</script>" +
			"<script type='text/javascript'>" +
			"GA_googleFetchAds();" +
			"</script>" +
			"</head>" +
			"<body style=\"margin:0px;\">" +
			"<div>" +
			"<script type='text/javascript'>" +
			"GA_googleFillSlot('%s2');" +
			"</script>" +
			"</div>" +
			"</body>" +
			"</html>";
	
	public static final String BANNER_URL = "bannerURL";
	public static final String BANNER_TARGET_URL = "bannerTargetURL";
	public static final String ADSENSE_SERVICE_ID = "adsenseServiceId";
	public static final String ADSENSE_SERVICE_SLOT = "adsenseServiceSlot";
	
	private Frame bannerFrame = new Frame();
	
	private String bannerUrl;
	private String bannerTargetUrl;
	private String adsenseServiceId;
	private String adsenseServiceSlot;

//	private final String RISE_BANNER_URL = "http://test.risevision.com/rise-app-banner/";
	
	public AdsenseBannerWidget() {
		setSize(BANNER_WIDTH + "px", BANNER_HEIGHT + "px");
		
		bannerFrame.getElement().getStyle().setBorderWidth(0, Unit.PX);
		bannerFrame.getElement().setAttribute("scrolling", "no");
		bannerFrame.setSize(BANNER_WIDTH + "px", BANNER_HEIGHT + "px");
	}
	
	public static AdsenseBannerWidget getInstance() {
		if (instance == null) {
			instance = new AdsenseBannerWidget();
		}
		return instance;
	}
	
	public void init(String bannerUrl, String bannerTargetUrl, String adsenseServiceId, String adsenseServiceSlot) {
		this.bannerUrl = bannerUrl;
		this.bannerTargetUrl = bannerTargetUrl;
		this.adsenseServiceId = adsenseServiceId;
		this.adsenseServiceSlot = adsenseServiceSlot;
	}
	
//	public void renderItem() {
//		setUrl(RISE_BANNER_URL);
//	}
	
	public void renderItem() {
		clear();
		
		if (!RiseUtils.strIsNullOrEmpty(adsenseServiceId)) {
			add(bannerFrame);
			renderAdsenseBanner(adsenseServiceId, adsenseServiceSlot);
		}
		else {
			if (!RiseUtils.strIsNullOrEmpty(bannerUrl)) {
				Anchor bannerLink = new Anchor();
				bannerLink.setTarget("_blank");
				if (!RiseUtils.strIsNullOrEmpty(bannerTargetUrl))
					bannerLink.setHref(bannerTargetUrl);
	
				DivElement div = createImageDiv(bannerUrl, BANNER_WIDTH, BANNER_HEIGHT);
				bannerLink.getElement().appendChild(div);

				add(bannerLink);
			}
			else {
				add(bannerFrame);
				renderAdsenseBanner();
			}
		}
	}
	
	private DivElement createImageDiv(String imageUrl, int width, int height) {
		DivElement div = Document.get().createDivElement();	 
		div.getStyle().setPropertyPx("width", width);
		div.getStyle().setPropertyPx("height", height);
		
		div.getStyle().setProperty("backgroundImage", "url(" + imageUrl + ")");
		div.getStyle().setProperty("backgroundPosition", "0% 50%");
		div.getStyle().setProperty("backgroundSize", "auto");
		div.getStyle().setProperty("backgroundRepeat", "no-repeat"); 
		
		return div;
	}
	
	private void renderAdsenseBanner() {
		renderAdsenseBanner(RISE_BANNER_ID, RISE_BANNER_SLOT);
	}
	
	private void renderAdsenseBanner(String bannerId, String bannerSlot) {
		String htmlString = BANNER_HTML;
		htmlString = htmlString.replace("%s1", bannerId);
		htmlString = htmlString.replace("%s2", bannerSlot);
		
		writeHtml(bannerFrame.getElement(), htmlString);
	}
	
	public static native void writeHtml(Element myFrame, String html)  /*-{
//		try {
//			debugger; 
			
			var el = (myFrame.contentWindow) ? myFrame.contentWindow : (myFrame.contentDocument.document) ? myFrame.contentDocument.document : myFrame.contentDocument;
		    el.document.open();
		    el.document.write(html);
		    el.document.close();
//		} catch (err) {}
	}-*/;
}
