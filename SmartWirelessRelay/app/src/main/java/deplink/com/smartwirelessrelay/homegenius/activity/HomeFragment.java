package deplink.com.smartwirelessrelay.homegenius.activity;import android.app.AlertDialog;import android.app.Fragment;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.util.Log;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.Button;import android.widget.TextView;import java.io.IOException;import java.io.InputStream;import javax.net.ssl.SSLSocket;import deplink.com.smartwirelessrelay.homegenius.Devices.ConnectManager;import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;import deplink.com.smartwirelessrelay.homegenius.Protocol.packet.GeneralPacket;import deplink.com.smartwirelessrelay.homegenius.util.DataExchange;import deplink.com.smartwirelessrelay.homegenius.util.PublicMethod;import deplink.com.smartwirelessrelay.homegenius.util.SharedPreference;import deplink.com.smartwirelessrelay.homegenius.util.WifiConnected;/** * */public class HomeFragment extends Fragment implements View.OnClickListener {    private static final String TAG = "HomeFragment";    private TextView textview_ssid_name;    private TextView textview_uid_name;    private Button button_connect;    private Button button_query_devlist;    private Button button_heath_switch;    private Button button_send_smart_dev;    private Button button_query_lock_history;    private SSLSocket Client_sslSocket = null;    private GeneralPacket packet;    @Override    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {        View view = inflater.inflate(R.layout.fragment_main_home, container, false);        initFindViewById(view);        return view;    }    @Override    public void onResume() {        super.onResume();        textview_uid_name.setText("uid:" + "还没有获取到");        packet = new GeneralPacket(getActivity());        textview_ssid_name.setText("当前连接的wifi是：" + PublicMethod.getSSID(getActivity()));        SharedPreference sharedPreference = new SharedPreference(getActivity(), "heathswitch");        String open = sharedPreference.getString("heathswitch");        Log.i(TAG,"onResume open="+open);        if (open != null) {            if (open.equals("open")) {                button_heath_switch.setText("发送心跳包:开");            } else {                button_heath_switch.setText("发送心跳包:关闭");            }        } else {            button_heath_switch.setText("发送心跳包:关闭");        }    }    protected void initFindViewById(View view) {        textview_ssid_name = (TextView) view.findViewById(R.id.textview_ssid_name);        textview_uid_name = (TextView) view.findViewById(R.id.textview_uid_name);        button_connect = (Button) view.findViewById(R.id.button_connect);        button_heath_switch = (Button) view.findViewById(R.id.button_heath_switch);        button_heath_switch.setOnClickListener(this);        button_connect.setOnClickListener(this);        button_query_devlist = (Button) view.findViewById(R.id.button_query_devlist);        button_query_devlist.setOnClickListener(this);        button_query_lock_history = (Button) view.findViewById(R.id.button_query_lock_history);        button_query_lock_history.setOnClickListener(this);        button_send_smart_dev = (Button) view.findViewById(R.id.button_send_smart_dev);        button_send_smart_dev.setOnClickListener(this);    }    @Override    public void onClick(View v) {        switch (v.getId()) {            case R.id.button_connect:                new Thread(new Runnable() {                    @Override                    public void run() {                        if (WifiConnected.isWifiAvailable(getActivity())) {                            //---------读数据---------------------------                            if (null == Client_sslSocket) {                                ConnectManager.getInstance().InitEllESDK(getActivity(), null);                                Client_sslSocket = ConnectManager.getInstance().getClient_sslSocket();                            }                            Log.i(TAG, "Client_sslSocket!=null:" + (Client_sslSocket != null));                            packet.packBindPacket();                            ConnectManager.getInstance().getOut(packet.data);                            getIn();                        } else {                            Message msg = Message.obtain();                            msg.what = MSG_TOAST;                            msg.obj = "wifi未连接";                            mHandler.sendMessage(msg);                        }                    }                }).start();                break;            case R.id.button_query_devlist:                startActivity(new Intent(getActivity(), DevListActivity.class));                break;            case R.id.button_query_lock_history:                startActivity(new Intent(getActivity(), LockHistory.class));                break;            case R.id.button_send_smart_dev:               //下发稚嫩设备列表                startActivity(new Intent(getActivity(), SendSmartDevListActivity.class));                break;            case R.id.button_heath_switch:                Log.i(TAG, "homefragment button_heath_switch onclick");                SharedPreference sharedPreference = new SharedPreference(getActivity(), "heathswitch");                String open = sharedPreference.getString("heathswitch");                if (open != null) {                    Log.i(TAG, "homefragment 发送心跳包开关 open=" + open);                    if (open.equals("open")) {                        sharedPreference.saveString("heathswitch", "close");                        button_heath_switch.setText("发送心跳包:关闭");                    } else {                        sharedPreference.saveString("heathswitch", "open");                        button_heath_switch.setText("发送心跳包:开");                    }                } else {                    sharedPreference.saveString("heathswitch", "close");                    button_heath_switch.setText("发送心跳包:关闭");                }                break;        }    }    public void getIn() {        String str;        if (null != Client_sslSocket) {            try {                InputStream input = Client_sslSocket.getInputStream();                byte[] buf = new byte[1024];                int len = input.read(buf);                if (len != -1) {                    str = new String(buf, 0, len);                    System.out.println("received:" + str + "length=" + len);                    System.out.println("received:" + DataExchange.byteArrayToHexString(buf));                    byte[] uid = new byte[32];                    System.arraycopy(buf, 7, uid, 0, 32);                    str = new String(uid);                    Message msg = Message.obtain();                    msg.obj = str;                    msg.what = MSG_GET_INPUTSTREAM;                    mHandler.sendMessage(msg);                }            } catch (IOException e) {                e.printStackTrace();            }        }    }    private static final int MSG_GET_INPUTSTREAM = 0x0;    private static final int MSG_TOAST = 0x1;    private Handler mHandler = new Handler() {        @Override        public void handleMessage(Message msg) {            super.handleMessage(msg);            String str = (String) msg.obj;            switch (msg.what) {                case MSG_GET_INPUTSTREAM:                    if (!"无法连接服务器".equals(str)) {                        textview_uid_name.setText("uid:" + str);                        SharedPreference sharedPreference = new SharedPreference(getActivity(), "uid");                        sharedPreference.saveString("uid", str);                        Log.i(TAG, "uid=" + str);                    }                    new AlertDialog                            .Builder(getActivity())                            .setTitle("设备UID")                            .setNegativeButton("确定", null)                            .setIcon(android.R.drawable.ic_menu_agenda)                            .setMessage(str)                            .show();                    break;                case MSG_TOAST:                    new AlertDialog                            .Builder(getActivity())                            .setTitle("设备UID")                            .setNegativeButton("确定", null)                            .setIcon(android.R.drawable.ic_menu_agenda)                            .setMessage(str)                            .show();                    break;            }        }    };}