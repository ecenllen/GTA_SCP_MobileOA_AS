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

package com.gta.cache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;

import com.gta.bitmap.core.ImageLoaderConfiguration;
import com.gta.bitmap.core.ImageWorker;
import com.gta.bitmap.core.RecyclingBitmapDrawable;
import com.gta.cache.FileCache.CacheParams;
import com.gta.gtabasenetwork.BuildConfig;
import com.gta.util.BaseBitmapUtils;
import com.gta.util.BaseFileUtils;
import com.gta.util.BaseVersionUtils;

/**
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorker} class and its subclasses. Use
 * {@link ImageCache#getInstance(android.support.v4.app.FragmentManager, CacheParams)}
 * to get an instance of this class, although usually a cache should be added
 * directly to an {@link ImageWorker} by calling
 * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, CacheParams)}
 * .
 */
public class ImageCache {
	private static final String TAG = "ImageCache";
	public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	public static final int DEFAULT_COMPRESS_QUALITY = 100;
	private static final int DISK_CACHE_INDEX = 0;
	public static String DEFAULE_DISKCACHE_DIR = "Image";
	// public static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	// public static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	public static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	private LruDiskCache mDiskLruCache;
	private LruMemoryCache<String, BitmapDrawable> mMemoryCache;
	private File diskCacheDir;
	private ImageLoaderConfiguration configuration;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static ImageCache imageCache;
	private Set<SoftReference<Bitmap>> mReusableBitmaps;
	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;
	private Context context;

	private ImageCache() {
	}

	public synchronized static ImageCache getInstance() {

		if (imageCache == null) {
			imageCache = new ImageCache();
		}
		return imageCache;
	}

	/**
	 * 初始化缓存
	 * 
	 * @param configuration
	 */
	public  void init(ImageLoaderConfiguration configuration) {
		this.context = configuration.appContext;
		this.configuration = configuration;
		iniCache(context, configuration);
	}

	private void iniCache(Context mContext,
			ImageLoaderConfiguration configuration) {
		diskCacheDir = BaseFileUtils.getDiskCacheDir(context,
				DEFAULE_DISKCACHE_DIR);
		initMemCache(mContext, configuration);
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	/**
	 * Initializes the disk cache. Note that this includes disk access so this
	 * should not be executed on the main/UI thread. By default an ImageCache
	 * does not initialize the disk cache when it is created, instead you should
	 * call initDiskCache() to initialize it on a background thread.
	 */
	private void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {

				if (configuration.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (getUsableSpace(diskCacheDir) > configuration.diskCacheSize) {
						try {
							mDiskLruCache = LruDiskCache.open(diskCacheDir, 1,
									1, configuration.diskCacheSize);
							if (BuildConfig.DEBUG) {
								Log.d(TAG, "Disk cache initialized");
							}
						} catch (final IOException e) {

						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	private void initMemCache(Context mContext,
			ImageLoaderConfiguration configuration) {
		if (configuration.memoryCacheEnabled) {

			// If we're running on Honeycomb or newer, create a set of reusable
			// bitmaps that can be
			// populated into the inBitmap field of BitmapFactory.Options. Note
			// that the set is
			// of SoftReferences which will actually not be very effective due
			// to the garbage
			// collector being aggressive clearing Soft/WeakReferences. A better
			// approach
			// would be to use a strongly references bitmaps, however this would
			// require some
			// balancing of memory usage between this set and the bitmap
			// LruCache. It would also
			// require knowledge of the expected size of the bitmaps. From
			// Honeycomb to JellyBean
			// the size would need to be precise, from KitKat onward the size
			// would just need to
			// be the upper bound (due to changes in how inBitmap can re-use
			// bitmaps).
			if (BaseVersionUtils.hasHoneycomb()) {
				mReusableBitmaps = Collections
						.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
			}

			mMemoryCache = new LruMemoryCache<String, BitmapDrawable>(
					configuration.memoryCacheSize) {

				/**
				 * Notify the removed entry that is no longer being cached
				 */
				@Override
				protected void entryRemoved(boolean evicted, String key,
						BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
						// The removed entry is a recycling drawable, so notify
						// it
						// that it has been removed from the memory cache
						((RecyclingBitmapDrawable) oldValue).setIsCached(false);
					} else {
						// The removed entry is a standard BitmapDrawable

						if (BaseVersionUtils.hasHoneycomb()) {
							// We're running on Honeycomb or later, so add the
							// bitmap
							// to a SoftReference set for possible use with
							// inBitmap later
							mReusableBitmaps.add(new SoftReference<Bitmap>(
									oldValue.getBitmap()));
						}
					}
				}

				/**
				 * Measure item size in kilobytes rather than units which is
				 * more practical for a bitmap cache
				 */
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int bitmapSize = getBitmapSize(value) / 1024;
					return bitmapSize == 0 ? 1 : bitmapSize;
				}
			};
		}
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * 
	 * @param data
	 *            Unique identifier for the bitmap to store
	 * @param value
	 *            The bitmap drawable to store
	 */
	public void addBitmapToCache(String key, BitmapDrawable value,
			long expiryTime) {
		// BEGIN_INCLUDE(add_bitmap_to_cache)
		if (key == null || value == null) {
			return;
		}
		expiryTime = System.currentTimeMillis() + expiryTime;
		// Add to memory cache
		if (mMemoryCache != null) {
			if (RecyclingBitmapDrawable.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
				((RecyclingBitmapDrawable) value).setIsCached(true);
			}
			mMemoryCache.put(key, value, expiryTime);
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mDiskLruCache != null) {
				// final String key = hashKeyForDisk(data);//已经转换了
				OutputStream out = null;
				try {
					LruDiskCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final LruDiskCache.Editor editor = mDiskLruCache
								.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.getBitmap().compress(DEFAULT_COMPRESS_FORMAT,
									DEFAULT_COMPRESS_QUALITY, out);
							editor.setEntryExpiryTimestamp(expiryTime);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
		// END_INCLUDE(add_bitmap_to_cache)
	}

	/**
	 * Get from memory cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
	public BitmapDrawable getBitmapFromMemCache(String data) {
		// BEGIN_INCLUDE(get_bitmap_from_mem_cache)
		BitmapDrawable memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(data);
		}

		if (BuildConfig.DEBUG && memValue != null) {
			// Log.d(TAG, "Memory cache hit");
		}

		return memValue;
		// END_INCLUDE(get_bitmap_from_mem_cache)
	}

	/**
	 * Get from disk cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromDiskCache(String key) {
		// BEGIN_INCLUDE(get_bitmap_from_disk_cache)
		// final String key = hashKeyForDisk(data);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final LruDiskCache.Snapshot snapshot = mDiskLruCache
							.get(key);
					if (snapshot != null) {
						if (BuildConfig.DEBUG) {
							// Log.d(TAG, "Disk cache hit");
						}
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream)
									.getFD();

							// Decode bitmap, but we don't want to sample so
							// give
							// MAX_VALUE as the target dimensions
							bitmap = BaseBitmapUtils
									.decodeSampledBitmapFromDescriptor(fd,
											Integer.MAX_VALUE,
											Integer.MAX_VALUE, this);
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}
			return bitmap;
		}
		// END_INCLUDE(get_bitmap_from_disk_cache)
	}

	/**
	 * @param options
	 *            - BitmapFactory.Options with out* options populated
	 * @return Bitmap that case be used for inBitmap
	 */
	public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
		// BEGIN_INCLUDE(get_bitmap_from_reusable_set)
		Bitmap bitmap = null;

		if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
			synchronized (mReusableBitmaps) {
				final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps
						.iterator();
				Bitmap item;

				while (iterator.hasNext()) {
					item = iterator.next().get();

					if (null != item && item.isMutable()) {
						// Check to see it the item can be used for inBitmap
						if (canUseForInBitmap(item, options)) {
							bitmap = item;

							// Remove from reusable set so it can't be used
							// again
							iterator.remove();
							break;
						}
					} else {
						// Remove from the set if the reference has been
						// cleared.
						iterator.remove();
					}
				}
			}
		}

		return bitmap;
		// END_INCLUDE(get_bitmap_from_reusable_set)
	}

	/**
	 * Clears both the memory and disk cache associated with this ImageCache
	 * object. Note that this includes disk access so this should not be
	 * executed on the main/UI thread.
	 */
	public void clear() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Memory cache cleared");
			}
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache cleared");
					}
				} catch (IOException e) {
					Log.e(TAG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	/**
	 * Flushes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache flushed");
					}
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	/**
	 * Closes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Disk cache closed");
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "close - " + e);
				}
			}
		}
	}

	/**
	 * @param candidate
	 *            - Bitmap to check
	 * @param targetOptions
	 *            - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use
	 *         with <code>targetOptions</code>
	 */
	@TargetApi(20)
	private static boolean canUseForInBitmap(Bitmap candidate,
			BitmapFactory.Options targetOptions) {
		// BEGIN_INCLUDE(can_use_for_inbitmap)
		if (!BaseVersionUtils.hasKitKat()) {
			// On earlier versions, the dimensions must match exactly and the
			// inSampleSize must be 1
			return candidate.getWidth() == targetOptions.outWidth
					&& candidate.getHeight() == targetOptions.outHeight
					&& targetOptions.inSampleSize == 1;
		}

		// From Android 4.4 (KitKat) onward we can re-use if the byte size of
		// the new bitmap
		// is smaller than the reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height
				* getBytesPerPixel(candidate.getConfig());
		/*********************  ****************************/
		return byteCount <= candidate.getAllocationByteCount();
		// return byteCount <= candidate.getByteCount();
		/*********************  ****************************/
		// END_INCLUDE(can_use_for_inbitmap)
	}

	/**
	 * Return the byte usage per pixel of a bitmap based on its configuration.
	 * 
	 * @param config
	 *            The bitmap configuration.
	 * @return The byte usage per pixel.
	 */
	private static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}

	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from
	 * Android 4.4 (KitKat) onward this returns the allocated memory size of the
	 * bitmap which can be larger than the actual bitmap data byte count (in the
	 * case it was re-used).
	 * 
	 * @param value
	 * @return size in bytes
	 */
	@TargetApi(20)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();

		// From KitKat onward use getAllocationByteCount() as allocated bytes
		// can potentially be
		// larger than bitmap byte count.
		if (BaseVersionUtils.hasKitKat()) {
			/********************* 已改 ****************************/
			// return bitmap.getAllocationByteCount();
			return bitmap.getByteCount();
			/*************************************************/
		}

		if (BaseVersionUtils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}

		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path
	 *            The path to check
	 * @return The space available in bytes
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(9)
	public static long getUsableSpace(File path) {
		if (BaseVersionUtils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	public Object getmDiskCacheLock() {
		return mDiskCacheLock;
	}

	public long size() {
		return mMemoryCache.size() + mDiskLruCache.size();
	}

	class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternal();
				break;
			case MESSAGE_INIT_DISK_CACHE:
				initDiskCacheInternal();
				break;
			case MESSAGE_FLUSH:
				flushCacheInternal();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternal();
				break;
			}
			return null;
		}
	}

	private void initDiskCacheInternal() {
		initDiskCache();
	}

	private void clearCacheInternal() {
		clear();

	}

	private void flushCacheInternal() {
		flush();

	}

	private void closeCacheInternal() {
		close();
	}

	public void clearCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	public void flushCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	public void closeCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}

}
