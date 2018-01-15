package com.deplink.homegenius.activity.device.smartlock.alarmhistory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deplink.homegenius.Protocol.json.device.lock.alertreport.Info;

import java.util.Date;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

import com.deplink.homegenius.util.DateUtil;

/**
 * Created by Administrator on 2017/10/31.
 * 开锁记录适配器
 */
public class AlarmHistoryAdapter extends BaseAdapter{
    private static final String TAG="AlarmHistoryAdapter";
    private Context mContext;
    private List<Info>mDatas;
    public AlarmHistoryAdapter(Context mContext, List<Info>mDevices) {
        this.mContext=mContext;
        this.mDatas=mDevices;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            vh=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(R.layout.alarmhistorylistitem,null);
            vh.textview_userid= convertView.findViewById(R.id.textview_userid);
            vh.textview_data_year_mouth_day= convertView.findViewById(R.id.textview_data_year_mouth_day);
            vh.textview_data_hour_minute_second= convertView.findViewById(R.id.textview_data_hour_minute_second);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }
        String time=mDatas.get(position).getTime();
        Date date= DateUtil.transStringTodata(time);
        String yearMouthDay=DateUtil.getYearMothDayStringFromData(date);
        String hourMinuteSecond=DateUtil.getHourMinuteSecondStringFromData(date);
        Log.i(TAG,"yearMouthDay="+yearMouthDay+"hourMinuteSecond="+hourMinuteSecond);
        vh.textview_userid.setText(mDatas.get(position).getUserid());
        vh.textview_data_year_mouth_day.setText(yearMouthDay);
        vh.textview_data_hour_minute_second.setText(hourMinuteSecond);
        return convertView;
    }

    private static class ViewHolder{
        TextView textview_userid;
        TextView textview_data_year_mouth_day;
        TextView textview_data_hour_minute_second;

    }
}