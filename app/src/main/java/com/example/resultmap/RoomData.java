package com.example.resultmap;


import android.content.Context;

public class RoomData {


    private String id;
    private String name;
    private String userLimit;
    private String gender;
    private String startAt;
    private String start;
    private String result;
    private Context context;
    private int useridx;
    private String myRoomCheck;

    public RoomData(String id, String name, String userLimit, String gender,int useridx,
                    String startAt,String start,String result,Context context,String myRoomCheck) {
        this.name = name;
        this.userLimit = userLimit;
        this.gender = gender;
        this.startAt = startAt;
        this.start=start;
        this.result=result;
        this.id=id;
        this.context=context;
        this.useridx=useridx;
        this.myRoomCheck=myRoomCheck;
    }



    public Context getContext(){
        return context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(String userLimit) {
        this.userLimit = userLimit;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public int getUseridx(){return useridx;}

    public String getmyRoomCheck(){return this.myRoomCheck;}

    public String getPlace() {

        return "출발지 : "+start+"      도착지 : "+result;

    }



    public void setId(String id)
    {
        this.id=id;
    }
    public String getId()
    {
        return id;
    }
}

