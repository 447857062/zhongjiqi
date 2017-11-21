package deplink.com.smartwirelessrelay.homegenius.activity.device.getway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.Room;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.device.DeviceList;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.lock.SSIDList;
import deplink.com.smartwirelessrelay.homegenius.activity.personal.wifi.ScanWifiListActivity;
import deplink.com.smartwirelessrelay.homegenius.manager.device.DeviceListener;
import deplink.com.smartwirelessrelay.homegenius.manager.device.DeviceManager;
import deplink.com.smartwirelessrelay.homegenius.manager.room.RoomManager;

/**
 * 添加网关走到添加网关名称，配置wifi网关
 */
public class AddGetwaySettingOptionsActivity extends Activity implements View.OnClickListener,DeviceListener{
    private Button button_save;
    private Button button_config_wifi;
    private EditText edittext_input_devie_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_getway_setting_options);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        button_save.setOnClickListener(this);
        button_config_wifi.setOnClickListener(this);
    }

    private void initViews() {
        button_save= (Button) findViewById(R.id.button_save);
        button_config_wifi= (Button) findViewById(R.id.button_config_wifi);
        edittext_input_devie_name= (EditText) findViewById(R.id.edittext_input_devie_name);
    }

    /**
     * 网关设备扫码出来的字符串
     */
    private String currentAddDevice;
    private void initDatas() {
        currentAddDevice = getIntent().getStringExtra("currentAddDevice");
        mRoomName = getIntent().getStringExtra("mRoomName");
        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.InitDeviceManager(this, this);
    }
    private DeviceManager mDeviceManager;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_save:
                if(currentAddDevice!=null){
                    mDeviceManager.bindDevice(currentAddDevice);
                }
                break;
            case R.id.button_config_wifi:
                startActivity(new Intent(this, ScanWifiListActivity.class));
                break;
        }
    }

    @Override
    public void responseQueryResult(String result) {

    }
    private static final int MSG_BIND_DEVICE_RESPONSE=100;
    private Handler mHanHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_BIND_DEVICE_RESPONSE:

                    Toast.makeText(AddGetwaySettingOptionsActivity.this,"绑定网关设备成功",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private boolean isDeviceAddSuccess(DeviceList aDeviceList) {
        for (int i = 0; i < aDeviceList.getDevice().size(); i++) {
            if (aDeviceList.getDevice().get(i).getUid().equals(currentAddDevice)) {
                return true;
            }
        }

        return false;
    }
    private String deviceName;
    private String mRoomName;
    @Override
    public void responseBindDeviceResult(String result) {
        Gson gson=new Gson();
        boolean addDeviceSuccess;
        DeviceList mDeviceList= gson.fromJson(result, DeviceList.class);
        addDeviceSuccess=isDeviceAddSuccess(mDeviceList);
        mDeviceManager.addDBGetwayDevice(currentAddDevice);
                deviceName=edittext_input_devie_name.getText().toString();
        if(deviceName.equals("")){
            deviceName="家里的网关";
        }
                for (int i = 0; i < mDeviceList.getDevice().size(); i++) {
                    if (mDeviceList.getDevice().get(i).getUid().equals(currentAddDevice)) {
                        Room room = RoomManager.getInstance().findRoom(mRoomName, false);
                        mDeviceManager.updateGetwayDeviceInWhatRoom(room,mDeviceList.getDevice().get(i).getUid(),deviceName);
                    }
                }


        if(addDeviceSuccess){
            mHanHandler.sendEmptyMessage(MSG_BIND_DEVICE_RESPONSE);
        }
    }

    @Override
    public void responseWifiListResult(List<SSIDList> wifiList) {

    }

    @Override
    public void responseSetWifirelayResult(int result) {

    }
}