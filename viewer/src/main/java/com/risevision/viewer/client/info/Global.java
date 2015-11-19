// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.client.info;

import java.util.Date;

public class Global {
  public static final String ENV = "prod";
  
  public static final String VIEWER_VERSION = "1-06-07";
  public static final String GADGET_SERVER_URL = "http://www-open-opensocial.googleusercontent.com/gadgets/ifr";
  public static final String SERVER_URL;
  public static final String DATA_SERVER_URL;
  public static final String CHANNEL_SERVER_URL;
  public static final String RVA_APP_URL;
  public static final String PREVIEW_TRACKER_ID;
  public static final String DISPLAY_TRACKER_ID;
	
  public static String VIEWER_UNIQUE_ID;
  public static String VIEWER_URL_IDENTIFIER;

  static {
    if(ENV.equals("prod")) {
      SERVER_URL = "https://rvaserver2.appspot.com";
      RVA_APP_URL = "http://rva.risevision.com";
      PREVIEW_TRACKER_ID = "UA-82239-28";
      DISPLAY_TRACKER_ID = "UA-82239-32";
    }
    else {
      SERVER_URL = "https://rvacore-test.appspot.com";
      RVA_APP_URL = "http://rva-test.appspot.com";
      PREVIEW_TRACKER_ID = "UA-82239-27";
      DISPLAY_TRACKER_ID = "UA-82239-31";
    }
    
    DATA_SERVER_URL = SERVER_URL + "/v2/viewer/{0}/{1}";
    CHANNEL_SERVER_URL = SERVER_URL + "/v2/viewer/display/";
    
    VIEWER_UNIQUE_ID = Integer.toString((int) (Math.random() * 10000)) + "_" + new Date().getTime();    
    VIEWER_URL_IDENTIFIER = "uid=" + VIEWER_UNIQUE_ID + "&vv=" + VIEWER_VERSION;
  }
}
