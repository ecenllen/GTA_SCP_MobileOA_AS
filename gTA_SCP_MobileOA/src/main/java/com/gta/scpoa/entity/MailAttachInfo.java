package com.gta.scpoa.entity;

import java.io.Serializable;

public class MailAttachInfo implements Serializable {
	/**
	 * 文件名
	 */
	private String FileName = "";
	
	/**
	 * 文件路径
	 */
	private String FilePath = "";
	
	/**
	 * 文件大小
	 */
	private int FileSize = 0 ;
	
	/**
	 * 文件类型
	 */
	private String FileType = "";
	
	/**
	 * 文件Id
	 */
	private String Id = "";

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getFilePath() {
		return FilePath;
	}

	public void setFilePath(String filePath) {
		FilePath = filePath;
	}

	public int getFileSize() {
		return FileSize;
	}

	public void setFileSize(int fileSize) {
		FileSize = fileSize;
	}

	public String getFileType() {
		return FileType;
	}

	public void setFileType(String fileType) {
		FileType = fileType;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}
}
