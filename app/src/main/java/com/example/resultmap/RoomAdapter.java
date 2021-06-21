package com.example.resultmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.CustomViewHolder> {

    private ArrayList<com.example.resultmap.RoomData> arrayList;
    private Drawable drawable;

    public RoomAdapter(ArrayList<com.example.resultmap.RoomData> arrayList,Drawable drawable)
    {   this.drawable=drawable;
        this.arrayList = arrayList;
    }


    @NonNull
    @Override
    public com.example.resultmap.RoomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.romm_list,parent,false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.resultmap.RoomAdapter.CustomViewHolder holder, int position) {
        if(holder.roomTitle != null)
            holder.roomTitle.setText("방이름 : " + arrayList.get(position).getName());

        System.out.println("DGDGDGD"+arrayList.get(position).getmyRoomCheck());
        if (arrayList.get(position).getmyRoomCheck()!=null){
            if (arrayList.get(position).getmyRoomCheck().equals(arrayList.get(position).getId())){
                holder.roomTitle.append("   (내방)");
                holder.roomList.setBackground(drawable);
            }
        }


        if(holder.roomLeftUser != null)
            holder.roomLeftUser.append(arrayList.get(position).getUseridx()+"/"+arrayList.get(position).getUserLimit());//현재 멤버 몆명인지 불러오기
        if(holder.roomGender != null)
            holder.roomGender.setText("성별 : " + arrayList.get(position).getGender());
        if (holder.roomTime != null)
            holder.roomTime.setText("시간 : " + arrayList.get(position).getStartAt());
        if(holder.roomPlace != null)
            holder.roomPlace.setText(arrayList.get(position).getPlace());
        holder.roomList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("**************눌렸어******************"+arrayList.get(position).getId());
                Intent intent=new Intent(arrayList.get(position).getContext(),Chatting.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("rommId",arrayList.get(position).getId());
                arrayList.get(position).getContext().startActivities(new Intent[]{intent});
//               채팅방 누르면 반응하는곳
//                여기에 arrayList.get(position).getId() 로 방아이디를 불러옴
//                intent 로 chatting.java 파일로 방 아이디를 넘겨주면서 넘어감
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected TextView roomTitle;
        protected TextView roomLeftUser;
        protected TextView roomGender;
        protected TextView roomTime;
        protected TextView roomPlace;
        protected LinearLayout roomList;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.roomTitle = (TextView) itemView.findViewById(R.id.roomTitle);
            this.roomLeftUser = (TextView) itemView.findViewById(R.id.roomLeftUser);
            this.roomGender = (TextView) itemView.findViewById(R.id.roomGender);
            this.roomTime = (TextView) itemView.findViewById(R.id.roomTime);
            this.roomPlace = (TextView) itemView.findViewById(R.id.roomPlace);
            this.roomList = (LinearLayout) itemView.findViewById(R.id.roomList);

        }
    }




}
