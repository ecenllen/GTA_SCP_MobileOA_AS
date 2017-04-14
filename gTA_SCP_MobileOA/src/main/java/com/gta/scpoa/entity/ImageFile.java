package com.gta.scpoa.entity;


/**
 * 上传的文件
 * 
 * @author bin.wang1
 * 
 */
public class ImageFile {

	private boolean Successed;
	private String ErrorMsg;
	
	public ImageFile() {
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
	
	
	
}
