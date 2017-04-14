package com.gta.http.parse;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.gta.http.RequestInfo;
import com.gta.http.ResponseInfo;
import com.gta.util.Logg;

/**
 * String 解析类
 * 
 * @author qinghua.liu
 * 
 */
public class StringParse implements ResponseParse {

	@Override
	public ResponseInfo parseResponse(RequestInfo requestInfo,
			ResponseInfo responseInfo) {
		String stringResult = null;
		if (responseInfo.stringResult == null) {// 不为null，从缓存取来的
			stringResult = StringResult(responseInfo);
			responseInfo.stringResult = stringResult;
		}

		return responseInfo;
	}

	private String StringResult(ResponseInfo response) {
		HttpEntity entity = response.httpResponse.getEntity();
		String StringResult = "";
		try {
			String charset = EntityUtils.getContentCharSet(entity) == null ? "utf-8"
					: EntityUtils.getContentCharSet(entity);
			StringResult = EntityUtils.toString(entity, charset);
			response.stringResult = StringResult;
			Logg.i("StringResult->"+StringResult,this);
			// 释放资源
			entity.consumeContent();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return StringResult;

	}

}
