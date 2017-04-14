package com.gta.scpoa.common;


/**
 * 保存常用的字符串、键值对等信息
 * 
 * @author bin.wang1
 * 
 */
public class Constant {
	//获取数据成功的消息
	public static final int MSG_GETDATA_SUCCESS = 1001;
	//操作成功的消息
	public static final int MSG_OPERATE_SUCCESS = 1002;
	public static final int MSG_DELETE_SCHEDULE_SUCCESS = 10021;
	public static final int MSG_UPDATE_SCHEDULE_SUCCESS = 10022;
	
	
	public static final int MSG_FAIL=-1001;
	public static final int MSG_SHOW_PROGRESS = 1000;

	public static final int MSG_TASK_AGREE = 10000;//（传ID出来）
	
	public static final String ACCOUNT_NAME = "user.name";

	public static final String ACCOUNT_PWD = "user.pwd";

	public static final String SERVER_ADDR = "server.addr";

	// K-V 键值对
	public static final String PROP_KEY_PRIVATE_TOKEN = "private_token";
	public static final String PROP_KEY_UID = "user.uid";
	public static final String PROP_KEY_TIME_SPAN = "time.span";
	public static final String PROP_KEY_PORTRAIT = "user.portrait";// 头像的文件名
	public static final String PROP_KEY_SERVER_PORTRAIT="user.serverportrait";//服务器传递过来的头像。
	public static final String PROP_KEY_USERNAME = "user.username";
	public static final String PROP_KEY_FULLNAME = "user.fullname";
	public static final String PROP_KEY_DEPARTMENT = "user.department";
	public static final String PROP_KEY_TELEPHONE = "user.telephone";
	public static final String PROP_KEY_EMAIL = "user.useremail";
	
	public static final String PROP_KEY_SWITCHER_NOTICE = "switcher.notice";
	public static final String PROP_KEY_SWITCHER_RING = "switcher.ring";
	public static final String PROP_KEY_SWITCHER_SHAKE = "switcher.shake";
	public static final String PROP_KEY_BPMHOST= "user.bpmhost";
	// 广播 字符串
	// 默认定时时间 10分钟
	public static final int DEFAULT_TIME_SPAN = 1;

	
	/*用于存储通知最新时间的键值*/
	public static final String TasksId = "TasksId";
	public static final String NoticeId = "NoticeId";
	public static final String MailId = "MailId";
	public static final String MeetingId = "MeetingId";
	public static final String RecordId = "RecordId";
	public static final String ScheduleId = "ScheduleId";
	/**** 邮件部分 ***************************************************/

	/* 单个邮件附件的大小限制 */
	public static int singleAttachSize = 6 * 1024 * 1024;
	/* 所有附件大小限制 */
	public static int allAttachSize = singleAttachSize * 2;
	/* 附件下载路径 */
	public static final String downLoadPath = "/gtaScpoa/";
	/* 进入邮件编辑界面的参数 */
	public static String SendStatue = "SendStatue"; // 进入编辑界面的参数
	public static int Send_NewMail = 0; // 0为新建邮件
	public static int Send_FastRepaly = 1; // 1为快速回复
	public static int Send_SendMail = 2; // 2为发件箱进入的时候
	public static int Send_draftMail = 3; // 3为草稿箱进入的时候
	public static final String UNREAD_COUNT = "UnreadCount";  //未读邮件记录
	public static final String RE_LAOD = "IsReLoad"; //是否返回要重新加载
	
	public static final int AttACH_LOAD_SUCCESS = 25;
	/***********************************************************/
	
	public static int MeetNumber = 0;    // 包括会议通知未读数和会议纪要未读数的总和
	public static String taskNumber = "";
	public static int Record = 0;
	
	/** 后来新增字段, 推送消息类型1.待办2.公文公告3.会议4.邮箱5.日程6.日程详情*/
	public static final int JPUSH_MSG_TYPE_TASKS = 1;
	public static final int JPUSH_MSG_TYPE_NOTICE = 2;
	public static final int JPUSH_MSG_TYPE_MEETING = 3;
	public static final int JPUSH_MSG_TYPE_MAIL = 4;
	public static final int JPUSH_MSG_TYPE_SCHEDULE = 5;
	public static final int JPUSH_MSG_TYPE_SCHEDULE_DETAIL = 6;
}
