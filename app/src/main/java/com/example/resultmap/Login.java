package com.example.resultmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    private EditText et_email, et_passwd;
    private Button btn_register, login_button;
    URL url;
    private String email;
    private String password;
    private Message msg;
    private Context context;
    private String setCookie;
    private long backKeyPressedTime = 0;
    private Toast toast;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        context = this;

        Thread checkLogin = new CheckLogin();
        checkLogin.start();



        et_email = findViewById(R.id.et_email);
        et_passwd = findViewById(R.id.et_passwd);

        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = et_email.getText().toString();
                password = et_passwd.getText().toString();

                Thread checkEmail = new CheckEmail();
                checkEmail.start();


            }
        });

    }

    public class CheckEmail extends Thread
    {
        @Override
        public void run()
        {

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("password",password)
                    .add("email", email)
                    .build();



            Request request = new Request.Builder()
                    .url("https://tazoapp.site/auth/login")
                    .post(formBody)
                    .build();

//                Response response = client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    System.out.println("연결 실패");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    System.out.println("연결 성공");
                    System.out.println(response);
                    String shard = "file";


                    if(response.code() == 200)
                    {
                        OkHttpClient client2 = new OkHttpClient();
                        setCookie = response.header("set-cookie");
                        Request request2 = new Request.Builder()
                                .addHeader("cookie", setCookie)
                                .url("https://tazoapp.site/auth")
                                .build();
                        Response response2 = client2.newCall(request2)
                                .execute();


                        JsonParser jsonParser = new JsonParser(response2.body().string());
                        System.out.println(jsonParser.getStatus());
                        if (jsonParser.getStatus().equals("1")) {

                            SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("cookie",response.header("set-cookie"));
                            editor.commit();
                            msg = handler.obtainMessage();
                            msg.what=1;

                            handler.sendMessage(msg);
                        } else
                        {
                            msg = handler.obtainMessage();
                            msg.what=2;

                            handler.sendMessage(msg);
                        }



                    }
                }
            });




        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){

            if(msg.what==1)
            {
                Intent intent2 = new Intent(context,MainActivity.class);
                startActivity(intent2);
            } else if(msg.what==2)
            {
                Intent intent2 = new Intent(context,Emailconfirm.class);
                intent2.putExtra("email",email);
                intent2.putExtra("setCookie",setCookie);
                startActivity(intent2);
            }



        }
    };


    public class CheckLogin extends Thread
    {
        @Override
        public void run()
        {
            String shard = "file";
            String url = "https://tazoapp.site/auth";
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

                if(!result.equals("null"))
                {
                    Log.d("bug_login",result);

                    Intent intent = new Intent(context,MainActivity.class);
                    startActivity(intent);
                }



            } catch(Exception e) {
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


}