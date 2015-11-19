// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.risevision.common.client.json.JSOModel;
import com.risevision.viewer.client.info.Global;
import com.risevision.viewer.client.widgets.oem.EnterClaimIdWidget;
import com.risevision.viewer.client.widgets.oem.EnterDisplayIdWidget;

public class DisplayRegistrationController {

	private static final String MODE_REGISTER = "REGISTER";
	private static final String MODE_VALIDATE = "VALIDATE";
	
	public static void registerDisplay(String claimId, String displayName) {
		// API: /v2/viewer/display/{claimId}/register?callback={callbackName}&name={displayName}
		//      callbackName is mandatory, displayName is optional.
    	int viewerWidth = Window.getClientWidth();
    	int viewerHeight = Window.getClientHeight();

		String url = Global.DATA_SERVER_URL.replace("{0}", "display");
		url = url.replace("{1}", claimId);
		url += "/register?width=" + viewerWidth + 
		"&height=" + viewerHeight;
		if (displayName != null && !displayName.isEmpty()) {
			url += "&name=" + URL.encodeQueryString(displayName);
		}
		url += "&callback=?";
		callUrlNative(url, MODE_REGISTER);
	}

	public static void validateDisplayId(String displayId) {
		// reuse Viewer API "display" call
		String url = Global.DATA_SERVER_URL.replace("{0}", "display");
		url = url.replace("{1}", displayId);
		url += "?callback=?";
		callUrlNative(url, MODE_VALIDATE);
	}

	private static void processResponse(JavaScriptObject jso, String mode) {
		int errorCode;
		String newDisplayId;

		JSOModel obj = (JSOModel) jso;
		JSOModel status = obj.getObject("status");

		if (status != null && mode != null) {
			String statusMessage = status.get("message");
			errorCode = status.getInt("code", 0); //note, JSOModel.get() returns 0 as null
			if (mode.equals(MODE_REGISTER)) {
				newDisplayId = obj.get("displayId", "");
				EnterClaimIdWidget.getInstance(false).RegisterDisplayCallback(errorCode, statusMessage, newDisplayId);
			}
			else if (mode.equals(MODE_VALIDATE)) {
				EnterDisplayIdWidget.getInstance().ValidateDisplayIdCallback(errorCode, statusMessage);
			}
		}
	}
	
	private static native void callUrlNative(String url, String mode) /*-{
	    try {
	    	$wnd.writeToLog("Register Display request - start");
	    	
			$wnd.$.getJSON(url,
				{format: 'json'},
				function(result) {
		    	    try { 
	//	    	    	debugger;
		    	    	
			        	$wnd.writeToLog("Register Display response - active");
		    	    	//http://stackoverflow.com/questions/6257532/gwt-jsni-boolean
		    	    	@com.risevision.viewer.client.data.DisplayRegistrationController::processResponse(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(result,mode);
		    	    }
		    	    catch (err) {
		    	    	$wnd.writeToLog("Register Display request failed - " + url + " - " + err.message);
		    	    }
				}
			);
	    }
	    catch (err) {
	    	$wnd.writeToLog("Register Display request error - " + url + " - " + err.message);
	    }
	}-*/;

}
