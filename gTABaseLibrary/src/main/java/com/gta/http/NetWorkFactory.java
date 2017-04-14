package com.gta.http;

import android.content.Context;

public class NetWorkFactory {
	public static AbstractNetWork netWork;

	public synchronized static AbstractNetWork createHttpClient(Context context) {
		if (netWork == null) {
			return netWork = new HttpclientNetWork(context);
		} else {
			return netWork;
		}

	}

	public synchronized static NetWork createHttpUrlConnection() {
		return null;

	}

}
