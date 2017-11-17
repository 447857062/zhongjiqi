package deplink.com.smartwirelessrelay.homegenius.Protocol.json;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/10/30.
 */
public class SmartDev extends DataSupport implements Serializable{
    @Column(unique = true,nullable = false)
    private String Uid;
    private String CtrUid;
    private String Type;
    private String Status;
    private String Org;
    private String Ver;
    private String name;
    private Room room;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getOrg() {
        return Org;
    }

    public void setOrg(String org) {
        Org = org;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getCtrUid() {
        return CtrUid;
    }

    public void setCtrUid(String ctrUid) {
        CtrUid = ctrUid;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getVer() {
        return Ver;
    }

    public void setVer(String ver) {
        Ver = ver;
    }

    @Override
    public String toString() {
        return "SmartDev{" +
                "Ver='" + Ver + '\'' +
                ", Org='" + Org + '\'' +
                ", Status='" + Status + '\'' +
                ", Type='" + Type + '\'' +
                ", CtrUid='" + CtrUid + '\'' +
                ", Uid='" + Uid + '\'' +
                ", roomname='" + room.getRoomName() + '\'' +
                '}';
    }
}
