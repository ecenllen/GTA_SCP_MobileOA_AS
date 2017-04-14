package com.gta.http.parse;

import com.google.gson.Gson;
import com.gta.http.RequestInfo;
import com.gta.http.ResponseInfo;

/**
 * JSON 解析类
 * 
 * @author qinghua.liu
 * 
 */
public class JsonParse extends StringParse {
	private Class<?> entityClass;

	public JsonParse(Class<?> entityClass) {
		super();
		this.entityClass = entityClass;
	}

	@Override
	public ResponseInfo parseResponse(RequestInfo requestInfo,
			ResponseInfo responseInfo) {
		super.parseResponse(requestInfo, responseInfo);
		Object resultRsp = null;
		String stringResult = responseInfo.stringResult;
		if (stringResult != null) {
			resultRsp = jsonToObject(stringResult);
			responseInfo.Entity = resultRsp;
		}
		return responseInfo;

	}

	private Object jsonToObject(String result) {
		Gson gson = new Gson();
		try {
			Object resultRsp = (Object) gson.fromJson(result, entityClass);
			return resultRsp;
		} catch (Exception e) {
		}
		return null;
	}
}
