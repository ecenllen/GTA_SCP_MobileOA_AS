/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gta.util;

import android.os.Build;

/**
 * Class containing some static utility methods.
 */
public class BaseVersionUtils {
	private BaseVersionUtils() {
	};

	public static boolean hasFroyo() {
		return Build.VERSION.SDK_INT >= 8;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= 9;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= 11;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= 12;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= 20;
	}
	


	



}
