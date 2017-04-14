package com.gta.util;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class BaseNetWorkUtil {

	/**
	 * 判断是否已连接到网络.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 从HttpResponse获取文件名
	 * 
	 * @param response
	 * @return
	 */
	public static String getFileNameFromHttpResponse(HttpResponse response) {
		if (response == null)
			return null;
		String fielName = null;
		HeaderIterator iterator = response
				.headerIterator("Content-Disposition");
		while (iterator.hasNext()) {
			Header header = iterator.nextHeader();
			if (header != null) {
				for (HeaderElement element : header.getElements()) {
					NameValuePair fileNamePair = element
							.getParameterByName("filename");
					if (fileNamePair != null) {
						fielName = fileNamePair.getValue();
						break;
					}
				}
			}
		}
		return fielName;
	}

	/**
	 * 从URL获取文件名
	 * 
	 * @param url
	 * @return
	 */
	public static String getFileNameFromUrl(String url) {
		String fileName = "";
		fileName = url.substring(url.lastIndexOf("/") + 1,
				url.indexOf("?") == -1 ? url.length() : url.indexOf("?"));
		return fileName;
	}

	/**
	 * 是否支持断点下载
	 * 
	 * @param response
	 * @return
	 */
	public static boolean isSupportRange(HttpResponse response) {
		if (response == null)
			return false;
		Header header = response.getFirstHeader("Accept-Ranges");
		if (header != null) {
			return "bytes".equals(header.getValue());
		}
		header = response.getFirstHeader("Content-Range");
		if (header != null) {
			String value = header.getValue();
			return value != null && value.startsWith("bytes");
		}
		return false;
	}

}
