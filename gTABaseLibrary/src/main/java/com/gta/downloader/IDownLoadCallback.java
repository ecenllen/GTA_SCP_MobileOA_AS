package com.gta.downloader;

public interface IDownLoadCallback {
	void onStrat(DownloadInfo downloadInfo);

	void onProgressUpdate(DownloadInfo downloadInfo);

	void onSuccess(DownloadInfo downloadInfo);

	void onCancelled(DownloadInfo downloadInfo);
	
	void onFialed(DownloadInfo downloadInfo);
}
