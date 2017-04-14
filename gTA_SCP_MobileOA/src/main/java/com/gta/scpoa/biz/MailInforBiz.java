package com.gta.scpoa.biz;

import java.util.List;

import com.gta.scpoa.entity.MailInfor;

import android.os.Handler;

/**
 * 功能  邮件首页显示的接口 
 *@author caixiaojie
 *
 */
public interface MailInforBiz {
	
	public static final int GET_Fail = 0; //加载失败
	public static final int GET_SUCCESS = 1; //第一次进  加载成功
	public static final int GET_MORE = 2; //加载更多
	public static final int GET_REFRSH = 3; //没有搜索的下拉刷新
	public static final int DELETE_RESUME=4; //删除或者恢复
	public static final int RETURN_RELOAD=8; //返回的时候重新加载
	public static final int SEARCH_SUCCESS=20; //搜索成功
	public static final int SEARCH_REFRSH_SUCCESS=21; //搜索下拉成功
	public static final int SEARCH_MORE_SUCCESS=22; //搜索上拉更多成功
	
	
	
	/*获取邮件的信息   用于第一次加载邮件的信息   搜索通用*/
	public void getMailInfor(int type, int command,int limit,String mailId,String searchString,Handler handler);
	/*删除邮件*/
	public void deleteMail(List<MailInfor> listMailInfors,boolean isrecycle, int type,  Handler handler);
	/*恢复邮件*/
	public void reSumeMails(List<MailInfor> listMailInfors, Handler handler);
	/*快速发送*/
	public void fastSendMail(String mailID,String OutBoxContent, Handler handler);
	/*设置全部的对象没有选中*/
	public void setAllMailNotPreDel(List<MailInfor> listMailInfors);
	/*获取多少个选中的*/
	public int getAllCheckNum(List<MailInfor> listMailInfors);
}
