package com.lemon95.ymtv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.lemon95.ymtv.R;
import com.lemon95.ymtv.bean.SerialDitions;

import java.util.ArrayList;

/**
 * Created by WXT on 2016/7/26.
 */
public class SerialAdapter extends BaseAdapter {

   // private ArrayList<SerialDitions.Data.SerialEpisodes> serialEpisodes;
    private String lastEpisode;
    private Context context;
    ArrayList<String> list = new ArrayList<String>();

    public SerialAdapter(String lastEpisode,Context context) {
        this.context = context;
        int size = Integer.parseInt(lastEpisode);
        for (int i=1;i<size + 1;i++) {
            list.add(i + "");
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.lemon_item_servil, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final String serial = list.get(position);
        holder.lemon_item_but.setText("第" + serial + "集");
       /* holder.lemon_item_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("videoId", serial.getId());  //剧集ID
                bundle.putString("videoName", getIntent().getStringExtra("videoName") + "(第" + serial.getSerialIndex() + "集)");
                bundle.putString("videoType", AppConstant.SERIALS);
                startActivity(PlayActivity.class, bundle);
            }
        });*/
        return view;
    }

    private static class ViewHolder {
        TextView lemon_item_but;

        public ViewHolder(View view) {
            lemon_item_but = (TextView) view.findViewById(R.id.lemon_item_but);
        }
    }


}
