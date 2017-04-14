package com.gta.bitmap.core;

import android.widget.ImageView;

import com.gta.bitmap.listener.ImageLoadingListener;
import com.gta.bitmap.listener.ImageLoadingProgressListener;
import com.gta.bitmap.other.ImageSize;

public class ImageLoadingInfo {

	public final String uri;
	public final String cacheKey;
	public final ImageView imageView;
	public final ImageSize targetSize;
	public final DisplayImageOptions options;
	public final ImageLoadingListener listener;
	public final ImageLoadingProgressListener progressListener;
	public int lenght;

	public ImageLoadingInfo(String uri, ImageView imageView,
			ImageSize targetSize, String cacheKey, DisplayImageOptions options,
			ImageLoadingListener listener,
			ImageLoadingProgressListener progressListener) {
		this.uri = uri;
		this.imageView = imageView;
		this.targetSize = targetSize;
		this.options = options;
		this.listener = listener;
		this.progressListener = progressListener;
		this.cacheKey = cacheKey;
	}
}
