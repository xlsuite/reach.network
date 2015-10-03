// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.data;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class GadgetScriptController {
	private static String DEFAULT_GADGET_SCRIPT = "" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/globals.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/base.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/string.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/urlparams.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/config.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/auth.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/auth-init.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/json-native.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/io.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/onload.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/prefs.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/log.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/wpm.transport.js\"></script>" +
//			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/rpc.js\"></script>" +
			"<script type=\"text/javascript\" language=\"javascript\" src=\"/gadgets/gadgets.min.js\"></script>" +
			"";
	
	private static String GADGET_SCRIPT_TAG = "" +
			"<script type=\"text/javascript\" language=\"javascript\">" +
			"%gadgetScript%" +
			"</script>" +
			"";
	
	private static String gadgetScript = DEFAULT_GADGET_SCRIPT;
	
	public static void retrieveScript() {		
			try {
				new RequestBuilder(RequestBuilder.GET, "gadgets/gadgets.min.js").sendRequest("", new RequestCallback() {
					  @Override
					  public void onResponseReceived(Request req, Response resp) {
						  gadgetScript = GADGET_SCRIPT_TAG.replace("%gadgetScript%", resp.getText());
					  }

					  @Override
					  public void onError(Request res, Throwable throwable) {
					    // handle errors
					  }
					});
			} catch (RequestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static String getGadgetScript() {
		return gadgetScript;
	}
}
