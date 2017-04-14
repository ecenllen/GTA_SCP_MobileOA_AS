package com.gta.scpoa.entity;

import java.io.Serializable;

public class VersionEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//版本编号
	private int versionCode;
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getExplain() {
		return explain;
	}
	public void setExplain(String explain) {
		this.explain = explain;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	//版本名
	private String versionName;
	//文件地址
	private String url;
	//说明
	private String explain;
	//文件大小
	private String size;
}
