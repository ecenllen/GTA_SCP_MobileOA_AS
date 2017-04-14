package com.gta.scpoa.entity;

/**
 * 实体类-用户
 * 
 * @author bin.wang1
 * 
 */
public class User {

	private boolean Successed;
	private String ErrorMsg;

	private String FullName;
	private String BpmHost;
	private String UserId; // 唯一标识
	private String LoginName;// 登录名
	private String UserName;// 用户的名字
	private String Password;
	private String AvatarUrl;// 头像的url
	private String Department;
	private String Telephone;
	private String Email;

	public User() {
		super();
	}

	public boolean isSuccessed() {
		return Successed;
	}

	public void setSuccessed(boolean successed) {
		Successed = successed;
	}

	public String getErrorMsg() {
		return ErrorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		ErrorMsg = errorMsg;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getLoginName() {
		return LoginName;
	}

	public void setLoginName(String loginName) {
		LoginName = loginName;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getAvatarUrl() {
		return AvatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		AvatarUrl = avatarUrl;
	}

	public String getDepartment() {
		return Department;
	}

	public void setDepartment(String department) {
		Department = department;
	}

	public String getTelephone() {
		return Telephone;
	}

	public void setTelephone(String telephone) {
		Telephone = telephone;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getFullName() {
		return FullName;
	}

	public void setFullName(String fullName) {
		FullName = fullName;
	}

	public String getBpmHost() {
		return BpmHost;
	}

	public void setBpmHost(String bpmHost) {
		BpmHost = bpmHost;
	}

	@Override
	public String toString() {
		return "User [Successed=" + Successed + ", ErrorMsg=" + ErrorMsg
				+ ", FullName=" + FullName + ", BpmHost=" + BpmHost
				+ ", UserId=" + UserId + ", LoginName=" + LoginName
				+ ", UserName=" + UserName + ", Password=" + Password
				+ ", AvatarUrl=" + AvatarUrl + ", Department=" + Department
				+ ", Telephone=" + Telephone + ", Email=" + Email + "]";
	}
}
