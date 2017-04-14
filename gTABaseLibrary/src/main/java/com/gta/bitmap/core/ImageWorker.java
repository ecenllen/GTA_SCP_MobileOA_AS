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

package com.gta.bitmap.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.gta.bitmap.listener.ImageLoadingListener;
import com.gta.bitmap.listener.ImageLoadingProgressListener;
import com.gta.bitmap.listener.ProgressListener;
import com.gta.cache.ImageCache;
import com.gta.cache.LoadedFrom;
import com.gta.gtabasenetwork.BuildConfig;
import com.gta.util.BaseVersionUtils;

/**
 * This class wraps up completing some arbitrary long running work when loading
 * a bitmap to an ImageView. It handles things like using a memory and disk
 * cache, running the work in a background thread and setting a placeholder
 * image.
 */
public class ImageWorker {
	private ImageCache mImageCache;
	private Bitmap mLoadingBitmap;
	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	protected Context mContext;
	private ImageLoaderConfiguration configuration;

	protected ImageWorker(ImageLoaderConfiguration configuration,
			Context mContext, ImageCache mImageCache) {
		this.configuration = configuration;
		this.mContext = mContext;
		this.mImageCache = mImageCache;
	}

	/**
	 * Load an image specified by the data parameter into an ImageView (override
	 * {@link ImageWorker#processBitmap(Object)} to define the processing
	 * logic). A memory and disk cache will be used if an {@link ImageCache} has
	 * been added using
	 * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.CacheParams)}
	 * . If the image is found in the memory cache, it is set immediately,
	 * otherwise an {@link AsyncTask} will be created to asynchronously load the
	 * bitmap.
	 * 
	 * @param data
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	public void loadImage(ImageLoadingInfo imageLoadingInfo) {
		if (imageLoadingInfo.uri == null) {
			return;
		}
		String uri = imageLoadingInfo.uri;
		ImageView imageView = imageLoadingInfo.imageView;
		DisplayImageOptions options = imageLoadingInfo.options;
		Bitmap bmp = null;
		if (mImageCache != null) {
			BitmapDrawable drawable = mImageCache.getBitmapFromMemCache(uri);
			if (null != drawable) {
				bmp = drawable.getBitmap();
			}
		}
		if (bmp != null && !bmp.isRecycled()) {

			imageLoadingInfo.options.getDisplayer().display(bmp, imageView,
					LoadedFrom.MEMORY_CACHE);
			imageLoadingInfo.listener.onLoadingComplete(uri, imageView, bmp);
		} else if (cancelPotentialWork(uri, imageView)) {
			// BEGIN_INCLUDE(execute_background_task)
			final BitmapWorkerTask task = new BitmapWorkerTask(imageLoadingInfo);
			/**
			 * 创建一个特殊的Drawable，这个Drawable有两个功能，一个是与Task形成一种绑定的关系，
			 * 另外也充当了ImageView的临时占位图像
			 */
			mLoadingBitmap = BitmapFactory.decodeResource(
					mContext.getResources(), options.getImageResOnLoading());
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					mContext.getResources(), mLoadingBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			if (BaseVersionUtils.hasHoneycomb()) {
				if (null != configuration.taskExecutor) {
					task.executeOnExecutor(configuration.taskExecutor);
				}
			} else {
				task.execute();
			}

		}
	}

	// 设置提前结束任务
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
		setPauseWork(false);// 唤醒阻塞的任务线程
	}

	/**
	 * @return The {@link ImageCache} object currently being used by this
	 *         ImageWorker.
	 */
	protected ImageCache getImageCache() {
		return mImageCache;
	}

	/**
	 * Cancels any pending work attached to the provided ImageView.
	 * 
	 * @param imageView
	 */
	public static void cancelWork(ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
		if (bitmapWorkerTask != null) {
			bitmapWorkerTask.cancel(true);
			if (BuildConfig.DEBUG) {
				// final Object bitmapData = bitmapWorkerTask.mData;
				// Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
			}
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no
	 * work in progress on this image view. Returns false if the work in
	 * progress deals with the same data. The work is not stopped in that case.
	 */
	/**
	 * 我们需要判断下ImageView之前是否已经绑定了， 如果之前绑定过但与本次的图片不同，那我们就要按最新的需要从新绑定下， 并取消之前绑定的任务。
	 * 如果之前与现在的一致，则保持原状，不再从新绑定， 代码中的cancelPotentialWork就是做这个工作的
	 * 
	 * @param data
	 * @param imageView
	 * @return
	 */
	public static boolean cancelPotentialWork(Object data, ImageView imageView) {
		// BEGIN_INCLUDE(cancel_potential_work)
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final Object bitmapData = bitmapWorkerTask.uri;
			if (bitmapData == null || !bitmapData.equals(data)) {
				bitmapWorkerTask.cancel(true);
				if (BuildConfig.DEBUG) {
					// Log.d(TAG, "cancelPotentialWork - cancelled work for "
					// + data);
				}
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active work task (if any) associated with
	 *         this imageView. null if there is no such task.
	 */
	// 通过iamgeView找到对应的Task
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	/**
	 * The actual AsyncTask that will asynchronously process the image.
	 */
	private class BitmapWorkerTask extends
			AsyncTask<Void, Integer, BitmapDrawable> implements
			ProgressListener {
		private String uri;
		private final WeakReference<ImageView> imageViewReference;
		private LoadedFrom loadedFrom = LoadedFrom.NETWORK;;
		private ImageLoadingInfo imageLoadingInfo;
		private ImageView imageView;
		private DisplayImageOptions options;
		private ImageLoadingListener listener;
		private ImageLoadingProgressListener progressListener;

		public BitmapWorkerTask(ImageLoadingInfo imageLoadingInfo) {
			this.imageLoadingInfo = imageLoadingInfo;
			this.imageView = imageLoadingInfo.imageView;
			this.uri = imageLoadingInfo.uri;
			this.options = imageLoadingInfo.options;
			this.listener = imageLoadingInfo.listener;
			this.progressListener = imageLoadingInfo.progressListener;
			this.imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Background processing.
		 */
		@Override
		protected BitmapDrawable doInBackground(Void... params) {
			Bitmap bitmap = null;
			BitmapDrawable drawable = null;
			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						// 如果work已经暂停并且图片请求没有取消，那么就等待
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			// If the image cache is available and this task has not been
			// cancelled by another
			// thread and the ImageView that was originally bound to this task
			// is still bound back
			// to this task and our "exit early" flag is not set then try and
			// fetch the bitmap from
			// the cache
			if (mImageCache != null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = mImageCache.getBitmapFromDiskCache(uri);
				if (bitmap != null)
					loadedFrom = LoadedFrom.DISC_CACHE;
			}

			// If the bitmap was not found in the cache and this task has not
			// been cancelled by
			// another thread and the ImageView that was originally bound to
			// this task is still
			// bound back to this task and our "exit early" flag is not set,
			// then call the main
			// process method (as implemented by a subclass)
			if (bitmap == null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				InputStream inputStream = null;
				try {
					inputStream = configuration.downloader.getStream(uri,
							imageLoadingInfo);
					bitmap = configuration.decoder.decode(
							new ProgressInputStream(imageLoadingInfo.lenght,
									inputStream, this), imageLoadingInfo);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (null != inputStream)
							inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if (options.shouldPreProcess() && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = options.getPreProcessor().process(bitmap);
			}

			if (bitmap != null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				if (BaseVersionUtils.hasHoneycomb()) {
					// Running on Honeycomb or newer, so wrap in a standard
					// BitmapDrawable
					drawable = new BitmapDrawable(mContext.getResources(),
							bitmap);
				} else {
					// Running on Gingerbread or older, so wrap in a
					// RecyclingBitmapDrawable
					// which will recycle automagically
					drawable = new RecyclingBitmapDrawable(
							mContext.getResources(), bitmap);
				}
			}
			if (mImageCache != null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				mImageCache.addBitmapToCache(uri, drawable,
						configuration.expiryTime);// 加入内存和磁盘缓存
			}

			return drawable;
		}

		/**
		 * Once the image is processed, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(BitmapDrawable drawable) {
			ImageView imageView = getAttachedImageView();
			if (imageView != null && !isCancelled() && !mExitTasksEarly) {
				if (drawable != null) {
					options.getDisplayer().display(drawable.getBitmap(),
							imageView, loadedFrom);
					if (listener != null) {
						listener.onLoadingComplete(uri, imageView,
								drawable.getBitmap());
					}

				} else {
					if (listener != null) {
						listener.onLoadingFailed(uri, imageView);
					}
				}
			}

		}

		// 任务取消了，必须通知后台线程停止等待
		@Override
		protected void onCancelled() {
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}

		@Override
		public void onBytesCopied(int current, int total) {
			publishProgress(current, total);

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (progressListener != null && !isCancelled() && !mExitTasksEarly
					&& getAttachedImageView() != null)
				progressListener.onProgressUpdate(uri, imageView, values[0],
						values[1]);
		}
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * Pause any ongoing background work. This can be used as a temporary
	 * measure to improve performance. For example background work could be
	 * paused when a ListView or GridView is being scrolled using a
	 * {@link android.widget.AbsListView.OnScrollListener} to keep scrolling
	 * smooth.
	 * <p>
	 * If work is paused, be sure setPauseWork(false) is called again before
	 * your fragment or activity is destroyed (for example during
	 * {@link android.app.Activity#onPause()}), or there is a risk the
	 * background thread will never finish.
	 */
	/**
	 * AbsListView OnScrollListener滑动监听调用，优化快速滑动时不加载网络 pauseWork 传入为true
	 * 时，认为AbsListView 快速滑动，阻塞线程，停止任务 pauseWork 传入为FALSE 时，认为AbsListView
	 * 停止滑动，唤醒阻塞的线程，加载任务
	 * 
	 * @param pauseWork
	 */
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

}
