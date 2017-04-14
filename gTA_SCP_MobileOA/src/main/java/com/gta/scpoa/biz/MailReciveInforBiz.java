package com.gta.scpoa.biz;

import com.gta.scpoa.entity.MailAttachInfo;

import android.os.Handler;


/**
 * 功能  邮件接收显示的接口 
 *@author caixiaojie
 *
 */
public interface MailReciveInforBiz {
	/*获取邮件的信息   用于第一次加载邮件的信息*/
	public void getMailReciveInfor(String id,int mailType,Handler handler);
	/*下载附件*/

	public void downMailAttachFile(MailAttachInfo mailAttachInfo);
}
