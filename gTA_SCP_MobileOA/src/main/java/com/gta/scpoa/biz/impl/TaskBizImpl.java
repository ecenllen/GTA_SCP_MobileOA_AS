package com.gta.scpoa.biz.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.TaskBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.URLs;

import java.util.ArrayList;
import java.util.List;

public class TaskBizImpl implements TaskBiz {

	private Context context = null;
	private Handler handler = null;
	private HttpUtil httpUtil = null;
	private String userName = "";

	public TaskBizImpl(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		httpUtil = HttpUtil.getInstance();
		userName = GTAApplication.instance.getUserName();
	}

	/**opinion
	 * id RunID type 1:待办,2:已办,3:公文公告 4会议通知 5会议纪要 command 第一加载
	 * TaskBizImpl.firstLoad 加载更多 TaskBizImpl.loadMore 刷新TaskBizImpl.onRefresh
	 * limit 加载条数 creatTimeString 第一次加载输入“”,获取最新的时候：输入最的大CreateTime,
	 * 加载更多的时候：输入最小的CreateTime searchString 有搜索输入搜索的文字 没有的话直接输入""
	 * 
	 * */
	@Override
	public void getTaskList(String id, final int type, final int command,
			int limit, String creatTimeString, final String searchString) {
		String url = "";
		if (type != 3 && type != 4 && type != 5) {
			url = URLs.getDefaultBaseURL() + "/GetTaskList";
		} else {
			url = URLs.getDefaultBaseURL() + "/GetProCopyList";
		}

		Log.e("=========", "url==" + url);
		RequestParams params = new RequestParams();
		params.addParams("userName", userName);
		params.addParams("limit", String.valueOf(limit));
		params.addParams("type", String.valueOf(type));

		if (command == firstLoad) { // 第一次加载

		} else if (command == loadMore) { // 加载更多
			params.addParams("endTime", creatTimeString);
			if (type == 1) {
				params.addParams("TaskId", id);
			} else {
				params.addParams("RunId", id);
			}
		} else if (command == onRefresh) { // 刷新
			params.addParams("startTime", creatTimeString);
		}

		/* 搜索的情况 */
		if (!searchString.equals("")) {
			params.addParams("subject", searchString);
		}

		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.POST;
		requestInfo.requestCode = type;
		requestInfo.params = params;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						Log.e("======log============", resultString);
						try {
							JSONObject reJsonObject = JSON
									.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									JSONArray jsonArray = reJsonObject
											.getJSONArray("Data");
									/* 新建实体类 */
									List<TaskNewInfor> list = new ArrayList<TaskNewInfor>();

									for (int i = 0; i < jsonArray.size(); i++) {
										TaskNewInfor taskInfo = new TaskNewInfor();
										JSONObject jsonObject = jsonArray
												.getJSONObject(i);
										/* id */
										if (jsonObject.containsKey("id")) {
											String id = jsonObject
													.getString("id");
											if (id != null) {
												taskInfo.setId(id);
											}
										}
										/* owner */
										if (jsonObject.containsKey("creator")) {
											String creator = jsonObject
													.getString("creator");
											if (creator != null) {
												taskInfo.setCreator(creator);
											}
										}
										/* subject */
										if (jsonObject.containsKey("subject")) {
											String subject = jsonObject
													.getString("subject");
											if (subject != null) {
												taskInfo.setSubject(subject);
											}
										}
										/* createTime2 */
										if (jsonObject
												.containsKey("createTime2")) {
											String createTime2 = jsonObject
													.getString("createTime2");
											if (createTime2 != null) {
												taskInfo.setCreateTime2(createTime2);
											}
										}
										/* createTime */
										if (jsonObject
												.containsKey("createTime")) {
											String createTime = jsonObject
													.getString("createTime");
											if (createTime != null) {
												taskInfo.setCreateTime(createTime);
											}
										}
										/* avatarUrl */
										if (jsonObject.containsKey("avatarUrl")) {
											String avatarUrl = jsonObject
													.getString("avatarUrl");
											if (avatarUrl != null) {
												taskInfo.setAvatarUrl(avatarUrl);
											}
										}
										/* content */
										if (jsonObject.containsKey("content")) {
											String content = jsonObject
													.getString("content");
											if (content != null) {
												taskInfo.setContent(content);
											}
										}

										/* runId */
										if (jsonObject.containsKey("runId")) {
											String runId = jsonObject
													.getString("runId");
											if (runId != null) {
												taskInfo.setRunId(runId);
											}
										}
										/* runId */
										if (jsonObject.containsKey("actInstId")) {
											String actInstId = jsonObject
													.getString("actInstId");
											if (actInstId != null) {
												taskInfo.setActInstId(actInstId);
											}
										}

										/* processName */
										if (jsonObject
												.containsKey("processName")) {
											String processName = jsonObject
													.getString("processName");
											if (processName != null) {
												taskInfo.setProcessName(processName);
											}
										}

										/* copyId */
										if (jsonObject.containsKey("copyId")) {
											String copyId = jsonObject
													.getString("copyId");
											if (copyId != null) {
												taskInfo.setCopyId(copyId);
											}
										}

										/* isReaded */
										if (jsonObject.containsKey("isReaded")) {
											String isReaded = jsonObject
													.getString("isReaded");
											if (isReaded != null) {
												taskInfo.setIsReaded(isReaded);
											}
										}

										/* readTime */
										if (jsonObject.containsKey("readTime")) {
											String readTime = jsonObject
													.getString("readTime");
											if (readTime != null) {
												taskInfo.setReadTime(readTime);
											}
										}

										taskInfo.setType(type);

										list.add(taskInfo);
									}
									/* 发送handler */
									Message msg = null;
									/* 不是搜索的情况 */
									if (searchString.equals("")) {
										if (command == firstLoad) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_SUCCESS);
										} else if (command == loadMore) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_MORE);
										} else if (command == onRefresh) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_REFRSH);
										}
									} else { /* 是搜索的情况 */
										if (command == firstLoad) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_SUCCESS);
										} else if (command == loadMore) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_MORE_SUCCESS);
										} else if (command == onRefresh) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_REFRSH_SUCCESS);
										}
									}

									msg.obj = list;
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {

						}
						/* 失败处理 */
						Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL);
						if (command == firstLoad) {
							msg.obj = "command=" + firstLoad;
						} else if (command == loadMore) {
							msg.obj = "command=" + loadMore;
						} else if (command == onRefresh) {
							msg.obj = "command=" + onRefresh;
						} else {
							msg.obj = "网络异常，加载数据失败!";
						}
						msg.sendToTarget();
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL); // 失败返回
						if (command == firstLoad) {
							msg.obj = "command=" + firstLoad;
						} else if (command == loadMore) {
							msg.obj = "command=" + loadMore;
						} else if (command == onRefresh) {
							msg.obj = "command=" + onRefresh;
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
						Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL); // 失败返回
						if (command == firstLoad) {
							msg.obj = "command=" + firstLoad;
						} else if (command == loadMore) {
							msg.obj = "command=" + loadMore;
						} else if (command == onRefresh) {
							msg.obj = "command=" + onRefresh;
						} else {
							msg.obj = "网络异常，加载数据失败!";
						}
						msg.sendToTarget();
						return;
					}
				}, context);
	}

	/**
	 * 获取
	 * 
	 * @param searchStr
	 *            搜索内容
	 * @param limit
	 *            分页条数
	 * @param noticeId
	 *            公告(会议)id
	 * @param isMore
	 *            是否加载更多
	 */
	@Override
	public void getMeetingList(final String searchStr, int limit, final int noticeId,
			boolean isMore, final int command) {
		String acountName =GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		String url = URLs.getDefaultBaseURL() + "/GetMeetNoticeList";
		RequestParams params = new RequestParams();
		params.addParams("userName", acountName);
		params.addParams("subject", searchStr);
		params.addParams("limit", String.valueOf(limit));
		params.addParams("noticeId", String.valueOf(noticeId));
		params.addParams("IsMore", String.valueOf(isMore));

		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.POST;
		requestInfo.params = params;
		httpUtil.doRequest(requestInfo, new StringParse(),
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

									// 解析数据
									JSONArray jsonArray = reJsonObject
											.getJSONArray("Data");
									List<TaskNewInfor> list = new ArrayList<TaskNewInfor>();
									for (int i = 0; i < jsonArray.size(); i++) {
										TaskNewInfor taskInfo = new TaskNewInfor();
										JSONObject jsonObject = jsonArray
												.getJSONObject(i);

										if (jsonObject.containsKey("id")) {
											String id = jsonObject.getString("id");
											if (null != id) {
												taskInfo.setId(id);
											}//9
										}

										if (jsonObject.containsKey("runId")) {
											String runId = jsonObject.getString("runId");
											if (null != runId) {
												taskInfo.setRunId(runId);
											}
										}

										if (jsonObject.containsKey("creator")) {
											String creator = jsonObject.getString("creator");
											if (null != creator) {
												taskInfo.setCreator(creator);
											}
										}

										if (jsonObject.containsKey("subject")) {
											String subject = jsonObject.getString("subject");
											if (null != subject) {
												taskInfo.setSubject(subject);
											}
										}

										if (jsonObject.containsKey("meetStartTime")) {
											String meetStartTime = jsonObject.getString("meetStartTime");
											if (null != meetStartTime) {
												taskInfo.setMeetStartTime(meetStartTime);
											}
										}

										if (jsonObject.containsKey("meetEndTime")) {
											String meetEndTime = jsonObject.getString("meetEndTime");
											if (null != meetEndTime) {
												taskInfo.setMeetEndTime(meetEndTime);
											}
										}

										if (jsonObject.containsKey("isReaded")) {
											String isReaded = jsonObject.getString("isReaded");
											if (null != isReaded) {
												taskInfo.setIsReaded(isReaded);
											}
										}

										if (jsonObject.containsKey("createTime")) {
											String createTime = jsonObject.getString("createTime");
											if (null != createTime) {
												taskInfo.setCreateTime(createTime);
											}
										}
										list.add(taskInfo);
									}
									/* 发送handler */
									Message msg = null;
									/* 不是搜索的情况 */
									if (searchStr.equals("")) {
										if (command == firstLoad) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_SUCCESS);
										} else if (command == loadMore) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_MORE);
										} else if (command == onRefresh) {
											msg = handler
													.obtainMessage(MSG_TASK_GET_REFRSH);
										}
									} else { /* 是搜索的情况 */
										if (command == firstLoad) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_SUCCESS);
										} else if (command == loadMore) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_MORE_SUCCESS);
										} else if (command == onRefresh) {
											msg = handler
													.obtainMessage(MSG_TASK_SEARCH_REFRSH_SUCCESS);
										}
									}

									msg.obj = list;
									msg.sendToTarget();
									return;
								}else{
									/* 失败处理 */
									Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL);
									if (command == firstLoad) {
										msg.obj = "command=" + firstLoad;
									} else if (command == loadMore) {
										msg.obj = "command=" + loadMore;
									} else if (command == onRefresh) {
										msg.obj = "command=" + onRefresh;
									} else {
										msg.obj = "网络异常,获取失败!";
									}
									msg.sendToTarget();
									return;
								}
							}
						} catch (Exception e) {
							Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL);
							msg.obj = "数据解析失败!";
							msg.sendToTarget();
							return;
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL); // 失败返回
						if (command == firstLoad) {
							msg.obj = "command=" + firstLoad;
						} else if (command == loadMore) {
							msg.obj = "command=" + loadMore;
						} else if (command == onRefresh) {
							msg.obj = "command=" + onRefresh;
						} else {
							msg.obj = "网络异常，获取失败!";
						}
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(MSG_TASK_GET_FAIL); // 失败返回
						if (command == firstLoad) {
							msg.obj = "command=" + firstLoad;
						} else if (command == loadMore) {
							msg.obj = "command=" + loadMore;
						} else if (command == onRefresh) {
							msg.obj = "command=" + onRefresh;
						} else {
							msg.obj = "网络异常，获取失败!";
						}
						msg.sendToTarget();
						return;
					}
				}, context);
	}

}
