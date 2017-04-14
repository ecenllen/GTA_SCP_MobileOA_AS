package com.gta.http;

import java.util.HashMap;

import com.gta.http.parse.ResponseParse;

public abstract class AbstractNetWork implements NetWork {
	abstract ResponseInfo getResponse(RequestInfo requestInfo,
			ResponseParse responseHandler, NetworkRequestTask networkRequestTask);

	abstract void addHeader(HashMap<String, String> map);

	abstract void addCookie(HashMap<String, String> map);

	abstract void setTimeout(int connectionTimeout, int soTimeout);

}
