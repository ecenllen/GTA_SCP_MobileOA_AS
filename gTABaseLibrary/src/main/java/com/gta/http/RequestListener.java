package com.gta.http;

/**
 * 与 @see NetworkRequestTask 关联的Listener
 * 
 */
public interface RequestListener {

	/**
	 * 当网络请求发生的错误回调
	 * 
	 */
	public void onRequestError(ResponseInfo responseInfo);

	/**
	 * 当网络请求被取消时回调
	 */
	public void onRequestCancelled(RequestInfo requestInfo);

	/**
	 * 网络请求成功或者取得缓存成功的回调
	 */
	public void onRequestSucceed(ResponseInfo responseInfo);

	/**
	 * 网络请求发生前 的回调
	 * 
	 * @author Stanley
	 */
	public void onRequestStart(RequestInfo requestInfo);

	/**
	 * 无网络
	 */
	public void onNoNetWork();

	/**
	 * 上传文件进度
	 * 
	 * @param progress
	 */
	public void onUploadProgress(String url, int progress);



}
