package com.gta.bitmap.core;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.ImageView;

import com.gta.bitmap.listener.ImageLoadingListener;
import com.gta.bitmap.listener.ImageLoadingProgressListener;
import com.gta.bitmap.listener.SimpleImageLoadingListener;
import com.gta.bitmap.other.ImageSize;
import com.gta.cache.ImageCache;

public class ImageLoader {
	static final String LOG_DESTROY = "Destroy ImageLoader";
	private static final String ERROR_WRONG_ARGUMENTS = "ImageView reference must not be null)";
	private static final String ERROR_NOT_INIT = "ImageLoader must be init with configuration before using";
	public static final String TAG = ImageLoader.class.getSimpleName();
	private ImageLoaderConfiguration configuration;
	private final ImageLoadingListener emptyListener = new SimpleImageLoadingListener();
	private volatile static ImageLoader instance;
	private ImageWorker imageWorker;
	private ImageCache mImageCache;
	private Resources resources;
	private Context context;

	/** Returns singleton class instance */
	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}

	protected ImageLoader() {
	}

	/**
	 * 使用前必须初始化配置
	 * 
	 * @param configuration
	 */
	public synchronized void init(ImageLoaderConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(ERROR_NOT_INIT);
		}
		if (this.configuration == null) {
			this.configuration = configuration;
		}
		iniCache(configuration);// 初始化缓存
		iniImageWorker(configuration);

	}

	private void iniImageWorker(ImageLoaderConfiguration configuration) {
		this.context = configuration.appContext;
		this.resources = configuration.appContext.getResources();
		imageWorker = new ImageWorker(configuration, context, mImageCache);
	}

	public void iniCache(ImageLoaderConfiguration configuration) {
		mImageCache = ImageCache.getInstance();
		mImageCache.init(configuration);
	}

	
	/**
	 * 显示图片
	 * @param uri 地址
	 * @param imageView 图片
	 */
	public void displayImage(String uri, ImageView imageView) {
		displayImage(uri, imageView, null, null, null);
	}
	/**
	 * 显示图片
	 * @param uri 地址 
	 * @param imageView 图片
	 * @param options 图片选项
	 */
	public void displayImage(String uri, ImageView imageView,
			DisplayImageOptions options) {
		displayImage(uri, imageView, options, null, null);
	}
	/**
	 * 显示图片
	 * @param uri 地址
	 * @param imageView 图片
	 * @param options 图片选项
	 * @param listener 图片加载监听
	 */
	public void displayImage(String uri, ImageView imageView,
			DisplayImageOptions options, ImageLoadingListener listener) {
		displayImage(uri, imageView, options, listener, null);
	}
	/**
	 * 显示图片
	 * @param uri 地址
	 * @param imageView 图片
	 * @param options 图片选项
	 * @param listener 图片加载监听
	 * @param progressListener 图片加载进度监听
	 */
	public void displayImage(String uri, ImageView imageView,
			DisplayImageOptions options, ImageLoadingListener listener,
			ImageLoadingProgressListener progressListener) {

		if (imageView == null) {
			throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
		}
		if (listener == null) {
			listener = emptyListener;
		}
		if (options == null) {
			options = configuration.defaultDisplayImageOptions;
		}
		if (options.getImageResOnLoading() <= 0) {
			throw new IllegalArgumentException(
					"int ImageResOnLoading must not be null");
		}

		if (TextUtils.isEmpty(uri)) {
			listener.onLoadingStarted(uri, imageView);
			if (options.shouldShowImageForEmptyUri()) {
				imageView.setImageDrawable(options
						.getImageForEmptyUri(resources));
			} else {
				imageView.setImageDrawable(null);
			}
			listener.onLoadingComplete(uri, imageView, null);
			return;
		}

		listener.onLoadingStarted(uri, imageView);
		ImageSize targetSize = ImageSize.defineTargetSizeForView(imageView,
				configuration.getMaxImageSize());
		ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(uri,
				imageView, targetSize, uri, options, listener, progressListener);
		imageWorker.loadImage(imageLoadingInfo);

	}

	/**
	 * Checks if ImageLoader's configuration was initialized
	 * 
	 * @throws IllegalStateException
	 *             if configuration wasn't initialized
	 */
	private void checkConfiguration() {
		if (configuration == null) {
			throw new IllegalStateException(ERROR_NOT_INIT);
		}
	}

	/**
	 * 清除内存+磁盘缓存
	 */
	public void clearCache() {
		checkConfiguration();
		mImageCache.clearCache();
	}

	/**
	 * 返回内存+磁盘缓存大小
	 * 
	 * @return
	 */
	public long size() {
		return mImageCache.size();
	}

	/**
	 * 暂停所有任务线程，正在运行的不会被取消
	 */
	public void pause() {
		imageWorker.setPauseWork(true);
	}

	/**
	 * 唤醒所有暂停的任务(唤醒等待的线程)
	 */
	public void resume() {
		imageWorker.setPauseWork(false);
		;
	}

	/**
	 * 提前结束任务[onDestroy()]
	 * 
	 * @param exitTasksEarly
	 */
	public void setExitTasksEarly(boolean exitTasksEarly) {
		imageWorker.setExitTasksEarly(exitTasksEarly);
	}
}
