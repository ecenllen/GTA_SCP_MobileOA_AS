package com.gta.http;

import org.apache.http.HttpResponse;

import com.gta.cache.LoadedFrom;

public class ResponseInfo {
	public RequestInfo requestInfo;
	
	/*请求码，对应RequestInfo请求码*/
	public int requestCode;
	/*被解析成的对象实体*/
	public Object Entity;
	/*数据来源（缓存或者网络）*/
	public LoadedFrom loadedFrom=LoadedFrom.NETWORK;
	/*http返回状态码*/
	public int httpStatus;
	/*返回的错误信息*/
	public String errorMessage;
	/*http响应*/
	public HttpResponse httpResponse;
	/*被解析的原始文本数据*/
	public String stringResult;
	/*状态码*/
	public int stateCode=0;

	public ResponseInfo() {
		super();
	}


}
