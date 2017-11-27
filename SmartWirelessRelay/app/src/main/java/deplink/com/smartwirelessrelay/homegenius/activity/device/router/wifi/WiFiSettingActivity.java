package deplink.com.smartwirelessrelay.homegenius.activity.device.router.wifi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.device.RouterDevice;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import deplink.com.smartwirelessrelay.homegenius.activity.personal.login.LoginActivity;
import deplink.com.smartwirelessrelay.homegenius.constant.AppConstant;
import deplink.com.smartwirelessrelay.homegenius.util.Perfence;
import deplink.com.smartwirelessrelay.homegenius.view.dialog.MakeSureDialog;

public class WiFiSettingActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "WiFiSettingActivity";
    private RelativeLayout layout_wifi_24;
    private RelativeLayout layout_wifi_custom;
    private RelativeLayout layout_signal_strength;
    private ImageView image_back;
    private boolean isUserLogin;
    private SDKManager manager;
    private RouterDevice routerDevice;
    private MakeSureDialog connectLostDialog;
    private EventCallback ec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_setting);
        initViews();
        initDatas();
        initEvents();
    }
    private void initDatas() {
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        connectLostDialog = new MakeSureDialog(WiFiSettingActivity.this);
        connectLostDialog.setSureBtnClickListener(new MakeSureDialog.onSureBtnClickListener() {
            @Override
            public void onSureBtnClicked() {
                startActivity(new Intent(WiFiSettingActivity.this, LoginActivity.class));
            }
        });
        manager = DeplinkSDK.getSDKManager();
        ec=new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {

            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {

            }

            @Override
            public void onGetImageSuccess(SDKAction action, Bitmap bm) {

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {

            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                connectLostDialog.show();
                connectLostDialog.setTitleText("账号异地登录");
                connectLostDialog.setMsg("当前账号已在其它设备上登录,是否重新登录");
            }
        };
    }

    private void getRouterDevice() {
        String currentDevcieKey = Perfence.getPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY);
        if (currentDevcieKey.equals("")) {
            if (manager.getDeviceList() != null && manager.getDeviceList().size() != 0) {
                Perfence.setPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY, manager.getDeviceList().get(0).getDeviceKey());
            }
        }
        routerDevice = (RouterDevice) manager.getDevice(Perfence.getPerfence(AppConstant.DEVICE.CURRENT_DEVICE_KEY));
    }

    private boolean deviceOnline;

    @Override
    protected void onResume() {
        super.onResume();
        Perfence.setContext(getApplicationContext());
        isUserLogin = Perfence.getBooleanPerfence(AppConstant.USER_LOGIN);

        getRouterDevice();
        if (routerDevice != null) {
            deviceOnline = routerDevice.getOnline();
        }
        //登录了，并且绑定了设备
        if (isUserLogin && routerDevice != null && deviceOnline) {
            layout_wifi_custom.setVisibility(View.VISIBLE);//目前不支持访客WIFI
            layout_signal_strength.setVisibility(View.VISIBLE);
        } else {
            layout_wifi_custom.setVisibility(View.GONE);
            layout_signal_strength.setVisibility(View.GONE);
        }
        manager.addEventCallback(ec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initEvents() {
        layout_wifi_24.setOnClickListener(this);
        layout_wifi_custom.setOnClickListener(this);
        layout_signal_strength.setOnClickListener(this);
        image_back.setOnClickListener(this);
    }

    private void initViews() {
        image_back = (ImageView) findViewById(R.id.image_back);
        layout_wifi_24 = (RelativeLayout) findViewById(R.id.layout_wifi_24);
        layout_wifi_custom = (RelativeLayout) findViewById(R.id.layout_wifi_custom);
        layout_signal_strength = (RelativeLayout) findViewById(R.id.layout_signal_strength);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_wifi_24:
                startActivity(new Intent(WiFiSettingActivity.this, WifiSetting24.class));
                break;
            case R.id.layout_wifi_custom:
                startActivity(new Intent(WiFiSettingActivity.this, WifiSettingCustom.class));
                break;
            case R.id.layout_signal_strength:
                startActivity(new Intent(WiFiSettingActivity.this, SignalStrengthSetting.class));
                break;
            case R.id.image_back:
                onBackPressed();
                break;
        }
    }
}
