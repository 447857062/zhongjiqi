package com.deplink.homegenius.activity.device.remoteControl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.Room;
import com.deplink.homegenius.Protocol.json.device.SmartDev;
import com.deplink.homegenius.activity.device.AddDeviceActivity;
import com.deplink.homegenius.activity.device.DevicesActivity;
import com.deplink.homegenius.activity.device.adapter.RemoteControlSelectListAdapter;
import com.deplink.homegenius.activity.personal.experienceCenter.ExperienceDevicesActivity;
import com.deplink.homegenius.manager.device.DeviceManager;
import com.deplink.homegenius.manager.device.remoteControl.RemoteControlManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.view.dialog.DeleteDeviceDialog;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.homegenius.view.toast.ToastSingleShow;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class EditRemoteDevicesActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "EditDoorbeelActivity";
    private FrameLayout image_back;
    private TextView textview_title;
    private TextView button_delete_device;
    private DeleteDeviceDialog deleteDialog;
    /**
     * 当前要绑定的物理遥控器
     */
    private SmartDev currentSelectRemotecontrol;
    private RelativeLayout layout_remotecontrol_select;
    private RelativeLayout layout_remotecontrol_list;
    private ImageView imageview_remotecontrol_arror_right;
    private RemoteControlSelectListAdapter selectRemotecontrolAdapter;
    private List<SmartDev> mRemoteControls;
    private ListView listview_select_remotecontrol;
    private TextView textview_select_remotecontrol_name;
    private RelativeLayout layout_room_select;
    private TextView textview_select_room_name;
    private TextView textview_edit;
    private ClearEditText edittext_add_device_input_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_remote_devices);
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        image_back.setOnClickListener(this);
        button_delete_device.setOnClickListener(this);
        layout_remotecontrol_select.setOnClickListener(this);
        layout_room_select.setOnClickListener(this);
        textview_edit.setOnClickListener(this);
    }

    private String deviceType;
    private String selectRemotecontrolName;

    private void initDatas() {
        textview_edit.setText("完成");
        deviceType = getIntent().getStringExtra("deviceType");
        String deviceName;
        isStartFromExperience= DeviceManager.getInstance().isStartFromExperience();
        if(isStartFromExperience){
            deviceName="智能空调遥控器";
            textview_select_room_name.setText("客厅");
            textview_select_remotecontrol_name.setText("未设置物理遥控器");
        }else{
            deviceName=RemoteControlManager.getInstance().getmSelectRemoteControlDevice().getName();
            List<Room>rooms=RemoteControlManager.getInstance().getmSelectRemoteControlDevice().getRooms();
            if(rooms.size()>0){
                textview_select_room_name.setText(rooms.get(0).getRoomName());
            }else{
                textview_select_room_name.setText("全部");

            }
            List<SmartDev>remoteControls=RemoteControlManager.getInstance().findRemotecontrolDevice();
            if(remoteControls.size()>0){
                textview_select_remotecontrol_name.setText(remoteControls.get(0).getName());
            }else{
                textview_select_remotecontrol_name.setText("未设置物理遥控器");
            }
        }

        edittext_add_device_input_name.setText(deviceName);
        edittext_add_device_input_name.setSelection(deviceName.length());

        Log.i(TAG, "initDatas deviceType=" + deviceType);
        textview_title.setText(deviceType);

        deleteDialog = new DeleteDeviceDialog(this);
        mRemoteControls = new ArrayList<>();
        mRemoteControls.addAll(RemoteControlManager.getInstance().findAllRemotecontrolDevice());
        selectRemotecontrolAdapter = new RemoteControlSelectListAdapter(this, mRemoteControls);
        listview_select_remotecontrol.setAdapter(selectRemotecontrolAdapter);
        listview_select_remotecontrol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectRemotecontrolName = mRemoteControls.get(position).getName();
                textview_select_remotecontrol_name.setText(selectRemotecontrolName);
                layout_remotecontrol_list.setVisibility(View.GONE);
                imageview_remotecontrol_arror_right.setImageResource(R.drawable.directionicon);
                currentSelectRemotecontrol = mRemoteControls.get(position);
            }
        });


    }

    private void initViews() {
        image_back = findViewById(R.id.image_back);
        textview_title = findViewById(R.id.textview_title);
        textview_select_remotecontrol_name = findViewById(R.id.textview_select_remotecontrol_name);
        button_delete_device = findViewById(R.id.button_delete_device);
        textview_select_room_name = findViewById(R.id.textview_select_room_name);
        textview_edit = findViewById(R.id.textview_edit);
        layout_remotecontrol_select = findViewById(R.id.layout_remotecontrol_select);
        layout_remotecontrol_list = findViewById(R.id.layout_remotecontrol_list);
        layout_room_select = findViewById(R.id.layout_room_select);
        imageview_remotecontrol_arror_right = findViewById(R.id.imageview_remotecontrol_arror_right);
        listview_select_remotecontrol = findViewById(R.id.listview_select_remotecontrol);
        edittext_add_device_input_name = findViewById(R.id.edittext_add_device_input_name);
    }

    private static final int REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM = 100;
    private boolean isOnActivityResult;
    private boolean isStartFromExperience;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM && resultCode == RESULT_OK) {
            isOnActivityResult = true;
            String roomName = data.getStringExtra("roomName");
            Log.i(TAG, "roomName=" + roomName);
            textview_select_room_name.setText(roomName);
            if(!isStartFromExperience){
                Room room= RoomManager.getInstance().findRoom(roomName,true);
                RemoteControlManager.getInstance().updateSelectedDeviceInWhatRoom(room);
            }

        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                onBackPressed();
                break;
            case R.id.textview_edit:
                if(!isStartFromExperience){
                    String changeDevicename=edittext_add_device_input_name.getText().toString();
                    if(!changeDevicename.equals("")){
                        boolean result= RemoteControlManager.getInstance().saveCurrentSelectDeviceName(changeDevicename);
                        if(result){
                            finish();
                        }else{
                            ToastSingleShow.showText(this,"修改设备名称失败");
                        }
                    }
                }



                break;
            case R.id.layout_room_select:
                Intent intent = new Intent(this, AddDeviceActivity.class);
                intent.putExtra("addDeviceSelectRoom", true);
                startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE_IN_WHAT_ROOM);
                break;
            case R.id.layout_remotecontrol_select:
                if (layout_remotecontrol_list.getVisibility() == View.VISIBLE) {
                    layout_remotecontrol_list.setVisibility(View.GONE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.directionicon);
                } else {
                    layout_remotecontrol_list.setVisibility(View.VISIBLE);
                    imageview_remotecontrol_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
            case R.id.button_delete_device:
                deleteDialog.setSureBtnClickListener(new DeleteDeviceDialog.onSureBtnClickListener() {
                    @Override
                    public void onSureBtnClicked() {
                        if (!isStartFromExperience) {
                            int result = RemoteControlManager.getInstance().deleteCurrentSelectDevice();
                            if (result > 0) {
                                startActivity(new Intent(EditRemoteDevicesActivity.this, DevicesActivity.class));
                            } else {
                                ToastSingleShow.showText(EditRemoteDevicesActivity.this, "删除" + deviceType + "失败");
                            }
                        } else {
                            startActivity(new Intent(EditRemoteDevicesActivity.this, ExperienceDevicesActivity.class));
                        }
                    }
                });
                deleteDialog.show();
                break;

        }
    }
}