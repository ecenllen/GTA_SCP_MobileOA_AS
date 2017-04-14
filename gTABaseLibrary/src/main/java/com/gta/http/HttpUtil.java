package com.gta.http;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import android.content.Context;

import com.gta.cache.FileCache;
import com.gta.http.parse.ResponseParse;

public class HttpUtil {
	private static HttpUtil httpUtil;
	/* 文件缓存 */
	public static FileCache httpCache;
	/* 保存请求的MAP */

	public AbstractNetWork netWork;
	public Map<Integer, List<WeakReference<NetworkRequestTask>>> requestMap;

	public static HttpUtil getInstance() {
		if (httpUtil == null) {
			synchronized (HttpUtil.class) {
				if (httpUtil == null) {
					httpUtil = new HttpUtil();
				}

			}
		}
		return httpUtil;
	}

	/**
	 * 初始化缓存
	 * 
	 * @param cacheParams
	 */
	public void iniCache(FileCache.CacheParams cacheParams) {
		httpCache = FileCache.getInstance(cacheParams);
	}

	public HttpUtil() {
		requestMap = Collections
				.synchronizedMap(new WeakHashMap<Integer, List<WeakReference<NetworkRequestTask>>>());
	}

	public HttpUtil configNetWork(AbstractNetWork netWork) {
		this.netWork = netWork;
		return httpUtil;
	}

	public HttpUtil addCookie(Context context) {
		if (netWork == null || context == null)
			return httpUtil;
		if (netWork instanceof HttpclientNetWork) {
			
			
			// CookieUtils cookieUtils = new CookieUtils(
			// context.getApplicationContext());
			// ((HttpclientNetWork) ((HttpclientNetWork) netWork))
			// .configCookieStore(cookieUtils);
		}
		return httpUtil;
	}

	/**
	 * http请求接口 注意httpClient默认为单例模式，前提httpClient必须是线程安全的连接管理。
	 * 
	 * @param requestInfo
	 *            请求相关信息类
	 * @param params
	 *            参数类(get方式无参数可以为null)
	 * @param netWork
	 *            实现网络请求接口的浏览器类
	 * @param responseHandler
	 *            返回数据的解析类
	 * @param listener
	 *            回调监听
	 * 
	 * @param object
	 *            请求所在的类，一般为activity。
	 */
	public void doRequest(RequestInfo requestInfo,
			ResponseParse responseHandler, RequestListener listener,
			Object object) {
		netWork = (null == netWork ? NetWorkFactory.createHttpClient(requestInfo.appContext)
				: netWork);
		NetworkRequestTask NRT = new NetworkRequestTask(requestInfo.appContext,
				requestInfo, netWork, responseHandler);
		NRT.setHttpCache(httpCache);
		NRT.setNetworkRequestListener(listener);
		NRT.execute();
		addRequestToMap(NRT, object);
	}

	public void addRequestToMap(NetworkRequestTask requestTask, Object object) {
		if (requestTask != null && object != null) {
			int key = object.hashCode();
			List<WeakReference<NetworkRequestTask>> list = requestMap.get(key);
			if (list == null) {
				list = new LinkedList<WeakReference<NetworkRequestTask>>();
				requestMap.put(key, list);
			}
			list.add(new WeakReference<NetworkRequestTask>(requestTask));
			cancelTask(list, true);// 取消已finish的任务

		}
	}

	/**
	 * 取消object所关联的全部请求
	 * 
	 * @param object
	 *            请求所在的类，一般为activity。
	 */
	public void cancelRequest(Object object) {
		if (object == null)
			return;
		List<WeakReference<NetworkRequestTask>> list = requestMap.get(object
				.hashCode());
		if (list != null) {
			cancelTask(list, false);
			requestMap.remove(object.hashCode());
		}
	}

	/**
	 * 取消全部请求
	 */
	public void cancelAllRequest() {
		for (Entry<Integer, List<WeakReference<NetworkRequestTask>>> entryList : requestMap
				.entrySet()) {
			cancelTask(entryList.getValue(), false);
		}
		requestMap.clear();

	}

	/**
	 * 取消任务
	 * 
	 * @param list
	 *            任务列表
	 * @param isDone
	 *            true:移除已完成的任务，false:不做操作
	 */
	private void cancelTask(final List<WeakReference<NetworkRequestTask>> list,
			boolean isDone) {

		Iterator<WeakReference<NetworkRequestTask>> iterator = list.iterator();
		while (iterator.hasNext()) {
			WeakReference<NetworkRequestTask> reference = iterator.next();
			final NetworkRequestTask task = reference.get();
			if (task == null)
				return;
			if (isDone) {
				if (task.isDone()) {
					new Thread() {
						public void run() {
							list.remove(task);
						};

					}.start();
				}
			} else {
				task.cancel(true);
				list.remove(task);
			}
		}

	}

	/**
	 * 清除内存+磁盘缓存
	 */
	public void clear() {
		if (httpCache != null)
			httpCache.clear();
	}

	/**
	 * 返回内存+磁盘缓存大小
	 * 
	 * @return
	 */
	public long size() {
		return httpCache != null ? httpCache.size() : 0;

	}
}
