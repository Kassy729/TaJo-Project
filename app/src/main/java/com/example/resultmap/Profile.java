package com.example.resultmap;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profile extends AppCompatActivity  {

    RequestQueue requestQueue;
    boolean name_check=true;
    private Toast toast;
    TextView id_view,email_view,gender_view,shadow;
    EditText nickname_view;
    ImageView photo;
    Button photoChang_btn,nameChang_btn,privius_btn;
    int id;
    String email,nickname,imageURL,gender;
    JSONObject myJson;
    Message msg;

    String cookie;
    Message cookieMgs;
    Spinner spinner;
    DrawerLayout drawerLayout2;

    View nickname_change_view;

    OkHttpClient client;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        Thread checkLogin = new CheckLogin();
        checkLogin.start();

        setContentView(R.layout.sub_profile);

        nickname_view=findViewById(R.id.nickname_view);
        photo=findViewById(R.id.photo);


        email_view=findViewById(R.id.email_view);



        photoChang_btn=findViewById(R.id.photoChang_btn);
        photoChang_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name_check){
                    checkSelfPermission();

                    Intent intent=new Intent();
//                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);//구글 갤러리
                    intent.setType("image/*");

                    intent.setAction(intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,101);
                }
                else{
                    Toast.makeText(Profile.this,"닉내임설정을 완료해주세요",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });



        nameChang_btn=findViewById(R.id.nameChang_btn);
        nameChang_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name_check){
                    nickname_view.setEnabled(true);
                    nameChang_btn.setText("완료");
                    nickname_view.requestFocus();
                    name_check=false;
                }else{
                    String str=nickname_view.getText().toString();
                    client=new OkHttpClient();
                    RequestBody Body=new FormBody.Builder().add("nickname",str).build();
                    Request request=new Request.Builder().url("https://tazoapp.site/user/nickname")
                            .addHeader("cookie",cookie)
                            .patch(Body)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.println("연결실패");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            System.out.println("연결성공");
                            System.out.println(response.body().string());
                            System.out.println(response);
                            Thread checkLogin = new CheckLogin();
                            checkLogin.start();

                        }
                    });
                    nickname_view.setEnabled(false);
                    Toast.makeText(Profile.this,"닉내임이 성공적으로 바뀌었습니다",Toast.LENGTH_LONG).show();
                    nameChang_btn.setText("바꾸기");
                    name_check=true;

                }

            }
        });
        privius_btn=findViewById(R.id.privius_btn);
        privius_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name_check){
                    finish();
                }
                else {
                    Toast.makeText(Profile.this,"닉내임 변경을 완료해주세요",Toast.LENGTH_LONG).show();
                    return;
                }

            }
        });

    }


    class myGetThead extends Thread{
        StringBuilder sb;
        public void run() {
            try {
                URL url = new URL("https://tazoapp.site/users/test");
//                URL url = new URL(user);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET"); //전송방식
//                    connection.setDoOutput(true);       //데이터를 쓸 지 설정
                connection.setDoInput(true);        //데이터를 읽어올지 설정

                InputStream is = connection.getInputStream();
                sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                String result;
                while((result = br.readLine())!=null){
                    sb.append(result+"\n");
                }
                try {
                    JSONObject temp=new JSONObject(sb.toString());
                }
                catch (Exception e){

                }
                System.out.println(sb.toString());
                msg=myhand.obtainMessage();
                msg.what=1;//구분
                msg.obj= sb.toString();
                myhand.sendMessage(msg);
                System.out.println(sb);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    Handler myhand=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject json2=new JSONObject((msg.obj.toString()));



//             id=json2.getInt("id");
//             id_view.setText(nickname);

                email=json2.getString("email");
                email_view.setText("email : "+email);

                nickname=json2.getString("nickname");
                nickname_view.setText(nickname);

                gender=json2.getString("gender");


                imageURL=json2.getString("image");


                spinner=findViewById(R.id.spinner);
                String hangul="";
                if (gender.equals("male")){
                    hangul="남자만";
                }else{
                    hangul="여자만";
                }

                String[] items={"상관없음",hangul};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        Profile.this,R.layout.simple_spinner_item2, items
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // 스피너에 어댑터 설정
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println(position);
                        String genderResult="none";
                        if (position==1){
                            genderResult=gender;
                        }
                        else {
                            genderResult="none";
                        }
                        String shard = "file";
                        SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("genderResult",genderResult);
                        editor.commit();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        System.out.println("클릭");
                    }
                });


                //이거 먼저 해보기
                if (imageURL==null||imageURL.equals("null")||imageURL.equals("")) {
                    imageURL="https://tazoapp.site/placeholder-profile.png";
                }

                System.out.println("adfafadfda" + imageURL);
                Glide.with(Profile.this).load(imageURL).into(photo);
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    };


    @Override
    public void onBackPressed() {
        if (name_check) {
            finish();
        }
        else {
            toast = Toast.makeText(this,"설정을 완료해 주세요",Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void checkSelfPermission(){
        String temp="";
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
            temp+=Manifest.permission.READ_EXTERNAL_STORAGE+" ";

            if(TextUtils.isEmpty(temp)==false){
                ActivityCompat.requestPermissions(this,temp.trim().split(" "),1);
            }
            else{
                Toast.makeText(this,"권환을 모두 허용",Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                System.out.println("권한허용");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101&&resultCode==RESULT_OK){
//            System.out.println(data.getData().getPath());

            String img_path = getPath(this,data.getData());
            System.out.println(img_path);
            client=new OkHttpClient();
            goSend(img_path);

        }
    }
    @Nullable
    public static String getPath(@NonNull Context context, @NonNull Uri uri)
    {
        final ContentResolver contentResolver = context.getContentResolver();

        if(contentResolver == null)
        {
            return null;
        }

        String filePath = context.getApplicationInfo().dataDir + File.separator + System.currentTimeMillis();

        File file = new File(filePath);

        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if(inputStream == null)
                return null;

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;while((len = inputStream.read(buf))
            > 0)
            {
                outputStream.write(buf,0,len);
            }

            outputStream.close();
            inputStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return file.getAbsolutePath();

    }
    private void goSend(String path)
    {
        System.out.println("보냈음 : "+path);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image","image.jpg",RequestBody.create(MultipartBody.FORM, new File(path)))
                .build();

        Request request = new Request.Builder()
                .addHeader("cookie",cookie)
                .url("https://tazoapp.site/user/image")
                .patch(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
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
                String result = response.toString();

                System.out.println(result);

                Thread checkLogin = new CheckLogin();
                checkLogin.start();

            }
        });
    }

    public class CheckLogin extends Thread
    {
        @Override
        public void run()
        {
            System.out.println("프로필");
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

                cookieMgs=Logincheck.obtainMessage();
                cookieMgs.what=3;
                cookieMgs.obj=setCookie;
                Logincheck.sendMessage(cookieMgs);


                String result = response.body().string();
                System.out.println("result : " + result);
                if(result.equals("null")){
                    System.out.println("adsfasdfasdfafdsfasdf");
                    Intent intent = new Intent(context,Login.class);
                    startActivity(intent);
                    finish();
                }
                msg=myhand.obtainMessage();
                msg.what=1;//구분
                msg.obj= result;
                myhand.sendMessage(msg);

            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }
    Handler Logincheck=new Handler(){
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


}