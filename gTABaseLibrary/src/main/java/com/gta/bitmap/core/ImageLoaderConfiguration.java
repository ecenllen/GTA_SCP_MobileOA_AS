package com.gta.bitmap.core;

import java.util.concurrent.ExecutorService;

import android.content.Context;
import android.util.DisplayMetrics;

import com.gta.bitmap.decode.ImageDecoder;
import com.gta.bitmap.download.ImageDownloader;
import com.gta.bitmap.other.ImageSize;

public final class ImageLoaderConfiguration {
	public final Context appContext;
	/* 图片的宽，默认屏幕的宽 */
	public final int Width;
	/* 图片的高，默认图片的高 */
	public final int Height;
	/* 任务线程池 */
	public final ExecutorService taskExecutor;
	/* 线程池大小，默认3个 */
	public final int threadPoolSize;
	/* 线程优先级 */
	public final int threadPriority;
	/* 图片实现下载者 */
	public final ImageDownloader downloader;
	/* 解码者 */
	public final ImageDecoder decoder;
	/* 图片选项 */
	public final DisplayImageOptions defaultDisplayImageOptions;
	/* 内存缓存大小，默认最大内存1/8大小 */
	public final int memoryCacheSize;
	/* 磁盘缓存大小，默认50MB */
	public final long diskCacheSize;
	/* 缓存过期时间，默认300day*/
	public final long expiryTime;
	/*开启/关闭内存缓存*/
	public boolean memoryCacheEnabled;
	/* 开启/关闭磁盘缓存 */
	public boolean diskCacheEnabled;

	private ImageLoaderConfiguration(final Builder builder) {
		appContext = builder.context;
		Width = builder.Width;
		Height = builder.Height;
		taskExecutor = builder.taskExecutor;
		threadPoolSize = builder.threadPoolSize;
		threadPriority = builder.threadPriority;
		defaultDisplayImageOptions = builder.defaultDisplayImageOptions;
		downloader = builder.downloader;
		decoder = builder.decoder;
		memoryCacheSize = builder.memoryCacheSize;
		diskCacheSize = builder.diskCacheSize;
		expiryTime = builder.expiryTime;
		memoryCacheEnabled = builder.memoryCacheEnabled;
		diskCacheEnabled = builder.diskCacheEnabled;
	}

	public static ImageLoaderConfiguration createDefault(Context context) {
		return new Builder(context).build();
	}

	public ImageSize getMaxImageSize() {
		DisplayMetrics displayMetrics = appContext.getResources()
				.getDisplayMetrics();
		int width = Width;
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}
		int height = Height;
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}
		return new ImageSize(width, height);
	}

	public static class Builder {
		public static final long DEFAULT_EXPIRYTIME = 1000L * 60 * 60 * 24 * 300; // 300day
		public static final int DEFAULT_THREAD_POOL_SIZE = 3;
		public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;
		private Context context;
		private int Width = 0;
		private int Height = 0;
		private ExecutorService taskExecutor = null;
		private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		private int threadPriority = DEFAULT_THREAD_PRIORITY;
		private int memoryCacheSize = 0;
		private long diskCacheSize = 0;
		private ImageDownloader downloader = null;
		private ImageDecoder decoder;
		private DisplayImageOptions defaultDisplayImageOptions = null;
		private long expiryTime = DEFAULT_EXPIRYTIME;
		private boolean memoryCacheEnabled = true;
		private boolean diskCacheEnabled = true;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		/**
		 * 开启内存缓存
		 * 
		 * @param enable
		 * @return
		 */
		public Builder memoryCacheEnabled(boolean enable) {
			this.memoryCacheEnabled = enable;
			return this;
		}

		/**
		 * 开启磁盘缓存
		 * 
		 * @param enable
		 * @return
		 */
		public Builder diskCacheEnabled(boolean enable) {
			this.diskCacheEnabled = enable;
			return this;
		}

		/**
		 * 配置图片的宽高，默认屏幕宽高
		 * 
		 * @param Width
		 * @param Height
		 * @return
		 */
		public Builder diskCacheExtraOptions(int Width, int Height) {
			this.Width = Width;
			this.Height = Height;
			return this;
		}

		/**
		 * 配置任务线程池
		 * 
		 * @param executor
		 * @return
		 */
		public Builder taskExecutor(ExecutorService executor) {
			this.taskExecutor = executor;
			return this;
		}

		/**
		 * 配置线程池大小
		 * 
		 * @param threadPoolSize
		 * @return
		 */
		public Builder threadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
			return this;
		}

		/**
		 * 配置线程优先级
		 * 
		 * @param threadPriority
		 * @return
		 */
		public Builder threadPriority(int threadPriority) {
			if (threadPriority < Thread.MIN_PRIORITY) {
				this.threadPriority = Thread.MIN_PRIORITY;
			} else {
				if (threadPriority > Thread.MAX_PRIORITY) {
					this.threadPriority = Thread.MAX_PRIORITY;
				} else {
					this.threadPriority = threadPriority;
				}
			}
			return this;
		}

		/**
		 * 配置内存缓存大小，默认最大内存1/8大小
		 * 
		 * @param memoryCacheSize
		 * @return
		 */
		public Builder memoryCacheSize(int memoryCacheSize) {
			if (memoryCacheSize <= 0)
				throw new IllegalArgumentException(
						"memoryCacheSize must be a positive number");
			this.memoryCacheSize = memoryCacheSize;
			return this;
		}

		/**
		 * 配置磁盘缓存大小，按百分比
		 * 
		 * @param availableMemoryPercent
		 * @return
		 */
		public Builder memoryCacheSizePercentage(int availableMemoryPercent) {
			if (availableMemoryPercent <= 0 || availableMemoryPercent >= 100) {
				throw new IllegalArgumentException(
						"availableMemoryPercent must be in range (0 < % < 100)");
			}

			long availableMemory = Runtime.getRuntime().maxMemory();
			memoryCacheSize = (int) (availableMemory * (availableMemoryPercent / 100f));
			return this;
		}

		/**
		 * 配置磁盘缓存大小，默认50MB
		 * 
		 * @param maxCacheSize
		 * @return
		 */
		public Builder diskCacheSize(int maxCacheSize) {
			if (maxCacheSize <= 0)
				throw new IllegalArgumentException(
						"maxCacheSize must be a positive number");

			this.diskCacheSize = maxCacheSize;
			return this;
		}

		/**
		 * 配置下载类
		 * 
		 * @param imageDownloader
		 * @return
		 */
		public Builder imageDownloader(ImageDownloader imageDownloader) {
			this.downloader = imageDownloader;
			return this;
		}

		/**
		 * 配置解码类
		 * 
		 * @param imageDecoder
		 * @return
		 */
		public Builder imageDecoder(ImageDecoder imageDecoder) {
			this.decoder = imageDecoder;
			return this;
		}

		/**
		 * 配置图片超时时间，默认300day
		 * 
		 * @param expiryTime
		 * @return
		 */
		public Builder expiryTime(long expiryTime) {
			this.expiryTime = expiryTime;
			return this;
		}

		/**
		 * 配置图片显示选项
		 * 
		 * @param defaultDisplayImageOptions
		 * @return
		 */
		public Builder defaultDisplayImageOptions(
				DisplayImageOptions defaultDisplayImageOptions) {
			this.defaultDisplayImageOptions = defaultDisplayImageOptions;
			return this;
		}

		/** Builds configured {@link ImageLoaderConfiguration} object */
		public ImageLoaderConfiguration build() {
			initEmptyFieldsWithDefaultValues();
			return new ImageLoaderConfiguration(this);
		}

		/**
		 * 初始化空值
		 */
		private void initEmptyFieldsWithDefaultValues() {
			if (taskExecutor == null) {
				taskExecutor = DefaultConfigurationFactory.createExecutor(
						threadPoolSize, threadPriority);
			}
			if (downloader == null) {
				downloader = DefaultConfigurationFactory
						.createImageDownloader(context);
			}
			if (decoder == null) {
				decoder = DefaultConfigurationFactory.createImageDecoder();
			}
			if (defaultDisplayImageOptions == null) {
				defaultDisplayImageOptions = DisplayImageOptions.createSimple();
			}
			if (memoryCacheSize <= 0) {
				memoryCacheSize = getIdealMemCacheSize();
			}
			if (diskCacheSize <= 0) {
				getDefaultDiskCacheSize();
			}
		}

		private int getIdealMemCacheSize() {
			return (int) (Runtime.getRuntime().maxMemory() / 8);

		}

		private long getDefaultDiskCacheSize() {
			return 1024 * 1024 * 50;// 50MB
		}

	}

}
