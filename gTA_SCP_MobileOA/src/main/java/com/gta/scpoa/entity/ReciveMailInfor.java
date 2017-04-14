package com.gta.scpoa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gta.db.annotation.Column;
import com.gta.db.annotation.Id;
import com.gta.db.annotation.Table;


@Table(getTableName = "t_mailInfor")
public class ReciveMailInfor implements Serializable {
	
	private int  dbId = 0;   //用于数据库里面的ID
	
	private String id = ""; //邮件ID
	
	private String UserName = "" ; //发送人 + ID
	
	private String OutBoxContent = "";//邮件正文的html
	
	private String ReceiverUsers = ""; //接收人和ID
	
	private String OutBoxTheme = ""; //接收主题
	
	private String OutBoxCopyer = ""; //抄送人和ID
	
	private String OutBoxSecret = ""; //密送人和ID
	
	private boolean IsreturnReceipt = false;//是不是回执
	
	private String CreateTime = ""; //邮件时间
	
	
	
	private List<MailAttachInfo> attachLists = new ArrayList<MailAttachInfo>();
	
//	public String getAttachFileId(int position){
//		if(position>=attachLists.size()) return "";
//		return attachLists.get(position).getId();
//	}
//	
//	/*获取文件名 + 后缀名*/
//	public String getFileName(int position){
//		if(position>=attachLists.size()) return "";
//		return attachLists.get(position).getFileName()
//				+ attachLists.get(position).getFileType();
//	}
//	
//	public String getFileNameAddID(int position){
//		if(position>=attachLists.size()) return "";
//		return attachLists.get(position).getFileName() + "-"
//				+ attachLists.get(position).getId()
//				+ attachLists.get(position).getFileType();
//	}
//	
//	/*获取文件大小*/
//	public int getFileSize(int position){
//		if(position>=attachLists.size()) return 0;
//		return attachLists.get(position).getFileSize();
//	}
//	
//	/*获取文件路径*/
//	public String getFilePath(int position){
//		if(position>=attachLists.size()) return "";
//		return attachLists.get(position).getFilePath()+attachLists.get(position).getFileType();
//	}
	
	public List<MailAttachInfo> getAttachLists() {
		return attachLists;
	}

	public void setAttachLists(List<MailAttachInfo> attachLists) {
		this.attachLists = attachLists;
	}

	public String getReceiverUsers() {
		return ReceiverUsers;
	}

	public void setReceiverUsers(String receiverUsers) {
		ReceiverUsers = receiverUsers;
	}

	public String getOutBoxTheme() {
		return OutBoxTheme;
	}

	public void setOutBoxTheme(String outBoxTheme) {
		OutBoxTheme = outBoxTheme;
	}

	public String getOutBoxCopyer() {
		return OutBoxCopyer;
	}

	public void setOutBoxCopyer(String outBoxCopyer) {
		OutBoxCopyer = outBoxCopyer;
	}

	public String getOutBoxSecret() {
		return OutBoxSecret;
	}

	public void setOutBoxSecret(String outBoxSecret) {
		OutBoxSecret = outBoxSecret;
	}

	public boolean isIsreturnReceipt() {
		return IsreturnReceipt;
	}

	public void setIsreturnReceipt(boolean isreturnReceipt) {
		IsreturnReceipt = isreturnReceipt;
	}

	public String getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getOutBoxContent() {
		return OutBoxContent;
	}

	public void setOutBoxContent(String outBoxContent) {
		OutBoxContent = outBoxContent;
	}

	
}
