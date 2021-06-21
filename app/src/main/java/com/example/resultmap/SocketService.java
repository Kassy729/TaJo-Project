package com.example.resultmap;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

//import io.socket.engineio.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SocketService extends Service {
    private Message msg;
    private Context context;

    private SocketThread socketThread;

    private boolean stopThread=true;

    private String roomNumber = "";
    String userID,roomOder;

    private Socket mSocket;

    public SocketService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LocalBroadcastManager.getInstance(this).registerReceiver(sBroadcastReceiver,
                new IntentFilter("socketClose"));
        Log.d("Boot1", "SocketService.onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Boot1", "SocketService.onStartCommand()");
        if (intent == null) return Service.START_STICKY;
        else Log.d("Boot1", "SocketService.onStartCommand().else");

        if ("startForground".equals(intent.getAction())) {
            Log.d("Boot2", "start getAction");
            roomNumber = intent.getStringExtra("roomNumber");

            String shard = "file";
            SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("roomNumber",roomNumber);
            editor.commit();

            System.out.println("**************" + roomNumber + "********************");
            startForgroundService();
        }


        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("Boot1", "SocketService.onDestory()");
    }



    private void startForgroundService() {
        context=this;
        Log.d("Boot2", "startForegroundService starting point");
        System.out.println("------------------------------------1");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("채팅 방 활성화...");
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        System.out.println("------------------------------------2");

        Intent notificationIntent = new Intent(this, Chatting.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        System.out.println("------------------------------------3");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default", "socket", NotificationManager.IMPORTANCE_HIGH));
        }
        System.out.println("------------------------------------4");



        startForeground(1, builder.build());


        System.out.println("------------------------------------5");
        String shard="file";
        SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
        String roomNumber = sharedPreferences.getString("roomNumber","");

        try{
            mSocket = IO.socket("https://tazoapp.site/ws-room-"+roomNumber);
            mSocket.connect();
            System.out.println("연결안됨~~~~~~~~`"+mSocket.connected()+" ");
            System.out.println("------------------------------------6");



            mSocket.on("chat", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.out.println("소캣 ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ");
                    try{
                        System.out.println("------------------------------------7");
                        String data = args[0].toString();
                        JSONParser jsonParser = new JSONParser();
                        Object obj = jsonParser.parse(data);
                        JSONObject jsonObject = (JSONObject) obj;

                        System.out.println("소캣 성공 성공"+jsonObject);


                        msg = handler.obtainMessage();
                        msg.what=1;
                        msg.obj=jsonObject;
                        handler.sendMessage(msg);

                    } catch(Exception e) {
                        e.printStackTrace();
                    }


                }
            });



            mSocket.on("destroyRoom", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    userID = "ok";
                    roomOder = "oj";
                    System.out.println("방 폭파" + userID + ", " + roomOder);
                    new CloseSocket().execute();

                }
            });

            mSocket.on("enterMember", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("않이여긴","오긴 왔어");
                    Intent intent = new Intent("change");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                }
            });

//            mSocket.on("leaveMember", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    Log.d("않이여긴","갔네 갔어");
//                    Intent intent = new Intent("change");
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//
//                }
//            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String content, userName, profileURL;

            if (msg.what == 1) {
                System.out.println("성공성공");
                JSONObject jsonStr = (JSONObject) msg.obj;
//              여기서 제이슨으로 파싱해서 쪼갬 *************************************************
                try {

                    content = (String) jsonStr.get("content");
                    JSONObject jsonUser = (JSONObject) jsonStr.get("User");
                    userName = (String) jsonUser.get("nickname");
                    profileURL = (String) jsonUser.get("image");
                    if(profileURL == null)
                        profileURL="https://tazoapp.site/placeholder-profile.png";
                    if(content!=null && content.contains("https://storage.googleapis.com/tazo-bucket/uploads"))
                        content = "[사진]";

                    Log.d("Boot2", userName);
                    if (content != null)
                        Log.d("Boot2", content);

                    if(! checkScreen().equals("Chatting"))
                    {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
                        Glide.with(getApplicationContext()).asBitmap().load(profileURL).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                builder.setLargeIcon(resource);

                            }


                        });
                        builder.setContentTitle(userName);
                        builder.setContentText(content);
                        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                        System.out.println("여기서 우선순위");
//                    builder.setPriority(Notification.PRIORITY_MAX);
                        builder.setPriority(NotificationCompat.PRIORITY_MAX);
                        builder.setDefaults(Notification.DEFAULT_VIBRATE);

                        Intent notificationIntent = new Intent(context, Chatting.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                        builder.setContentIntent(pendingIntent);



                        startForeground(1, builder.build());
                    } else {
                        System.out.println("여긴 채팅 방 안이야");
                        Intent intent = new Intent("chattingReceiver");
                        String str=jsonStr.toString();
                        intent.putExtra("jsonStr", str);


                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }









                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        }


    };

    public String checkScreen()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        ComponentName componentName = info.get(0).topActivity;
        String ActivityName = componentName.getShortClassName().substring(1);
        return ActivityName;
    }

    private BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            userID=intent.getStringExtra("userId");
            roomOder=intent.getStringExtra("roomId");

            new CloseSocket().execute();

        }
    };
    public class SocketThread extends Thread {

        private String roomNumber;
        public SocketThread(String roomNumber)
        {
            this.roomNumber=roomNumber;
        }
        @Override
        public void run() {

            try {
                mSocket = IO.socket("https://tazoapp.site/ws-room-"+roomNumber);
                mSocket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }


        }
    }

    public class CloseSocket extends AsyncTask<String, Void, Void> {
        @Override
        public Void doInBackground(String... params) {
            System.out.println("**************************");
            System.out.println("종료할꺼야");

            String shard = "file";
            String setCookie = "";
            SharedPreferences sharedPreferences1 = getSharedPreferences(shard,0);
            String roomNumber = sharedPreferences1.getString("roomNumber","");
            String  url = "https://tazoapp.site/rooms/" + roomNumber + "/member";


                SharedPreferences sharedPreferences = getSharedPreferences(shard,0);
            setCookie = sharedPreferences.getString("cookie","");


            System.out.println(setCookie);
            RequestBody requestBody = new FormBody.Builder()
                    .build();

            Request request = new Request.Builder()
                    .addHeader("cookie", setCookie)
                    .url(url)
                    .delete(requestBody)
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
                    System.out.println(response);

                    if(response.code() == 200)
                    {
                        mSocket.off("chat");
                        mSocket.off("destroyRoom");
                        mSocket.off("enterMember");
                        mSocket.off("leaveMember");

                        mSocket.disconnect();

                        mSocket.close();


                        Intent intent2 = new Intent("stopService");
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                    }
                }
            });




            return null;

        }
    }

}