package deplink.com.smartwirelessrelay.homegenius.activity.personal.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.sdk.android.sdk.DeplinkSDK;
import com.deplink.sdk.android.sdk.EventCallback;
import com.deplink.sdk.android.sdk.SDKAction;
import com.deplink.sdk.android.sdk.bean.User;
import com.deplink.sdk.android.sdk.manager.SDKManager;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import deplink.com.smartwirelessrelay.homegenius.activity.homepage.SmartHomeMainActivity;
import deplink.com.smartwirelessrelay.homegenius.constant.AppConstant;
import deplink.com.smartwirelessrelay.homegenius.util.Perfence;
import deplink.com.smartwirelessrelay.homegenius.util.StringValidatorUtil;
import deplink.com.smartwirelessrelay.homegenius.view.dialog.loadingdialog.DialogLoading;
import deplink.com.smartwirelessrelay.homegenius.view.toast.ToastSingleShow;

public class LoginActivity extends Activity implements View.OnClickListener{
    private static final String TAG="LoginActivity";
    private ImageView imageview_delete;
    private TextView textview_forget_password;
    private TextView textview_regist_now;
    private Button button_login;
    private EditText edittext_input_phone_number;
    private EditText edittext_input_password;
    private boolean isConnectedMqtt=false;
    private SDKManager manager;
    private EventCallback ec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        initDatas();
        initEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.addEventCallback(ec);
    }
    @Override
    protected void onPause() {
        super.onPause();
        manager.removeEventCallback(ec);
    }

    private void initDatas() {
        DeplinkSDK.initSDK(getApplicationContext(), Perfence.SDK_APP_KEY);
        manager = DeplinkSDK.getSDKManager();
        ec = new EventCallback() {
            @Override
            public void onSuccess(SDKAction action) {
                switch (action) {
                    case LOGIN:
                        manager.connectMQTT(getApplicationContext());
                        Log.i(TAG, "onSuccess login");
                        break;
                    case CONNECTED:
                        isConnectedMqtt=true;
                        User user = manager.getUserInfo();
                        Perfence.setPerfence(Perfence.USER_PASSWORD, user.getPassword());
                        Perfence.setPerfence(Perfence.PERFENCE_PHONE, user.getName());
                        Perfence.setPerfence(AppConstant.USER_LOGIN, true);
                        startActivity(new Intent(LoginActivity.this, SmartHomeMainActivity.class));
                        DialogLoading.hideLoading();
                        LoginActivity.this.finish();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onBindSuccess(SDKAction action, String devicekey) {


            }

            @Override
            public void onGetImageSuccess(SDKAction action, Bitmap bm) {

            }

            @Override
            public void onFailure(SDKAction action, Throwable throwable) {
                switch (action) {
                    case LOGIN:
                        Perfence.setPerfence(AppConstant.USER_LOGIN, false);
                        DialogLoading.hideLoading();
                        ToastSingleShow.showText(LoginActivity.this, throwable.getMessage());
                        break;
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                super.connectionLost(throwable);
                ToastSingleShow.showText(LoginActivity.this, "当前账号已在其它设备上登录");
            }
        };
    }

    private void initEvents() {
        imageview_delete.setOnClickListener(this);
        textview_forget_password.setOnClickListener(this);
        textview_regist_now.setOnClickListener(this);
        button_login.setOnClickListener(this);
    }

    private void initViews() {
        imageview_delete= (ImageView) findViewById(R.id.imageview_delete);
        textview_forget_password= (TextView) findViewById(R.id.textview_forget_password);
        textview_regist_now= (TextView) findViewById(R.id.textview_regist_now);
        button_login= (Button) findViewById(R.id.button_login);
        edittext_input_password= (EditText) findViewById(R.id.edittext_input_password);
        edittext_input_phone_number= (EditText) findViewById(R.id.edittext_input_phone_number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageview_delete:
                this.finish();
                break;
            case R.id.button_login:
                final String phoneNumber = edittext_input_phone_number.getText().toString().trim();
                boolean isValidatorPhone = StringValidatorUtil.isMobileNO(phoneNumber);
                if (!isValidatorPhone) {
                    Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                Perfence.setPerfence(Perfence.PERFENCE_PHONE, phoneNumber);
                String password = edittext_input_password.getText().toString().trim();
                if (password.length() < 6) {
                    Toast.makeText(this, "密码位数6-20", Toast.LENGTH_SHORT).show();
                    return;
                }

                DialogLoading.showLoading(this);
                manager.login(phoneNumber, password);
                break;
            case R.id.textview_forget_password:
                startActivity(new Intent(LoginActivity.this,ForgetPasswordActivity.class));
                break;
            case R.id.textview_regist_now:
                startActivity(new Intent(LoginActivity.this,RegistActivity.class));
                break;
        }
    }
}