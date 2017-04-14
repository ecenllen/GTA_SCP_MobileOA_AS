package com.gta.scpoa.entity;

/**
 * 实体类-表示首页的一个视图
 * 
 * @author bin.wang1
 * 
 */
public class HomeItem {
	private int id;
	private String noticeNum;
	private int ivDrawable;// ImageView的src
	private int llBackground;// LinearLayout的Background

	private String title;

	public HomeItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HomeItem(int id, String noticeNum, int ivDrawable, int llBackground,
			String title) {
		super();
		this.id = id;
		this.noticeNum = noticeNum;
		this.ivDrawable = ivDrawable;
		this.llBackground = llBackground;
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNoticeNum() {
		return noticeNum;
	}

	public void setNoticeNum(String noticeNum) {
		this.noticeNum = noticeNum;
	}

	public int getIvDrawable() {
		return ivDrawable;
	}

	public void setIvDrawable(int ivDrawable) {
		this.ivDrawable = ivDrawable;
	}

	public int getLlBackground() {
		return llBackground;
	}

	public void setLlBackground(int llBackground) {
		this.llBackground = llBackground;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
