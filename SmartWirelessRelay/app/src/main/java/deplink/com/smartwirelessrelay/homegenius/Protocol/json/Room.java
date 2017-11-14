package deplink.com.smartwirelessrelay.homegenius.Protocol.json;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/10/30.
 * 房间管理
 */
public class Room extends DataSupport implements Serializable{

    @Column(unique = true,nullable = false)
    /**
     * 房间名称
     */
    private String roomName;

    @Column(nullable = false)
    /**
     * 房间序号（用于显示的时候排序）
     */
    private int roomOrdinalNumber;

    private List<SmartDev>mDevices;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomOrdinalNumber() {
        return roomOrdinalNumber;
    }

    public void setRoomOrdinalNumber(int roomOrdinalNumber) {
        this.roomOrdinalNumber = roomOrdinalNumber;
    }

    public List<SmartDev> getmDevices() {
        return mDevices;
    }

    public void setmDevices(List<SmartDev> mDevices) {
        this.mDevices = mDevices;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomName='" + roomName + '\'' +
                ", roomOrdinalNumber=" + roomOrdinalNumber +
                ", mDevices=" + mDevices +
                '}';
    }
}