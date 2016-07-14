
package com.soma.daemin.data;

public class User {
    private String uName;
    private String fullPhotoURL;
    private String thumbPhotoURL;

    public User() {

    }
    public User(String fullPhotoURL, String thumbPhotoURL) {
        this.fullPhotoURL = fullPhotoURL;
        this.thumbPhotoURL = thumbPhotoURL;
    }
    public User(String uName, String fullPhotoURL, String thumbPhotoURL) {
        this.uName = uName;
        this.fullPhotoURL = fullPhotoURL;
        this.thumbPhotoURL = thumbPhotoURL;
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
}
