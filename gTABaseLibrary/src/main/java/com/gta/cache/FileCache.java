/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gta.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StatFs;

import com.gta.gtabasenetwork.BuildConfig;
import com.gta.util.BaseFileUtils;
import com.gta.util.BaseIoUtils;
import com.gta.util.BaseVersionUtils;

/**
 * Author: wyouflf Date: 13-8-1 Time: 下午12:04
 */
public class FileCache {

	private LruMemoryCache<String, byte[]> mMemoryCache;
	private LruDiskCache mLruDiskCache;
	private final static int DEFAULT_MEMCACHE_SIZE = 1024 * 100;// 100K
	private final static int DEFAULT_DISKCACHE_SIZE = 1024 * 1024 * 5;// 5M
	private final static long DEFAULT_EXPIRY_TIME = 1000 * 60; // 60
																// seconds
	protected boolean mPauseWork = false;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int MESSAGE_CLEAR = 0;
	private static final int MESSAGE_INIT_DISK_CACHE = 1;
	private static final int MESSAGE_FLUSH = 2;
	private static final int MESSAGE_CLOSE = 3;
	private CacheParams mCacheParams;
	private static final int DISK_CACHE_INDEX = 0;
	public static FileCache httpCache;

	public synchronized static FileCache getInstance(CacheParams cacheParams) {
		if (httpCache == null) {
			httpCache = new FileCache(cacheParams.appContext, cacheParams);
		}
		return httpCache;

	}

	private FileCache(Context context, CacheParams cacheParams) {
		ini(context, cacheParams);

	}

	/**
	 * 使用前必须初始化
	 * 
	 * @param context
	 * @param cacheParams
	 */
	private void ini(Context context, CacheParams cacheParams) {
		checkCacheParams(cacheParams);
		if (mCacheParams.memoryCacheEnabled) {
			mMemoryCache = new LruMemoryCache<String, byte[]>(
					mCacheParams.memCacheSize) {
				@Override
				protected int sizeOf(String key, byte[] value) {
					if (value == null)
						return 0;
					return value.length;
				}
			};
		}
		iniDisk();
	}

	public void iniDisk() {
		iniDiskCache();
	}

	private void checkCacheParams(CacheParams mCacheParams) {
		if (mCacheParams == null) {
			new IllegalArgumentException("cacheParams must be nut null");
		}
		this.mCacheParams = mCacheParams;
		mCacheParams.memCacheSize = mCacheParams.memCacheSize <= 0 ? DEFAULT_MEMCACHE_SIZE
				: mCacheParams.memCacheSize;
		mCacheParams.diskCacheSize = mCacheParams.diskCacheSize <= 0 ? DEFAULT_DISKCACHE_SIZE
				: mCacheParams.diskCacheSize;
		mCacheParams.expiryTime = mCacheParams.expiryTime <= 0 ? DEFAULT_EXPIRY_TIME
				: mCacheParams.expiryTime;

	}

	@SuppressWarnings("deprecation")
	@TargetApi(9)
	public static long getUsableSpace(File path) {
		if (BaseVersionUtils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * 数据存入缓存
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, byte[] value) {
		if (key == null || value == null) {
			return;
		}
		long expiryTime = System.currentTimeMillis() + mCacheParams.expiryTime;
		if (mMemoryCache != null)
			mMemoryCache.put(key, value, expiryTime);
		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mLruDiskCache != null) {
				// final String key = hashKeyForDisk(data);//已经转换了
				OutputStream out = null;
				try {
					LruDiskCache.Snapshot snapshot = mLruDiskCache.get(key);
					if (snapshot == null) {
						final LruDiskCache.Editor editor = mLruDiskCache
								.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							out.write(value, 0, value.length);
							// 内部默认过期时间为long.max
							editor.setEntryExpiryTimestamp(expiryTime);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
				} catch (Exception e) {
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
	 * 从磁盘取缓存数据
	 * 
	 * @param key
	 * @return
	 */
	public byte[] getBufferFromDiskCache(String key) {
		byte[] bytes = null;
		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mLruDiskCache != null) {
				InputStream inputStream = null;
				try {
					final LruDiskCache.Snapshot snapshot = mLruDiskCache
							.get(key);
					if (snapshot != null) {
						if (BuildConfig.DEBUG) {
						}
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							bytes = BaseIoUtils.readStreamToByte(inputStream);
						}
					}
				} catch (final Exception e) {
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}
			return bytes;
		}

	}

	/**
	 * 从内存取缓存数据
	 * 
	 * @param url
	 * @return 无数据返回null
	 */
	public byte[] getBufferFromMemCache(String url) {
		if (mMemoryCache != null) {
			return mMemoryCache.get(url);
		} else {

			return null;
		}
	}

	/**
	 * 取缓存数据（内存或者磁盘）
	 * 
	 * @param url
	 * @return 无数据返回null
	 */
	public byte[] getBufferFromCache(String url) {
		byte[] bytes = null;
		bytes = getBufferFromMemCache(url);
		if (bytes == null)
			bytes = getBufferFromDiskCache(url);

		return bytes;

	}

	/**
	 * 清除内存+磁盘缓存
	 */
	public void clear() {
		if (mMemoryCache != null)
			mMemoryCache.evictAll();
		clearDiskCache();
	}

	/**
	 * 返回内存+磁盘缓存大小
	 * 
	 * @return
	 */
	public long size() {
		if (mMemoryCache != null)
			return mMemoryCache.size() + mLruDiskCache.size();
		else
			return 0;

	}

	protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

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

	protected void initDiskCacheInternal() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mLruDiskCache == null || mLruDiskCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mLruDiskCache = LruDiskCache.open(diskCacheDir, 1,
									1, mCacheParams.diskCacheSize);
							if (BuildConfig.DEBUG) {
							}
						} catch (final IOException e) {
							mCacheParams.diskCacheDir = null;
						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	protected void clearCacheInternal() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			if (BuildConfig.DEBUG) {
			}
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mLruDiskCache != null && !mLruDiskCache.isClosed()) {
				try {
					mLruDiskCache.delete();
					if (BuildConfig.DEBUG) {
					}
				} catch (IOException e) {
				}
				mLruDiskCache = null;
				initDiskCacheInternal();
			}
		}
	}

	protected void flushCacheInternal() {
		synchronized (mDiskCacheLock) {
			if (mLruDiskCache != null) {
				try {
					mLruDiskCache.flush();
					if (BuildConfig.DEBUG) {
					}
				} catch (IOException e) {
				}
			}
		}
	}

	protected void closeCacheInternal() {
		synchronized (mDiskCacheLock) {
			if (mLruDiskCache != null) {
				try {
					if (!mLruDiskCache.isClosed()) {
						mLruDiskCache.close();
						mLruDiskCache = null;
						if (BuildConfig.DEBUG) {
						}
					}
				} catch (IOException e) {
				}
			}
		}
	}

	public void iniDiskCache() {
		new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
	}

	public void clearDiskCache() {
		new CacheAsyncTask().execute(MESSAGE_CLEAR);
	}

	public void flushDiskCache() {
		new CacheAsyncTask().execute(MESSAGE_FLUSH);
	}

	public void closeDiskCache() {
		new CacheAsyncTask().execute(MESSAGE_CLOSE);
	}

	public static class CacheParams {
		/* 内存缓存 */
		public int memCacheSize;
		/* 磁盘缓存 */
		public long diskCacheSize;
		/* 磁盘缓存目录 */
		public File diskCacheDir;
		/* 开启/关闭内存缓存 */
		public boolean memoryCacheEnabled = true;
		/* 开启/关闭磁盘缓存 */
		public boolean diskCacheEnabled = true;
		/* 缓存有效时间 */
		public long expiryTime;
		/* appContext */
		public Context appContext;

		public CacheParams(Context context) {
			this(context, "file");
		}

		public CacheParams(Context appContext, String diskCacheDirectoryName) {
			this.appContext = appContext.getApplicationContext();
			diskCacheDir = BaseFileUtils.getDiskCacheDir(appContext,
					diskCacheDirectoryName);
		}

	}

}
