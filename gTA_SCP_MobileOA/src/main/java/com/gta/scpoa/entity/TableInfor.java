package com.gta.scpoa.entity;

/**
 * 表单的数据类
 * @author xiaojie.cai
 *
 */
public class TableInfor {
	/**
	 * 字段的ID
	 */
	private String keyId = "";   
	/**
	 * 字段
	 */
	private String key = "";    
	/**
	 * 值
	 */
	private String value = "";  
	/**
	 * 类型
	 */
	private int type = 0;     
	
	/**
	 * 是否是表的标题
	 */
	private boolean isTitle = false;
	
	public boolean isTitle() {
		return isTitle;
	}
	public void setTitle(boolean isTitle) {
		this.isTitle = isTitle;
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
