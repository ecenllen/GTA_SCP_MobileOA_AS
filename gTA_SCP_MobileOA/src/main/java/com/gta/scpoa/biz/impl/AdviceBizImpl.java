package com.gta.scpoa.biz.impl;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.AdviceBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.ExplainTableUtil;
import com.gta.scpoa.util.URLs;

public class AdviceBizImpl implements AdviceBiz{

	
	private Context context = null;
	private Handler handler = null;
	private HttpUtil httpUtil = null;
	
	public AdviceBizImpl(Context context,Handler handler) {
		this.context = context;
		this.handler = handler;
		httpUtil = HttpUtil.getInstance();
	}
	
	@Override
	public void getTableData(TaskNewInfor taskNewInfor) {
		String url = URLs.getDefaultBaseURL()+ "/GetFormField";
		RequestParams params = new RequestParams();
		final int type = taskNewInfor.getType();
		if(type ==1){
			params.addParams("TaskId", taskNewInfor.getId());
		}else{ //已办和公文公告使用
			params.addParams("runId", taskNewInfor.getRunId());
			if(type == -1){  // 会议通知的详情参数
				params.addParams("noticeId", taskNewInfor.getId());
			}
			if(type>2&&taskNewInfor.getIsReaded().equals("0")){  //未读
				params.addParams("copyId", taskNewInfor.getCopyId());
			}
		}
		String acountName =GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		params.addParams("UserName", acountName);
		Log.e("====================", "+++++++++++=" +url+"  UserName=" +acountName+"  TaskId="+taskNewInfor.getId()+ "  runId="+taskNewInfor.getRunId()+"  copyId="+taskNewInfor.getCopyId()+"  noticeId="+ taskNewInfor.getId());
		
		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.POST;
		requestInfo.requestCode = 1;
		requestInfo.params = params;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {
						
					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						Log.e("====================", resultString);
						JSONObject reJsonObject = JSON
								.parseObject(resultString);
						if (reJsonObject.containsKey("result")) {
							if (reJsonObject.getBooleanValue("result")) {
								
								/*获取表单数据*/
								List<TableInfor> lisTableInfors = ExplainTableUtil.getAllTable(resultString);
								if(lisTableInfors.size()==0){
									
									Message msg = handler.obtainMessage(MSG_TABLE_GET_FAIL); // 获取失败
									if(type == 1){
										msg.obj = resultString;
									}else{
										msg.obj = "获取数据失败!";
									}
									msg.sendToTarget();
								}else{
									Message msg = handler.obtainMessage(MSG_TABLE_GET_SUCCESS); // 获取成功返回
									msg.obj = lisTableInfors;
									msg.sendToTarget();
								}
							}else {
								Message msg = handler.obtainMessage(MSG_TABLE_GET_FAIL); // 获取失败
								if(type == 1){
									msg.obj = resultString;
								}else{
									msg.obj = "获取数据失败!";
								}
								msg.sendToTarget();
							}
						}
						
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Log.e("====================", "+++++++++++" + responseInfo.httpStatus+ "  ===" + responseInfo.errorMessage);
						Message msg = handler.obtainMessage(MSG_TABLE_GET_FAIL); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
						Message msg = handler.obtainMessage(MSG_TABLE_GET_FAIL); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}
//					GetTaskList
					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(MSG_TABLE_GET_FAIL); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}
				}, context);
	}

}
