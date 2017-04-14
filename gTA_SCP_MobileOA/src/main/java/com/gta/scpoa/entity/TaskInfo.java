package com.gta.scpoa.entity;

/**
 * 待办、已办 item信息 
 * @author jianbin.liang
 */
public class TaskInfo {
	private String id;				//事项id
	private String name;			//姓名
	private String picPath;			//头像url路径
	private String title;			//事项标题
	private String detail;			//事项详细信息
	private String applyDate;		//申请时间
	private String dealState;		//办理状态 ，待办/已办
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPicPath() {
		return picPath;
	}
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getApplyDate() {
		return applyDate;
	}
	public void setApplyDate(String applyDate) {
		this.applyDate = applyDate;
	}
	public String getDealState() {
		return dealState;
	}
	public void setDealState(String dealState) {
		this.dealState = dealState;
	}
}
