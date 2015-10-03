// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.viewer.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.risevision.common.client.utils.HtmlParser;
import com.risevision.common.client.utils.RiseUtils;

@SuppressWarnings("serial")
public class MakeRequestServlet extends HttpServlet {
	private static final String OAUTH_REQUEST_TOKEN = "OAUTH_REQUEST_TOKEN";
	private static final String OAUTH_REQUEST_TOKEN_SECRET = "OAUTH_REQUEST_TOKEN_SECRET";
	
	private static final String TWITTER_CONSUMER_KEY  = "xx";
	private static final String TWITTER_CONSUMER_SECRET = "xx";
	
	private static final Logger log = Logger.getAnonymousLogger();
	
	private class RequestResponse {
		int responseCode = 200;
		String responseBody;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// example: ?url=http%3A%2F%2F4d3e117097a8a0a6c64f-c5d7b94595fac0975958bfe464d37659.r77.cf2.rackcdn.com%2FCurrent%2FCurrent-Landscape.xml&httpMethod=GET&headers=&postData=&authz=&st=&contentType=DOM&numEntries=3&getSummaries=false&signOwner=true&signViewer=true&gadget=undefined&container=default&bypassSpecCache=&getFullHeaders=false  
		String requestUrl = request.getParameter("url");
		String dataHash = "";
		
		String responseString = "throw 1; < don't be evil' >" +
//				"{\"%requestURL%\":" +
//				"{\"body\":\"%responseBody%\"," +
//				"\"DataHash\":\"%dataHash%\"," +
//				"\"rc\":%responseCode%}}" +
				"";
		
		RequestResponse responseObject = new RequestResponse();
		try {
			responseObject = makeRequest(request);
			
//			responseBody = StringEscapeUtils.escapeJavaScript(responseBody);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			responseString = responseString + 
								new JSONStringer()
									.object()
										.key(requestUrl)
										.object()
											.key("body")
											.value(responseObject.responseBody)
											.key("DataHash")
											.value(dataHash)
											.key("rc")
											.value(responseObject.responseCode)
										.endObject()
									.endObject()
									.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		responseString = responseString.replace("%requestURL%", requestUrl)
//									.replace("%responseBody%", responseBody)
//									.replace("%dataHash%", dataHash)
//									.replace("%responseCode%", responseCode);

		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().println(responseString);
//		response.getOutputStream().println(responseString);
	}
	

	public RequestResponse makeRequest(HttpServletRequest request) throws Exception {
		RequestResponse response = new RequestResponse();

		String requestUrl = request.getParameter("url");
		if ((requestUrl != null) && (!requestUrl.isEmpty())) {
			try { 
				URL payload_url = new URL(requestUrl.replace(" ", "+"));
				
				HttpURLConnection urlConnection = (HttpURLConnection) payload_url.openConnection();
				
				urlConnection.setReadTimeout(20000);
				urlConnection.setRequestMethod("GET");
						
				if ("oauth".equals(request.getParameter("authz")) && requestUrl.contains("api.twitter.com/1.1")) {
					signTwitterRequest(request, urlConnection);
				}
				
    	        response.responseBody = getResponseAsJson(urlConnection);
    	        response.responseCode = urlConnection.getResponseCode();
	    	        
	        } catch (MalformedURLException e) { 
				e.printStackTrace();
	        	throw new Exception("Malformed URL, " + e.getMessage());
	        } catch (Exception e) {
//				e.printStackTrace();
	        	log.log(Level.SEVERE, e.getMessage(), e);
//	        	log.severe("Exception - " + e.getMessage(), e);
	        }
		}
		return response;
	}

	public void signTwitterRequest(HttpServletRequest request, HttpURLConnection urlConnection) {
		String userToken = request.getParameter(OAUTH_REQUEST_TOKEN);
		String userTokenSecret = request.getParameter(OAUTH_REQUEST_TOKEN_SECRET);
		
		OAuthConsumer consumer = new DefaultOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
		
		consumer.setTokenWithSecret(userToken, userTokenSecret);
		
        // sign the request
        try {
			consumer.sign(urlConnection);
		} catch (OAuthMessageSignerException e) {
//			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
//			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
//			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	// [AD] Removes BOM (encoded as the first 3 characters in the doc) if present
	// Source: http://stackoverflow.com/questions/9736999/how-to-remove-bom-from-an-xml-file-in-java
//	private static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
//		PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
//	    byte[] bom = new byte[3];
//	    if (pushbackInputStream.read(bom) != -1) {
//	    	if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
//	        	pushbackInputStream.unread(bom);
//	        }
//	    }
//	    return pushbackInputStream; 
//	}

	private static String getResponseAsJson(HttpURLConnection urlConnection) {
		String result = null;
		
//		InputStream is = checkForUtf8BOMAndDiscardIfAny(urlConnection.getInputStream());
//		String charset = getCharsetType(urlConnection);
		
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset)); 
//        String line = new String(bytes, charsetName); 
//
//        while ((line = new String(reader.readLine(), "ISO-8859-1")) != null) { 
//        	result += line + "\n";
//        } 
//        reader.close(); 
		
		String charset = getCharsetType(urlConnection);
		
//		byte[] bytes = IOUtils.toByteArray(urlConnection.getInputStream());
		byte[] bytes = null;
		
		try {
			bytes = inputStreamToByteArray(urlConnection.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (RiseUtils.strIsNullOrEmpty(charset)) {
			charset = getEncodingType(bytes);
			
			if (RiseUtils.strIsNullOrEmpty(charset)) {
				charset = "UTF-8";
			}
		}
		
		Charset encoding = Charset.forName(charset);
		
		if (result == null) {
			result = encoding.decode(ByteBuffer.wrap(bytes)).toString();

			// Strip BOM if present.
			if (result.length() > 0
					&& result.codePointAt(0) == 0xFEFF) {
				result = result.substring(1);
			}
		}
		
		return result;
	}
	
	private static String getCharsetType(HttpURLConnection connection) {
		String contentType = connection.getContentType();
		String[] values = contentType.split(";"); //The values.length must be equal to 2...
		String charset = "";

		for (String value : values) {
		    value = value.trim();

		    if (value.toLowerCase().startsWith("charset=")) {
		        charset = value.substring("charset=".length()).toUpperCase();
		        
				// Some servers include quotes around the charset:
				// Content-Type: text/html; charset="UTF-8"
				if (charset.length() >= 2 && charset.startsWith("\"") && charset.endsWith("\"")) {
					charset = charset.substring(1, charset.length() - 1);
				}
		    }
		}

		return charset;
	}
	
	private static String getEncodingType(byte[] bytes) {
		String header = "";
		int start = -1, end = -1;

		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == '<') {
				start = i;
			}
			else if (start != -1) {
				if (bytes[i] == '>') {
					end = i;
					break;
				}
			}
		}
		
		header = new String(Arrays.copyOfRange(bytes, start, end));
		if (header.contains("?xml")) {
			return HtmlParser.getPropertyValue(header, "encoding");
		}

		return "";
	}
	
	private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}
	
}
