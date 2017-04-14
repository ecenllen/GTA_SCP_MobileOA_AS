package com.gta.scpoa.biz;

import java.util.HashMap;
import java.util.List;

import com.gta.scpoa.entity.ReciveMailInfor;
import com.gta.scpoa.entity.UpAttachFileInfor;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

public interface MailWriteBiz {
	/* 根据URi判断路径 */
	public String uriToPath(Context context, Uri uri);

	/* 判断能否发送 */
	public boolean defineMailCanSend(TextView reciver_edit,
			TextView copyer_edit,TextView secretor_edit,
			EditText subject_edit, long fileSize);
	
	/*附件选择*/
	public void goShowAttachFile(boolean isImage);
	
	/*发送邮件前做的准备*/
	public void preferToSendMail(ReciveMailInfor reciveMailInfor,List<UpAttachFileInfor> upAttachFileInfors);

	/*发送邮件*/
	public void sendMail(ReciveMailInfor reciveMailInfor,List<HashMap<String, String>> reciveList,
			List<HashMap<String, String>> coperList,List<HashMap<String, String>> secretorLists
			,boolean isDraft,boolean sendOrSave,Handler handler);
}
