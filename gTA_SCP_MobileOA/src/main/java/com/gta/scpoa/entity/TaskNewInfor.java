package com.gta.scpoa.entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TaskNewInfor  implements Serializable{
	private String id = ""; //任务ID
	private String creator = ""; //发起人
	private String subject = ""; //主题
	private String createTime = ""; //时间
	private String avatarUrl = ""; //头像的地址
	private String content = "";  //内容
	private int type = -1; //1为待办  2为已办
	private String runId = ""; //流程Id
	private String actInstId = "";  //
	private String createTime2 = "";
	private String processName = ""; //表单名称
	
	/*公文公告专用*/
	private String copyId = "";   //抄送转发id
	private String isReaded = ""; //是否已读
	private String readTime = "";  //阅读时间
	
	//会议
	private String meetRoom = "";
	private String meetStartTime = "";
	private String meetEndTime = "";
	
	public String getMeetRoom() {
		return meetRoom;
	}
	public void setMeetRoom(String meetRoom) {
		this.meetRoom = meetRoom;
	}
	public String getMeetStartTime() {
		return meetStartTime;
	}
	public void setMeetStartTime(String meetStartTime) {
		this.meetStartTime = meetStartTime;
	}
	public String getMeetEndTime() {
		return meetEndTime;
	}
	public void setMeetEndTime(String meetEndTime) {
		this.meetEndTime = meetEndTime;
	}
	public String getCopyId() {
		return copyId;
	}
	public void setCopyId(String copyId) {
		this.copyId = copyId;
	}
	public String getIsReaded() {
		return isReaded;
	}
	public void setIsReaded(String isReaded) {
		this.isReaded = isReaded;
	}
	public String getReadTime() {
		return readTime;
	}
	public void setReadTime(String readTime) {
		this.readTime = readTime;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getCreateTime2() {
		
		return createTime2;
	}
	public void setCreateTime2(String createTime2) {
		this.createTime2 = createTime2;
	}
	public String getActInstId() {
		return actInstId;
	}
	public void setActInstId(String actInstId) {
		this.actInstId = actInstId;
	}
	public String getRunId() {
		return runId;
	}
	public void setRunId(String runId) {
		this.runId = runId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getAvatarUrl() {
		
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "TaskNewInfor [id=" + id + ", creator=" + creator + ", subject="
				+ subject + ", createTime=" + createTime + ", avatarUrl="
				+ avatarUrl + ", content=" + content + ", type=" + type
				+ ", runId=" + runId + ", actInstId=" + actInstId
				+ ", createTime2=" + createTime2 + ", processName="
				+ processName + ", copyId=" + copyId + ", isReaded=" + isReaded
				+ ", readTime=" + readTime + ", meetRoom=" + meetRoom
				+ ", meetStartTime=" + meetStartTime + ", meetEndTime="
				+ meetEndTime + "]";
	}
	
	
}
