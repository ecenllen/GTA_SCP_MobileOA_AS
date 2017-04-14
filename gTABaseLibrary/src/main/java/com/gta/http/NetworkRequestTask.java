package com.gta.http;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.os.AsyncTask;

import com.gta.cache.FileCache;
import com.gta.cache.LoadedFrom;
import com.gta.http.parse.ResponseParse;
import com.gta.util.BaseNetWorkUtil;
import com.gta.util.Logg;

public class NetworkRequestTask extends AsyncTask<Void, Integer, ResponseInfo> {
	private RequestListener networkRequestListener;
	private RequestInfo requestInfo;
	private FileCache httpCache;;
	private Context context;
	private boolean netWorkAvailability;
	private ResponseParse responseHandler;
	private AbstractNetWork netWork;
	private String key = null;

	public NetworkRequestTask(Context context, RequestInfo requestInfo,
			AbstractNetWork netWork, ResponseParse responseHandler) {
		this.netWork = netWork;
		this.context = context;
		this.requestInfo = requestInfo;
		this.responseHandler = responseHandler;

	}

	@Override
	protected ResponseInfo doInBackground(Void... params) {
		if (requestInfo != null && requestInfo.url != null && !isCancelled()) {
			ResponseInfo responseInfo = null;
			if (netWorkAvailability) {
				responseInfo = takeCache();// 从缓存中取
				if (null != responseInfo) {
					responseInfo.stateCode = NetWork.SUCCESS;
					return responseInfo;
				} else {
					responseInfo = netWork.getResponse(requestInfo,
							responseHandler, this);// 联网与解析
					// 存入缓存
					if (requestInfo.enableCache && null != responseInfo
							&& responseInfo.stringResult != null) {
						responseInfo.loadedFrom = LoadedFrom.NETWORK;
						getHttpCache().put(key,
								responseInfo.stringResult.getBytes());
					}
				}

			} else {
				responseInfo = takeCache();// 从缓存中取
				if (null != responseInfo) {
					responseInfo.stateCode = NetWork.SUCCESS;
					return responseInfo;
				}
			}
			return responseInfo;
		}
		return null;
	}

	/**
	 * 从缓存中取
	 * 
	 * @return
	 */
	private ResponseInfo takeCache() {
		ResponseInfo responseInfo = null;
		if (requestInfo.enableCache) {
			byte[] data = getHttpCache().getBufferFromMemCache(key);
			if (data != null) {
				responseInfo = new ResponseInfo();
				responseInfo.loadedFrom = LoadedFrom.MEMORY_CACHE;
				responseInfo = parseString(responseInfo, data);
			} else {
				data = getHttpCache().getBufferFromDiskCache(key);
				if (data != null) {
					responseInfo = new ResponseInfo();
					responseInfo.loadedFrom = LoadedFrom.DISC_CACHE;
					responseInfo = parseString(responseInfo, data);
				}
			}
		}
		return responseInfo;
	}

	private ResponseInfo parseString(ResponseInfo responseInfo, byte[] data) {
		if (data != null) {
			String stringResult = null;
			try {
				stringResult = new String(data, "utf-8");
			} catch (UnsupportedEncodingException e) {
			}
			responseInfo.requestCode = requestInfo.requestCode;
			responseInfo.stringResult = stringResult;
			if (responseHandler != null)
				responseInfo = responseHandler.parseResponse(requestInfo,
						responseInfo);
		}
		return responseInfo;

	}

	@Override
	protected void onPreExecute() {
		if (requestInfo != null && requestInfo.url != null) {

			key = requestInfo.url;
			Logg.i("url-->" + requestInfo.url, this);
			if (checkParams(requestInfo.params)) {
				String param = requestInfo.params.JoinParams();
				key = key + param;
				Logg.i("param-->" + param, this);
				Logg.i("fullUrl-->" + requestInfo.url + "?" + param, this);
			}
			if (networkRequestListener != null) {
				networkRequestListener.onRequestStart(requestInfo);
				netWorkAvailability = BaseNetWorkUtil
						.isNetworkConnected(context);
				if (!netWorkAvailability) {// 网络无连接
					networkRequestListener.onNoNetWork();
				}
			}
		}
	}

	/**
	 * 更新上传进度
	 * 
	 * @param num
	 * @param ContentLength
	 */
	public void uploadProgress(long num, long ContentLength) {
		publishProgress((int) ((num * 100 / ContentLength)));
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		if (networkRequestListener != null)
			networkRequestListener.onUploadProgress(requestInfo.url,
					progress[0]);
	}

	@Override
	protected void onPostExecute(ResponseInfo responseInfo) {
		if (null != responseInfo && !isCancelled()) {
			responseInfo.requestInfo=requestInfo;
			switch (responseInfo.stateCode) {
			case NetWork.SUCCESS:
				if (networkRequestListener != null)
					networkRequestListener.onRequestSucceed(responseInfo);
				break;
			case NetWork.REQ_TIME_OUT:
			case NetWork.READ_TIME_OUT:
			case NetWork.HOST_NORESPONSE:
			case NetWork.PARAMETER_ERROR:
			case NetWork.CONNECTION_SERVER_ERROR:
				if (networkRequestListener != null)
					networkRequestListener.onRequestError(responseInfo);
				break;

			default:
				break;
			}
		}

	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		if (networkRequestListener != null)
			networkRequestListener.onRequestCancelled(requestInfo);
	}

	boolean checkParams(RequestParams params) {
		if (params != null && params.params != null && params.params.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 任务是否完成，包括已经取消了的任务
	 * 
	 * @return
	 */
	public boolean isDone() {
		if (getStatus().equals(Status.FINISHED)) {
			return true;
		}
		return false;
	}

	public RequestListener getNetworkRequestListener() {
		return networkRequestListener;
	}

	public void setNetworkRequestListener(RequestListener networkRequestListener) {
		this.networkRequestListener = networkRequestListener;
	}

	public FileCache getHttpCache() {
		return httpCache;
	}

	public void setHttpCache(FileCache httpCache) {
		this.httpCache = httpCache;
	}
}
