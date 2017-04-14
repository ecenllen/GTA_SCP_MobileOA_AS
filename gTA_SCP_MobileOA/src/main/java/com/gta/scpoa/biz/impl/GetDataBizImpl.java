package com.gta.scpoa.biz.impl;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.gta.http.RequestParams;
import com.gta.http.entity.MimeType;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.entity.TaskPeople;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.URLs;

/**
 * 用于访问网络的数据的类
 * 
 * @author bin.wang1
 * 
 */
public class GetDataBizImpl implements IGetDataBiz {

	private static HttpRequestMethod request;

	public GetDataBizImpl() {
		super();
		if (null == request) {
			request = new HttpRequestMethod();
		}
	}

	public void uploadProtrait(Handler handler, String userId, File file) {
		RequestParams params = new RequestParams();
		params.addParams("userId", userId);
		params.addBodyParameter("key", file, MimeType.APPLICATION_OCTET_STREAM);
		request.uploadProtrait(handler, URLs.getUploadFileURL(), params);
	}

	/**
	 * 获取某一月的所有日程情况
	 * 
	 * @param context
	 */
	public void getScheduleList(Context context, Handler handler, String userId,
			String startTime, String endTime) {
		RequestParams params = new RequestParams();
		params.addParams("userId", userId);
		params.addParams("beginTime", startTime);
		params.addParams("endTime", endTime);
		request.getMonthScheduleList(context, handler,
				URLs.getScheduleListURL(), params);

	}

	public void sendNewSchedule(Context context, Handler handler, String userId,
			Schedule schedule) {
		RequestParams params = new RequestParams();
		params.addParams("id", String.valueOf(schedule.getId()));
		params.addParams("startTime", schedule.getStartTime());
		params.addParams("endTime", schedule.getEndTime());
		params.addParams("scheduleType",
				String.valueOf(schedule.getScheduleType()));
		params.addParams("remind", String.valueOf(schedule.getRemind()));
		params.addParams("scheduleContent", schedule.getScheduleContent());
		params.addParams("createBy", userId);

		request.sendNewSchedule(context, handler, URLs.getSendScheduleURL(),
				params, schedule.getId());
	}

	@Override
	public void changeScheduleStatus(Context context, Handler handler, int id,
			int status) {
		RequestParams params = new RequestParams();
		params.addParams("id", String.valueOf(id));
		params.addParams("status", String.valueOf(status));
		request.updateScheduleStatus(context, handler,
				URLs.getUpdateScheduleStatusURL(), params, id, status);

	}

	@Override
	public void deleteSchedule(Context context, Handler handler, int id) {
		RequestParams params = new RequestParams();
		params.addParams("id", String.valueOf(id));
		request.deleteSchedule(context, handler, URLs.getDeleteScheduleURL(),
				params, id);
	}

	@Override
	public void login(Context context, Handler handler, String userName,
			String password) {
		RequestParams params = new RequestParams();
		params.addParams("LoginName", userName);
		params.addParams("password", password);
		Log.e("m_tag", "=====================url==" + URLs.getLoginURL());
		request.login(context, handler, URLs.getLoginURL(), params);
	}

	@Override
	public void updateHome(Context context, Handler handler, String userId) {
		RequestParams params = new RequestParams();
		params.addParams("userId", userId);
		String userName = GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		params.addParams("userName", userName);
		request.refreshHome(context, handler, URLs.getHomeInfoRefreshURL(),params);
	}

	@Override
	public void updateHomeInService(Context context) {
		RequestParams params = new RequestParams();
		String userId = ((GTAApplication) context.getApplicationContext()).getUserID();
		String userName = GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME); 
		params.addParams("userId", userId);
		params.addParams("userName", userName);
		request.refreshHomeInService(context, URLs.getHomeInfoRefreshURL(),params);
	}

	@Override
	public void loadProtrait(Handler handler, String imgUrl) {
		RequestParams params = new RequestParams();
		String userId = GTAApplication.instance.getUserID();
		params.addParams("userId", userId);
		request.loadBitmap(handler, imgUrl, params);
	}

	/**
	 * 以下四个接口是待办已办功能模块的。
	 */
	@Override
	public void getPeopleList(String acountName, String taskId, boolean flag,
			Handler handler) {
		RequestParams params = new RequestParams();
		params.addParams("UserName", acountName);
		params.addParams("TaskId", taskId);
		request.getPeopleList(handler, URLs.getPeopleListURL(), params);
	}

	@Override
	public void TaskSubmit(String taskId, String acountName, int voteAgree,
			String voteContent, ArrayList<TaskPeople> list,String nextNodeId,String isBack, boolean flag,
			Handler handler) {

		RequestParams params = new RequestParams();
		params.addParams("TaskId", taskId);
		params.addParams("UserName", acountName);
		
		//0=弃权、1=同意、2=反对、3=驳回、4=追回、5=会签通过、6=会签不通
		if (voteAgree == -1) {//如果不填默认为1（同意）
			voteAgree = 1; // 默认为同意
		}
		params.addParams("voteAgree", String.valueOf(voteAgree));

		if (StringUtils.isEmpty(voteContent)) {
			if (voteAgree == 1) {
				voteContent = "同意";
			} else if (voteAgree == 3) {
				voteContent = "不同意";
			}else if (voteAgree == 4) {
				voteContent = "同意";
			}
		}
		params.addParams("voteContent", voteContent);
		StringBuilder sb = new StringBuilder();
		if (null != list &&list.size()!=0) {
			
			for (int i = 0; i < list.size(); i++) {		
				sb.append(list.get(i).getKey()).append(",");			
			}
			sb.deleteCharAt(sb.length()-1);		
		}			
		params.addParams("nextUser", sb.toString());
		
		if (!StringUtils.isEmpty(nextNodeId)) {			
			params.addParams("nextNodeId", nextNodeId);
		}
		params.addParams("isBack",isBack);
		request.TaskSubmit(handler, URLs.getTaskSubmitURL(), params,voteAgree);
	}

	@Override
	public void getTaskHistory(String ActInstId, String runId, Handler handler) {
		
		RequestParams params = new RequestParams();
		params.addParams("ActInstId", ActInstId);
		params.addParams("RunId", runId);
		request.getTaskHistory(handler,URLs.getTaskHistoryURL(),params);
	}

	@Override
	public void getSchedule(Context context, Handler handler, int scheduleId) {
		RequestParams params = new RequestParams();
		params.addParams("id", String.valueOf(scheduleId));
		request.getSchedule(handler,URLs.getScheduleURL(),params);
	}

}
