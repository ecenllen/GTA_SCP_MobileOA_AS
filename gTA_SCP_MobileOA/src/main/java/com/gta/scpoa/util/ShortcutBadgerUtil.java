package com.gta.scpoa.util;

import android.content.Context;

import com.gta.scpoa.entity.HomeInfo;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by weiye.chen on 2017/3/23.
 */

public class ShortcutBadgerUtil {
    /**
     * 更新桌面APP图标消息数量
     * @param info
     */
    public static void updateUnReadMesNum(Context context, HomeInfo info) {
        if(info == null)
            return;
        int lNum;
        try {
            if(info.getAllCount() > 0) {
                lNum = info.getAllCount();
            } else {
                lNum = info.getRecord();
                lNum += Integer.parseInt(info.getMail());
                lNum += Integer.parseInt(info.getMeeting());
                lNum += Integer.parseInt(info.getNotice());
                lNum += Integer.parseInt(info.getTasks());
                lNum += Integer.parseInt(info.getSchedule());
            }

            ShortcutBadger.applyCount(context, lNum); //for 1.1.4+
//			ShortcutBadger.with(getApplicationContext()).count(badgeCount); //for 1.1.3
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
