package com.gta.scpoa.biz.impl;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.activity.WriteMailActivity;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.MailWriteBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailAttachInfo;
import com.gta.scpoa.entity.ReciveMailInfor;
import com.gta.scpoa.entity.UpAttachFileInfor;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.util.URLs;

public class MailWriteBizImpl implements MailWriteBiz {

	private Context context;
	private static int FILE_SELECT_CODE = 10; // 附件选择

	public MailWriteBizImpl(Context context) {
		this.context = context;
	}

	/* 根据URi判断路径 */
	@Override
	public String uriToPath(Context context, Uri uri) {
		String[] pojo = { MediaStore.Images.Media.DATA };
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(uri, pojo, null, null, null);
		if (cursor != null) {
			int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
			cursor.moveToFirst();
			String picPath = cursor.getString(columnIndex);
			if (Integer.parseInt(Build.VERSION.SDK) < 14) {
				cursor.close();
			}
			return picPath;
		}
		return null;
	}

	/* 判断能否发送 */
	@Override
	public boolean defineMailCanSend(TextView reciver_edit,
			TextView copyer_edit, TextView secretor_edit,
			EditText subject_edit, long fileSize) {
		String recicerString = reciver_edit.getText().toString();
		String copyerString = copyer_edit.getText().toString();
		String secretorString = secretor_edit.getText().toString();
		String subjectString = subject_edit.getText().toString();
		if (StringUtils.isEmpty(recicerString)
				&& StringUtils.isEmpty(copyerString)
				&& StringUtils.isEmpty(secretorString)) {
			UIUtils.ToastMessage(context, "收件人,抄送人,密送人 不可全为空");
			return false;
		}

		if (StringUtils.isEmpty(subjectString)) {
			UIUtils.ToastMessage(context, "主题不能为空");
			return false;
		}

		if (fileSize > Constant.allAttachSize) {
			UIUtils.ToastMessage(context, "附件大小不能超过12M");
			return false;
		}
		return true;
	}

	@Override
	public void goShowAttachFile(boolean isImage) {
		Activity activity = (WriteMailActivity) context;
		if (isImage) {
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			activity.startActivityForResult(i, FILE_SELECT_CODE);
			return;
		}
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			activity.startActivityForResult(
					Intent.createChooser(intent, "请选择一个要上传的文件"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			UIUtils.ToastMessage(context, "请安装文件管理器");
		}
	}

	@Override
	public void preferToSendMail(ReciveMailInfor reciveMailInfor,
			List<UpAttachFileInfor> upAttachFileInfors) {

	}

	/* 发送邮件 */
	@Override
	public void sendMail(ReciveMailInfor reciveMailInfor,
			List<HashMap<String, String>> reciveList,
			List<HashMap<String, String>> coperList,
			List<HashMap<String, String>> secretorList, boolean isDraft,
			boolean isSend, final Handler handler) {
		String urlString = URLs.getDefaultBaseURL() + "/SendMail";

		GTAApplication app = (GTAApplication) context.getApplicationContext();
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		RequestParams params = new RequestParams();
		requestInfo.method = RequestMethod.POST;// 默认post请求

		if (isSend) {
			requestInfo.requestCode = 10;
		} else {
			requestInfo.requestCode = 11;
		}
		String UseID = app.getProperty(Constant.PROP_KEY_UID);
		requestInfo.enableCache = false;
		params.addParams("UserId", UseID);
		params.addParams("UserName", app.getFullName());

		/* 是否是草稿箱 */
		if (isDraft) {
			params.addParams("Id", reciveMailInfor.getId());
		} else {
			params.addParams("Id", "-1");
		}
		/* 是否是发送 */
		if (isSend) {
			params.addParams("Status", "1");
		} else {
			params.addParams("Status", "0");
		}
		/* 收件人ID列表 */
		String reciverIdString = "";

		for (HashMap<String, String> hashMap : reciveList) {
			reciverIdString += hashMap.get("id") + ",";
		}
		if (reciverIdString.length() > 0) {
			reciverIdString = reciverIdString.substring(0,
					reciverIdString.length() - 1);
		}

		if (reciveList.size() == 0) {
			params.addParams("TO", null);
		} else {
			params.addParams("TO", reciverIdString);
		}
		/* 抄送人列表 */
		String coperIdString = "";
		for (HashMap<String, String> hashMap : coperList) {
			coperIdString += hashMap.get("id") + ",";
		}
		if (coperIdString.length() > 0) {
			coperIdString = coperIdString.substring(0,
					coperIdString.length() - 1);
		}
		if (coperList.size() == 0) {
			params.addParams("CC", null);
		} else {
			params.addParams("CC", coperIdString);
		}
		/* 密送人列表 */
		String secretorIdString = "";
		for (HashMap<String, String> hashMap : secretorList) {
			secretorIdString += hashMap.get("id") + ",";
		}
		if (secretorIdString.length() > 0) {
			secretorIdString = secretorIdString.substring(0,
					secretorIdString.length() - 1);
		}
		if (secretorList.size() == 0) {
			params.addParams("BCC", null);
		} else {
			params.addParams("BCC", secretorIdString);
		}
		/* 收件人—ID */
		String recive_id_String = "";

		for (HashMap<String, String> hashMap : reciveList) {
			recive_id_String += hashMap.get("name") + "|" + hashMap.get("id")
					+ ",";
		}
		if (recive_id_String.length() > 0) {
			recive_id_String = recive_id_String.substring(0,
					recive_id_String.length() - 1);
		}

		if (reciveList.size() == 0) {
			params.addParams("ReceiverUsers", null);
		} else {
			params.addParams("ReceiverUsers", recive_id_String);
		}

		/* 抄送人—ID */
		String coper_id_String = "";

		for (HashMap<String, String> hashMap : coperList) {
			coper_id_String += hashMap.get("name") + "|" + hashMap.get("id")
					+ ",";
		}
		if (coper_id_String.length() > 0) {
			coper_id_String = coper_id_String.substring(0,
					coper_id_String.length() - 1);
		}

		if (coperList.size() == 0) {
			params.addParams("OutBoxCopyer", null);
		} else {
			params.addParams("OutBoxCopyer", coper_id_String);
		}
		/* 密送人—ID */
		String secretor_id_String = "";

		for (HashMap<String, String> hashMap : secretorList) {
			secretor_id_String += hashMap.get("name") + "|" + hashMap.get("id")
					+ ",";
		}
		if (secretor_id_String.length() > 0) {
			secretor_id_String = secretor_id_String.substring(0,
					secretor_id_String.length() - 1);
		}
		if (secretorList.size() == 0) {
			params.addParams("OutBoxSecret", null);
		} else {
			params.addParams("OutBoxSecret", secretor_id_String);
		}
		/* 主题 */
		params.addParams("OutBoxTheme", reciveMailInfor.getOutBoxTheme());
		/* 正文内容 */
		String[] strs = explainString(reciveMailInfor.getOutBoxContent());
		String OutBoxContent = "";
		for (int i = 0; i < strs.length; i++) {
			OutBoxContent = OutBoxContent + strs[i] + "<br>";
		}
		params.addParams("OutBoxContent", OutBoxContent);
		/* 是否回执 */
		params.addParams("IsreturnReceipt",
				String.valueOf(reciveMailInfor.isIsreturnReceipt()));
		/* 回复或者转发 */
		params.addParams("IsReplyOrTranspond", String.valueOf(false)); // 回复用true
																		// 其他用false
																		// 目前都用fasle
																		// 回复当新建处理
		/* 附件信息 */
		String AccessoryIds = "";
		List<MailAttachInfo> listAttach = reciveMailInfor.getAttachLists();

		for (MailAttachInfo attachInfo : listAttach) {
			AccessoryIds += attachInfo.getId() + ",";
		}
		if (AccessoryIds.length() > 0) {
			AccessoryIds = AccessoryIds.substring(0, AccessoryIds.length() - 1);
		}

		if (listAttach.size() == 0) {
			params.addParams("AccessoryIds", null);
		} else {
			params.addParams("AccessoryIds", AccessoryIds);
		}
		requestInfo.params = params;
		HttpUtil.getInstance().doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						try {
							JSONObject reJsonObject = JSON
									.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									Message msg = handler.obtainMessage(2); // 发送成功
									if (response.requestCode == 10) {
										msg.obj = "发送成功";
									} else {
										msg.obj = "保存成功";
									}
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(2); // 失败返回
						msg.obj = "";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(2); // 失败返回
						msg.obj = "";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
						Message msg = handler.obtainMessage(2); // 失败返回
						msg.obj = "";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(2); // 失败返回
						msg.obj = "";
						msg.sendToTarget();
						return;
					}
				}, this);
	}

	private String[] explainString(String str) {
		String[] strs = null;
		strs = str.split("\n");
		return strs;
	}

}
