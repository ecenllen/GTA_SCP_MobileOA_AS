package com.gta.scpoa.service;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;

import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 首页定时获取服务器的数据，刷新界面(待办、未读邮件数等)
 * 
 * @author bin.wang1
 * 
 */
@SuppressLint("NewApi")
public class TimerService extends Service {

	private Timer timer = new Timer();
	private TimerBroadcastReceiver receiver;
	private FileDownLoadReceiver downReceiver;
	private MyTimerTask mTimerTask;
	public static String ACTION_TIMER_SPAM_CHANGED = "action_timerservice_timerspan_changed";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		receiver = new TimerBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_TIMER_SPAM_CHANGED);
		registerReceiver(receiver, filter);
		
		
		downReceiver = new FileDownLoadReceiver();
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(downReceiver, filter1);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO 已做推送则不需要下面功能，否则需要
//		firstNotify();
//		initTimerTask();
//		timingUpdateState();
		return super.onStartCommand(intent, flags, startId);
	}

	public void timingUpdateState() {
		long span = getTimeSpan();
		if (null != timer) {
			timer.scheduleAtFixedRate(mTimerTask, span, span);
		}
	}

	/**
	 * 服务器启动时去执行一次获取数据并通知
	 */
	private void firstNotify() {
		IGetDataBiz api = new GetDataBizImpl();
		api.updateHomeInService(getApplicationContext());
	}

	/**
	 * 定时器 注意 10（10 * 60 * 1000）分钟为一个单位
	 */

	public long getTimeSpan() {
		String timeSpan = GTAApplication.instance.getProperty(Constant.PROP_KEY_TIME_SPAN);
		long min = 1000*60*10;//10分钟
		if (StringUtils.isEmpty(timeSpan) || timeSpan.equals("0")) {			
			return Constant.DEFAULT_TIME_SPAN * min;// 如果为空，则使用默认时间		
		} else {
			int d=Integer.parseInt(timeSpan);					
			return d*min;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		unregisterReceiver(downReceiver);
		if (null != timer) {
			if (null != mTimerTask) {
				mTimerTask.cancel();
			}
			timer.cancel();
		}
		/*去掉所有未下载的文件*/
		deleteAllUnLoad();
	}

	private class TimerBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			initTimerTask();
			timingUpdateState();
		}
	}

	private void initTimerTask() {
		if (null != mTimerTask) {
			mTimerTask.cancel();// 将原任务从队列中移除
		}
		mTimerTask = new MyTimerTask();// 新建一个任务 注意：每次放定时任务前，确保之前任务已从定时器队列中移除。
	}

	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// 调用刷新接口，获取最新的数据
			IGetDataBiz api = new GetDataBizImpl();
			api.updateHomeInService(getApplicationContext());
		}
	}
	
	
	
	/*文件下载成功使用的广播*/
	private class FileDownLoadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
			removeKey(reference); //下载成功  去除有包含ID的Key
		}
	}
	
	/*下载成功  去除有包含ID的Key*/
	private void removeKey(long Id) {
		Map<String, Object> map = PreferencesUtils
				.getAll(getApplicationContext());
		if (map.size() <= 0)
			return;
		Set<String> keys = map.keySet();
		if (keys != null) {
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				try {
					Long value = (Long) map.get(key);
					if(value == Id){
						PreferencesUtils.deleteKey(getApplicationContext(), key);
						break;
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	/* 退出登录的时候 删除未下载的文件 */
	private void deleteAllUnLoad() {
		Map<String, Object> map = PreferencesUtils
				.getAll(getApplicationContext());
		if (map.size() <= 0)
			return;
		Set<String> keys = map.keySet();
		if (keys != null) {
			Cursor cursor = null;
			try {  // 对Cursor操作，加try-catch-finally块，在catch、finally块中进行Cursor判断置null
				
				Iterator<String> iterator = keys.iterator();
				/*取消下载*/
				DownloadManager downloadManager = (DownloadManager) getApplicationContext()
						.getSystemService(Service.DOWNLOAD_SERVICE);
				while (iterator.hasNext()) {
					String key = iterator.next();
					Long id = 0L;
					try {
						id = (Long) map.get(key);
					} catch (Exception e) {
						continue;
					}
					/*删除key value*/
					PreferencesUtils.deleteKey(getApplicationContext(), key);
					DownloadManager.Query query = new DownloadManager.Query();
					query.setFilterById(id);
					cursor = downloadManager.query(query);
					if (cursor != null && cursor.moveToFirst()) {
						int columnStatus = cursor
								.getColumnIndex(DownloadManager.COLUMN_STATUS);
						int status = cursor.getInt(columnStatus);
						switch (status) {
						case DownloadManager.STATUS_SUCCESSFUL:
							break;
						case DownloadManager.STATUS_FAILED:
						case DownloadManager.STATUS_PAUSED: // 暂停
						case DownloadManager.STATUS_PENDING: // 网络状态改变
						case DownloadManager.STATUS_RUNNING:
						default:
							downloadManager.remove(id);
						break;
						}
					}
					if(cursor!=null)cursor.close();
				}
			} catch (Exception e) {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}finally{
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}
	}
}
