package com.gta.scpoa.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.bitmap.core.ImageLoader;
import com.gta.scpoa.R;
import com.gta.scpoa.activity.LoginActivity;
import com.gta.scpoa.activity.MainActivity;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.service.TimerService;
import com.gta.scpoa.views.BaseView;
import com.gta.utils.thirdParty.jPush.JpushManager;

import java.util.Stack;

/**
 * 集成一些对界面的操作，如toast等
 * 
 * @author bin.wang1
 * 
 */
public class UIUtils {

	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 弹出Toast消息
	 * @param context
	 * @param msg
	 */
	public static void ToastMessage(Context context, int msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 弹出Toast消息
	 * @param context
	 * @param msg
	 * @param time
	 */
	public static void ToastMessage(Context context, String msg, int time) {
		Toast.makeText(context, msg, time).show();
	}
	public static void ToastMessageInDebugMode(Context context,String msg){
		if (com.gta.scpoa.common.Config.ISDEBUG) {
			ToastMessage(context, msg,1000);
		}
	}
	/**
	 * 返回到首页
	 * @param activity
	 */
	public static void goMainActivity(Activity activity) {
		Intent intent = new Intent(activity, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
		activity.finish();
	}

	/**
	 * 注销
	 */
	public static void logout(final Activity activity) {
		final AlertDialog myDialog = new AlertDialog.Builder(activity).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(
				R.id.tv_title);
		textView.setText("确定要退出吗?");
		myDialog.getWindow().findViewById(R.id.ok_button)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						myDialog.dismiss();
						
						/*清除待办已办的图片缓存*/
						ImageLoader.getInstance().clearCache();
						
						GTAApplication.instance.cleanLoginInfo();
						GTAApplication.instance.cleanPassword();
						GTAApplication.instance.clearTodayTempSche();

						Stack<Activity> activityStack = GTAApplication.instance
								.getActivityList();
						if (!(activityStack == null || activityStack.size() == 0)) {
							// finish所有Activity
							for (Activity act : activityStack) {
								act.finish();
							}
						}

						Intent intent = new Intent();
						intent.setClass(activity, LoginActivity.class);
						activity.startActivity(intent);
						activity.finish();
						// 清空所有的通知
						NotificationUtlis.clearAllNotify();
						/* 退出登录的时候 将提醒的最新时间ID全部清空 */
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.TasksId, 0);
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.NoticeId, 0);
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.MailId, 0);
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.MeetingId, 0);
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.RecordId, 0);
						PreferencesUtils.putLong(
								GTAApplication.instance.getApplicationContext(),
								Constant.ScheduleId, 0);
						/* 关闭服务 */
						Intent intent1 = new Intent(GTAApplication.instance
								.getApplicationContext(), TimerService.class);
						GTAApplication.instance.getApplicationContext()
								.stopService(intent1);
						
//						Intent intent2 = new Intent(GTAApplication.instance
//								.getApplicationContext(),
//								ScheduleTimerService.class);
//						GTAApplication.instance.getApplicationContext()
//								.stopService(intent2);
						
						/** 设置恢复推送*/
						JpushManager.getInstance().setResumeJpushEnable(false);
					}
				});

		myDialog.getWindow().findViewById(R.id.cancel_button)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						myDialog.dismiss();
					}
				});
	}

	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
	/**
	 * 显示头像
	 * @param handler
	 * @param iv
	 * @param url
	 * @param localPath
	 * @param w
	 * @param h
	 */
	public static void showPortrait(Handler handler, ImageView iv, String url,
			String localPath, int w, int h) {

		if (!StringUtils.isEmpty(url)) {
		
			if (!StringUtils.isEmpty(localPath)) {
				String urlName = AdvancedFileUtils.getFileName(url);		
				String pathName = AdvancedFileUtils.getFileName(localPath);
				if (pathName.equals(urlName)) {
					// 同一个文件 加载本地的
					showLocalPortrait(handler, iv, url, localPath, w, h);					
				} else {
					showRemotePortrait(handler, url);
				}
			}else{
				showRemotePortrait(handler, url);
			}
				
			
			//不进行名称比较
//			showRemotePortrait(handler, url);
		} else {
			// 修改过，没有网络头像则显示默认，不显示本地的
			showDefaultProtrait(iv);
			// 本地/默认
//			if (!StringUtils.isEmpty(localPath)) {
//				showLocalPortrait(iv, localPath, w, h);
//			} else {
//				showDefaultProtrait(iv);
//			}
		}

	}
	/**
	 * 显示本地的最新头像 ，用户登录页面
	 * @param iv
	 * @param localPath
	 * @param w
	 * @param h
	 */
	public static void showLocalPortrait(ImageView iv, String localPath, int w,int h) {
		Bitmap bm = ImageUtils.loadImgThumbnail(localPath, w, h);
		if (null != bm) {
			iv.setImageBitmap(toRoundBitmap(bm));
		} else {
			showDefaultProtrait(iv);
		}
	}
	/**
	 * 显示本地的最新头像，用于个人设置页面
	 * @param handler
	 * @param iv
	 * @param url
	 * @param localPath
	 * @param w
	 * @param h
	 */
	public static void showLocalPortrait(Handler handler, ImageView iv,String url, String localPath, int w, int h) {
		Bitmap bm = ImageUtils.loadImgThumbnail(localPath, w, h);
		if (null != bm) {
			iv.setImageBitmap(toRoundBitmap(bm));
		} else {
			showRemotePortrait(handler, url);
		}
	}
	/**
	 * 显示本地默认的头像
	 * @param iv
	 */
	public static void showDefaultProtrait(ImageView iv) {
		iv.setImageResource(R.drawable.ic_launcher);
	}
	/**
	 * 显示服务器端传递过头的头像
	 * @param handler
	 * @param url
	 */
	public static void showRemotePortrait(Handler handler, String url) {
		IGetDataBiz api = new GetDataBizImpl();
		api.loadProtrait(handler, url);
	}
	/**
	 * 显示登录页面的头像
	 * @param iv
	 * @param localPath
	 * @param w
	 * @param h
	 */
	public static void showLoginPortrait(ImageView iv, String localPath, int w,
			int h) {
		if (StringUtils.isEmpty(localPath)) {
			showDefaultProtrait(iv);
			return;
		}
		showLocalPortrait(iv, localPath, w, h);
	}
	/**
	 * 显示一个AlertDialog，用于待办已办的审批
	 * @param d
	 * @param view
	 */
	public static void showAlertDialog(AlertDialog d, BaseView view) {		
		d.setView(view);
		d.show();		
	}
	
	/**
	 * 创建一个AlertDialog，用于待办已办的审批
	 * @param context
	 * @return
	 */
	public static AlertDialog createAlertDailog(Context context){
		return new AlertDialog.Builder(context).create();
	}
}
