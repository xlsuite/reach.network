// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JsArrayString;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.viewer.client.ViewerEntryPoint;
import com.risevision.viewer.client.controller.ViewerControllerInterface;
import com.risevision.viewer.client.controller.ViewerGadgetController;
import com.risevision.viewer.client.data.ViewerDataController;
import com.risevision.viewer.client.data.ViewerDataParser;
import com.risevision.viewer.client.info.Global;

public class ViewerHtmlUtils {
	public static final String PARAM_DISPLAY_ID = "displayId";
	public static final String PARAM_COMPANY_ID = "companyId";
	public static final String PARAM_DISPLAY_ADDRESS = "displayAddress";
	public static final String PARAM_COMPANY_AUTH_KEY = "companyAuthKey";
	public static final String PARAM_SOCIAL_CONNECTION = "social";
	public static final String PARAM_GADGET_ADDITIONAL_PARAMS = "additionalParams";
	
	private static Map<String, ViewerControllerInterface> gadgetMap = new HashMap<String, ViewerControllerInterface>();
	
//	private static Map<String, ViewerPresentationController> presentationMap = new HashMap<String, ViewerPresentationController>();
	
//	public static void registerPresentation(String htmlName, ViewerPresentationController presentationController) {
//		presentationMap.put(htmlName, presentationController);
//	}
		
	public static void registerGadget(String htmlName, ViewerControllerInterface gadgetController) {
		gadgetMap.put(htmlName, gadgetController);
	}
	
//	public static int reportPresentationLoadEvent(String presFrame) {
//		if (!RiseUtils.strIsNullOrEmpty(presFrame) && presentationMap.get(presFrame) != null) {
//			presentationMap.get(presFrame).onPresentationLoad();
//			
//			return 1;
//		}
//		return 0;
//	}
	
	public static int reportGadgetLoadEvent(String presFrame, String id, String gadgetXml) {
		if (!RiseUtils.strIsNullOrEmpty(id) && gadgetMap.get(id) != null && gadgetMap.get(id) instanceof ViewerGadgetController) {
			((ViewerGadgetController) gadgetMap.get(id)).setLoad(presFrame, gadgetXml);
			return 1;
		}
		return 0;
	}
	
	public static int reportReadyEvent(String presFrame, String id, boolean canPlay, boolean canStop, boolean canPause, boolean canReportReady, boolean canReportDone) { 
		if (!RiseUtils.strIsNullOrEmpty(id) && gadgetMap.get(id) != null) {
			gadgetMap.get(id).setReady(presFrame, canPlay, canStop, canPause, canReportReady, canReportDone);
			return 1;
		}
		return 0;
	}
	
	public static int reportErrorEvent(String presFrame, String id, String reason) { 
		if (id != null && !id.isEmpty() && gadgetMap.get(id) != null) {
			gadgetMap.get(id).setError(presFrame, reason);
			return 1;
		}
		return 0;
	}
	
	public static int reportDoneEvent(String presFrame, String id) { 
		if (id != null && !id.isEmpty() && gadgetMap.get(id) != null) {
			gadgetMap.get(id).setDone();
			return 1;
		}
		return 0;
	}
	
	public static String getParam(String param, String id) {
		if (param != null && !param.isEmpty()) {
			if (param.toLowerCase().equalsIgnoreCase(PARAM_DISPLAY_ADDRESS)) {
				return ViewerDataParser.getInstance().getDisplayAddress();
			}
			else if (param.equalsIgnoreCase(PARAM_DISPLAY_ID)) {
				return ViewerEntryPoint.getDisplayId();
			}
			else if (param.equalsIgnoreCase(PARAM_COMPANY_ID)) {
				return ViewerDataController.getItemCompanyId();
			}
			else if (param.equalsIgnoreCase(PARAM_COMPANY_AUTH_KEY)) {
				return ViewerDataParser.getInstance().getAuthKey();
			}
			else if (param.toLowerCase().contains(PARAM_SOCIAL_CONNECTION)) {
				String[] s = param.split(":");
				if (s.length == 2) {
					return ViewerDataParser.getInstance().getSocialConnection(s[1]);
				}
			}
			else if (param.equalsIgnoreCase(PARAM_GADGET_ADDITIONAL_PARAMS)) {
				if (id != null && !id.isEmpty() && gadgetMap.get(id) != null && gadgetMap.get(id).getItem() != null) {
					return gadgetMap.get(id).getItem().getAdditionalParams();
				}
				return null;
			}
		}
		// marginal case; return nothing
		return "";
	}
	
//	private static native void setParam(String presFrame, String id, String name, String value) /*-{
//		try {
//			$wnd.setParam(presFrame, id, name, value);
//		} catch (err) {$wnd.writeToLog("itemParamSet - " + err);}
//	}-*/;
	
	private static String requestPlaceholderIFrameIds(String presFrame, String id) {
		String result = "";
		if (id != null && !id.isEmpty() 
				&& presFrame != null && !presFrame.isEmpty() && presFrame.contains("iFrame_")) {
			String presFrameName = presFrame.substring("iFrame_".length(), presFrame.length()) + "_" + id;
		
			for (String gadgetName: gadgetMap.keySet()) {
				if (gadgetName.substring(0, gadgetName.lastIndexOf("_")).equals(presFrameName)) {
					result += "if_" + gadgetName + ",";
				}	
			}
			
			if (!result.isEmpty())
				result = result.substring(0, result.length() - 1);
		}
	
		return result;
	}

	public static native void exportStaticMethods() /*-{
//		$wnd.reportPresentationLoadEvent =
//		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::reportPresentationLoadEvent(Ljava/lang/String;));
				
		$wnd.reportGadgetLoadEvent =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::reportGadgetLoadEvent(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
				
		$wnd.reportReadyEvent =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::reportReadyEvent(Ljava/lang/String;Ljava/lang/String;ZZZZZ));
		
		$wnd.reportErrorEvent =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::reportErrorEvent(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
		
		$wnd.reportDoneEvent =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::reportDoneEvent(Ljava/lang/String;Ljava/lang/String;));
		
		$wnd.getParam =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::getParam(Ljava/lang/String;Ljava/lang/String;));

		$wnd.requestPlaceholderIFrameIds =
		$entry(@com.risevision.viewer.client.utils.ViewerHtmlUtils::requestPlaceholderIFrameIds(Ljava/lang/String;Ljava/lang/String;));
	
//		$wnd.reportDataReady =
//		$entry(@com.risevision.viewer.client.data.ViewerDataProvider::reportDataReady(Lcom/google/gwt/core/client/JavaScriptObject;));
		
//		$wnd.setPreviewData =
//		$entry(@com.risevision.viewer.client.data.ViewerDataProvider::retrievePreviewData(Lcom/google/gwt/core/client/JavaScriptObject;));
		
		$wnd.getEmbedItemData =
		$entry(@com.risevision.viewer.client.data.ViewerDataController::getEmbedData(Ljava/lang/String;Ljava/lang/String;));

		$wnd.embedPlay =
		$entry(@com.risevision.viewer.client.ViewerEntryPoint::embedPlay());
		
		$wnd.embedPause =
		$entry(@com.risevision.viewer.client.ViewerEntryPoint::embedPause());
		
		$wnd.embedStop =
		$entry(@com.risevision.viewer.client.ViewerEntryPoint::embedStop());
		
//		$wnd.bitlyResponse =
//		$entry(@com.risevision.viewer.client.widgets.ViewerPreviewShareWidget::bitlyResponse(Lcom/google/gwt/core/client/JavaScriptObject;));
		
		$wnd.bitlyResponse =
		$entry(@com.risevision.viewer.client.widgets.ViewerPreviewWidget::bitlyResponse(Lcom/google/gwt/core/client/JavaScriptObject;));
		
		$wnd.channelMessage = 
		$entry(@com.risevision.viewer.client.channel.ChannelConnectionController::setChannelMessage(Ljava/lang/String;));
		
		$wnd.channelError = 
		$entry(@com.risevision.viewer.client.channel.ChannelConnectionController::setChannelError(ILjava/lang/String;));
	}-*/;

	public static native void playCommand(String presFrame, String htmlName, boolean show) /*-{
		try {
			$wnd.playCmd(presFrame, htmlName);
			
			if (show) {
				$wnd.document.getElementById(presFrame).contentWindow.showElement(htmlName);
			}
		} catch (err) {$wnd.writeToLog("Play Error - " + htmlName + " - " + err);}
	}-*/;
	
	public static native void stopCommand(String presFrame, String htmlName, boolean hide) /*-{
		try {
			$wnd.stopCmd(presFrame, htmlName);
			
			if (hide) {
				$wnd.document.getElementById(presFrame).contentWindow.hideElement(htmlName);
			}
		} catch (err) {$wnd.writeToLog("Stop Error - " + htmlName + " - " + err);}
	}-*/;
	
	public static native void pauseCommand(String presFrame, String htmlName, boolean hide) /*-{
		try {
			$wnd.pauseCmd(presFrame, htmlName);
			
			if (hide) {
				$wnd.document.getElementById(presFrame).contentWindow.hideElement(htmlName);
			}
		} catch (err) {$wnd.writeToLog("Pause Error - " + htmlName + " - " + err);}
	}-*/;
	
//	public static native void showGadget(String presFrame, String htmlName, boolean show) /*-{
//		try {
//			if (show) {
//				$wnd.document.getElementById(presFrame).contentWindow.showElement(htmlName);
//			}
//			else {
//				$wnd.document.getElementById(presFrame).contentWindow.hideElement(htmlName);
//			}
//		} catch (err) {$wnd.writeToLog("showGagdet - " + htmlName + " - " + err);}
//	}-*/;
	
	public static native void embedReady() /*-{
		try {
			$wnd.embedReady();
		} catch (err) {$wnd.writeToLog("Embed Ready Error - " + err);}
	}-*/;
	
	public static native void embedDone() /*-{
		try {
			$wnd.embedDone();
		} catch (err) {$wnd.writeToLog("Embed Done Error - " + err);}
	}-*/;
	
	public static native void addUrl(String elementName, String containerName, int width, int height, int top, int left, String contentUrl) /*-{
		try {
			$wnd.createURL(elementName, containerName,
								width, height,
		                        top, left,
		                        0, 0,
		                        contentUrl);
		} catch (err) {$wnd.writeToLog("error");}
	}-*/;
	
	public static native void addPresentation(String elementName, String containerName, int width, int height, int top, int left, String contentHtml, String presentationWidth, String presentationHeight, boolean hidePointer, boolean enableScroll) /*-{
		try {
			$wnd.createPresentation(elementName, containerName,
		                        width, height,
		                        top, left,
		                        contentHtml, 
		                        presentationWidth, presentationHeight,
		                        hidePointer, enableScroll);
		} catch (err) {$wnd.writeToLog("error");}
	}-*/;
	
//	public static native void createContainer(String containerName, int width, int height, int top, int left) /*-{
//		try {
//			$wnd.createContainer(containerName,
//		                        width, height,
//		                        top, left);
//		} catch (err) {}
//	}-*/;
	
	public static native void createContainer(String containerName) /*-{
		try {
			$wnd.createContainer(containerName);
		} catch (err) {}
	}-*/;

	public static native void showElement(String elementName, boolean show) /*-{
		try {
			if (show) {
				$wnd.showElement(elementName);
			}
			else {
				$wnd.hideElement(elementName);
			}
		} catch (err) {}
	}-*/;
	
	public static native void showFrameElement(String presFrame, String elementName, boolean show) /*-{
		try {
			$wnd.showFrameElement(presFrame, elementName, show);
		} catch (err) {$wnd.writeToLog("showFrameElement - " + elementName + " - " + err);}
	}-*/;

	public static native void addGadgetHtml(String presFrame, String frameName, String html) /*-{
		try {
			$wnd.document.getElementById(presFrame).contentWindow.addGadgetHtml(frameName, html);			
		} catch (err) {$wnd.writeToLog("addGadgetHtml - " + frameName + " - " + err);}
	}-*/;
	
	public static void destroyContainer(String elementName) {
		destroyElement(elementName, "mainDiv");
	}
	
	public static native void destroyElement(String elementName, String containerName) /*-{
		try {
			$wnd.destroyElement(elementName, containerName);
		} catch (err) {}
	}-*/;
	
	public static native void destroyFrameElement(String presFrame, String elementName, String containerName) /*-{
		try {
			$wnd.destroyFrameElement(presFrame, elementName, containerName);
		} catch (err) {$wnd.writeToLog("destroyFrameElement - " + htmlName + " - " + err);}
	}-*/;
	
	public static native void setBackground(String elementName, String color) /*-{
		try {
			$wnd.setBackground(elementName, color);
		} catch (err) {}
	}-*/;

	public static native void resizeContainer(String elementName, int width, int height) /*-{
		try {
			$wnd.resizeContainer(elementName, width, height);
		} catch (err) {}
	}-*/;	
	
	public static native boolean isChrome() /*-{
	try {
			return $wnd.navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
		} catch (err) {}
	}-*/;	
	
	public static native JsArrayString getBrowserVersion() /*-{
	try {
//			debugger;
			// from http://stackoverflow.com/questions/5916900/detect-version-of-browser
		    var N= $wnd.navigator.appName, ua= $wnd.navigator.userAgent, tem;
		    
    		var M= ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
    		if(M && (tem= ua.match(/version\/([\.\d]+)/i))!= null) M[2]= tem[1];
    		M= M? [M[1], M[2]]: [N, navigator.appVersion, '-?'];

    		//return M[0] + " " + M[1];
    		return M;
    		
		} catch (err) {}
	}-*/;	
	
	public static void initAnalytics() {
		if (ViewerEntryPoint.isPreview()) {
			initAnalyticsNative(Global.PREVIEW_TRACKER_ID);
		}
		else if (ViewerEntryPoint.isDisplay() && !ViewerEntryPoint.isEmbed()) {
			initAnalyticsNative(Global.DISPLAY_TRACKER_ID);
		}
		
	}
	
	public static native void initAnalyticsNative(String trackerId) /*-{
	try {
			$wnd.initTracker(trackerId);
		} catch (err) {}
	}-*/;	
	
	public static native void trackAnalyticsEvent(String eventName, String eventAction, String eventLabel) /*-{
	try {
			$wnd.trackEvent(eventName, eventAction, eventLabel);
		} catch (err) {}
	}-*/;
	
	public static void logExternalMessage(String eventName, String eventDetails) {
		logExternalMessageNative(eventName, ViewerEntryPoint.getDisplayId(), Global.VIEWER_VERSION, eventDetails);
	}
	
	private static native void logExternalMessageNative(String eventName, String displayId, String version, String eventDetails) /*-{
		try {	
			$wnd.logExternal(eventName, displayId, version, eventDetails);
		} catch (err) {}
	}-*/;	
}
