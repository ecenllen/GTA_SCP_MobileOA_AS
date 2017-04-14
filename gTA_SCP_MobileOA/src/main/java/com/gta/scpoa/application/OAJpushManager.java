package com.gta.scpoa.application;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.gta.scpoa.activity.MainActivity;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.HomeInfo;
import com.gta.scpoa.util.NotificationUtlis;
import com.gta.utils.thirdParty.jPush.JpushManager;

import java.lang.ref.WeakReference;


/**
 * [Description]
 * <p>
 * [How to use]
 * <p>
 * [Tips]
 *
 * @author Created by Zhimin.Huang on 2016/8/22.
 * @since 1.0.0
 */
public class OAJpushManager {

    //    private static UserBean sUserBean;
    private static WeakReference<Context> sContext;
    private static Gson mGson;

    public static Gson getmGson() {
        if(mGson == null)
            mGson = new Gson();
        return mGson;
    }

    /**
     * 在首页注册推送，设置别名
     * @param context
     */
    public static void registerJpushManager(final Context context) {

        sContext = new WeakReference<>(context);


        /*一定要有清除监听事件的步骤，不然会导致重复代开很多页面*/
        JpushManager.getInstance().clearReceiveListener();
        unRegisterJpushManager(true);
//        String userId = GTAApplication.instance.getUserID();
        String account = GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
        if (!TextUtils.isEmpty(account)) {
            JpushManager.getInstance().setAlias(account.toLowerCase());
            JpushManager.getInstance().setResumeJpushEnable(true);
        } else {
            JpushManager.getInstance().setAlias(null);
            JpushManager.getInstance().setResumeJpushEnable(false);
        }

        /**
         *
         * 点击通知栏推送消息操作(暂时不需要，未读信息是用轮询方式实现，并未做推送)
         */
        /*JpushManager.getInstance().addReceiveListener(new JpushManager.JpushMessageReceiveListener() {
            @Override
            public void OnReceiveJpushMessage(String actionType, int startWay, String extras, String alert) {

            }
        });*/

        //用于自定义消息推送
//        JpushManager.getInstance().addOtherActionListener(new JpushManager.JpushMessageReceiveListener() {
//            @Override
//            public void OnReceiveJpushMessage(String actionType, int startWay, String extras, String alert) {
//                /* 接收到推送信息*/
//                try {
//                    if (!TextUtils.isEmpty(extras)) {
//                        HomeInfo homeInfo = getmGson().fromJson(extras, HomeInfo.class);
//                        if(homeInfo != null && homeInfo.isSuccessed()) {
//                            Context context = sContext.get();
//                            if(context == null)
//                                return;
//                            /** 更新桌面图标消息数量*/
//                            ShortcutBadgerUtil.updateUnReadMesNum(context, homeInfo);
//                            /** 更新首页界面*/
//                            if(context instanceof MainActivity)
//                                ((MainActivity)context).handleResult(homeInfo);
//                            /** 更新通知栏消息*/
//                            NotificationUtlis.showNotification(context, homeInfo);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
        
        JpushManager.getInstance().setJpushMessageReceiveListener(new JpushManager.JpushMessageReceiveListener() {
            @Override
            public void OnReceiveJpushMessage(String actionType, int startWay, String extras, String alert, String msgContent) {
                
                /* 接收到推送信息*/
                try {
                    if (!TextUtils.isEmpty(extras)) {
                        HomeInfo homeInfo = getmGson().fromJson(extras, HomeInfo.class);
                        if(homeInfo != null && homeInfo.isSuccessed()) {
                            Context context = sContext.get();
                            if(context == null)
                                return;
                            /** 更新首页界面*/    
                            if(context instanceof MainActivity)
                                ((MainActivity)context).handleResult(homeInfo);
                            /** 更新通知栏消息*/
                            NotificationUtlis.showNotification(context, homeInfo);
                        }
                        
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void unRegisterJpushManager(boolean enable) {
        JpushManager.getInstance().setResumeJpushEnable(enable);
    }

}
