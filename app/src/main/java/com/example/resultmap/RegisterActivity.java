package com.example.resultmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.kakao.auth.ISessionCallback;
//import com.kakao.auth.Session;
//import com.kakao.network.ErrorResult;
//import com.kakao.usermgmt.UserManagement;
//import com.kakao.usermgmt.callback.MeV2ResponseCallback;
//import com.kakao.usermgmt.response.MeV2Response;
//import com.kakao.util.exception.KakaoException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

//    private RequestQueue requestQueue;
    private EditText et_name, et_nickname, et_email, et_passwd, et_passwd_confirm, et_gender;
    private Button btn_submit;
    private RadioGroup rg_gender;
    private RadioButton rb_male, rb_female;
    private String gender="";
    private Context context;
    private String email;

    //<=====================================카카오========================================================================================>

//    private ISessionCallback mSessionCallback;

//    public void intent(){
//        Intent intent = new Intent(this, EmailConfirm.class);
//        startActivity(intent);
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = this;

        btn_submit = (Button) findViewById(R.id.btn_submit);
        et_name = (EditText) findViewById(R.id.et_name);
        et_nickname = (EditText) findViewById(R.id.et_nickname);
        et_passwd = (EditText) findViewById(R.id.et_passwd);
        et_email = (EditText) findViewById(R.id.et_email);
        rg_gender = (RadioGroup) findViewById(R.id.radioGroup);
        et_passwd_confirm = (EditText) findViewById(R.id.et_passwd_confirm);



        rb_male = (RadioButton) findViewById(R.id.rb_male);
        rb_female = (RadioButton) findViewById(R.id.rb_female);




        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //이메일형식체크
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches())
            {
                Toast.makeText(RegisterActivity.this,"이메일 형식이 아닙니다",Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_passwd.getText().toString().length() == 0){
                Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                et_passwd.requestFocus();
                return;
            }

            if(et_passwd.getText().toString().length() < 3){
                Toast.makeText(RegisterActivity.this, "비밀번호는 4자리 이상 이어야 합니다.", Toast.LENGTH_SHORT).show();
                et_passwd.requestFocus();
                return;
            }


            if(!et_passwd_confirm.getText().toString().equals(et_passwd.getText().toString())){
                Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                et_passwd_confirm.requestFocus();
                return;
            }

            if(et_name.getText().toString().length() == 0){
                Toast.makeText(RegisterActivity.this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
                et_name.requestFocus();
                return;
            }

            if(et_nickname.getText().toString().length() == 0){
                Toast.makeText(RegisterActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
                et_nickname.requestFocus();
                return;
            }









                RadioButton rd = (RadioButton) findViewById(rg_gender.getCheckedRadioButtonId());
                gender = rd.getText().toString();

                if(gender.equals("남자"))
                    gender="male";
                else
                    gender="female";

                Thread checkEmail = new CheckEmail();
                checkEmail.start();

            }
        });


//


    }

    public class CheckEmail extends Thread
    {
        @Override
        public void run()
        {
            Log.d("senderThread","senderThread start");

            OkHttpClient client = new OkHttpClient();

            email = et_email.getText().toString();
            RequestBody formBody = new FormBody.Builder()
                    .add("nickname",et_nickname.getText().toString())
                    .add("email", email)
                    .add("password",et_passwd.getText().toString())
                    .add("gender",gender)
                    .build();



            okhttp3.Request request = new Request.Builder()
                    .url("https://tazoapp.site/auth/signup")
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

                    if(response.code() == 201)
                    {
                        Intent intent1 = new Intent(context, Emailconfirm.class);
                        intent1.putExtra("email",email);
                        startActivity(intent1);
                        finish();
                    }
                }
            });




        }

    }
}