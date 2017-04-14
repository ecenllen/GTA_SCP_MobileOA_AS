package com.gta.http;

import org.apache.http.protocol.HTTP;

import android.content.Context;

public class RequestInfo {
	/*建议用appContext*/
	public Context appContext;
	
	public RequestParams params;
	/*地址*/
	public String url;
	/*请求方法get/post*/
	public RequestMethod method = RequestMethod.POST;
	/*请求码，对应ResponseInfo请求码*/
	public int requestCode = -1;
	/*是否使用缓存*/
	public boolean enableCache=false;
	/*请求提示信息(如 正在加载中..)*/
	public String message;
	public String charset = HTTP.UTF_8;
	public RequestInfo(Context mcontext, String url) {
		super();
		this.appContext = mcontext.getApplicationContext();
		this.url = url;
	}
	
	public static enum RequestMethod {
		POST,GET
	}

}
