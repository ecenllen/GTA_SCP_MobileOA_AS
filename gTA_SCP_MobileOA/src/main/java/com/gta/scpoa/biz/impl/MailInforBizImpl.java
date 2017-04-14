package com.gta.scpoa.biz.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.MailInforBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailInfor;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.URLs;

public class MailInforBizImpl implements MailInforBiz {

	private Context context;

	public MailInforBizImpl(Context mContext) {
		context = mContext;
	}

	/**
	 * 用于联网获取邮件的信息 type是指邮件的类型 command 命令(1: 首次获取，2：获取更多,3: 获取最新)
	 * */
	@Override
	public void getMailInfor(final int type, final int command, int limit,
			String mailId, final String searchString, final Handler handler) {
		if (type <= 0 || type > 4) {
			Message msg = handler.obtainMessage(GET_Fail); // 失败返回
			if (command == 1) {
				msg.obj = "command=1";
			} else if (command == 2) {
				msg.obj = "command=2";
			} else if (command == 3) {
				msg.obj = "command=3";
			} else {
				msg.obj = "加载失败";
			}
			msg.sendToTarget();
			return;
		}
		if (command <= 0 || command > 4) {
			Message msg = handler.obtainMessage(GET_Fail); // 失败返回
			msg.obj = "连接失败";
			msg.sendToTarget();
			return;
		}
		/*
		 * http://192.168.193.120/api/MobileApi/GetMailList?userId=1&mailType=1&
		 * start=1&limit=10
		 */
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String urlString = URLs.getDefaultBaseURL()+"/GetMailList";
		RequestParams params = new RequestParams();   //参数类
		String useId = app.getProperty(Constant.PROP_KEY_UID);
		if(type!=4){
			if (searchString.equals("")) {
				params.addParams("userId", useId);
				params.addParams("mailType", String.valueOf(type));
				params.addParams("command", String.valueOf(command));
				params.addParams("limit", String.valueOf(limit));
				params.addParams("mailId",mailId);
			} else {
				params.addParams("userId", useId);
				params.addParams("mailType", String.valueOf(type));
				params.addParams("command", String.valueOf(command));
				params.addParams("limit", String.valueOf(limit));
				params.addParams("mailId", mailId);
				params.addParams("searchContent", searchString);
			}
		}
		
		if (type == 4) { // 回收站的时候
			if (searchString.equals("")) {
				params.addParams("userId", useId);
				params.addParams("mailType", String.valueOf(type));
				params.addParams("command", String.valueOf(command));
				params.addParams("limit", String.valueOf(limit));
				params.addParams("createTime",mailId);
//				urlString = "http://192.168.193.120/api/MobileApi/GetMailList?userId=1&mailType=4&command=1&limit=20&createTime=0";
//				params = null;
			} else {
				params.addParams("userId", useId);
				params.addParams("mailType", String.valueOf(type));
				params.addParams("command", String.valueOf(command));
				params.addParams("limit", String.valueOf(limit));
				params.addParams("createTime",mailId);
				params.addParams("searchContent", searchString);
			}
		}
		
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		requestInfo.method = RequestMethod.POST;// 默认post请求
		requestInfo.requestCode = command;
		requestInfo.params = params;
		HttpUtil.getInstance().doRequest(requestInfo,
				new StringParse(), new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						try {
							
							JSONObject reJsonObject = JSON.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {

									if (reJsonObject.containsKey("UnreadCount")) {
										// 未读数据 只有收件箱有 存入sherePhere里面
										PreferencesUtils.putInt(context,
												Constant.UNREAD_COUNT,
												reJsonObject.getIntValue("UnreadCount"));
									}

									JSONArray jsonArray = reJsonObject.getJSONArray("Data");

									List<MailInfor> listInfors = new ArrayList<MailInfor>();
									for (int i = 0; i < jsonArray.size(); i++) {
										JSONObject jsonObject = jsonArray.getJSONObject(i);
										MailInfor mailInfor = new MailInfor();
										if (jsonObject.containsKey("HasAccessory")) {
											mailInfor.setAttach(jsonObject
													.getBooleanValue("HasAccessory")); // 附件
										}

										if (jsonObject.containsKey("OutboxContent")) {
											String OutboxContent = jsonObject
													.getString("OutboxContent");
											if (OutboxContent == null) {
												OutboxContent = "";
											}
											mailInfor.setContentString(OutboxContent); // 内容
										}

										if (jsonObject.containsKey("CreateTime")) {
											mailInfor.setCreateTime(jsonObject
													.getString("CreateTime")); // 时间
										}

										if (jsonObject.containsKey("Id")) {
											mailInfor.setId(String
													.valueOf(jsonObject
															.get("Id"))); // id
										}

										if (jsonObject.containsKey("OutBoxTheme")) {
											String  OutBoxTheme = jsonObject
													.getString("OutBoxTheme");
											if(OutBoxTheme==null){
												OutBoxTheme = "";
											}
											mailInfor.setOutBoxTheme(OutBoxTheme);// 主题
										}

										if (jsonObject.containsKey("IsRead")) {
											mailInfor.setRead(jsonObject
													.getBooleanValue("IsRead")); // 是否已经读
										}

										if (jsonObject.containsKey("UserName")) {
											String  UserName = jsonObject
													.getString("UserName");
											if(UserName==null){
												UserName = "";
											}
											mailInfor.setUserName(UserName); // 发件人
										}

										if (jsonObject.containsKey("MailType")) {
											mailInfor.setMailType(jsonObject
													.getIntValue("MailType")); // 只有回收站有这个类型
										} else {
											mailInfor.setMailType(type);
										}

										listInfors.add(mailInfor);
									}
									if (listInfors.size() >= 0) {
										/* 返回给主界面处理 */
										Message msg = null;
										if (searchString.equals("")) {
											if (command == 1) {
												msg = handler.obtainMessage(GET_SUCCESS); // 最新
											} else if (command == 2) {
												msg = handler.obtainMessage(GET_MORE); // 更多
											} else if (command == 3) {
												msg = handler.obtainMessage(GET_REFRSH); // 更新
											}
										} else {
											if (command == 1) {
												msg = handler.obtainMessage(SEARCH_SUCCESS); // 最新
											} else if (command == 2) {
												msg = handler.obtainMessage(SEARCH_MORE_SUCCESS); // 更多
											} else if (command == 3) {
												msg = handler.obtainMessage(SEARCH_REFRSH_SUCCESS); // 更新
											}
										}
										msg.obj = listInfors;
										msg.sendToTarget();
										return;
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						if (command == 1) {
							msg.obj = "command=1";
						} else if (command == 2) {
							msg.obj = "command=2";
						} else if (command == 3) {
							msg.obj = "command=3";
						} else {
							msg.obj = "网络异常，加载数据失败!";
						}
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						if (command == 1) {
							msg.obj = "command=1";
						} else if (command == 2) {
							msg.obj = "command=2";
						} else if (command == 3) {
							msg.obj = "command=3";
						} else {
							msg.obj = "网络异常，加载数据失败!";
						}
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						if (command == 1) {
							msg.obj = "command=1";
						} else if (command == 2) {
							msg.obj = "command=2";
						} else if (command == 3) {
							msg.obj = "command=3";
						} else {
							msg.obj = "网络异常，加载数据失败!";
						}
						msg.sendToTarget();
						return;
					}
				}, this);
	}

	/* 删除单个邮件操作 */
	@Override
	public void deleteMail(final List<MailInfor> listMailInfors,
			boolean isrecycle, int type, final Handler handler) {
		// http://192.168.193.120/api/MobileApi/DeleteMail?userId=1&mailIds=1,2,3&mailType=1;
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String urlString = "";
		if (listMailInfors.size() == 0) {
			Message msg = handler.obtainMessage(GET_Fail);
			msg.obj = "删除失败";
			msg.sendToTarget();
			return;
		}

		RequestParams params = new RequestParams();   //参数类
		
		String UseID = app.getProperty(Constant.PROP_KEY_UID);
		
		if (!isrecycle) {
			String mailIds = "";
			for (int i = 0; i < listMailInfors.size(); i++) {
				if (i == listMailInfors.size() - 1) {
					mailIds = mailIds + listMailInfors.get(i).getId();
				} else {
					mailIds = mailIds + listMailInfors.get(i).getId() + ",";
				}
			}
			urlString = URLs.getDefaultBaseURL()+"/DeleteMail";
			params.addParams("userId", UseID);
			params.addParams("mailIds", mailIds);
			params.addParams("mailType",String.valueOf(type));
		} else {
			String sendIds = "";
			String receiveIds = "";
			for (int i = 0; i < listMailInfors.size(); i++) {
				if (listMailInfors.get(i).getMailType() == 1) { // 收件箱
					receiveIds = receiveIds + listMailInfors.get(i).getId()
							+ ",";
				} else {
					sendIds = sendIds + listMailInfors.get(i).getId() + ",";
				}
			}
			if (sendIds.equals(""))
				sendIds = null;
			if (receiveIds.equals(""))
				receiveIds = null;
			urlString = URLs.getDefaultBaseURL()+"/DeleteOrRebackRecycled";
			params.addParams("userId", UseID);
			params.addParams("command", "1");// 1 是删除 2是恢复
			params.addParams("sendIds",sendIds);
			params.addParams("receiveIds",receiveIds);
		}
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		requestInfo.method = RequestMethod.POST;// 默认post请求
		requestInfo.requestCode = 1;
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
							JSONObject reJsonObject = JSON.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									Message msg = handler.obtainMessage(DELETE_RESUME); // 删除成功
									msg.obj = listMailInfors;
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,删除失败";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,删除失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
						
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常，删除失败!";
						msg.sendToTarget();
						return;
					}
				}, this);
	}

	/* 恢复邮件 */
	@Override
	public void reSumeMails(final List<MailInfor> listMailInfors,
			final Handler handler) {
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String urlString = "";
		if (listMailInfors.size() == 0) {
			Message msg = handler.obtainMessage(GET_Fail);
			msg.obj = "恢复失败";
			msg.sendToTarget();
			return;
		}

		RequestParams params = new RequestParams();   //参数类
		String sendIds = "";
		String receiveIds = "";
		String useId = app.getProperty(Constant.PROP_KEY_UID);
		for (int i = 0; i < listMailInfors.size(); i++) {
			if (listMailInfors.get(i).getMailType() == 1) { // 收件箱
				receiveIds = receiveIds + listMailInfors.get(i).getId() + ",";
			} else {
				sendIds = sendIds + listMailInfors.get(i).getId() + ",";
			}
		}
		if (sendIds.equals(""))
			sendIds = null;
		if (receiveIds.equals(""))
			receiveIds = null;
		urlString = URLs.getDefaultBaseURL()+"/DeleteOrRebackRecycled";
		params.addParams("userId", useId);
		params.addParams("command", "2");// 1 是删除 2是恢复
		params.addParams("sendIds", sendIds);
		params.addParams("receiveIds", receiveIds);
		
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		requestInfo.method = RequestMethod.POST;// 默认post请求
		requestInfo.requestCode = 1;
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
							JSONObject reJsonObject =JSON.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									Message msg = handler.obtainMessage(DELETE_RESUME); // 恢复成功
									msg.obj = listMailInfors;
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,恢复失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常，恢复失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,恢复失败!";
						msg.sendToTarget();
						return;
					}
				}, this);
	}

	/* 快速回复 */
	@Override
	public void fastSendMail(String mailID, String OutBoxContent,
			final Handler handler) {
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String urlString = URLs.getDefaultBaseURL()+"/QuickReplyMail";
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		String useId = app.getProperty(Constant.PROP_KEY_UID);
		RequestParams params = new RequestParams();
		params.addParams("UserId", useId);
		params.addParams("UserName", app.getFullName());
		params.addParams("Id", mailID);
		params.addParams("OutBoxContent", OutBoxContent);
		requestInfo.method = RequestMethod.POST;// 默认post请求
		requestInfo.requestCode = 11;
		requestInfo.params = params;
		HttpUtil.getInstance().doRequest(requestInfo,
				new StringParse(), new RequestListener() { // 这里采用局部
															// 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						try {
							JSONObject reJsonObject = JSON.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									Message msg = handler.obtainMessage(GET_Fail); // 回复成功
									msg.obj = "发送成功";
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,发送失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,发送失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,发送失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(GET_Fail); // 失败返回
						msg.obj = "网络异常,发送失败!";
						msg.sendToTarget();
						return;
					}
				}, this);
	}

	/* 设置全部的对象没有选中 */
	@Override
	public void setAllMailNotPreDel(List<MailInfor> listMailInfors) {
		for (MailInfor mailInfor : listMailInfors) {
			if (mailInfor.isPrefreDel()) {
				mailInfor.setPrefreDel(false);
			}
		}
	}

	/* 获取多少个选中的 */
	@Override
	public int getAllCheckNum(List<MailInfor> listMailInfors) {
		int num = 0;
		for (MailInfor mailInfor : listMailInfors) {
			if (mailInfor.isPrefreDel()) {
				num++;
			}
		}
		return num;
	}
}
