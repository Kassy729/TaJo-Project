package com.example.resultmap;


public class ChattingData {

    private String profile;
    private String nickName;
    private String chattingText;
    private String myName;
    private int inOut;
    private int imgCheck;

    public ChattingData(String profile, String nickName, String chattingText, String myName, int inOut, int imgCheck) {
        this.profile = profile;
        this.nickName = nickName;
        this.chattingText = chattingText;
        this.myName = myName;
        this.inOut = inOut;
        this.imgCheck=imgCheck;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getChattingText() {
        return chattingText;
    }

    public void setChattingText(String chattingText) {
        this.chattingText = chattingText;
    }

    public void setMyName(String myName) { this.myName = myName; }

    public String getMyName() { return myName; }

    public void setInOut(int inOut) { this.inOut = inOut; }

    public int getInOut() { return inOut; }

    public void setImgCheck(int imgCheck) { this.imgCheck = imgCheck; }

    public int getImgCheck() { return imgCheck; }
}

