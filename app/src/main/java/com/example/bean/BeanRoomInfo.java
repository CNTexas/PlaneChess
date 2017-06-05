package com.example.bean;

import com.example.network.broadcast.DataBroaCastSerlied;

/**
 * @author XQF
 * @created 2017/6/5
 */
public class BeanRoomInfo {
    private String mRoomeName;
    private String mIp;
    private DataBroaCastSerlied mRoomData;

    public String getRoomeName() {
        return mRoomeName;
    }

    public void setRoomeName(String roomeName) {
        mRoomeName = roomeName;
    }

    public String getIp() {
        return mIp;
    }

    public void setIp(String ip) {
        mIp = ip;
    }

    public DataBroaCastSerlied getRoomData() {
        return mRoomData;
    }

    public void setRoomData(DataBroaCastSerlied roomData) {
        mRoomData = roomData;
    }
}
