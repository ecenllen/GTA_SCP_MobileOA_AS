package com.gta.scpoa.entity;

import java.io.Serializable;

/**
 * 联系人信息
 * 
 * @author shengping.pan
 * 
 */
@SuppressWarnings("serial")
public class ContactInfo implements Serializable {

	/**
	 * 通讯录id
	 */
	private String contactId;
	
	/**
	 * 邮件通讯录人员标识
	 */
	private String userId;

	/**
	 * 通讯录名称
	 */
	private String contactName;

	/**
	 * 手机号
	 */
	private String mobilePhone;

	/**
	 * 电子邮箱
	 */
	private String email;

	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 职务
	 */
	private String duty;

	/**
	 * 工作电话
	 */
	private String deptPhone;

	/**
	 * 工作单位地址
	 */
	private String workAddress;

	/**
	 * 家庭地址
	 */
	private String homeAddress;

	/**
	 * 家庭电话
	 */
	private String homePhone;

	/**
	 * 联系人类型
	 */
	private String contactType;

	/**
	 * 显示数据拼音的首字母
	 */
	private String sortLetters;

	/**
	 * 是否选中
	 */
	private boolean isSelected;

	/**
	 * 性别
	 */
	private String sex;

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactName() {
		if(null == contactName){
			this.contactName = "";
		}
		return contactName;
	}

	public void setContactName(String contactName) {
		if(null == contactName){
			this.contactName = "";
		}
		this.contactName = contactName;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		if(null == mobilePhone){
			this.mobilePhone = "";
		}
		this.mobilePhone = mobilePhone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if(null == email){
			this.email = "";
		}
		this.email = email;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String unitName) {
		if(null == unitName){
			this.deptName = "";
		}
		this.deptName = unitName;
	}

	public String getDuty() {
		return duty;
	}

	public void setDuty(String duty) {
		if(null == duty){
			this.duty = "";
		}
		this.duty = duty;
	}

	public String getDeptPhone() {
		return deptPhone;
	}

	public void setDeptPhone(String deptPhone) {
		if(null == deptPhone){
			this.deptPhone = "";
		}
		this.deptPhone = deptPhone;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		if(null == homeAddress){
			this.homeAddress = "";
		}
		this.homeAddress = homeAddress;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public void setHomePhone(String homePhone) {
		if(null == homePhone){
			this.homePhone = "";
		}
		this.homePhone = homePhone;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		if(null == contactType){
			this.contactType = "";
		}
		this.contactType = contactType;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getWorkAddress() {
		return workAddress;
	}

	public void setWorkAddress(String workAddress) {
		if(null == workAddress){
			this.workAddress = "";
		}
		this.workAddress = workAddress;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		if(null == userId){
			this.userId = "";
		}
		this.userId = userId;
	}

}
