package com.gta.downloader;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.os.AsyncTask;

import com.gta.http.HttpclientNetWork;
import com.gta.util.BaseNetWorkUtil;

public class DownLoader extends AsyncTask<Void, DownloadInfo, DownloadInfo> {

	public int STATE = -1;
	private DownloadInfo downloadInfo;
	private IDownloadHandler iDownloadHandler;
	private HttpclientNetWork network;
	private IDownLoadCallback  downLoadCallback;
	private long fileLength;
	private final String TEMP_SUFFIX = ".download";
	private boolean interrupt = false;
	private HttpGet httpGet;

	public DownLoader(IDownloadHandler iDownloadHandler,
			DownloadInfo downloadInfo,Context context) {
		this.iDownloadHandler = iDownloadHandler;
		this.network = new HttpclientNetWork(context);
		this.downloadInfo =downloadInfo;
	}

	@Override
	protected DownloadInfo doInBackground(Void... params) {
		if (null == downloadInfo && null == downloadInfo.downloadUrl)
			return downloadInfo;
		if (downloadInfo.STATE != DownloadInfo.LOADING && !interrupt
				&& !isCancelled()) {
			downloadInfo.STATE=DownloadInfo.LOADING;
			HttpResponse httpResponse = null;
			try {
				httpResponse = getResponse(downloadInfo);// 请求
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				downloadInfo.statusCode=statusCode;
				if (statusCode == HttpStatus.SC_OK
						|| statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
					makeFileDir();
					downloadInfo =  iDownloadHandler.parseResponse(downloadInfo,// 解析
							httpResponse);
				} else {
					downloadInfo.STATE=DownloadInfo.FAILED;
				}
			} catch (Exception e) {
				downloadInfo.STATE=DownloadInfo.FAILED;
				// e.printStackTrace();
			} finally {
				abort();
			}
		}
		return downloadInfo;
	}

	/**
	 * 创建目录
	 */
	public void makeFileDir() {
		String fileName = downloadInfo.fileName;
		downloadInfo.file=new File(downloadInfo.fileSaveDir, fileName);
		downloadInfo.tempFile=new File(downloadInfo.fileSaveDir,
				fileName + TEMP_SUFFIX);
		File DirFile = new File(downloadInfo.fileSaveDir);
		downloadInfo.fileSaveDir=downloadInfo.fileSaveDir;
		if (!DirFile.exists()) {
			DirFile.mkdirs();
		}

	}

	public HttpResponse getResponse(DownloadInfo downloadInfo)
			throws ClientProtocolException, IOException {
		return httpGet(downloadInfo);
	}

	/**
	 * GET请求
	 * 
	 * @param downloadInfo
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HttpResponse httpGet(DownloadInfo downloadInfo)
			throws ClientProtocolException, IOException {
		HttpResponse httpResponse = null;
		String fileName = downloadInfo.fileName;
		String url = downloadInfo.downloadUrl;
		if (null == fileName) {
			fileName = BaseNetWorkUtil.getFileNameFromUrl(url);
			if (!"".equals(fileName)) {
				downloadInfo.fileName=fileName;
			} else {
				fileName = getFileNameByHttp(downloadInfo);// 尝试从网络获取文名
				if (null != fileName) {
					downloadInfo.fileName=fileName;
				}
			}
		}
		// 临时文件是否存在
		File file = new File(downloadInfo.fileSaveDir, fileName
				+ TEMP_SUFFIX);
		if (file.exists() && !file.isDirectory()) {
			fileLength = file.length();
		}
		httpGet = new HttpGet(downloadInfo.downloadUrl);
		if (fileLength > 0) {
			httpGet.setHeader("RANGE", "bytes=" + fileLength + "-");
		}
		httpResponse = network.httpClient.execute(httpGet,HttpclientNetWork.httpContext);
		return httpResponse;

	}

	/**
	 * 尝试从网络获取文名
	 * 
	 * @param downloadInfo
	 * @return
	 */
	private String getFileNameByHttp(DownloadInfo downloadInfo) {
		String url = downloadInfo.downloadUrl;
		String fileName = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = network.httpClient.execute(httpGet,HttpclientNetWork.httpContext);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			downloadInfo.statusCode=statusCode;
			if (statusCode == HttpStatus.SC_OK) {
				fileName = BaseNetWorkUtil
						.getFileNameFromHttpResponse(httpResponse);
				if (null != fileName) {
					downloadInfo.fileName=fileName;
				}
			} else {
				downloadInfo.STATE=DownloadInfo.FAILED;
			}

		} catch (Exception e) {
			downloadInfo.STATE=DownloadInfo.FAILED;
			e.printStackTrace();
		} finally {
			if (httpGet != null && !httpGet.isAborted()) {
				httpGet.abort();
			}
		}

		return fileName;

	}

	// 终止下载
	protected void stopDownload() {
		interrupt = true;
		downloadInfo.STATE=DownloadInfo.STOP;
		iDownloadHandler.setCancel(true);
		if (!isCancelled()) {
			cancel(true);
		}
		abort();
		if (null != downLoadCallback) {
			downLoadCallback.onCancelled(downloadInfo);
		}

	}

	@Override
	protected void onProgressUpdate(DownloadInfo... downloadInfos) {
		super.onProgressUpdate(downloadInfos[0]);
		if (null != downLoadCallback) {
			int progress = (int) (100 * downloadInfo.currentLenght / downloadInfo
					.fileLength);
			downloadInfos[0].progress=progress;
			downLoadCallback.onProgressUpdate(downloadInfo);
		}
	}

	@Override
	protected void onPreExecute() {
		this.downLoadCallback = downloadInfo.downLoadCallback;
		if (null != downLoadCallback) {
			downloadInfo.STATE=DownloadInfo.WAITING;
			downLoadCallback.onStrat(downloadInfo);
		}
	}

	@Override
	protected void onPostExecute(DownloadInfo downloadInfo) {
		super.onPostExecute(downloadInfo);
		downloadInfo.networkSpeed=0;
		if (downloadInfo.STATE == DownloadInfo.FAILED) {
			if (null != downLoadCallback)
				downLoadCallback.onFialed(downloadInfo);
			abort();
			return;
		}
		if (downloadInfo.STATE != DownloadInfo.STOP
				&& downloadInfo.currentLenght == downloadInfo
						.fileLength) {
			downloadInfo.STATE=DownloadInfo.SUCCESS;
			if (null != downLoadCallback)
				downLoadCallback.onSuccess(downloadInfo);
			abort();
		}
	}

	private void abort() {
		if (httpGet != null && !httpGet.isAborted()) {
			httpGet.abort();
		}
	}

	/**
	 * {@link FileDownloadHandler #startTimer()}
	 * 
	 * @param downloadInfo
	 */
	public void onLoading(DownloadInfo downloadInfo) {
		publishProgress(downloadInfo);
	}
}
