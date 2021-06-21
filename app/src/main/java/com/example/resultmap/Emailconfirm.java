package com.example.resultmap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.identity.CipherSuiteNotSupportedException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Emailconfirm extends AppCompatActivity {

    private String email;

    private Button sendPassword;
    private Context context;
    private Message msg;
    private String setCookie;
    private EditText et_comfirm_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailconfirm);

        System.out.println("***********************");
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        setCookie = intent.getStringExtra("setCookie");

        System.out.println(email);
        sendPassword = (Button) findViewById(R.id.sendPassword);
        et_comfirm_number = (EditText) findViewById(R.id.et_comfirm_number);


        context = this;
        sendPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread check = new Check();
                check.start();
            }
        });
    }

    public class Check extends Thread
    {
        @Override
        public void run()
        {


            OkHttpClient client = new OkHttpClient();

            System.out.println("token : " + et_comfirm_number.getText().toString() );
            RequestBody formBody = new FormBody.Builder()
                    .add("token",et_comfirm_number.getText().toString())
                    .add("email", email)
                    .build();


            System.out.println("setCookie : " + setCookie);
            okhttp3.Request request = new Request.Builder()
                    .url("https://tazoapp.site/auth/email")
                    .patch(formBody)
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
                    System.out.println(response.body().string());
                    if(response.code() == 200)
                    {

                        msg = handler.obtainMessage();
                        msg.what=2;
                        handler.sendMessage(msg);


                    }else
                    {
                        msg = handler.obtainMessage();
                        msg.what=1;
                        handler.sendMessage(msg);
                    }
                }
            });




        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){

            if(msg.what ==1)
            {
                Toast.makeText(Emailconfirm.this, "유효하지 않은 번호!", Toast.LENGTH_SHORT).show();
                Toast.makeText(Emailconfirm.this, "로그인 해서 다시입력해!", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(context,Login.class);
                startActivity(intent2);
            }else if(msg.what == 2)
            {
                String shard = "file";
                SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cookie",setCookie);
                editor.commit();
                Toast.makeText(Emailconfirm.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                Intent intent2 = new Intent(context, Login.class);
                startActivity(intent2);

                finish();
            }

        }
    };

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this,Login.class);
        startActivity(intent);
        finish();


    }



}