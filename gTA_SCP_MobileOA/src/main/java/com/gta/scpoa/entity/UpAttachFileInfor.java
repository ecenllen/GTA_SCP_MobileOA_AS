package com.gta.scpoa.entity;

import java.io.Serializable;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;
import com.gta.db.annotation.Table;

/*邮件附件的列表*/
@Table(getTableName = "t_UpAttachFile")
public class UpAttachFileInfor implements Serializable {
	
	@Id(generator = "AUTOINCREMENT")
	@Column(name = "Id", type = "INTEGER")
	private int id = 0;  //id
	
	@Column(name = "MessageId", type = "TEXT")
	private String myMessageId = "";  //邮件的ID   新建  快速回复是自定义  取时间   草稿的话是去服务器
	
	@Column(name = "FileName", type = "TEXT")
	private String fileName = "" ; //上传附件的名字
	
	@Column(name = "FilePath", type = "TEXT")
	private String filePath = ""; //文件的路径
	
	@Column(name = "IsFromNet", type = "TEXT")
	private String isFromNet = "0";  //0为false  1为true

	@Column(name = "FileSize", type = "TEXT")
	private String fileSize = "0";  //文件的大小
	
	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMyMessageId() {
		return myMessageId;
	}

	public void setMyMessageId(String myMessageId) {
		this.myMessageId = myMessageId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getIsFromNet() {
		return isFromNet;
	}

	public void setIsFromNet(String isFromNet) {
		this.isFromNet = isFromNet;
	}
	
}
