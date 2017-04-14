package com.gta.http;

import java.io.IOException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

public class DefaultRetryHandler implements HttpRequestRetryHandler {

	@Override
	public boolean retryRequest(IOException exception, int retriedTimes,
			HttpContext context) {
		// retry a max of 5 times
		if (retriedTimes >= 3) {
			return false;
		}
		if (exception instanceof NoHttpResponseException) {
			return true;
		} else if (exception instanceof ClientProtocolException) {
			return true;
		}
		return false;
	}

}
