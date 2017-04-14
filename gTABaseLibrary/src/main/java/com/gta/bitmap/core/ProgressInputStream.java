package com.gta.bitmap.core;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.gta.bitmap.listener.ProgressListener;

public class ProgressInputStream extends FilterInputStream {
	private ProgressListener listener;
	private int length;

	protected ProgressInputStream(int length, InputStream in,
			ProgressListener listener) {
		super(in);
		this.length = length;
		this.listener = listener;

	}

	int current = 0;

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
			throws IOException {
		listener.onBytesCopied(current += byteCount, length);
		return super.read(buffer, byteOffset, byteCount);
	}

}
