package deplink.com.smartwirelessrelay.homegenius.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Collections;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;
import deplink.com.smartwirelessrelay.homegenius.Protocol.json.Room;
import deplink.com.smartwirelessrelay.homegenius.activity.adapter.GridViewAdapter;
import deplink.com.smartwirelessrelay.homegenius.manager.room.RoomManager;
import deplink.com.smartwirelessrelay.homegenius.view.gridview.DragGridView;

public class RoomActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RoomActivity";
    private LinearLayout layout_home_page;
    private LinearLayout layout_devices;
    private LinearLayout layout_rooms;
    private LinearLayout layout_personal_center;
    private DragGridView mDragGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initViews();
        initDatas();
        initEvents();
    }

    private GridViewAdapter mRoomsAdapter;

    //TODO 初始化数据库
    private RoomManager mRoomManager;

    private void initDatas() {
        mRoomManager = RoomManager.getInstance();
        mRoomManager.initRoomManager();
        mRoomManager.getDatabaseRooms();
        //TODO 使用数据库中的数据
        mRoomsAdapter = new GridViewAdapter(this, RoomManager.getInstance().getmRooms());
    }

    private void initEvents() {
        layout_home_page.setOnClickListener(this);
        layout_devices.setOnClickListener(this);
        layout_rooms.setOnClickListener(this);
        layout_personal_center.setOnClickListener(this);

        //房间适配器
        mDragGridView.setAdapter(mRoomsAdapter);

        mDragGridView.setOnChangeListener(new DragGridView.OnChanageListener() {

            @Override
            public void onChange(int from, int to) {
                Room temp = RoomManager.getInstance().getmRooms().get(from);
                //这里的处理需要注意下
                if (from < to) {
                    for (int i = from; i < to; i++) {
                        Collections.swap(RoomManager.getInstance().getmRooms(), i, i + 1);
                    }
                } else if (from > to) {
                    for (int i = from; i > to; i--) {
                        Collections.swap(RoomManager.getInstance().getmRooms(), i, i - 1);
                    }
                }
                RoomManager.getInstance().getmRooms().set(to, temp);
                mRoomsAdapter.notifyDataSetChanged();
                //TODO 重新赋值下标
                int fronIndex=RoomManager.getInstance().getmRooms().get(from).getRoomOrdinalNumber();
                int toIndex=RoomManager.getInstance().getmRooms().get(to).getRoomOrdinalNumber();

                RoomManager.getInstance().getmRooms().get(from).setRoomOrdinalNumber(toIndex);
                RoomManager.getInstance().getmRooms().get(from).save();

                RoomManager.getInstance().getmRooms().get(to).setRoomOrdinalNumber(fronIndex);
                RoomManager.getInstance().getmRooms().get(to).save();

            }
        });
        mDragGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //最大值，最后一个，添加房间
                if (position == mRoomsAdapter.getCount() - 1) {
                    startActivity(new Intent(RoomActivity.this, AddRommActivity.class));
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("roomName", RoomManager.getInstance().getmRooms().get(position).getRoomName());
                    bundle.putInt("roomOrdinalNumber", RoomManager.getInstance().getmRooms().get(position).getRoomOrdinalNumber());
                    Intent intent = new Intent(RoomActivity.this, ManageRoomActivity.class);
                    Log.i(TAG, "传递当前房间名字=" + bundle.get("roomName") + "获取到的名字是=" + RoomManager.getInstance().getmRooms().get(position));
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_MODIFY_ROOM);
                }
                Toast.makeText(RoomActivity.this, "onclick position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static final int REQUEST_MODIFY_ROOM = 100;

    private void initViews() {
        layout_home_page = (LinearLayout) findViewById(R.id.layout_home_page);
        layout_devices = (LinearLayout) findViewById(R.id.layout_devices);
        layout_rooms = (LinearLayout) findViewById(R.id.layout_rooms);
        layout_personal_center = (LinearLayout) findViewById(R.id.layout_personal_center);
        mDragGridView = (DragGridView) findViewById(R.id.dragGridView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_home_page:
                startActivity(new Intent(this, SmartHomeMainActivity.class));
                break;
            case R.id.layout_devices:
                startActivity(new Intent(this, DevicesActivity.class));
                break;
            case R.id.layout_rooms:
                //  startActivity(new Intent(this,RoomActivity.class));
                break;
            case R.id.layout_personal_center:
                startActivity(new Intent(this, PersonalCenterActivity.class));
                break;
        }
    }
}