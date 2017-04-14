package com.gta.scpoa.entity;

public class MailInfor {
	

	private boolean isRead = false;  //是否已读
	private String userName = "";    //发件人
	private String id = "";              //邮件的ID
	private String outBoxTheme = "";  //邮件的主题
	private String createTime = "";   //创建邮件的时间
	private String contentString = "";  //邮件的内容
	private boolean isAttach = false;   //是否有附件
	private boolean Status = false;  //是否发送  
	private boolean IsDel  = false;  //是否删除
	private int MailType = 1;      //邮件类型
	
	
	/*cxj添加   当前的类是不是删除标记删除状态*/
	private boolean isPrefreDel = false;
	
	public boolean isPrefreDel() {
		return isPrefreDel;
	}

	public void setPrefreDel(boolean isPrefreDel) {
		this.isPrefreDel = isPrefreDel;
	}

	public int getMailType() {
		return MailType;
	}

	public void setMailType(int mailType) {
		MailType = mailType;
	}

	public boolean isStatus() {
		return Status;
	}

	public void setStatus(boolean status) {
		Status = status;
	}

	public boolean isIsDel() {
		return IsDel;
	}

	public void setIsDel(boolean isDel) {
		IsDel = isDel;
	}

	
	public boolean isRead() {
		return isRead;
	}
	
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getOutBoxTheme() {
		return outBoxTheme;
	}
	
	public void setOutBoxTheme(String outBoxTheme) {
		this.outBoxTheme = outBoxTheme;
	}
	
	public String getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String getContentString() {
		return contentString;
	}
	
	public void setContentString(String contentString) {
		this.contentString = contentString;
	}
	
	public boolean isAttach() {
		return isAttach;
	}
	
	public void setAttach(boolean isAttach) {
		this.isAttach = isAttach;
	}
}
