package com.gta.downloader;

import org.apache.http.HttpResponse;

public interface IDownloadHandler {

	DownloadInfo parseResponse(DownloadInfo downloadInfo, HttpResponse response) throws Exception;

	void setCancel(boolean cancel);
}
