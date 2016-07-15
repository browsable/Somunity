
package com.soma.daemin.data;

public class User {
    private String uName;
    private String fullPhotoURL;
    private String thumbPhotoURL;
    private String uId;
    private String startTime;
    private String endTime;
    private String study;
    private String career;

    public User() {

    }
    public User(String fullPhotoURL, String thumbPhotoURL) {
        this.fullPhotoURL = fullPhotoURL;
        this.thumbPhotoURL = thumbPhotoURL;
    }
    public User(String uName, String uId, String fullPhotoURL, String thumbPhotoURL) {
        this.uId = uId;
        this.uName = uName;
        this.fullPhotoURL = fullPhotoURL;
        this.thumbPhotoURL = thumbPhotoURL;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getFullPhotoURL() {
        return fullPhotoURL;
    }

    public void setFullPhotoURL(String fullPhotoURL) {
        this.fullPhotoURL = fullPhotoURL;
    }

    public String getThumbPhotoURL() {
        return thumbPhotoURL;
    }

    public void setThumbPhotoURL(String thumbPhotoURL) {
        this.thumbPhotoURL = thumbPhotoURL;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }
}
