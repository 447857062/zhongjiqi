package com.deplink.homegenius.activity.room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deplink.homegenius.Protocol.json.device.getway.Device;
import com.deplink.homegenius.activity.device.AddDeviceActivity;
import com.deplink.homegenius.activity.device.adapter.GetwaySelectListAdapter;
import com.deplink.homegenius.activity.room.adapter.GridViewRommTypeAdapter;
import com.deplink.homegenius.constant.RoomConstant;
import com.deplink.homegenius.manager.device.getway.GetwayManager;
import com.deplink.homegenius.manager.room.RoomManager;
import com.deplink.homegenius.util.NetUtil;
import com.deplink.homegenius.util.Perfence;
import com.deplink.homegenius.view.edittext.ClearEditText;
import com.deplink.homegenius.view.toast.ToastSingleShow;
import com.deplink.sdk.android.sdk.homegenius.DeviceOperationResponse;
import com.deplink.sdk.android.sdk.homegenius.Room;
import com.deplink.sdk.android.sdk.rest.RestfulToolsHomeGenius;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRommActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "AddRommActivity";
    private TextView textview_add_room_complement;
    private ImageView image_back;
    private RelativeLayout layout_getway;
    private ClearEditText edittext_room_name;
    private GridView gridview_room_type;
    private GridViewRommTypeAdapter mGridViewRommTypeAdapter;
    private RoomManager roomManager;
    private List<String> listTop = new ArrayList<>();
    private RelativeLayout layout_getway_list;
    private TextView textview_getway_name;
    private GetwaySelectListAdapter selectGetwayAdapter;
    private List<Device> mGetways;
    private ListView listview_select_getway;
    private ImageView imageview_getway_arror_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_romm);
        initViews();
        initDatas();
        initEvents();
    }

    private boolean fromAddDevice;
    private String selectGetwayName;

    private void initDatas() {
        roomManager = RoomManager.getInstance();
        mGridViewRommTypeAdapter = new GridViewRommTypeAdapter(this);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_LIVING);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_BED);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_KITCHEN);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STUDY);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_STORAGE);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_TOILET);
        listTop.add(RoomConstant.ROOMTYPE.TYPE_DINING);
        roomType = RoomConstant.ROOMTYPE.TYPE_LIVING;
        edittext_room_name.setText(roomType);
        edittext_room_name.setSelection(roomType.length());
        fromAddDevice = getIntent().getBooleanExtra("fromAddDevice", false);
        mGetways = new ArrayList<>();
        mGetways.addAll(GetwayManager.getInstance().getAllGetwayDevice());
        if(mGetways.size()>0){
            currentSelectGetway=mGetways.get(0);
        }

        selectGetwayAdapter = new GetwaySelectListAdapter(this, mGetways);
        listview_select_getway.setAdapter(selectGetwayAdapter);
        listview_select_getway.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGetwayName = mGetways.get(position).getName();
                currentSelectGetway=mGetways.get(position);
                textview_getway_name.setText(selectGetwayName);
                layout_getway_list.setVisibility(View.GONE);
            }
        });
    }

    private void initEvents() {
        textview_add_room_complement.setOnClickListener(this);
        image_back.setOnClickListener(this);
        layout_getway.setOnClickListener(this);
        gridview_room_type.setAdapter(mGridViewRommTypeAdapter);
        gridview_room_type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomType = listTop.get(position);
                mGridViewRommTypeAdapter.setSelectedPosition(position);
                mGridViewRommTypeAdapter.notifyDataSetInvalidated();
                edittext_room_name.setText(roomType);
                edittext_room_name.setSelection(roomType.length());
            }
        });

    }

    private void initViews() {
        textview_add_room_complement = findViewById(R.id.textview_add_room_complement);
        image_back = findViewById(R.id.image_back);
        layout_getway = findViewById(R.id.layout_getway);
        edittext_room_name = findViewById(R.id.edittext_room_name);
        gridview_room_type = findViewById(R.id.gridview_room_type);
        layout_getway_list = findViewById(R.id.layout_getway_list);
        listview_select_getway = findViewById(R.id.listview_select_getway);
        textview_getway_name = findViewById(R.id.textview_getway_name);
        imageview_getway_arror_right = findViewById(R.id.imageview_getway_arror_right);
    }

    private String roomType;
    private static final int MSG_ADD_ROOM_FAILED = 100;
    private static final int MSG_ADD_ROOM_SUCCESS = 101;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ADD_ROOM_FAILED:
                    Toast.makeText(AddRommActivity.this, "添加房间失败，已存在同名房间", Toast.LENGTH_LONG).show();
                    break;
                case MSG_ADD_ROOM_SUCCESS:
                    Toast.makeText(AddRommActivity.this, "添加房间成功", Toast.LENGTH_LONG).show();
                    if (fromAddDevice) {
                        RoomManager.getInstance().setCurrentSelectedRoom(null);
                        Intent intent = new Intent(AddRommActivity.this, AddDeviceActivity.class);
                        startActivity(intent);
                    }
                    break;
            }

        }
    };
    private Device currentSelectGetway;
    private String userName;

    @Override
    protected void onResume() {
        super.onResume();
        userName= Perfence.getPerfence(Perfence.PERFENCE_PHONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //完成
            case R.id.textview_add_room_complement:
                final String roomName = edittext_room_name.getText().toString();
                if(!NetUtil.isNetAvailable(this)){
                    ToastSingleShow.showText(this,"无可用网络连接,请检查网络");
                    return;
                }
                if(userName.equals("")){
                    ToastSingleShow.showText(this,"用户未登录");
                    return;
                }
                Room room=new Room();
                room.setRoom_name(roomName);
                room.setRoom_type(roomType);
                RestfulToolsHomeGenius.getSingleton(this).addRomm(userName, room, new Callback<DeviceOperationResponse>() {
                    @Override
                    public void onResponse(Call<DeviceOperationResponse> call, Response<DeviceOperationResponse> response) {
                        Log.i(TAG,"response.code()="+response.code());
                        if(response.code()==200){
                            if (!roomName.equals("")) {
                                boolean result = roomManager.addRoom(roomType, roomName,currentSelectGetway);
                                if (result) {
                                    mHandler.sendEmptyMessage(MSG_ADD_ROOM_SUCCESS);
                                    finish();
                                } else {
                                    mHandler.sendEmptyMessage(MSG_ADD_ROOM_FAILED);
                                }
                            } else {
                                Toast.makeText(AddRommActivity.this, "请输入房间名称", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DeviceOperationResponse> call, Throwable t) {
                        Log.i(TAG,"addroom onFailure="+t.toString());
                        ToastSingleShow.showText(AddRommActivity.this,"添加房间失败");
                    }
                });

                break;
            case R.id.image_back:
                onBackPressed();
                break;
            case R.id.layout_getway:
                if (layout_getway_list.getVisibility() == View.VISIBLE) {
                    layout_getway_list.setVisibility(View.GONE);
                    imageview_getway_arror_right.setImageResource(R.drawable.directionicon);
                } else {
                    layout_getway_list.setVisibility(View.VISIBLE);
                    imageview_getway_arror_right.setImageResource(R.drawable.nextdirectionicon);
                }
                break;
        }
    }
}
