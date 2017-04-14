package com.gta.bitmap.core;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

import com.gta.bitmap.decode.BaseImageDecoder;
import com.gta.bitmap.decode.ImageDecoder;
import com.gta.bitmap.display.BitmapDisplayer;
import com.gta.bitmap.display.SimpleBitmapDisplayer;
import com.gta.bitmap.download.BaseImageDownloader;
import com.gta.bitmap.download.ImageDownloader;

public class DefaultConfigurationFactory {

	public static ExecutorService createExecutor(int threadPoolSize,
			int threadPriority) {
		return Executors.newFixedThreadPool(threadPoolSize,
				createThreadFactory(threadPriority, "gta-pool-"));
	}

	public static ExecutorService createExecutorCache(int threadPriority) {
		return Executors.newCachedThreadPool(createThreadFactory(
				threadPriority, "gta-pool-cache"));
	}

	/** Creates default implementation of task distributor */
	public static Executor createTaskDistributor() {
		return Executors.newCachedThreadPool(createThreadFactory(
				Thread.NORM_PRIORITY, "uil-pool-d-"));
	}




	/**
	 * Creates default implementation of {@link ImageDownloader} -
	 * {@link BaseImageDownloader}
	 */
	public static ImageDownloader createImageDownloader(Context context) {
		return new BaseImageDownloader(context);
	}

	/**
	 * Creates default implementation of {@link ImageDecoder} -
	 * {@link BaseImageDecoder}
	 */
	public static ImageDecoder createImageDecoder() {
		return new BaseImageDecoder();
	}

	/**
	 * Creates default implementation of {@link BitmapDisplayer} -
	 * {@link SimpleBitmapDisplayer}
	 */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}

	/**
	 * Creates default implementation of {@linkplain ThreadFactory thread
	 * factory} for task executor
	 */
	private static ThreadFactory createThreadFactory(int threadPriority,
			String threadNamePrefix) {
		return new DefaultThreadFactory(threadPriority, threadNamePrefix);
	}

	private static class DefaultThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
			this.threadPriority = threadPriority;
			group = Thread.currentThread().getThreadGroup();
			namePrefix = threadNamePrefix + poolNumber.getAndIncrement()
					+ "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix
					+ threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}
}
