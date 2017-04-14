package com.gta.util;

import android.util.Log;

public final class Logg {

	private static volatile boolean writeLogs = true;

	private Logg() {
	}

	public static void enableLogging() {
		Logg.writeLogs = true;
	}

	public static void disableLogging() {
		Logg.writeLogs = false;
	}

	public static boolean isEnable() {
		return writeLogs;
	}

	public static void d(String message, Object args) {
		log(Log.DEBUG, message, args);
	}

	public static void i(String message, Object args) {
		log(Log.INFO, message, args);
	}

	public static void w(String message, Object args) {
		log(Log.WARN, message, args);
	}

	public static void e(String message, Object args) {
		log(Log.ERROR, message, args);
	}

	private static void log(int priority, String message, Object args) {
		if (!writeLogs && args == null && message == null)
			return;
		String tag = args.getClass().getSimpleName();

		Log.println(priority, tag, message);
	}
}