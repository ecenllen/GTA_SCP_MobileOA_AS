package com.gta.scpoa.util;

import java.util.Date;

import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.common.Constant;

public class URLs {

	public final static String SEPARATOR = "/";

	private static String getHostIP() {
		return GTAApplication.instance.getProperty(Constant.SERVER_ADDR);
	}
	public static String getDefaultBaseURL() {
		return "http://" + getHostIP() + "/api/MobileApi";
	}
	public static String getLoginURL() {
		return getDefaultBaseURL() + SEPARATOR + "Login";
	}
	public static String getHomeInfoRefreshURL() {
		return getDefaultBaseURL() + SEPARATOR + "GetPendingCount";
	}
	/**
	 * 192.168.193.120/api/MobileApi/UserImgModify
	 * @return
	 */
	public static String getUploadFileURL(){
		return getDefaultBaseURL() + SEPARATOR + "UserImgModify";
	}
	public static String getScheduleListURL(){
		return getDefaultBaseURL() + SEPARATOR 	+ "GetCalendarList";
	}
	/**
	 * http://192.168.193.120/api/MobileApi/AddOrUpdateSchedule
	 * @return
	 */
	public static String getSendScheduleURL(){
		return getDefaultBaseURL()+SEPARATOR+"AddOrUpdateSchedule";
	}
	/**
	 * http://192.168.193.120/api/MobileApi/ChangeScheduleStatus
	 * @return
	 */
	public static String getUpdateScheduleStatusURL(){
		return getDefaultBaseURL()+SEPARATOR+"ChangeScheduleStatus";
	}
	/**
	 * http://192.168.193.120/api/MobileApi/DeleteSchedule
	 * @return
	 */
	public static String getDeleteScheduleURL(){
		return getDefaultBaseURL()+SEPARATOR+"DeleteSchedule";
	}
	
	/**
	 * 获取一天的所有日程
	 * http://192.168.193.120/api/MobileApi/GetCalendarDetail
	 */
	public static String getScheduleByDayURL(){
		return getDefaultBaseURL()+SEPARATOR+"GetCalendarDetail";
	}
	
	/**--以下是待办已办的3个接口的地址--*/
	/**
	 * 获取下个任务节点执行人
	 * @return
	 */
	public static String getPeopleListURL(){
		return getDefaultBaseURL()+SEPARATOR+"GetTaskHandleUsers";
	}
	/**
	 * 流程任务审批处理
	 * @return
	 */
	public static String getTaskSubmitURL(){
		return getDefaultBaseURL()+SEPARATOR+"ProcessDoNext";
	}
	/**
	 * http://192.168.193.120/API/MobileAPI/GetProcessOpinion
	 * @return
	 */
	public static String getTaskHistoryURL(){
		return getDefaultBaseURL()+ SEPARATOR+"GetProcessOpinion";
	}
	
	public static String getScheduleURL(){
		return getDefaultBaseURL()+SEPARATOR+"GetScheduleDetail";
	}
	//获取版本更新路径
	public static String getVersionURL(){
//		return "http://" + "10.1.41.21" + "/OA_Version.txt?time="+new Date().getTime();
		return "http://" + getHostIP() + "/OA_Version.html?time="+new Date().getTime();
	}
}
