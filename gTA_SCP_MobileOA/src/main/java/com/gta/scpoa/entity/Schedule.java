package com.gta.scpoa.entity;

import java.io.Serializable;

/**
 * 实体类-日程
 * 
 * @author bin.wang1
 * 
 */
public class Schedule implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 20L;

	private int Id;// 日程id

	private int ScheduleType;//日程类型(1:工作事务,2:个人事务)
	private int Remind;//提醒周期(1:单次提醒,5:不提醒,2:每日提醒,3:每周提醒,4:每月提醒)
	private String StartTime;// 开始时间
	private String EndTime;// 结束时间
	private String ScheduleContent;// 日程内容
	private int Status;// 日程安排完成状态(0:未完成,1:已完成)
	private boolean IsExternal;// 是否为外部资源（如为true，则不能编辑）
	
	private boolean hasNoti = false;
	
	public Schedule() {
		super();
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	
	public int getScheduleType() {
		return ScheduleType;
	}
	public void setScheduleType(int scheduleType) {
		ScheduleType = scheduleType;
	}
	public int getRemind() {
		return Remind;
	}
	public void setRemind(int remind) {
		Remind = remind;
	}
	public String getStartTime() {
		return StartTime;
	}
	public void setStartTime(String startTime) {
		StartTime = startTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	public String getScheduleContent() {
		return ScheduleContent;
	}
	public void setScheduleContent(String scheduleContent) {
		ScheduleContent = scheduleContent;
	}
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
	public boolean isIsExternal() {
		return IsExternal;
	}
	public void setIsExternal(boolean isExternal) {
		IsExternal = isExternal;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public boolean isHasNoti() {
		return hasNoti;
	}
	public void setHasNoti(boolean hasNoti) {
		this.hasNoti = hasNoti;
	}
	@Override
	public String toString() {
		return "Schedule [Id=" + Id + ", ScheduleType=" + ScheduleType
				+ ", Remind=" + Remind + ", StartTime=" + StartTime
				+ ", EndTime=" + EndTime + ", ScheduleContent="
				+ ScheduleContent + ", Status=" + Status + ", IsExternal="
				+ IsExternal + ", hasNoti=" + hasNoti + "]";
	}
	
	
}

