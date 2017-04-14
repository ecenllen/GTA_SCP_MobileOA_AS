package com.gta.scpoa.biz.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.parse.BitmapParse;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.HomeInfo;
import com.gta.scpoa.entity.ImageFile;
import com.gta.scpoa.entity.ProcessState;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.entity.TaskPeople;
import com.gta.scpoa.entity.User;
import com.gta.scpoa.util.JudgeUtils;
import com.gta.scpoa.util.NotificationUtlis;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * 联网调用框架
 * 
 * @author bin.wang1
 * 
 */
public class HttpRequestMethod {
	private static Gson mGson = new Gson();
	

	public void uploadProtrait(final Handler handler, String url,
			RequestParams params) {

		RequestInfo request = new RequestInfo(GTAApplication.instance, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {

					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (!StringUtils.isEmpty(result)) {
						
							ImageFile file = mGson.fromJson(result,ImageFile.class);
							if (null != file) {

								if (file.isSuccessed()) {
									msg.what = Constant.MSG_OPERATE_SUCCESS;
									msg.arg1 = 1;
									msg.obj = "上传成功 !";
									handler.sendMessage(msg);
								} else {
									msg.what = Constant.MSG_FAIL;
									msg.arg1 = 1;
									msg.obj = "网络异常，上传失败 !";

									handler.sendMessage(msg);
								}
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "正在上传...";						
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {						
						msg.what = Constant.MSG_FAIL;
						msg.arg1 = 1;
						msg.obj = "网络异常，上传失败！ ";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.arg1 = 1;
						msg.obj = "网络异常，上传失败！ ";
						handler.sendMessage(msg);
					}

				}, this);

	}

	public void getMonthScheduleList(Context context, final Handler handler,
			String url, RequestParams params) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (null != result && !"".equals(result)) {
							try {
								JSONObject reJsonObject = new JSONObject(result);
								if (reJsonObject.has("Successed")) {

									if (reJsonObject.getBoolean("Successed")) {
										JSONArray jsonArray = reJsonObject
												.getJSONArray("Data");
										

										Type listType = new TypeToken<List<Schedule>>() {
										}.getType();
										List<Schedule> schedulelist = mGson
												.fromJson(jsonArray.toString(),
														listType);

										msg.what = Constant.MSG_GETDATA_SUCCESS;
										msg.obj = schedulelist;
										handler.sendMessage(msg);
									} else {
										
										msg.what = Constant.MSG_FAIL;
										msg.obj = "网络异常，加载数据失败！ ";
									
										handler.sendMessage(msg);
									}

								}
							} catch (Exception e) {
								e.printStackTrace();
								msg.what = Constant.MSG_FAIL;
								msg.obj = "返回数据错误!";
								handler.sendMessage(msg);
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，加载数据失败!" ;
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，加载数据失败!";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void sendNewSchedule(Context context, final Handler handler,
			String url, RequestParams params, final int scheId) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {

					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (!StringUtils.isEmpty(result)) {
							try {
								JSONObject reJsonObject = new JSONObject(result);
								if (reJsonObject.has("Successed")) {
									if (reJsonObject.getBoolean("Successed")) {
										msg.what = Constant.MSG_OPERATE_SUCCESS;
										msg.arg1 = scheId;
										msg.obj = "提交成功 !";
										handler.sendMessage(msg);
									} else {
										msg.what = Constant.MSG_FAIL;
										msg.obj = "提交失败 !";
								
										handler.sendMessage(msg);
									}
								}

							} catch (JSONException e) {
								e.printStackTrace();
								msg.what = Constant.MSG_FAIL;
								msg.obj = "返回数据错误";
								handler.sendMessage(msg);
							}

						}

					}

					/*
					 * 确保下方的Message 一定是news
					 */
					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "正在提交...";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，提交失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，提交失败 !";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void updateScheduleStatus(Context context, final Handler handler,
			String url, RequestParams params, final int id, final int status) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (!StringUtils.isEmpty(result)) {
							try {
								JSONObject reJsonObject = new JSONObject(result);

								if (reJsonObject.has("Successed")) {
									if (reJsonObject.getBoolean("Successed")) {

										msg.what = Constant.MSG_UPDATE_SCHEDULE_SUCCESS;
																			
										msg.arg1 = id;
										msg.arg2 = status;

										handler.sendMessage(msg);

									} else {
										msg.what = Constant.MSG_FAIL;
										msg.obj = "日程更新失败 !";
								
										handler.sendMessage(msg);
									}
								}

							} catch (JSONException e) {
								e.printStackTrace();
								msg.what = Constant.MSG_FAIL;
								msg.obj = "返回数据错误";
								handler.sendMessage(msg);
							}
						}

					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message message = handler.obtainMessage();
						message.what = Constant.MSG_SHOW_PROGRESS;
						message.obj = "正在更新...";
						handler.sendMessage(message);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，日程更新失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，日程更新失败 !";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void deleteSchedule(Context context, final Handler handler,
			String url, RequestParams params, final int id) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String resultString = responseInfo.stringResult;
						if (!StringUtils.isEmpty(resultString)) {
							try {
								JSONObject reJsonObject = new JSONObject(resultString);

								if (reJsonObject.has("Successed")) {
									if (reJsonObject.getBoolean("Successed")) {
										
										msg.what = Constant.MSG_DELETE_SCHEDULE_SUCCESS;										
										msg.arg1 = id;
										
										handler.sendMessage(msg);

									} else {
										msg.what = Constant.MSG_FAIL;
										msg.obj = "网络异常，日程删除失败 !";

									
										handler.sendMessage(msg);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
								msg.what = Constant.MSG_FAIL;
								msg.obj = "返回数据错误";
								handler.sendMessage(msg);
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "正在删除...";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，日程删除失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，日程删除失败 !";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void login(Context context, final Handler handler, String url,
			RequestParams params) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.GET;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						
						
						if (!StringUtils.isEmpty(result)) {
																															
							User user = mGson.fromJson(result, User.class);	
							if (user!=null && user.isSuccessed()){
								msg.what = Constant.MSG_GETDATA_SUCCESS;
								msg.obj = user;
								handler.sendMessage(msg);
								
							}else {
								String error = user.getErrorMsg();
								msg.what = Constant.MSG_FAIL;
																																																																					
								if (!StringUtils.isEmpty(error)) {
									msg.obj = error+"!";
								}else {
									msg.obj = "用户名或密码错误 ,登录失败!";
								} 
								handler.sendMessage(msg);
							}
									
						}
						
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						// 显示进度条
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "正在登录...";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Log.e("m_tag", "==============" + responseInfo.httpStatus + "  === " +responseInfo.stringResult);
						// 包含请求超时，服务器地址错误等异常
						msg.what = Constant.MSG_FAIL;
						msg.obj = responseInfo.errorMessage/*"网络异常或服务器地址错误，登录失败!"*/;
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，登录失败 !";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void refreshHome(final Context context, final Handler handler,
			String url, RequestParams params) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
				
						HomeInfo homeInfo = mGson.fromJson(result,HomeInfo.class);
						if (null != homeInfo) {

							if (homeInfo.isSuccessed()) {
								msg.what = Constant.MSG_GETDATA_SUCCESS;
								msg.obj = homeInfo;
								handler.sendMessage(msg);

								NotificationUtlis.showNotification(context,
										homeInfo);

								/* 判断是否有日程提醒 */
								if (JudgeUtils.check(homeInfo)) {
									getScheduleByDay(context);
								}

							} else {
								msg.what = Constant.MSG_FAIL;

								msg.obj = "网络异常，刷新失败 !";

								handler.sendMessage(msg);
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "正在刷新...";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，刷新失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.obj = "网络异常，刷新失败 !";
						handler.sendMessage(msg);
					}
				}, this);
	}

	public void refreshHomeInService(final Context context, String url,
			RequestParams params) {
		RequestInfo request = new RequestInfo(context, url);
		request.method = RequestMethod.POST;
		request.params = params;

		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
					
						HomeInfo homeInfo = mGson.fromJson(result,
								HomeInfo.class);
						if (null != homeInfo) {

							if (homeInfo.isSuccessed()) {
								NotificationUtlis.showNotification(context,
										homeInfo);
								/* 判断是否有日程提醒 */
								if (JudgeUtils.check(homeInfo)) {
									//
									getScheduleByDay(context);
								}
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {

					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {

					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {

					}
				}, this);

	}

	/**
	 * 获取一天的全部日程
	 */
	public void getScheduleByDay(Context context) {
		RequestParams params = new RequestParams();
		String id = ((GTAApplication) context.getApplicationContext()).getUserID();
		String today = JudgeUtils.format.format(new Date());
		params.addParams("userId", id);
		params.addParams("selectTime", today);

		RequestInfo request = new RequestInfo(context,
				URLs.getScheduleByDayURL());
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (!StringUtils.isEmpty(result)) {
							try {

								JSONObject reJsonObject = new JSONObject(result);
								if (reJsonObject.has("Successed")) {
									if (reJsonObject.getBoolean("Successed")) {
										JSONArray jsonArray = reJsonObject
												.getJSONArray("Data");
									

										Type listType = new TypeToken<List<Schedule>>() {
										}.getType();
										List<Schedule> schedulelist = mGson.fromJson(jsonArray.toString(),listType);
										GTAApplication.instance.setTodayScheTemp(schedulelist);
									} else {
										
										String error = reJsonObject.getJSONObject("ErrorMsg").toString();
										
									

									}
								}
							} catch (JSONException e) {
								e.printStackTrace();

							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {

					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {

					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {

					}
				}, this);

	}


	public void loadBitmap(final Handler handler, String url,
			RequestParams params) {
		RequestInfo request = new RequestInfo(GTAApplication.instance, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new BitmapParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {

						Bitmap bm = (Bitmap) responseInfo.Entity;
						msg.what = Constant.MSG_OPERATE_SUCCESS;
						msg.arg1 = 2;
						msg.obj = bm;
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						Message news = handler.obtainMessage();
						news.what = Constant.MSG_SHOW_PROGRESS;
						news.obj = "加载中...";
						handler.sendMessage(news);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = Constant.MSG_FAIL;
						msg.arg1 = 2;
						msg.obj = "网络异常，获取服务器最新头像失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = Constant.MSG_FAIL;
						msg.arg1 = 2;
						msg.obj = "网络异常，获取服务器最新头像失败 !";
						handler.sendMessage(msg);
					}
				}, this);

	}

	/**
	 * 以下是待办已办的2个联网
	 */
	public void getPeopleList(final Handler handler, String url,
			RequestParams params) {
		RequestInfo request = new RequestInfo(GTAApplication.instance, url);
		request.method = RequestMethod.POST;
		request.params = params;
		
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					Message msg = handler.obtainMessage();
					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result=responseInfo.stringResult;
						
						TaskPeople taskPeople = mGson.fromJson(result, TaskPeople.class);
						
//						TaskPeople taskPeople	= (TaskPeople) responseInfo.Entity;
						
						
						if (null == taskPeople) {
							msg.what = -1;
							msg.obj = "获取失败";
							handler.sendMessage(msg);	
							return;
						}
						if (taskPeople.isSuccessed()) {
							msg.what = 1;
							msg.obj = taskPeople;
							handler.sendMessage(msg);					
						}else {
							msg.what = -1;
							msg.obj = result;
							handler.sendMessage(msg);					
						}
						
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						handler.sendEmptyMessage(10);
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						msg.what = -1;
						msg.obj = "网络异常，获取人员列表失败 !";
						handler.sendMessage(msg);
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						msg.what = -1;
						msg.obj = "网络异常，获取人员列表失败 !";
						handler.sendMessage(msg);
					}
				}, this);

	}

	public void TaskSubmit(final Handler handler, String url,
			RequestParams params,final int voteAgree) {
		RequestInfo request = new RequestInfo(GTAApplication.instance, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),new RequestListener() {
			Message msg = handler.obtainMessage();
			
			@Override
			public void onUploadProgress(String url, int progress) {				
			}
			
			@Override
			public void onRequestSucceed(ResponseInfo responseInfo) {
				String result = responseInfo.stringResult;
				if (!StringUtils.isEmpty(result)) {
					try {
						JSONObject reJsonObject = new JSONObject(result);
						if (reJsonObject.has("Successed")) {
							
							if (reJsonObject.getBoolean("Successed")) {								
								msg.obj="提交成功！";	
								msg.what = 2;
								handler.sendMessage(msg);
							}else{
								msg.what= -1;
								if(voteAgree == 3){ // 驳回
									msg.obj = "3";
								}else {
									msg.obj = result;
								}
								handler.sendMessage(msg);
							}
							
						}else {						
							String error = "网络异常，提交失败!";
							msg.what=-2;
							msg.obj = error;
							handler.sendMessage(msg);
						}
																																
					}catch(JSONException e){
						msg.what=-2;
						msg.obj = "返回数据错误，提交失败 !";
						handler.sendMessage(msg);
					}
				}
			}
			
			@Override
			public void onRequestStart(RequestInfo requestInfo) {
				handler.sendEmptyMessage(10);
			}
			
			@Override
			public void onRequestError(ResponseInfo responseInfo) {
				msg.what=-2;
				msg.obj = "网络异常，提交失败！";
				handler.sendMessage(msg);
			}
			
			@Override
			public void onRequestCancelled(RequestInfo requestInfo) {			
			}
			
			@Override
			public void onNoNetWork() {
				msg.what=-2;
				msg.obj = "网络异常，提交失败！";
				handler.sendMessage(msg);			
			}
		},this);
	}

	public void getTaskHistory(final Handler handler, String url,
			RequestParams params) {
		RequestInfo request = new RequestInfo(GTAApplication.instance, url);
		request.method = RequestMethod.POST;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),new RequestListener() {
			Message msg = handler.obtainMessage();
			@Override
			public void onUploadProgress(String url, int progress) {
				
			}
			
			@Override
			public void onRequestSucceed(ResponseInfo responseInfo) {
				String result = responseInfo.stringResult;
				if (!StringUtils.isEmpty(result)) {
					try {
						JSONObject reJsonObject = new JSONObject(result);
						if (reJsonObject.has("Successed")) {
							if (reJsonObject.getBoolean("Successed")) {
								JSONArray jsonArray = reJsonObject.getJSONArray("Data");
							
								Type listType = new TypeToken<List<ProcessState>>() {}.getType();
								List<ProcessState> list = mGson.fromJson(jsonArray.toString(),listType);
								msg.what =1;
								msg.obj =list;
								handler.sendMessage(msg);
										
							}else{
								msg.what = -1;
								msg.obj = "网络异常，加载数据失败!";
								handler.sendMessage(msg);
							}
						}
					}catch(JSONException e){
						msg.what = -1;
						msg.obj = "返回的数据格式错误，加载数据失败！";
						handler.sendMessage(msg);
					}
				}
				
			}
			
			@Override
			public void onRequestStart(RequestInfo requestInfo) {
				Message news =  handler.obtainMessage();
				news.what = 10;
				news.obj = "正在获取数据...请稍候";
				handler.sendMessage(news);
			}
			
			@Override
			public void onRequestError(ResponseInfo responseInfo) {
				msg.what =-1;
				msg.obj ="网络异常，加载数据失败！";
				handler.sendMessage(msg);
				
			}
			
			@Override
			public void onRequestCancelled(RequestInfo requestInfo) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNoNetWork() {
				msg.what =-1;
				msg.obj ="网络异常，加载数据失败！";
				handler.sendMessage(msg);
				
			}
		},this);
		
	}

	public void getSchedule(final Handler handler, String scheduleURL,
			RequestParams params) {
		RequestInfo request = new RequestInfo(GTAApplication.instance, scheduleURL);
		request.method = RequestMethod.GET;
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),new RequestListener() {
			Message msg = handler.obtainMessage();
			@Override
			public void onUploadProgress(String url, int progress) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onRequestSucceed(ResponseInfo responseInfo) {
				
				String result = responseInfo.stringResult;
				if (!StringUtils.isEmpty(result)) {
					try {
						JSONObject jsonObject = new JSONObject(result);
						
						if (jsonObject.has("Successed")) {
							if (jsonObject.getBoolean("Successed")) {
								
								JSONObject obj = jsonObject.getJSONObject("Data");								
								String string = obj.toString();
								Schedule schedule = mGson.fromJson(string, Schedule.class);
															
								msg.obj = schedule;
								msg.what =Constant.MSG_GETDATA_SUCCESS;
								handler.sendMessage(msg);
										
							}else{
								msg.what = Constant.MSG_FAIL;
								msg.obj = "网络异常，加载数据失败!";
								handler.sendMessage(msg);
							}
						}
					}catch(JSONException e){
						msg.what = Constant.MSG_FAIL;
						msg.obj = "返回的数据格式错误，加载数据失败！";
						handler.sendMessage(msg);
					}
					
				}
									
			}
			
			@Override
			public void onRequestStart(RequestInfo requestInfo) {
				Message news = handler.obtainMessage();
				news.what = Constant.MSG_SHOW_PROGRESS;
				news.obj = "正在加载...";
				handler.sendMessage(news);
				
			}
			
			@Override
			public void onRequestError(ResponseInfo responseInfo) {
				msg.what = Constant.MSG_FAIL;
				msg.obj = "网络异常,加载数据失败!";
				handler.sendMessage(msg);
				
			}
			
			@Override
			public void onRequestCancelled(RequestInfo requestInfo) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNoNetWork() {
				msg.what = Constant.MSG_FAIL;
				msg.obj = "网络异常,加载数据失败!";
				handler.sendMessage(msg);
				
			}
		},this);
		
	}
}
