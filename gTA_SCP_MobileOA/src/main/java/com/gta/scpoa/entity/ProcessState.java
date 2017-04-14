package com.gta.scpoa.entity;

/**
 * 实体类-流程状态
 * 
 * @author bin.wang1
 * 
 */
public class ProcessState {
	private String opinionId;// 任务ID
	private String exeFullname;// 执行人姓名
	private String taskName;//任务名称
	private String opinion;//处理意见
	private String startTime;//开始时间
	private String endTime;//结束时间
	private String checkStatus;//流程状态
//	private boolean state;// 流程的状态 true:已处理 /false：待处理
	boolean isShouldHideCycle;
//	/**
//	 * 表示该实体是否为"待处理"的前一个实体，即是否为倒数第二个
//	 * true:mContentTV的drawableLeft用红箭头	/	false:mContentTV的drawableLeft用绿箭头
//	 */
//	private boolean isNextToLast;

	public ProcessState() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getOpinionId() {
		return opinionId;
	}

	public void setOpinionId(String opinionId) {
		this.opinionId = opinionId;
	}

	public String getExeFullname() {
		return exeFullname;
	}

	public void setExeFullname(String exeFullname) {
		this.exeFullname = exeFullname;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}
	
	

	public boolean isShouldHideCycle() {
		return isShouldHideCycle;
	}

	public void setShouldHideCycle(boolean isShouldHideCycle) {
		this.isShouldHideCycle = isShouldHideCycle;
	}

	@Override
	public String toString() {
		return "ProcessState [opinionId=" + opinionId + ", exeFullname="
				+ exeFullname + ", taskName=" + taskName + ", opinion="
				+ opinion + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", checkStatus=" + checkStatus + "]";
	}

}
