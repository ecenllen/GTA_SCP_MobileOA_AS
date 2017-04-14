package com.gta.downloader;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.AsyncTask;

import com.gta.util.BaseVersionUtils;

public class DownloadManager<T extends DownloadInfo> implements IDownload<T> {
	// private static DownloadManager manager;
	private ArrayList<T> DownloadList;
	private ExecutorService executorService;
	private int nThreads = 3;
	private Context context;

	// public synchronized static DownloadManager getInstance() {
	//
	// if (null == manager) {
	// manager = new DownloadManager();
	// }
	// return manager;
	//
	// }

	public DownloadManager(Context context) {
		this.context=context;
		DownloadList = new ArrayList<T>();
		executorService = Executors
				.newFixedThreadPool(nThreads, sThreadFactory);
	}

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "GTA_DOWNLOADER#"
					+ mCount.getAndIncrement());
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			return thread;
		}
	};

	@Override
	public void addDownload(T info) {
		if (null == info && DownloadList.contains(info))
			return;
		DownloadList.add(info);
		DownLoader downLoader = new DownLoader(new FileDownloadHandler(), info,context);
		info.downLoader = downLoader;
	}

	@Override
	public void removeDownload(T info) {
		if (null == info)
			return;
		stopDownload(info);
		DownloadList.remove(info);

	}

	@Override
	public void startOrResumeDownload(T downloadInfo) {
		if (null == downloadInfo)
			return;
		DownLoader downLoader = downloadInfo.downLoader;
		if (null != downLoader)
			if (downLoader.getStatus() == AsyncTask.Status.RUNNING) {
				return;
			} else if (downLoader.getStatus() == AsyncTask.Status.PENDING) {
				executeTask(downLoader);
				return;
			}
		downLoader = new DownLoader(new FileDownloadHandler(), downloadInfo,context);
		downloadInfo.downLoader = downLoader;
		executeTask(downLoader);
	}

	public void executeTask(DownLoader downLoader) {
		if (BaseVersionUtils.hasHoneycomb()) {
			downLoader.executeOnExecutor(executorService);
		} else {
			downLoader.execute();
		}
	}

	@Override
	public void stopDownload(DownloadInfo info) {
		info.downLoader.stopDownload();

	}

	@Override
	public DownloadInfo getDownloadInfo(int index) {
		return DownloadList.get(index);

	}

	@Override
	public int getDownloadCount() {
		return DownloadList.size();

	}

	public ArrayList<T> getDownloadList() {
		return DownloadList;
	}

	public void setDownloadList(ArrayList<T> downloadList) {
		DownloadList = downloadList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startAllDownload() {
		int size = DownloadList.size();
		for (int i = 0; i < size; i++) {
			DownloadInfo downloadInfo = DownloadList.get(i);
			if (null != downloadInfo
					&& downloadInfo.STATE != DownloadInfo.LOADING) {
				startOrResumeDownload((T) downloadInfo);
			}
		}

	}

	@Override
	public void removeAllDownload() {
		int size = DownloadList.size();
		for (int i = 0; i < size; i++) {
			DownloadInfo downloadInfo = DownloadList.get(i);
			if (null != downloadInfo) {
				stopDownload(downloadInfo);
			}
		}
		DownloadList.clear();
	}

}
