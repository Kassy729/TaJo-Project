package com.example.resultmap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.naver.maps.geometry.LatLng;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class chattingRoom extends AppCompatActivity {


    private Intent socketServiceIntent;

    private ArrayList<RoomData> arrayList;
    private RoomAdapter roomAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RoomData roomData;
    private Button Menu;
    DrawerLayout drawerLayout;
    private Message msg,msg2,cookiecheck;

    private Socket mSocket;

    String roomId=null;
    int myId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);

        Log.d("방","버튼 눌렀어요");
        String className = SocketService.class.getName();

//        drawerLayout=findViewById(R.id.drawer_layout);
//
//        Menu=findViewById(R.id.Menu);
//        Menu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                drawerLayout.openDrawer(menubar);
//            }
//        });

        recyclerView = (RecyclerView) findViewById(R.id.RoomList);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        arrayList = new ArrayList<>();

        Drawable drawable= ContextCompat.getDrawable(this,R.drawable.my_romm_back);

        roomAdapter = new RoomAdapter(arrayList,drawable);
        recyclerView.setAdapter(roomAdapter);

        new CheckLogin().start();
//        RecyclerDecoration spa=new RecyclerDecoration(20);
//        recyclerView.addItemDecoration(spa);
//        setRoom(setRoomList());

        try {
            mSocket = IO.socket("https://tazoapp.site/ws-room");
            mSocket.connect();

            mSocket.on("createRoom", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        //TODO 액티비티 화면 재갱신 시키는 코드
                        Intent intent = getIntent();
                        finish(); //현재 액티비티 종료 실시
                        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                        startActivity(intent); //현재 액티비티 재실행 실시
                        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            mSocket.on("destroyRoom", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        //TODO 액티비티 화면 재갱신 시키는 코드
                        Intent intent = getIntent();
                        finish(); //현재 액티비티 종료 실시
                        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                        startActivity(intent); //현재 액티비티 재실행 실시
                        overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }




                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }



    public String setRoomList()
    {
        String record = "";
        HttpAsyncTask httpAsyncTask = new HttpAsyncTask(record);
        try {
            System.out.println("error");
            record =httpAsyncTask.execute("https://tazoapp.site/rooms").get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return record;
    }

    private static class HttpAsyncTask extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String result;

        public HttpAsyncTask(String result)
        {
            this.result=result;
        }

        @Override
        protected String doInBackground(String... params) {

            String strUrl = params[0];

            try {
                Request request = new Request.Builder().url(strUrl).build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return result;
        }



    }

    public void setRoom(String json)
    {
        try{
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(json);
            JSONArray jsonArray = (JSONArray) obj;
//            JSONObject jsonArray=(JSONObject)obj;

            for(int i=0; i<jsonArray.size(); i++)
            {
                msg = handler.obtainMessage();
//                msg.what=1;
                msg.obj=jsonArray.get(i);
                handler.sendMessage(msg);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){

            JSONObject jsonStr = (JSONObject) msg.obj;
            JSONArray jsonArray=(JSONArray)jsonStr.get("Members");
            int userInx=jsonArray.size();

            System.out.println(jsonStr.toString());
            String id = jsonStr.get("id")+"";
            String name = (String) jsonStr.get("name");
            String userLimit = jsonStr.get("userLimit") + "";
//            double originLat =Double.parseDouble(jsonStr.get("originLat").toString());
//            double originLng =Double.parseDouble(jsonStr.get("originLng").toString());
//            double destinationLat = Double.parseDouble(jsonStr.get("destinationLat").toString());
//            double destinationLng = Double.parseDouble(jsonStr.get("destinationLng").toString());
//            System.out.println("xxxxx"+destinationLat+" "+destinationLng);
            String start=(String)jsonStr.get("originName");
            String result=(String)jsonStr.get("destinationName");


            Context context=getBaseContext();


            String startAt = (String) jsonStr.get("startAt");
            System.out.println(startAt);

            StringTokenizer st=new StringTokenizer(startAt,"T");
            String  temp1=st.nextToken();
            String  temp2=st.nextToken();

            StringTokenizer st2=new StringTokenizer(temp1,"-");
            st2.nextToken();
            String time=st2.nextToken()+"월";
            time+=st2.nextToken()+"일   ";
            StringTokenizer st3=new StringTokenizer(temp2,":");
            time+=st3.nextToken()+"시";
            time+=st3.nextToken()+"분";
            System.out.println(time);
            String gender = (String) jsonStr.get("gender");

            roomData = new RoomData(id, name, userLimit,gender,userInx,
                    time,start,result,context,roomId);
            arrayList.add(roomData);
            roomAdapter.notifyDataSetChanged();
            System.out.println("실행은됨");
        }
    };

    @Override
    public void onBackPressed() {
            Intent intent=new Intent(chattingRoom.this,MainActivity.class);
            startActivity(intent);
            finish();

    }
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2
    class myGetThead extends Thread{
        StringBuilder sb2;

        public void run() {
            try {
                URL url2 = new URL("https://tazoapp.site/rooms");

                HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                connection2.setRequestMethod("GET");
                connection2.setDoInput(true);

                InputStream is2 = connection2.getInputStream();
                sb2 = new StringBuilder();
                BufferedReader br2 = new BufferedReader(new InputStreamReader(is2,"UTF-8"));
                String result="";
                String temp=null;
                while ((temp=br2.readLine())!=null){
                    result+=temp;
                }
                System.out.println("받아옴"+result);

                try {
                    JSONParser jsonParser = new JSONParser();
                    Object obj = jsonParser.parse(result);
                    org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) obj;


                    msg2=handler2.obtainMessage();
                    msg2.what=2;
                    msg2.obj=jsonArray.toString();
                    handler2.sendMessage(msg2);
                }catch (Exception e){

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    Handler handler2=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            org.json.JSONObject jsonObject= null;
            try {
                org.json.JSONArray array=new org.json.JSONArray(msg.obj.toString());
                if (array.length()<1){
                    return;
                }
               loop: for (int i=0;i<array.length();i++){
                    org.json.JSONObject jsonObject2=(org.json.JSONObject)array.get(i);
                    org.json.JSONArray jsonArray=(org.json.JSONArray)jsonObject2.get("Members");

                    System.out.println(jsonArray.toString());
                    System.out.println("dddddddddddfdfdfdfd"+myId);

                    for (int j=0;j<jsonArray.length();j++){
                        org.json.JSONObject temp=(org.json.JSONObject)jsonArray.get(j);

                        System.out.println(temp.get("id").toString());

                        if (Integer.parseInt(temp.get("id").toString())==myId){
                            roomId=jsonObject2.get("id").toString();
                            System.out.println("uuuuuuuuuuuuu"+roomId);
                            break loop;
                        }
                        else roomId=null;

                    }
                }
                setRoom(setRoomList());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    public class CheckLogin extends Thread
    {
        @Override
        public void run()
        {
            System.out.println("로그인 체크 시작~");
            String url = "https://tazoapp.site/auth";
            String shard="file";
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                String setCookie = sharedPreferences.getString("cookie","");
                Log.d("세션",setCookie);

                Request request = new Request.Builder()
                        .addHeader("cookie", setCookie)
                        .url(url)
                        .build();
                Response response = client.newCall(request)
                        .execute();

                String result = response.body().string();
                System.out.println("result : " + result);
                if(result.equals("null")){
                    Intent intent = new Intent(chattingRoom.this,Login.class);
                    startActivity(intent);
                    finish();
                }
                cookiecheck=Logincheck.obtainMessage();
                cookiecheck.what=3;
                cookiecheck.obj=result;
                Logincheck.sendMessage(cookiecheck);

            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }
    Handler Logincheck=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg){
            try {
                org.json.JSONObject json=new org.json.JSONObject(msg.obj.toString());
                myId=Integer.parseInt(json.get("id").toString());
                System.out.println("YYYYYYYYYYYYYYYYY"+myId);
                new myGetThead().start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    };

}