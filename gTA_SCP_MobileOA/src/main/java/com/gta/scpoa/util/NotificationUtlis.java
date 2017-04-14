package com.gta.scpoa.util;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.MailMainNewActivity;
import com.gta.scpoa.activity.MeetingMainActivity;
import com.gta.scpoa.activity.OfficialNoticeActivity;
import com.gta.scpoa.activity.ScheduleActivity;
import com.gta.scpoa.activity.TaskMainActivity;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.HomeInfo;
import com.gta.scpoa.entity.Schedule;

@SuppressLint("SimpleDateFormat")
public class NotificationUtlis {

	private static NotificationManager mNotiManager =(NotificationManager) GTAApplication.instance.
			getSystemService(Context.NOTIFICATION_SERVICE);
	
	private static int scheduleNum = 10;

	public static void showNotification(Context context, HomeInfo homeInfo) {
		if (!isShowNotice(context) || homeInfo == null) {
			return;
		}
		switch (homeInfo.getMsgType()) {
			case Constant.JPUSH_MSG_TYPE_TASKS:
				showTasksNotification(context, homeInfo); // 待办
				break;
			case Constant.JPUSH_MSG_TYPE_NOTICE:
				showNoticeNotification(context, homeInfo); // 公文公告
				break;
			case Constant.JPUSH_MSG_TYPE_MEETING:
				showMeetingNotification(context, homeInfo); // 会议
				break;
			case Constant.JPUSH_MSG_TYPE_MAIL:
				showMailNotification(context, homeInfo); // 邮件
				break;
			case Constant.JPUSH_MSG_TYPE_SCHEDULE:
				showScheduleNotification(context, homeInfo); // 日程
				break;
			case Constant.JPUSH_MSG_TYPE_SCHEDULE_DETAIL:
				// TODO 注意这个方法接收的参数不同，需要和PC沟通
				showScheduleNotification(context, homeInfo, scheduleNum++); // 日程详情
				break;
			default:

		}
	}

	public static boolean isShowNotice(Context context) {
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String noticeString = app
				.getProperty(Constant.PROP_KEY_SWITCHER_NOTICE);
		return Boolean.parseBoolean(noticeString);
	}

	/**
	 * 显示邮件通知
	 * @param context
	 * @param homeInfo
	 */
	public static void showMailNotification(Context context, HomeInfo homeInfo) {
		int nums = 0;
		try {
			nums = Integer.parseInt(homeInfo.getMail());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (nums <= 0)
			return;

		String showText = "未读邮件" + nums + "条";
		builderNotification(context, MailMainNewActivity.class, showText, 0, false);

	}

	/**
	 * 显示待办/已办通知 
	 * @param context
	 * @param homeInfo
	 */
	public static void showTasksNotification(Context context, HomeInfo homeInfo) {
		int nums = 0;
		try {
			nums = Integer.parseInt(homeInfo.getTasks());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (nums <= 0)
			return;

		String showText = "待办事宜" + nums + "条";

		builderNotification(context, TaskMainActivity.class, showText, 1, false);

	}

	/**
	 * 显示公文公告通知
	 * @param context
	 * @param homeInfo
	 */
	public static void showNoticeNotification(Context context, HomeInfo homeInfo) {
		int nums = 0;
		try {
			nums = Integer.parseInt(homeInfo.getNotice());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (nums <= 0)
			return;

		String showText = "未读公文公告" + nums + "条";
		
		builderNotification(context, OfficialNoticeActivity.class, showText, 2, false);
	}

	/**
	 * 显示会议通知
	 * @param context
	 * @param homeInfo
	 */
	public static void showMeetingNotification(Context context,
			HomeInfo homeInfo) {
		int nums = 0;
		try {
			nums = Integer.parseInt(homeInfo.getMeeting());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (nums <= 0)
			return;
		String showText = "未读会议" + nums + "条";

		builderNotification(context, MeetingMainActivity.class, showText, 3, false);
	}

	/**
	 * 通知栏显示未处理日程总数
	 * @param context
	 * @param homeInfo
     */
	public static void showScheduleNotification(Context context,
			HomeInfo homeInfo) {
		int nums = 0;
		try {
			nums = Integer.parseInt(homeInfo.getSchedule());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (nums <= 0)
			return;

//		long preId= PreferencesUtils.getLong(context, Constant.ScheduleId, 0);
//		String ScheduleTime = homeInfo.getSchUpdateTime();
//		if(ScheduleTime == null || ScheduleTime.equals("")){
//			return;
//		}
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
//		long curId;
//		try {
//			curId = sdf.parse(ScheduleTime).getTime();
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return ;
//		}
//		if (preId >= curId)return;
//		PreferencesUtils.putLong(context, Constant.ScheduleId, curId);

		String showText = "待处理日程" + nums + "条";

		builderNotification(context, ScheduleActivity.class, showText, 4, false);
	}

	/**
	 * 通知栏显示某时间点待处理日程详情(目前不是推送实现的，而是在ScheduleTimerService内部轮循)
	 * example 2017-3-14 14:00 至 2017-3-14 14:30 日程内容....
	 * @param context
	 */
	public static void showScheduleNotification(Context context,
												Schedule schedule, int num) {

		String content = schedule.getScheduleContent();
		String startT = schedule.getStartTime().replace("T", " ");
		startT = startT.replace(":00", "");
		String endT = schedule.getEndTime().replace("T", " ");
		endT = endT.replace(":00", "");
		String showText = startT + "至" + endT + "   " + content;

		builderNotification(context, ScheduleActivity.class, showText, num, true);
	}
	/**
	 * 通知栏显示某时间点待处理日程详情(目前不是推送实现的，而是在ScheduleTimerService内部轮循)
	 * example 2017-3-14 14:00 至 2017-3-14 14:30 日程内容....
	 * @param context
	 */
	public static void showScheduleNotification(Context context, HomeInfo homeInfo, int num) {

		String showText = homeInfo.getScheduleMsg();
		builderNotification(context, ScheduleActivity.class, showText, num, true);
	}

	/**
	 * 创建一条通知提醒
	 * @param context
	 * @param showText  通知内容
	 * @param notificationId 通知标识的ID
	 * @param isScheduleDetailNotification   是否日程详情提醒
	 */
	private static void builderNotification(Context context, Class activity, String showText, int notificationId, boolean isScheduleDetailNotification) {
		Notification.Builder Schedule = new Notification.Builder(context);
		Schedule.setSmallIcon(R.drawable.app_icon);
		Schedule.setContentTitle(context.getString(R.string.app_name));
		Schedule.setContentText(showText);
		Schedule.setTicker(showText);
		Schedule.setAutoCancel(true);


		// 点击通知跳转
		Intent notificationIntent = new Intent(context, activity);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		Schedule.setContentIntent(contentIntent);

		Notification notification;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification = Schedule.build(); //高于16
		} else {
			notification = Schedule.getNotification();
		}

//		Schedule.setLatestEventInfo(context, "移动OA", showText, contentIntent);
//		Schedule.flags = Notification.FLAG_AUTO_CANCEL; // 点击通知，自动清除

		if(isScheduleDetailNotification) { // 日程详情默认声音加震动
			notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		} else {
			setNotiFiCation(context, notification);// 设置通知是否震动和响铃
		}
		mNotiManager.notify(notificationId, notification);
	}

	/* 根据设置提示 */
	private static void setNotiFiCation(Context context,
			Notification notification) {

		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String ringString = app.getProperty(Constant.PROP_KEY_SWITCHER_RING);
		String shakeString = app.getProperty(Constant.PROP_KEY_SWITCHER_SHAKE);

		boolean isRing = Boolean.parseBoolean(ringString);
		boolean isShake = Boolean.parseBoolean(shakeString);

		/* 有震动有响铃 */
		if (isRing && isShake) {
			notification.defaults = Notification.DEFAULT_SOUND
					| Notification.DEFAULT_VIBRATE;
			return;
		}

		/* 有响铃没有震动 */
		if (isRing && !isShake) {
			notification.defaults = Notification.DEFAULT_SOUND;
			return;
		}

		/* 有震动没有响铃 */
		if (!isRing && isShake) {
			notification.defaults = Notification.DEFAULT_VIBRATE;
			return;
		}
	}

	public static void clearAllNotify() {
		mNotiManager.cancelAll();
	}
}
