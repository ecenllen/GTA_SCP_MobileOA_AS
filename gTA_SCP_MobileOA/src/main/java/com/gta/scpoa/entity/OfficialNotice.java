package com.gta.scpoa.entity;

/**
 * 实体类-公文公告(用于一级页面公文公告的列表)，用于OfficialNoticeActivity的ListView
 * 
 * @author bin.wang1
 * 
 */
public class OfficialNotice {
	private int id;
	private String title;
	private int type;// 类型 公文/公告
	private String department;// 发文部门
	private String date;

	public OfficialNotice() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
