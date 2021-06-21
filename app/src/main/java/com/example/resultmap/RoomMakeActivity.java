package com.example.resultmap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RoomMakeActivity extends AppCompatActivity {
    Message msg;
    Message cookieMsg,msg2;
    String nickname,gender,start,result;
    EditText roomName;
    String gerderRimit="none";
    String userLimit="2";
    Spinner spinner,spinner2;
    TextView orderName,startPlace,resultPlace,startTime;
    ImageButton makeStart;
    int myId;
    String cookie;

    TimePicker timePicker;
    private Context  context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_make);


        context = this;




        orderName=findViewById(R.id.orderName);





        startPlace=findViewById(R.id.startPlace);
        resultPlace=findViewById(R.id.resultPlace);

        Intent intent=getIntent();
        start=(String)intent.getExtras().get("start");
        result=(String)intent.getExtras().get("result");
        myId=Integer.parseInt(intent.getExtras().get("myId").toString());

        startPlace.setText(start);
        resultPlace.setText(result);

        Date date=new Date(System.currentTimeMillis());
        System.out.println(date);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

        StringTokenizer st=new StringTokenizer(sdf.format(date),"-");

        startTime=findViewById(R.id.startTime);
        startTime.append(st.nextToken()+"월 "+st.nextToken()+"일 (오늘)");

        timePicker=findViewById(R.id.timePicker);

        roomName=findViewById(R.id.roomName);


        spinner2=findViewById(R.id.spinner2);
        String[] items2={"2명","3명","4명"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items2
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 스피너에 어댑터 설정
        spinner2.setAdapter(adapter2);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userLimit=String.valueOf(position+2);
                System.out.println("클릭");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                System.out.println("클릭");
            }
        });


        makeStart=findViewById(R.id.makeStart);
        makeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (roomName.getText().length()<3){
                    Toast.makeText(getApplicationContext(),"방이름은 2글자 이상을로 해주세요",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                System.out.println(start+" "+result);

                int hourchech=timePicker.getCurrentHour();
                int mintuecheck=timePicker.getCurrentMinute();

                Date date=new Date(System.currentTimeMillis());

                if (hourchech<date.getHours()){
                    Toast.makeText(getApplicationContext(),"현재시간 이후로 설정 해주세요",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    if (hourchech==date.getHours()&&mintuecheck<date.getMinutes()){
                        Toast.makeText(getApplicationContext(),"현재시간 이후로 설정 해주세요",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR,timePicker.getCurrentHour());
                        calendar.set(Calendar.MINUTE,timePicker.getCurrentMinute());
                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                        String timeresult=simpleDateFormat.format(calendar.getTime());

                            OkHttpClient client = new OkHttpClient.Builder()
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .writeTimeout(30,TimeUnit.SECONDS)
                                    .readTimeout(30,TimeUnit.SECONDS)
                                    .build();
                            RequestBody formBody =new FormBody.Builder()
                                    .add("name", roomName.getText().toString())
                                    .add("userLimit",userLimit)
                                    .add("gender",gerderRimit)
                                    .add("originName",start)
                                    .add("destinationName",result)
                                    .add("startAt",timeresult)
                                    .build();
                            System.out.println(formBody.toString());
                            Request request = new Request.Builder()
                                    .addHeader("cookie",cookie)
                                    .url("https://tazoapp.site/rooms")
                                    .post(formBody)
                                    .build();


                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.getStackTrace();
                                    System.out.println("포트스 에러"+e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    System.out.println("aaaa"+response.code());
                                    System.out.println("bbbbb"+response.message());
                                    System.out.println("sssss"+response.body().string());
                                    new myGetThead().start();
                                }
                            });


                    }
                }



            }

        });
        Thread checkLogin=new CheckLogin();
        checkLogin.start();
    }
    Handler myhand=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject json2=new JSONObject((msg.obj.toString()));

                nickname=json2.getString("nickname");
                orderName.setText(nickname);

                gender=json2.getString("gender");

                spinner=findViewById(R.id.spinner);
                String hangul="";
                if (gender.equals("male")){
                    hangul="남자만";
                }else{
                    hangul="여자만";
                }

                String[] items={"상관없음",hangul};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        RoomMakeActivity.this, android.R.layout.simple_spinner_item, items
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // 스피너에 어댑터 설정
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println(position);
                        if (position==1){
                            gerderRimit=gender;
                        }
                        else {
                            gerderRimit="none";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        System.out.println("클릭");
                    }
                });


            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    };

    Handler cookiemake=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg){

//                JSONObject json=new JSONObject(msg.obj.toString());
            cookie=msg.obj.toString();
            System.out.println("쿠키 받아라!"+cookie);


        }

    };

    public class CheckLogin extends Thread
    {
        @Override
        public void run()
        {

            String url = "https://tazoapp.site/auth";
            String shard="file";
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                String setCookie = sharedPreferences.getString("cookie","");
                Log.d("세션",setCookie);

                cookieMsg=cookiemake.obtainMessage();
                cookieMsg.what=1;//구분
                cookieMsg.obj= setCookie;
                cookiemake.sendMessage(cookieMsg);

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
                }else{
                    msg=myhand.obtainMessage();
                    msg.what=1;//구분
                    msg.obj= result;
                    myhand.sendMessage(msg);

                }



            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }
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
    }Handler handler2=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                JSONArray array=new JSONArray(msg.obj.toString());

                for (int i=0;i<array.length();i++){
                    JSONObject jsonObject2=(JSONObject)array.get(i);
                    JSONArray jsonArray=(JSONArray)jsonObject2.get("Members");
                    for (int j=0;j<jsonArray.length();j++){
                        JSONObject temp=(JSONObject)jsonArray.get(j);
                        if (Integer.parseInt(temp.get("id").toString())==myId){
                            Intent intent=new Intent(RoomMakeActivity.this,Chatting.class);
                            intent.putExtra("rommId",jsonObject2.get("id").toString());
                            intent.putExtra("cookie",cookie);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(RoomMakeActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }



}