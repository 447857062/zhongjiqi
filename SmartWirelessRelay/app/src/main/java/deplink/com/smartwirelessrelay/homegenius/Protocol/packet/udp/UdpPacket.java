package deplink.com.smartwirelessrelay.homegenius.Protocol.packet.udp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import deplink.com.smartwirelessrelay.homegenius.manager.connect.local.udp.interfaces.OnGetIpListener;
import deplink.com.smartwirelessrelay.homegenius.manager.connect.local.udp.interfaces.OnRecvLocalConnectIpListener;
import deplink.com.smartwirelessrelay.homegenius.Protocol.packet.BasicPacket;

/**
 * udp发送广播包，探测本地连接的IP地址
 * 接收获取到的IP地址
 * 负责发送udp广播
 */
public class UdpPacket  implements OnRecvLocalConnectIpListener {

    public static final String TAG = "UdpPacket";
    private UdpComm netUdp;
    //发送网络包队列
    public  ArrayList<BasicPacket> sendNetPakcetList;
    private UdpPacketThread udpPacketThread;
    private Context mContext;
    private OnGetIpListener mOnGetIpListener;
    public UdpPacket(Context context,OnGetIpListener listener) {
        this.mContext = context;
        this.mOnGetIpListener=listener;
        if (sendNetPakcetList == null) {
            sendNetPakcetList = new ArrayList<>();
        }
    }

    /**
     * 发送网络数据包
     *
     * @param packet
     * @return
     */
    public synchronized int writeNet(BasicPacket packet) {
        Log.i(TAG,"添加一个udp发送数据包");
        sendNetPakcetList.add(packet);
        return 0;
    }

    /**
     * 从udp发送数据队列里面删除一条数据
     * 删除一个发送包
     * @param list
     * @param packet
     * @return
     */
    private synchronized boolean delOneSendPacket(ArrayList<BasicPacket> list, BasicPacket packet) {
        list.remove(packet);
        return true;
    }


    public void start() {
        stop();
        netUdp = new UdpComm(mContext, this);
        netUdp.startServer();
        udpPacketThread = new UdpPacketThread();
        udpPacketThread.start();
    }

    public void stop() {
        if (udpPacketThread != null) {
            udpPacketThread.stopThis();
            udpPacketThread = null;
        }
        sendNetPakcetList.clear();
    }

    public void restart() {
        start();
    }
    @Override
    public void OnRecvIp(byte[] ip) {
        Log.i(TAG,"获取到连接的IP地址");
        mOnGetIpListener.onRecvLocalConnectIp(ip);
    }

    private class UdpPacketThread extends Thread {
        boolean isRun;

        public void stopThis() {
            isRun = false;
        }

        @Override
        public void run() {
            super.run();
            isRun = true;
            while (isRun) {
                for (int i = 0; i < sendNetPakcetList.size(); i++) {
                    BasicPacket tmp = sendNetPakcetList.get(i);
                    Log.i(TAG,"UdpPacketThread Udp packet ip != null"+(tmp.ip != null)+"Udp packet.isFinish="+tmp.isFinish);
                    if (tmp.ip != null ) {
                        netUdp.sendData(tmp.getUdpData());
                        delOneSendPacket(sendNetPakcetList, tmp);
                    }
                }
            }
        }
    }
}