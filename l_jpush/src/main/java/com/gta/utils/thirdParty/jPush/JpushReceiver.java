package com.gta.utils.thirdParty.jPush;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p/>
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {

    private static final String TAG = "JpushReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();

        JpushManager lJpushManager = JpushManager.getInstance();
        boolean lIsDebugMode = lJpushManager.isDebugMode();

        if (lIsDebugMode) {
            Log.d(TAG, "onReceive - " + intent.getAction() + ", \nextras: " + printBundle(bundle));
        }

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);

            if (lIsDebugMode) {
                Log.d(TAG, "接收Registration Id : " + regId);
            }
            sendJpushBroadcast(context, bundle,
                    JPushInterface.ACTION_REGISTRATION_ID, JpushManager.START_WAY_REOPEN);
            // send the Registration Id to your server...
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            if (lIsDebugMode) {
                Log.d(TAG, "接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            }
            sendJpushBroadcast(context, bundle,
                    JPushInterface.ACTION_MESSAGE_RECEIVED, JpushManager.START_WAY_REOPEN);
            // processCustomMessage(context, bundle);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);

            if (lIsDebugMode) {
                Log.d(TAG, "接收到推送下来的通知的ID: " + notifactionId);
            }
            sendJpushBroadcast(context, bundle,
                    JPushInterface.ACTION_NOTIFICATION_RECEIVED, JpushManager.START_WAY_REOPEN);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {

            if (lIsDebugMode) {
                Log.d(TAG, "用户点击打开了通知");
                Log.i(TAG, bundle.toString());
            }
            // 判断程序是否正在运行
            String lPackageName = context.getApplicationContext().getPackageName();
            //优先使用调用方已经传入的前后台标识，否则使用checkAppRunState()方法
            final Boolean lAppInFront = JpushManager.getInstance().isAppInFront();
            boolean isAppRunning = lAppInFront != null ? lAppInFront : checkAppRunState(context,
                    lPackageName, lIsDebugMode);
            if (isAppRunning) {
                if (!isApplicationBroughtToBackground(context)) {
                    // 程序正在运行，直接处理push的数据，跳转到详细页面
                    sendJpushBroadcast(context, bundle,
                            JPushInterface.ACTION_NOTIFICATION_OPENED, JpushManager.START_WAY_FOREGROUND);
                } else {
                    //带回前台
                    Intent newIntent = new Intent(context, lJpushManager.getActivityClass());
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(newIntent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendJpushBroadcast(context, bundle,
                                    JPushInterface.ACTION_NOTIFICATION_OPENED, JpushManager
                                            .START_WAY_BACKGROUND);
                        }
                    }, 1000);
                }
            } else {
                // 程序没有运行，则启动程序，再跳转到收件夹
                if (lIsDebugMode) {
                    Log.e(TAG, "程序没有启动");
                }

                Intent newIntent = new Intent(context, lJpushManager.getActivityClass());
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
//                Intent mainIntent = new Intent();
//                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mainIntent.setClassName("com.cardapp.hoshk", "com.cardapp.hos.activity.splashScreen");
//                context.startActivity(mainIntent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendJpushBroadcast(context, bundle,
                                JPushInterface.ACTION_NOTIFICATION_OPENED, JpushManager.START_WAY_REOPEN);
                    }
                }, 1000);
            }

            // Activity 被打开，上报服务器统计。
            JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
//            dealWithJpush(context, bundle);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            if (lIsDebugMode) {
                Log.d(TAG, "用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            }
            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
            // 打开一个网页等..
            sendJpushBroadcast(context, bundle,
                    JPushInterface.ACTION_RICHPUSH_CALLBACK, JpushManager.START_WAY_REOPEN);
        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
            sendJpushBroadcast(context, bundle,
                    JPushInterface.ACTION_CONNECTION_CHANGE, JpushManager.START_WAY_REOPEN);
        } else {
            if (lIsDebugMode) {
                Log.d(TAG, "Unhandled intent - " + intent.getAction());
            }
        }
    }

    private void sendJpushBroadcast(Context context, Bundle jpushBundle, String jpushActionType, int
            startWay) {
        Intent lIntent = new Intent(JpushManager.JPUSH_INTERFACE_MESSAGE_RECEIVED_ACTION);
        lIntent.putExtra(JpushManager.JPUSH_KEY_BUNDLE, jpushBundle);
        lIntent.putExtra(JpushManager.JPUSH_KEY_ACTION_TYPE, jpushActionType);
        lIntent.putExtra(JpushManager.JPUSH_KEY_START_WAY, startWay);
        LocalBroadcastManager.getInstance(context).sendBroadcast(lIntent);
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    /**
     * 检测程序运行状态
     */
    private boolean checkAppRunState(Context context, String packageName, boolean lIsDebugMode) {

        boolean isAppRunning = false;

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningTaskInfo> list = activityManager.getRunningTasks(100);

        for (RunningTaskInfo info : list) {
            if (packageName.equals(info.topActivity.getPackageName())) {
                if (lIsDebugMode) {
                    Log.e(TAG, "" + info.topActivity.getPackageName());
                }
                isAppRunning = true;
                break;
            } else {
                if (lIsDebugMode) {
                    Log.i(TAG, "" + info.topActivity.getPackageName());
                }
                isAppRunning = false;
            }
        }

        if (lIsDebugMode) {
            Log.e(TAG, "checkAppRunState() - isAppRunning: " + isAppRunning);
        }

        return isAppRunning;
    }

    /**
     * 检测程序运行状态
     */
    public static boolean checkAppRunState(Context context, String packageName) {

        boolean isAppRunning = false;

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> list = activityManager.getRunningTasks(100);

        for (ActivityManager.RunningTaskInfo info : list) {
            if (packageName.equals(info.topActivity.getPackageName())) {
                isAppRunning = true;
                break;
            } else {
                isAppRunning = false;
            }
        }

        return isAppRunning;
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

}
