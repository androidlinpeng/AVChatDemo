package com.netease.nical.avchatdemo.session;

import com.netease.nical.avchatdemo.api.NimUIKit;
import com.netease.nical.avchatdemo.api.model.session.SessionCustomization;
import com.netease.nim.avchatkit.TeamAVChatProfile;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static SessionCustomization normalTeamCustomization;

    public static void init() {
        NimUIKit.setCommonTeamSessionCustomization(getTeamCustomization(null));
    }

    private static SessionCustomization getTeamCustomization(String tid) {
        TeamAVChatProfile.sharedInstance().registerObserver(true);
        return normalTeamCustomization;
    }

}
