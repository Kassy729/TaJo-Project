package com.example.resultmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private long backKeyPressedTime = 0;
    private Toast toast;

    String str;
    String roomId;
    String cookie;
    private Boolean check=true;
    private Boolean roommadeCheck=true;
    private Boolean speedButton=true;

    private Button button,profile_btn,makeroom;
    private ImageButton cahage_Button,menubutton,myLocation;
    private EditText text;

    private  static int permission_code=100;
    private  static String[] permissions={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

    };
    private FusedLocationSource mLoction;
    private NaverMap mNaverMap;
    Marker marker;

    private DrawerLayout drawerLayout;
    private  View menubar;

    TextView spinner;
    ArrayAdapter<String> arrayAdapter;

    LinearLayout mLlinearLayout;
    Message msg;
    Message msg2;
    Message cookiecheck,cookieMgs;
    OkHttpClient client=new OkHttpClient();

    private Context context;
    private Button logout;

    int myId;
    Double[] origin;
    Double[] destin;
    private Button roomSelect;
    Animation animation;

    private com.github.nkzawa.socketio.client.Socket mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Thread checkLogin = new CheckLogin();
        checkLogin.start();




        roomSelect = (Button) findViewById(R.id.roomSelect);
        roomSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, chattingRoom.class);
                startActivity(intent);
                finish();
            }
        });



        Button speed=findViewById(R.id.speed);
        speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (speedButton){
                   if(str==null){
                       Toast.makeText(getApplicationContext(),"선택지를 선택해주세요", Toast.LENGTH_SHORT).show();
                       return;
                   }
                if (roommadeCheck==false){
                    Toast.makeText(getApplicationContext(),"입장중인 방이 있습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                   String genderResult;
                   String genderCookie;
                   try{
                       String shard="file";
                       SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                       genderCookie = sharedPreferences.getString("genderResult","");

                   }catch (Exception  e){
                       genderCookie=null;
                   }
                   if (genderCookie==null){
                       genderResult="none";
                   }
                   else {
                       genderResult=genderCookie;
                   }
                   System.out.println("gener select"+genderResult);
                   client=new OkHttpClient();
                   RequestBody Body;
                   if (check){
                       Body=new FormBody.Builder().
                               add("originLat",String.valueOf(origin[0]))
                               .add("originLng",String.valueOf(origin[1]))
                               .add("destinationLat",String.valueOf(destin[0]))
                               .add("destinationLng",String.valueOf(destin[1]))
                               .add("originName",str)
                               .add("destinationName","영진대")
                               .add("gender",genderResult)
                               .build();
                   }else{
                       Body=new FormBody.Builder().
                               add("originLat",String.valueOf(destin[0]))
                               .add("originLng",String.valueOf(destin[1]))
                               .add("destinationLat",String.valueOf(origin[0]))
                               .add("destinationLng",String.valueOf(origin[1]))
                               .add("originName","영진대")
                               .add("destinationName",str)
                               .add("gender",genderResult)
                               .build();
                   }

                   speedButton=false;
                   speed.setTextSize(40);
                   speed.setText("       ●");
//                   speed.setTextColor(0xAAC38E71);
                   animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flow);
                   speed.startAnimation(animation);

                   try {
                       mSocket = IO.socket("https://tazoapp.site/ws-queue");
                   } catch (URISyntaxException e) {
                       e.printStackTrace();
                   }
                   mSocket.connect();

                   Request request = new Request.Builder()
                           .addHeader("cookie",cookie)
                           .url("https://tazoapp.site/rooms/queue")
                           .post(Body)
                           .build();
                   client.newCall(request).enqueue(new Callback() {
                       @Override
                       public void onFailure(Call call, IOException e) {
                           System.out.println("연결실패");
                           System.out.println(e.getMessage());
                       }

                       @Override
                       public void onResponse(Call call, Response response) throws IOException {
                           System.out.println("연결성공");
                           System.out.println(response.toString()+" "+response);
                           System.out.println(response.code());
                           String checkBody=response.body().string();
                           if (!checkBody.equals("{\"success\":true}")){
                               try {
                                   JSONObject myjson=new JSONObject(checkBody);
                                   String MyroomId_sub=myjson.get("RoomId").toString();
                                   Intent intent=new Intent(MainActivity.this,Chatting.class);
                                   intent.putExtra("rommId",MyroomId_sub);
                                   startActivity(intent);
                                   mSocket.close();
                                   finish();
                               } catch (JSONException e) {
                                   System.out.println("파싱 오류");
                               }

                           }
//                           if ()

                       }
                   });

                   mSocket.on("enterUser", new Emitter.Listener() {
                       @Override
                       public void call(Object... args) {
                           try{
                               System.out.println("소캣 접속 완료!");
                               String data =  args[0].toString();
                               JSONObject object=new JSONObject(data);
                               System.out.println(object.toString());
                               JSONArray array=(JSONArray)object.get("matchedUsersId");
                               for (int i=0;i<array.length();i++){
                                   JSONObject temp=(JSONObject)array.get(i);
                                   if (myId==Integer.parseInt(temp.get("id").toString())){
                                       System.out.println("매칭됨 방아이디 ="+object.get("RoomId"));

                                       Intent intent=new Intent(MainActivity.this,Chatting.class);
                                       System.out.println("khkhkhkhkhkhkhkhk"+temp.get("id").toString());
                                       intent.putExtra("rommId",object.get("RoomId").toString());
                                       startActivity(intent);
                                       mSocket.close();
                                       finish();

                                   }
                               }

                               Log.d("aaaaaaaaaa","왔어용");
//                            JSONParser jsonParser = new JSONParser();
//                            Object obj = jsonParser.parse(data);
//                            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;



//
//                            msg = handler.obtainMessage();
//                            msg.what=1;
//                            msg.obj=jsonObject;
//                            handler.sendMessage(msg);

                           } catch(Exception e) {
                               e.printStackTrace();
                           }


                       }
                   });
               }else{
                   mSocket.close();
                   Request request = new Request.Builder()
                           .addHeader("cookie",cookie)
                           .url("https://tazoapp.site/rooms/queue")
                           .delete()
                           .build();
                   client.newCall(request).enqueue(new Callback() {
                       @Override
                       public void onFailure(Call call, IOException e) {
                           System.out.println("연결실패");
                       }

                       @Override
                       public void onResponse(Call call, Response response) throws IOException {
                           System.out.println("닫기 성공");
                           System.out.println(response.code());
                           System.out.println(response.body().string());

                       }
                   });
                   speedButton=true;
                   animation.cancel();
//                   animation=null;
//                   speed.setTextColor(0xAAC38E71);
                   speed.clearAnimation();
                   speed.setTextSize(12);
                   speed.setText("빨리찾기");
               }



            }
        });


        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);


        mLoction=new FusedLocationSource(this,permission_code);


        button=findViewById(R.id.button);
        text=findViewById(R.id.text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str=text.getText().toString();
                System.out.println(str);
                new searchlocation(str).start();
            }

        });
        myLocation=findViewById(R.id.myLocation);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng2 = new LatLng(mLoction.getLastLocation().getLatitude(),mLoction.getLastLocation().getLongitude());
                CameraUpdate cameraUpdate2=CameraUpdate.scrollTo(latLng2);
                cameraUpdate2.zoomTo(15.0);
                mNaverMap.moveCamera(cameraUpdate2);
            }
        });

        drawerLayout =(DrawerLayout)findViewById(R.id.drawer_layout);
        menubar=(View) findViewById(R.id.menu);
        drawerLayout.setDrawerListener(listener);
        Button close=findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });

        String[] array={" 영진대 후문","영진대 정문"};
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                array);

        spinner=findViewById(R.id.spinner);

        menubutton=findViewById(R.id.menubutton);
        menubutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(menubar);
            }
        });


        mLlinearLayout = findViewById(R.id.mLlinearLayout);

        cahage_Button=findViewById(R.id.cahage_Button);
        cahage_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                marker.setMap(null);
                if(check){
                    mLlinearLayout.removeAllViews();
                    mLlinearLayout.addView(spinner);
                    mLlinearLayout.addView(text);
                    text.setText(null);
                    text.setHint("도착지");
                    Toast.makeText(getApplicationContext(),"도착지를 선택해 주세요",
                            Toast.LENGTH_SHORT).show();
                    check=false;
                }
                else{
                    mLlinearLayout.removeAllViews();
                    mLlinearLayout.addView(text);
                    mLlinearLayout.addView(spinner);
                    text.setText(null);
                    text.setHint("출발지");
                    Toast.makeText(getApplicationContext(),"출발지를 선택해 주세요",
                            Toast.LENGTH_SHORT).show();
                    check=true;
                }
//                str=null;
//                LatLng latLng2 = new LatLng(mLoction.getLastLocation().getLatitude(),mLoction.getLastLocation().getLongitude());
//                CameraUpdate cameraUpdate2=CameraUpdate.scrollTo(latLng2);
//                cameraUpdate2.zoomTo(15.0);
//                mNaverMap.moveCamera(cameraUpdate2);
            }



        });
        profile_btn=findViewById(R.id.profile_btn);
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
            }
        });

        makeroom=findViewById(R.id.makeroom);
        makeroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(roommadeCheck){
                   if(str==null){
                       Toast.makeText(getApplicationContext(),"목적지or도착지 선택후 이용하실수 있습니다",
                               Toast.LENGTH_SHORT).show();
                   }else{
                       Intent intent=new Intent(MainActivity.this,RoomMakeActivity.class);
                       intent.putExtra("myId",myId);
                       if (check){
                           intent.putExtra("start",str);
                           intent.putExtra("result","영진대");
                       }
                       else{
                           intent.putExtra("result",str);
                           intent.putExtra("start","영진대");
                       }
                       startActivity(intent);
                       finish();
                   }
               }
               else{
                   Intent intent=new Intent(MainActivity.this,Chatting.class);
                   intent.putExtra("rommId",roomId);
                   startActivity(intent);
                   finish();
               }
            }
        });

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread logout = new Logout();
                logout.start();
            }
        });
    }

    DrawerLayout.DrawerListener listener =new DrawerLayout.DrawerListener(){

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };



    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        ActivityCompat.requestPermissions(this,permissions,permission_code);

        mNaverMap=naverMap;
        mNaverMap.setLocationSource(mLoction);

//        mNaverMap.setOnMapClickListener((point, coord) ->{
//                    marker.setPosition(new LatLng(coord.latitude,coord.longitude));
//                    marker.setMap(mNaverMap);
//                }
//                Toast.makeText(this, coord.latitude + ", " + coord.longitude, Toast.LENGTH_SHORT).show()


//        );


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
//        Toast.makeText(this,"request함수 호출됨",Toast.LENGTH_LONG).show();
        if(requestCode==permission_code){
            if(grantResults.length>0 &&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }

    }
    class searchlocation extends Thread{
        String str;
        int index=0;
        String temp;
        public searchlocation(String str){
            this.str=str;
            this.temp=str;
        }
        @Override
        public void run() {
            super.run();
            List<Address> addresses ;


            try {
                Geocoder geocoder=new Geocoder(getBaseContext());
                System.out.println(str);
                addresses = geocoder.getFromLocationName(str, 10);
                Address address = addresses.get(0);
                System.out.println("get get"+address);
                System.out.println("get get"+address.getAdminArea());
                if (!address.getAdminArea().equals("대구광역시")&&!address.getLocality().equals("대구광역시")){
                    throw new Exception();
                }
                if (address != null && !address.equals(" ")) {
                    msg=handler.obtainMessage();
                    msg.what=1;//구분
                    msg.obj= address.getLatitude()+" "+address.getLongitude();
                    handler.sendMessage(msg);
                }
            } catch(Exception e) {
                System.out.println(e.getMessage());
                if ((index==0))str="대구 복현동 "+str;
                if (index==1)str="대구 "+temp;
                if (index<2){
                    index++;
                    run();
                }

            }
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try{
                String[] a=msg.obj.toString().split(" ");
                LatLng latLng = new LatLng(Double.parseDouble(a[0]), Double.parseDouble(a[1]));
                destin= new Double[]{Double.parseDouble(a[0]), Double.parseDouble(a[1])};
                origin= new Double[]{35.89562240000000,128.62242659999998};
                CameraUpdate cameraUpdate=CameraUpdate.scrollTo(latLng);
                cameraUpdate.zoomTo(15.0);

                mNaverMap.moveCamera(cameraUpdate);
                marker=new Marker();
                marker.setPosition(latLng);
                marker.setMap(mNaverMap);

            }catch (Exception e){
                e.getStackTrace();
                System.out.println(e.getMessage());
            }
        }
    };
    Handler handler2=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            JSONObject jsonObject= null;
            makeroom.setText("방만들기");
            roommadeCheck=true;
            try {
                JSONArray array=new JSONArray(msg.obj.toString());
                if (array.length()<1){
                    return;
                }

                for (int i=0;i<array.length();i++){
                    JSONObject jsonObject2=(JSONObject)array.get(i);
                    JSONArray jsonArray=(JSONArray)jsonObject2.get("Members");
                    for (int j=0;j<jsonArray.length();j++){
                        JSONObject temp=(JSONObject)jsonArray.get(j);
                        if (Integer.parseInt(temp.get("id").toString())==myId){
                            roommadeCheck=false;
                            roomId=jsonObject2.get("id").toString();
                            makeroom.setText("내 채팅방");
                            return;
                        }
                    }
                }

//                jsonObject = new JSONObject(msg.obj.toString());
//                System.out.println("kkkkkll"+jsonObject.toString());
//                JSONArray arr=(JSONArray)jsonObject.get("Members");
//                makeroom.setText("방만들기");
//                for (int i=0;i<arr.length();i++){
//                    JSONObject temp=(JSONObject) arr.get(i);
//                    roommadeCheck=true;
//                    if(Integer.parseInt(temp.get("id").toString())==myId){
//                        roommadeCheck=false;
//                        roomId=jsonObject.get("id").toString();
//                        makeroom.setText("내 채팅방");
//                        break;
//                    }
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    class myGetThead extends Thread{
        StringBuilder sb;
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
                  System.out.println("진홍이 방법"+jsonArray.toString());


//                    System.out.println("받아옴2"+array.toString());
//                    JSONObject object=(JSONObject)array.get(1);
//                    JSONArray array2=object.getJSONArray("Members");
//                    System.out.println(array2.toString());
                    msg2=handler2.obtainMessage();
                    msg2.what=2;
                    msg2.obj=jsonArray.toString();
                    handler2.sendMessage(msg2);
                }catch (Exception e){

                }


//                msg=myhand.obtainMessage();
//                msg.what=1;//구분
//                msg.obj= sb.toString();
//                myhand.sendMessage(msg);
//                System.out.println(sb);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            moveTaskToBack(true);

            finish();

            android.os.Process.killProcess(android.os.Process.myPid());
            toast.cancel();
        }
    }

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

                cookieMgs=cookiemake.obtainMessage();
                cookieMgs.what=3;
                cookieMgs.obj=setCookie;
                cookiemake.sendMessage(cookieMgs);

                Request request = new Request.Builder()
                        .addHeader("cookie", setCookie)
                        .url(url)
                        .build();
                Response response = client.newCall(request)
                        .execute();

                String result = response.body().string();
                System.out.println("result : " + result);
                if(result.equals("null")){
                    Intent intent = new Intent(context,Login.class);
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
                JSONObject json=new JSONObject(msg.obj.toString());
                myId=Integer.parseInt(json.get("id").toString());
                new myGetThead().start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    };
    Handler cookiemake=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg){
            try {
                cookie=msg.obj.toString();
                System.out.println(cookie);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    };

    public class Logout extends Thread
    {
        @Override
        public void run()
        {

            String url = "https://tazoapp.site/auth/logout";
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

                System.out.println(result);

                Intent intent = new Intent(context, Login.class);
                startActivity(intent);

            } catch(Exception e) {
                e.printStackTrace();
            }


        }

    }

}