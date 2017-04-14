package com.gta.scpoa.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.util.NotificationUtlis;
import com.gta.scpoa.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 日程提醒的服务，每隔 5 分钟去查询一次缓存集合
 * 
 * @author bin.wang1
 * 
 */
public class ScheduleTimerService extends Service {

	private Timer timer = new Timer();
	private int num = 10;
	public static SimpleDateFormat format = new SimpleDateFormat("H:m",
			Locale.CHINA);

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		timingUpdateState();
		return super.onStartCommand(intent, flags, startId);
	}

	public void timingUpdateState() {
		/*
		 * 定义计划任务，根据参数的不同可以完成以下种类的工作：在固定时间执行某任务，在固定时间开始重复执行某任务，重复时间间隔可控，在延迟多久后执行某任务
		 * ，在延迟多久后重复执行某任务，重复时间间隔可控
		 */timer.scheduleAtFixedRate(new TimerTask() {
			// TimerTask 是个抽象类,实现的是Runable类
			@Override
			public void run() {
				// 查询缓存集合的日程，查看其日程的开始时间，与当前时间进行对比，如果差小于10分钟，则用通知将该日程提醒出来。
				search();
			}

		}, 0, getTimeSpan());
	}

	/**
	 * 查询
	 */
	protected void search() {
		GTAApplication context = ((GTAApplication) getApplicationContext());
		// 比较开始时间
		List<Schedule> list = context.getTodayScheTemp();
		if (null != list && !list.isEmpty()) {

			String now = format.format(new Date());
			String[] nowArray = now.split(":");

			for (Schedule schedule : list) {
				String start = schedule.getStartTime();// StartTime":"2015-01-22T16:00:00
				start = start.split("T")[1];
				String[] startArray = start.split(":");
				boolean result = compareTime(nowArray, startArray);
				if (result) {
					if (schedule.getRemind() != 5) {
						//如果是未完成才会提醒
						if (schedule.getStatus()==0) {
							
						
							// 如果没有通知过
							if (!schedule.isHasNoti()) {
								NotificationUtlis.showScheduleNotification(	getApplicationContext(), schedule, num++);
								context.updateTodaySche(schedule.getId(),true);
							}
						
						}
					}
				}
			}
		}
	}

	private boolean compareTime(String[] nowArray, String[] startArray) {
		boolean b = false;
		int nowHour = 0;
		int nowMin = 0;
		if (nowArray.length == 2) {
			nowHour = Integer.parseInt(nowArray[0]);
			nowMin = Integer.parseInt(nowArray[1]);
		}

		int startHour = 0;
		int startMin = 0;

		if (startArray.length == 3) {
			startHour = Integer.parseInt(deleteZero(startArray[0]));
			startMin = Integer.parseInt(deleteZero(startArray[1]));
		} else if (startArray.length == 2) {
			startHour = Integer.parseInt(deleteZero(startArray[0]));
			startMin = Integer.parseInt(deleteZero(startArray[1]));
		}

		if (nowHour == startHour) {
			int d = startMin - nowMin;
			if (d > 0 && d <= 10) {
				b = true;
			}
		} else if (nowHour < startHour) {
			int c = (60 - nowMin) + startMin;
			if (c > 0 && c <= 10) {
				b = true;
			}
		}

		return b;
	}

	private String deleteZero(String t) {
		String s = null;
		if (!StringUtils.isEmpty(t)) {
			if (t.startsWith("0") && t.length() == 2) {
				s = t.substring(1, t.length());
			} else {
				s = t;
			}
		}
		return s;
	}

	/**
	 * 定时器
	 */
	public long getTimeSpan() {
		return 1 * 10 * 1000; // 10秒
	}

	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}
}
