package com.gta.downloader;

import java.io.File;
import java.io.Serializable;

import android.view.View;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;
import com.gta.db.annotation.Table;

@Table(getTableName = "DownloadInfo")
public class DownloadInfo implements Serializable {

	private static final long serialVersionUID = 8494755766951525901L;
	public static final int READY = 0;
	public static final int WAITING = 1;
	public static final int STOP = 2;
	public static final int LOADING = 3;
	public static final int SUCCESS = 4;
	public static final int FAILED = -1;
	public int STATE = READY;
	/* 主键id，自增长 */
	@Id(generator = "AUTOINCREMENT")
	@Column(name = "_id")
	public int id;
	/* 文件 */
	public File file;
	/* 对应的临时文件 */
	public File tempFile;
	/* 文件URL */
	@Column(name = "downloadUrl")
	public String downloadUrl;
	/* 文件名 */
	@Column(name = "fileName")
	public String fileName;
	/* 文件目录路径 */
	@Column(name = "fileSaveDir")
	public String fileSaveDir;
	/* 文件进度 */
	@Column(name = "progress")
	public int progress;
	/* 已下载的长度 */
	@Column(name = "currentLenght")
	public long currentLenght;
	/* 文件总长度 */
	@Column(name = "fileLength")
	public long fileLength;
	/* 响应状态码 */
	@Column(name = "statusCode")
	public int statusCode;
	/* 下载速度 */
	@Column(name = "networkSpeed")
	public long networkSpeed;
	/* 下载者 */
	public DownLoader  downLoader;
	/* AbsListView:getView():convertView 复用的view */
	public View convertView;
	/* 下载时的回调 */
	public IDownLoadCallback  downLoadCallback;

	/**
	 * 应该需要一个空的构造方法
	 */
	public DownloadInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param downloadUrl
	 *            下载地址
	 * @param fileSaveDir
	 *            保存的文件夹
	 */
	public DownloadInfo(String downloadUrl, String fileSaveDir) {
		this.downloadUrl = downloadUrl;
		this.fileSaveDir = fileSaveDir;
	}

	public DownloadInfo(String downloadUrl, String fileName, String fileSaveDir) {
		super();
		this.downloadUrl = downloadUrl;
		this.fileName = fileName;
		this.fileSaveDir = fileSaveDir;
	}

	


}
