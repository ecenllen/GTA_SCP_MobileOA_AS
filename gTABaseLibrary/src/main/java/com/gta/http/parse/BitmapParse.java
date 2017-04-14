package com.gta.http.parse;

import java.io.IOException;

import org.apache.http.HttpResponse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gta.http.RequestInfo;
import com.gta.http.ResponseInfo;

public class BitmapParse implements ResponseParse {

	@Override
	public ResponseInfo parseResponse(RequestInfo requestInfo,
			ResponseInfo responseInfo) {
		HttpResponse response=responseInfo.httpResponse;
		try {
			Bitmap bm = BitmapFactory.decodeStream(response.getEntity().getContent());
			responseInfo.Entity=bm;					
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return responseInfo;
	}

}
