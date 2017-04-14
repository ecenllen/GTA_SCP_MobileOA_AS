package com.gta.scpoa.entity;

public class TabAttachInfo {
	/**
	 * id  
	 */
	private String id = "";
	/**
	 * name 附件的名称
	 */
	private String name = "";
	/**
	 * url  附件的路径
	 */
	
	private Long size = 0L; 
	
	
	private String url = "";
	
	
	
	
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
