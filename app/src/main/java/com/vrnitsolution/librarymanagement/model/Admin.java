package com.vrnitsolution.librarymanagement.model;

public class Admin {
    private String fullname;
    private String mobileno;
    private String address;
    private String designation;
    private String staffid;
    private String email;
    private String profileImageUrl;

    private String accountType;
    private String fcmToken;


    public Admin() {
    }

    public Admin(String fullname, String mobileno, String address, String designation, String staffid, String email, String profileImageUrl, String accountType, String fcmToken) {
        this.fullname = fullname;
        this.mobileno = mobileno;
        this.address = address;
        this.designation = designation;
        this.staffid = staffid;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.accountType = accountType;
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getMobileno() {
        return mobileno;
    }

    public void setMobileno(String mobileno) {
        this.mobileno = mobileno;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStaffid() {
        return staffid;
    }

    public void setStaffid(String staffid) {
        this.staffid = staffid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
