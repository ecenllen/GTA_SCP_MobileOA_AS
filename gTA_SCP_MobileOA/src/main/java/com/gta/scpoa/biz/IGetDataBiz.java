package com.gta.scpoa.biz;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;

import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.entity.TaskPeople;

public interface IGetDataBiz {

	public void login(Context context, Handler handler, String userName,String password);

	public void uploadProtrait(Handler handler, String userId, File file);

	public void getScheduleList(Context context, Handler handler, String userId,String startTime, String endTime);
	public void getSchedule(Context context,Handler handler,int scheduleId);
	public void sendNewSchedule(Context context, Handler handler, String userId,Schedule schedule);

	public void changeScheduleStatus(Context context, Handler handler, int id,int status);

	public void deleteSchedule(Context context, Handler handler, int id);

	public void updateHome(Context context, Handler handler, String userId);

	public void updateHomeInService(Context context);

	/**
	 * modify 个人设置加载头像
	 */
	public void loadProtrait(Handler handler, String imgUrl);

	// 以下是待办已办的4个接口
	/**
	 * 
	 * @param acountName
	 *            用户的账户名，现在暂时都用"admin" ，等服务器端有账户验证之后，用acountName =
	 *            GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);就好了
	 * @param taskId
	 *            任务的编号
	 * @param flag
	 *            是否为转交 true:转交 false:提交
	 * @param handler
	 */
	public void getPeopleList(String acountName, String taskId, boolean flag,
			Handler handler);

	/**
	 * 
	 * @param taskId
	 *            任务的编号
	 * @param acountName
	 *            用户的账户名，现在暂时都用"admin" ，等服务器端有账户验证之后，用acountName =
	 *            GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);就好了
	 * @param type
	 *            类型 0=弃权、1=同意、2=反对、3=驳回、4=追回、5=会签通过、6=会签不通过，如果不填默认为1（同意）
	 * @param content
	 *            审批意见
	 * @param list
	 *            所提交的联系人集合
	 * @param flag
	 *            是否为转交 true:转交 false:提交
	 * @param handler
	 */
	public void TaskSubmit(String taskId, String acountName, int type,
			String content, ArrayList<TaskPeople> list,String nextNodeId,String isBack, boolean flag,
			Handler handler);

	/**
	 *  审批历史 联网业务方法。
	 * @param ActInstId
	 *            流程实例Id(按流程实例Id取得某个流程的审批历史,必填)
	 * @param runId
	 *            流程运行Id(按运行实例Id取得某个流程的审批历史,必填)
	 * @param handler
	 */
	public void getTaskHistory(String ActInstId, String runId, Handler handler);
}
