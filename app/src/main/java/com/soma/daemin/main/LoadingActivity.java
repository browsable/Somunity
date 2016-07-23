package com.soma.daemin.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.soma.daemin.R;
import com.soma.daemin.auth.SignInActivity;
import com.soma.daemin.common.CustomJSONObjectRequest;
import com.soma.daemin.common.MyVolley;
import com.soma.daemin.firebase.fUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by user on 2016-06-11.
 */
public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        fUtil.FirebaseInstanceInit();
        if(fUtil.firebaseUser==null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    Intent i = new Intent(LoadingActivity.this, SignInActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, 2200);
            return;
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    Intent i = new Intent(LoadingActivity.this, MainActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, 2200);
            analytics();
        }

    }


    public static final String CNT_UP = "http://52.192.204.226/cnt";

    public static void analytics() {
        CustomJSONObjectRequest rq = new CustomJSONObjectRequest(Request.Method.GET, CNT_UP, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("LoadingActivity",response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Response Error", error.toString());
            }
        });
        rq.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        MyVolley.getRequestQueue().add(rq);
    }
}
