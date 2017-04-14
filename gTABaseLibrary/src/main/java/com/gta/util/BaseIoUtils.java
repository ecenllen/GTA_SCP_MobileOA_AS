/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gta.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides I/O operations
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.0.0
 */
public class BaseIoUtils {

	/** {@value} */
	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 KB
	/** {@value} */
	public static final int DEFAULT_IMAGE_TOTAL_SIZE = 500 * 1024; // 500 Kb
	/** {@value} */
	public static final int CONTINUE_LOADING_PERCENTAGE = 75;

	public static void copyStream(InputStream is, OutputStream os)
			throws IOException {
		int total = is.available();
		int bufferSize = 1024 * 4;
		if (total <= 0) {
		}
		final byte[] bytes = new byte[bufferSize];
		int count;
		while ((count = is.read(bytes, 0, bufferSize)) != -1) {
			os.write(bytes, 0, count);
		}
		os.flush();
	}

	public static byte[] readStreamToByte(InputStream inStream)
			throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		outStream.close();
		return outStream.toByteArray();
	}

	public static void readAndCloseStream(InputStream is) {
		final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
		try {
			while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1)
				;
		} catch (IOException ignored) {
		} finally {
			closeSilently(is);
		}
	}

	public static void closeSilently(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ignored) {
			}
		}
	}

	public static interface CopyListener {
		boolean onBytesCopied(int current, int total);
	}
}
