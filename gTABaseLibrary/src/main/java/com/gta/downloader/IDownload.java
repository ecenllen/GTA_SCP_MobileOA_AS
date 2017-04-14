package com.gta.downloader;

public interface IDownload <T extends DownloadInfo >{
	/**
	 * 加入下载
	 * 
	 * @param info
	 */
	void addDownload(T info);

	/**
	 * 移除下载
	 * 
	 * @param info
	 */
	void removeDownload(T info);

	/**
	 * 开始或者恢复下载
	 * 
	 * @param info
	 */
	void startOrResumeDownload(T info);

	/**
	 * 停止下载
	 * 
	 * @param info
	 */
	void stopDownload(T info);

	/**
	 * 开始全部下载列表
	 */
	void startAllDownload();

	/**
	 * 移除全部下载列表
	 */
	void removeAllDownload();

	/**
	 * 获得下载列表总数
	 * 
	 * @return
	 */
	int getDownloadCount();

	/**
	 * 获得下载信息
	 * 
	 * @param index 下标
	 * @return 返回下载信息类
	 */
	DownloadInfo getDownloadInfo(int index);

}
