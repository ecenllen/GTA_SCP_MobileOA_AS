package com.gta.scpoa.entity;

import java.util.List;

public class TaskPeople {
	private boolean Successed;
	private String nextNodeId;
	private String nextNodeName;
	
	private List<TaskPeople> Data;
	
	private String executeId;// 主键
	private String executor;// 姓名
	private String key;
	private String type;
	
	
	private boolean isTick;// 是否打勾
	private boolean isTickHidden;// 是否需要隐藏勾号

	public TaskPeople() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean isSuccessed() {
		return Successed;
	}

	public void setSuccessed(boolean successed) {
		Successed = successed;
	}

	public String getNextNodeId() {
		return nextNodeId;
	}

	public void setNextNodeId(String nextNodeId) {
		this.nextNodeId = nextNodeId;
	}

	public String getNextNodeName() {
		return nextNodeName;
	}

	public void setNextNodeName(String nextNodeName) {
		this.nextNodeName = nextNodeName;
	}

	public List<TaskPeople> getData() {
		return Data;
	}

	public void setData(List<TaskPeople> data) {
		Data = data;
	}

	public String getExecuteId() {
		return executeId;
	}

	public void setExecuteId(String executeId) {
		this.executeId = executeId;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isTick() {
		return isTick;
	}

	public void setTick(boolean isTick) {
		this.isTick = isTick;
	}

	public boolean isTickHidden() {
		return isTickHidden;
	}

	public void setTickHidden(boolean isTickHidden) {
		this.isTickHidden = isTickHidden;
	}

	@Override
	public String toString() {
		return "TaskPeople [Successed=" + Successed + ", nextNodeId="
				+ nextNodeId + ", nextNodeName=" + nextNodeName + ", Data="
				+ Data + ", executeId=" + executeId + ", executor=" + executor
				+ ", key=" + key + ", type=" + type + ", isTick=" + isTick
				+ ", isTickHidden=" + isTickHidden + "]";
	}

	

	

}
