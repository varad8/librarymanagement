package com.vrnitsolution.librarymanagement.model;

public class User {
    private String username;
    private String useremail;
    private String  userrollno;
    private String profileImageUrl;
    private String accountType;
    private String fcmToken;
    private String librarycardid;
    private String address;
    private String mobileno;

    private String rollNo;

    private String userid;

    public User() {
    }

    public User(String username, String useremail, String userrollno, String profileImageUrl, String accountType, String fcmToken, String librarycardid, String address, String mobileno, String rollNo, String userid) {
        this.username = username;
        this.useremail = useremail;
        this.userrollno = userrollno;
        this.profileImageUrl = profileImageUrl;
        this.accountType = accountType;
        this.fcmToken = fcmToken;
        this.librarycardid = librarycardid;
        this.address = address;
        this.mobileno = mobileno;
        this.rollNo = rollNo;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getUserrollno() {
        return userrollno;
    }

    public void setUserrollno(String userrollno) {
        this.userrollno = userrollno;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getLibrarycardid() {
        return librarycardid;
    }

    public void setLibrarycardid(String librarycardid) {
        this.librarycardid = librarycardid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobileno() {
        return mobileno;
    }

    public void setMobileno(String mobileno) {
        this.mobileno = mobileno;
    }
}
