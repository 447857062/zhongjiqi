package com.deplink.homegenius.activity.device.smartSwitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.OpResult;
import com.deplink.homegenius.activity.personal.login.LoginActivity;
import com.deplink.homegenius.constant.AppConstant;
import com.deplink.homegenius.manager.connect.local.tcp.LocalConnectmanager;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchListener;
import com.deplink.homegenius.manager.device.smartswitch.SmartSwitchManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.util.WeakRefHandler;
import com.deplink.homegenius.view.dialog.DeleteDeviceDialog;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.manager.SDKManager;
import com.google.gson.Gson;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class SwitchOneActivity extends Activity implements View.OnClickListener, SmartSwitchListener {
    private static final String TAG = "SwitchTwoActivity";
    private FrameLayout image_back;
    private TextView textview_title;
    private TextView textview_edit;
    private Button button_switch;
    private SmartSwitchManager mSmartSwitchManager;
    private boolean switch_one_open;
    private SDKManager manager;
    private EventCallback ec;
    private boolean isUserLogin;
    private DeleteDeviceDialog connectLostDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_one);
        initViews();
        initDatas();
        initEvents();
    }
    private boolean isStartFromExperience;
    @Override
    protected void onResume() {
        super.onResume();
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);
        manager.addEventCallback(ec);
        mSmartSwitchManager.addSmartSwitchListener(this);
        isStartFromExperience = DeviceManager.getInstance().isStartFromExperience();
        if (isStartFromExperience) {
            switch_one_open = true;
        } else {
            switch_one_open = mSmartSwitchManager.getCurrentSelectSmartDevice().isSwitch_one_open();
            mSmartSwitchManager.querySwitchStatus("query");
        }
        setSwitchImageviewBackground();

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
        mSmartSwitchManager.removeSmartSwitchListener(this);
    }

    private void setSwitchImageviewBackground() {
        Log.i(TAG, "switch_one_open=" + switch_one_open);
        if (switch_one_open) {
            button_switch.setBackgroundResource(R.drawable.switchallthewayon);
        } else {
            button_switch.setBackgroundResource(R.drawable.switchallthewayoff);
        }
    }

    private void initEvents() {
        image_back.setOnClickListener(this);
        textview_edit.setOnClickListener(this);
        button_switch.setOnClickListener(this);
    }

    private void initDatas() {
        textview_title.setText("一路开关");
        textview_edit.setText("编辑");
        mSmartSwitchManager = SmartSwitchManager.getInstance();
        mSmartSwitchManager.InitSmartSwitchManager(this);
        mSmartSwitchManager.addSmartSwitchListener(this);
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        connectLostDialog = new DeleteDeviceDialog(SwitchOneActivity.this);
        connectLostDialog.setSureBtnClickListener(new DeleteDeviceDialog.onSureBtnClickListener() {
            @Override
            public void onSureBtnClicked() {
                startActivity(new Intent(SwitchOneActivity.this, LoginActivity.class));
            }
        });
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {
            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {


            }

            @Override
            public void notifyHomeGeniusResponse(String result) {
                super.notifyHomeGeniusResponse(result);
                Log.i(TAG, "设备列表界面收到回调的mqtt消息=" + result);
                Gson gson = new Gson();
                OpResult mOpResult = gson.fromJson(result, OpResult.class);
                if (mOpResult.getOP().equalsIgnoreCase("REPORT") && mOpResult.getMethod().equalsIgnoreCase("SmartWallSwitch")) {
                    String mSwitchStatus = mOpResult.getSwitchStatus();
                    String[] sourceStrArray = mSwitchStatus.split(" ", 4);
                    if (sourceStrArray[0].equals("01")) {
                        switch_one_open = true;
                    } else if (sourceStrArray[0].equals("02")) {
                        switch_one_open = false;
                    }
                    mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                    switch (mOpResult.getCommand()) {
                        case "close1":
                            switch_one_open = false;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                        case "open1":
                            switch_one_open = true;
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                            break;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setSwitchImageviewBackground();
                            mSmartSwitchManager.getCurrentSelectSmartDevice().setStatus("在线");
                            mSmartSwitchManager.getCurrentSelectSmartDevice().saveFast();
                        }
                    });
                }
            }

            @Override
            public void deviceOpSuccess(String op, String deviceKey) {
                super.deviceOpSuccess(op, deviceKey);
            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                isUserLogin = false;
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                connectLostDialog.show();
                connectLostDialog.setTitleText("账号异地登录");
                connectLostDialog.setContentText("当前账号已在其它设备上登录,是否重新登录");
            }
        };
    }

    private void initViews() {
        image_back = findViewById(R.id.image_back);
        textview_title = findViewById(R.id.textview_title);
        textview_edit = findViewById(R.id.textview_edit);
        button_switch = findViewById(R.id.button_switch);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                onBackPressed();
                break;
            case R.id.button_switch:
                Log.i(TAG, "switch_one_open=" + switch_one_open);

                if (isStartFromExperience) {
                    if (switch_one_open) {
                        switch_one_open = false;
                    } else {
                        switch_one_open = true;
                    }
                    setSwitchImageviewBackground();
                } else {
                    if(NetUtil.isNetAvailable(this)){
                        if(!isUserLogin && !LocalConnectmanager.getInstance().isLocalconnectAvailable()){
                            ToastSingleShow.showText(this,"本地连接不可用,需要登录后才能操作");
                        }else{
                            if (switch_one_open) {
                                mSmartSwitchManager.setSwitchCommand("close1");
                            } else {
                                mSmartSwitchManager.setSwitchCommand("open1");
                            }
                        }
                    }
                    else{
                        ToastSingleShow.showText(this,"网络连接不正常");
                    }
                }

                break;
            case R.id.textview_edit:
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("switchType", "一路开关");
                startActivity(intent);
                break;
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void responseResult(String result) {
        Gson gson = new Gson();
        OpResult mOpResult = gson.fromJson(result, OpResult.class);
        String mSwitchStatus = mOpResult.getSwitchStatus();
        String[] sourceStrArray = mSwitchStatus.split(" ", 1);
        Log.i(TAG, "sourceStrArray[0]" + sourceStrArray[0]);
        if (sourceStrArray[0].equals("01")) {
            switch_one_open = true;
        } else if (sourceStrArray[0].equals("02")) {
            switch_one_open = false;
        }
        mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
        switch (mOpResult.getCommand()) {
            case "close1":
                ToastSingleShow.showText(SwitchOneActivity.this,"开关已关闭");
                switch_one_open = false;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
            case "open1":
                ToastSingleShow.showText(SwitchOneActivity.this,"开关已开启");
                switch_one_open = true;
                mSmartSwitchManager.getCurrentSelectSmartDevice().setSwitch_one_open(switch_one_open);
                break;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setSwitchImageviewBackground();
                mSmartSwitchManager.getCurrentSelectSmartDevice().setStatus("在线");
                mSmartSwitchManager.getCurrentSelectSmartDevice().saveFast();
            }
        });
    }
}
