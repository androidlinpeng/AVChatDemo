package com.netease.nical.avchatdemo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nical.avchatdemo.DemoCache;
import com.netease.nical.avchatdemo.R;
import com.netease.nical.avchatdemo.business.TeamHelper;
import com.netease.nical.avchatdemo.common.ToastHelper;
import com.netease.nical.avchatdemo.common.util.log.LogUtil;
import com.netease.nical.avchatdemo.common.util.string.StringUtil;
import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.TeamAVChatProfile;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AVChatCallActivity extends AppCompatActivity {

    private static final String TAG = "AVChatCallActivity";

    private Button StartRTSButton;  //发起白板会话的按钮
    private Button GroupStartRTSButton;  //发起白板会话的按钮
    private EditText toAccountText;

    private LaunchTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avchat_call);
        //初始化界面
        initView();

//        registerCustomMessageObservers(true);

        StartRTSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toAccount = toAccountText.getText().toString();
                if(!toAccount.isEmpty()){
                    NimUserInfo user = NIMClient.getService(UserService.class).getUserInfo(toAccount);
                    if(user == null){
                        String[] array = {toAccount,toAccount};
                        List<String> accounts = Arrays.asList(array);
                        NIMClient.getService(UserService.class).fetchUserInfo(accounts)
                                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                                    @Override
                                    public void onSuccess(List<NimUserInfo> nimUserInfos) {
                                        if(!nimUserInfos.isEmpty()){
                                            NimUserInfo nimUserInfo = nimUserInfos.get(0);
                                            Log.d("RTSActivity", "onSuccess: 获取到"+nimUserInfo.getAccount()+"的云端资料");
                                            AVChatKit.outgoingCall(AVChatCallActivity.this,nimUserInfo.getAccount(),nimUserInfo.getName(),2, AVChatActivity.FROM_INTERNAL);
                                        }else {
                                            Toast.makeText(AVChatCallActivity.this, "请检查对方账号是否存在！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailed(int i) {
                                        Log.e("RTSActivity", "onFailed: 获取云端好友资料失败,错误码："+i );
                                    }

                                    @Override
                                    public void onException(Throwable throwable) {

                                    }
                                });
                    }else {
                        AVChatKit.outgoingCall(AVChatCallActivity.this,user.getAccount(),user.getName(),2, AVChatActivity.FROM_INTERNAL);
                    }
                }
            }
        });

        GroupStartRTSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.ui("onClick: getAccount = "+DemoCache.getAccount());
                transaction = new LaunchTransaction();
                transaction.setTeamID("2729940143");

                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("19920026487");
                arrayList.add("quakoo@123");
                onSelectedAccountsResult(arrayList);
            }
        });
    }

    public void onSelectedAccountsResult(final ArrayList<String> accounts) {
        LogUtil.ui("start teamVideo " + transaction.getTeamID() + " accounts = " + accounts);

        if (!checkTransactionValid()) {
            return;
        }

        final String roomName = StringUtil.get32UUID();
        LogUtil.ui("create room " + roomName);
        // 创建房间
        AVChatManager.getInstance().createRoom(roomName, null, new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                LogUtil.ui("create room " + roomName + " success !");
                if (!checkTransactionValid()) {
                    return;
                }
                onCreateRoomSuccess(roomName, accounts);
                transaction.setRoomName(roomName);

                String teamName = TeamHelper.getTeamName(transaction.getTeamID());

                TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                AVChatKit.outgoingTeamCall(getApplication(), false, transaction.getTeamID(), roomName, accounts, teamName);
                transaction = null;
            }

            @Override
            public void onFailed(int code) {
                if (!checkTransactionValid()) {
                    return;
                }
                onCreateRoomFail();
            }

            @Override
            public void onException(Throwable exception) {
                if (!checkTransactionValid()) {
                    return;
                }
                onCreateRoomFail();
            }
        });
    }

    private boolean checkTransactionValid() {
        if (transaction == null) {
            return false;
        }
        if (transaction.getTeamID() == null || !transaction.getTeamID().equals(transaction.getTeamID())) {
            transaction = null;
            return false;
        }
        return true;
    }

    private void onCreateRoomSuccess(String roomName, List<String> accounts) {
        String teamID = transaction.getTeamID();
        // 在群里发送tip消息
        IMMessage message = MessageBuilder.createTipMessage(teamID, SessionTypeEnum.Team);
        CustomMessageConfig tipConfig = new CustomMessageConfig();
        tipConfig.enableHistory = false;
        tipConfig.enableRoaming = false;
        tipConfig.enablePush = false;
        String teamNick = TeamHelper.getDisplayNameWithoutMe(teamID, DemoCache.getAccount());
        message.setContent(teamNick + getApplication().getString(R.string.t_avchat_start));
        message.setConfig(tipConfig);
//        sendMessage(message);
        // 对各个成员发送点对点自定义通知
        String teamName = TeamHelper.getTeamName(transaction.getTeamID());
        String content = TeamAVChatProfile.sharedInstance().buildContent(roomName, teamID, accounts, teamName);
        CustomNotificationConfig config = new CustomNotificationConfig();
        config.enablePush = true;
        config.enablePushNick = false;
        config.enableUnreadCount = true;

        for (String account : accounts) {
            CustomNotification command = new CustomNotification();
            command.setSessionId(account);
            command.setSessionType(SessionTypeEnum.P2P);
            command.setConfig(config);
            command.setContent(content);
            command.setApnsText(teamNick + getApplication().getString(R.string.t_avchat_push_content));

            command.setSendToOnlineUserOnly(false);
            NIMClient.getService(MsgService.class).sendCustomNotification(command);
        }
    }

    private void onCreateRoomFail() {
        // 本地插一条tip消息
        IMMessage message = MessageBuilder.createTipMessage(transaction.getTeamID(), SessionTypeEnum.Team);
        message.setContent(getApplication().getString(R.string.t_avchat_create_room_fail));
        LogUtil.i("status", "team action set:" + MsgStatusEnum.success);
        message.setStatus(MsgStatusEnum.success);
        NIMClient.getService(MsgService.class).saveMessageToLocal(message, true);
    }

    private class LaunchTransaction implements Serializable {
        private String teamID;
        private String roomName;

        public String getRoomName() {
            return roomName;
        }

        public String getTeamID() {
            return teamID;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public void setTeamID(String teamID) {
            this.teamID = teamID;
        }
    }





    private void registerCustomMessageObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(
                customNotificationObserver, register);
    }

    // sample
    Observer<CustomNotification> customNotificationObserver = (Observer<CustomNotification>) notification -> {
        // 处理自定义通知消息
        LogUtil.i("demo", "receive custom notification: " + notification.getContent() + " from :" +
                notification.getSessionId() + "/" + notification.getSessionType() +
                "unread=" + notification.getConfig().enableUnreadCount + " " + "push=" +
                notification.getConfig().enablePush + " nick=" +
                notification.getConfig().enablePushNick);
        try {
            JSONObject obj = JSONObject.parseObject(notification.getContent());
            if (obj != null && obj.getIntValue("id") == 2) {
                // 加入缓存中
//                CustomNotificationCache.getInstance().addCustomNotification(notification);
                // Toast
                String content = obj.getString("content");
                String tip = String.format("自定义消息[%s]：%s", notification.getFromAccount(), content);
                ToastHelper.showToast(AVChatCallActivity.this, tip);
            }
        } catch (JSONException e) {
            LogUtil.e("demo", e.getMessage());
        }
    };



    /**
     * 初始化界面
     */
    private void initView(){
        StartRTSButton = findViewById(R.id.startrts);
        GroupStartRTSButton = findViewById(R.id.group_startrts);
        toAccountText = findViewById(R.id.toaccountid);
    }


    /**
     * 点击空白位置 隐藏软键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }
}