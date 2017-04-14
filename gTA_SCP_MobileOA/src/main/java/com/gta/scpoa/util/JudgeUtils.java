package com.gta.scpoa.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.gta.scpoa.entity.HomeInfo;

public class JudgeUtils {
	/**
	 * 判断通知的未提醒日程数，已获取今日的日程，并更新缓存
	 */
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);	
	private static String scheduleUpdateTime="";
	private static String currentDate = format.format(new Date()); //存储年月日 
	private static int scheduleCount = 0;
	
	public static boolean check(HomeInfo info){
		boolean b = false;
		int count =Integer.parseInt(info.getSchedule());
		if (count > 0) {
			String today = format.format( new Date());
			if (! scheduleUpdateTime.equals( info.getSchUpdateTime() ) || ! currentDate.equals(today) || scheduleCount != Integer.parseInt(info.getSchedule()) ) {
				b = true;
				scheduleUpdateTime = info.getSchUpdateTime();
				currentDate = today;
				scheduleCount = count;
			}		
		}
		return b;		
	}
	
}
