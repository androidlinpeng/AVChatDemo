package com.netease.nical.avchatdemo.impl.customization;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.netease.nical.avchatdemo.common.ToastHelper;

import com.netease.nical.avchatdemo.R;
import com.netease.nical.avchatdemo.api.NimUIKit;
import com.netease.nical.avchatdemo.api.model.session.SessionCustomization;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;

/**
 * SessionCustomization 可以实现聊天界面定制项：
 * 1. 聊天背景 <br>
 * 2. 加号展开后的按钮和动作，如自定义消息 <br>
 * 3. ActionBar右侧按钮。
 * <p>
 * DefaultTeamSessionCustomization 提供默认的群聊界面定制，添加了ActionBar右侧按钮，用于跳转群信息界面
 * <p>
 * Created by hzchenkang on 2016/12/20.
 */

public class DefaultTeamSessionCustomization extends SessionCustomization {

    public DefaultTeamSessionCustomization() {

        // ActionBar右侧按钮，跳转至群信息界面
        OptionsButton infoButton = new OptionsButton() {
            @Override
            public void onClick(Context context, View view, String sessionId) {
                Team team = NimUIKit.getTeamProvider().getTeamById(sessionId);
                if (team != null && team.isMyTeam()) {
                    NimUIKit.startTeamInfo(context, sessionId);
                } else {
                    ToastHelper.showToast(context, R.string.app_name);
                }
            }
        };
        infoButton.iconId = R.drawable.avchat_audio_record_icon_checked;
        buttons = new ArrayList<>();
        buttons.add(infoButton);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

}
