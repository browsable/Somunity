package com.soma.daemin.firebase;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by user on 2016-06-14.
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * The Application's current Instance ID token is no longer valid
     * and thus a new one must be requested.
     */
    @Override
    public void onTokenRefresh() { //앱 설치 혹은 재설치 앱데이터 삭제시 호출되면서 fcm token 생성
    }
}
