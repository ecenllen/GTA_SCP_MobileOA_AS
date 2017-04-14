package com.gta.utils.thirdParty.jPush;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.android.data.JPushLocalNotification;

/**
 * [Description]
 * <p/>
 * [How to use]
 * <p/>
 * [Tips]
 *
 * @author
 * @since 1.0.0
 */
public class JpushManager {

    public static final String JPUSH_INTERFACE_MESSAGE_RECEIVED_ACTION =
            "JPUSH_INTERFACE_MESSAGE_RECEIVED_ACTION";
    public static final String JPUSH_KEY_ACTION_TYPE = "JPUSH_KEY_ACTION_TYPE";
    public static final String JPUSH_KEY_START_WAY = "JPUSH_KEY_START_WAY";
    public static final String JPUSH_KEY_BUNDLE = "JPUSH_KEY_BUNDLE";
    public static final int START_WAY_FOREGROUND = 0;
    public static final int START_WAY_BACKGROUND = 1;
    public static final int START_WAY_REOPEN = 2;
    private static final String TAG = JpushManager.class.getSimpleName();
    private static JpushManager sJpushManager;
    private static WeakReference<Context> contextWeakReference;
    private boolean mDebugMode;
    private Class<? extends Activity> mActivityClass;
    private ArrayList<JpushMessageReceiveListener> mOtherActionListeners;
    private Boolean appInFront;

    private LocalBroadcastManager localBroadcastManager;
    
    private JpushMessageReceiveListener jpushMessageReceiveListener;

    public static JpushManager getInstance() {
        if (sJpushManager == null) {
            synchronized (JpushManager.class) {
                if (sJpushManager == null) {
                    sJpushManager = new JpushManager();
                }
            }
        }
        return sJpushManager;
    }

    public void init(Context context, Class<? extends Activity> activityClass) {
        contextWeakReference = new WeakReference<>(context);
        mActivityClass = activityClass;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        JPushInterface.init(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(JpushManager.JPUSH_INTERFACE_MESSAGE_RECEIVED_ACTION);
        localBroadcastManager.registerReceiver(jpushBroadcastReceiver, filter);
    }

    public static final TagAlia TAG_ALIA_EMPTY = new TagAlia("", new String[]{"null"});

    /**
     * set tag list and alias
     *
     * @param tagAlia the obj of the tag list and alias to set,or null to clear tagAlia;
     */
    public void setTagsAndAlias(TagAlia tagAlia) {
        if (tagAlia == null) {
            tagAlia = TAG_ALIA_EMPTY;
        }

        if (tagAlia.isEmpty()) {
            tagAlia = TAG_ALIA_EMPTY;
        }
        String Alias = tagAlia.getAlias();
        // 设置别名，服务器可以单发。userNames是别名，set是标签
        String[] tags = tagAlia.getTags();
        Set<String> set = new HashSet<>();
        Collections.addAll(set, tags);
        Context context = getContext();
        JPushInterface.setAliasAndTags(context, Alias, set, new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                String result = null;
                switch (i) {
                    case 0:
                        result = "succ";
                        break;
                    case 6005:
                        result = "6005";
                        break;
                }
                if (mDebugMode) {
                    Log.d(TAG, String.format("setTagsAndAlias: result: %1$s;type:%2$s;alias:%3$s;tags " +
                            "%4$s", result, i, s, set));
                }
            }
        });
    }

    public void setAlias(String alias) {
        Context lContext = getContext();
        JPushInterface.setAlias(lContext, alias, new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                String result = null;
                switch (i) {
                    case 0:
                        result = "succ";
                        break;
                    case 6005:
                        result = "6005";
                        break;
                }
                if (mDebugMode) {
                    Log.d(TAG, String.format("setTagsAndAlias: result: %1$s;type:%2$s;alias:%3$s;tags " +
                            "%4$s", result, i, s, set));
                }
            }
        });
    }


    /**
     * 设置是否恢复推送
     * @param enable true 恢复 false 停止
     */
    public void setResumeJpushEnable(boolean enable) {
        Context context = getContext();
        if (context!=null){
            if (enable) { // 确认接送推送
                if (JPushInterface.isPushStopped(context)) {
                    JPushInterface.resumePush(context);
                }
            } else {// 停止推送消息
                if (!JPushInterface.isPushStopped(context)) {
                    JPushInterface.stopPush(context);
                }
            }
        }
    }

    BroadcastReceiver jpushBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundleExtra = intent.getBundleExtra(JpushManager.JPUSH_KEY_BUNDLE);
            String actionType = intent.getStringExtra(JpushManager.JPUSH_KEY_ACTION_TYPE);
            int startWay = intent.getIntExtra(JpushManager.JPUSH_KEY_START_WAY, START_WAY_FOREGROUND);
            dealWhitJpushMessage(actionType, startWay, bundleExtra);
        }
    };


    /**
     * 此处清空【通知栏打开消息】类型的回调事件
     */
    public void clearReceiveListener() {
        mOtherActionListeners = new ArrayList<>();
    }


    public void setJpushMessageReceiveListener(JpushMessageReceiveListener jpushMessageReceiveListener) {
        this.jpushMessageReceiveListener = jpushMessageReceiveListener;
    }


    /**
     * 此处注销的回调监听只接受非【非通知栏打开消息】类型的事件
     *
     * @param jpushMessageReceiveListener 注销的回调
     */
    public void removeOtherActionListener(JpushMessageReceiveListener jpushMessageReceiveListener) {
        mOtherActionListeners.remove(jpushMessageReceiveListener);
    }

    /**
     * 收到推送回调方法
     * @param actionType 推送消息类型
     * @param startWay  
     * @param jpushBundleExtra  数据
     */
    private void dealWhitJpushMessage(String actionType, int startWay, Bundle jpushBundleExtra) {
        String extras = jpushBundleExtra.getString(JPushInterface.EXTRA_EXTRA);
        String alert = jpushBundleExtra.getString(JPushInterface.EXTRA_ALERT);
        String msgContent = jpushBundleExtra.getString(JPushInterface.EXTRA_MESSAGE);
        
        if(jpushMessageReceiveListener != null)
            jpushMessageReceiveListener.OnReceiveJpushMessage(actionType, startWay, extras, alert, msgContent);
        
    }

    /**
     * send a jpush just like from background
     *
     * @param title         本地推送的標題
     * @param content       本地推送的內容
     * @param broadcastTime 推送時間
     * @param extrasItems   額外的數據信息
     */
    public void sendLocalPush(String title, String content,
                              long broadcastTime, ExtrasItem[] extrasItems) {

        JPushLocalNotification ln = new JPushLocalNotification();
        //设置本地通知样式
//        ln.setBuilderId(builderId);
        //設置本地通知的ID
//        ln.setNotificationId(notificationId);
        //設置本地推送的標題
        ln.setTitle(title);
        //設置本地推送的內容
        ln.setContent(content);
        //設置推送時間
        ln.setBroadcastTime(broadcastTime);
        //設置額外的數據信息
        Map<String, Object> map = new HashMap<>();

        for (ExtrasItem lExtrasItem : extrasItems) {
            map.put(lExtrasItem.getName(), lExtrasItem.getValue());
        }
        JSONObject lJSONObject = new JSONObject(map);
        ln.setExtras(lJSONObject.toString());

        JPushInterface.addLocalNotification(getContext(), ln);
    }

    public JpushManager setDebugMode(boolean debugMode) {
        JPushInterface.setDebugMode(debugMode);
        mDebugMode = debugMode;
        return this;
    }

    public void setAppInFront(boolean appInFront) {
        this.appInFront = appInFront;
    }

    Context getContext() {
        return contextWeakReference.get();
    }

    Class<?> getActivityClass() {
        return mActivityClass;
    }

    boolean isDebugMode() {
        return mDebugMode;
    }

    public Boolean isAppInFront() {
        return appInFront;
    }

    /**
     * @param isResume true to call {@link JPushInterface#onResume(Context)},
     *                 false to call {@link JPushInterface#onPause(Context)}
     */
    public void toggleResumeOrPause(boolean isResume) {
        if (isResume) {
            JPushInterface.onResume(getContext());
        } else {
            JPushInterface.onPause(getContext());
        }
    }

    public static class ExtrasItem {
        private String mName;
        private Object mValue;

        public ExtrasItem(String mName, Object mValue) {
            super();
            this.mName = mName;
            this.mValue = mValue;
        }

        public String getName() {
            return mName;
        }

        public Object getValue() {
            return mValue;
        }
    }

    public static class TagAlia {

        private String alias;

        private String[] tags;

        public TagAlia(String alias, String[] tags) {
            this.alias = alias == null ? "" : alias;//为空不能正确设置jpush，必须传空字符
            this.tags = tags;
        }

        public String getAlias() {
            return alias;
        }

        public String[] getTags() {
            return tags;
        }

        public boolean isEmpty() {
            return alias == null && tags == null;
        }

    }

    public interface JpushMessageReceiveListener {
        void OnReceiveJpushMessage(String actionType, int startWay, String extras, String alert, String msgContent);

    }
}
