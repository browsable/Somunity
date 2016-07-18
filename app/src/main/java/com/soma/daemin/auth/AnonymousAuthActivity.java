/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soma.daemin.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soma.daemin.R;
import com.soma.daemin.data.User;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.main.MainActivity;

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
public class AnonymousAuthActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "AnonymousAuth";
    private ProgressBar bar;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_auth);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        // [START initialize_auth]
        mAuth = fUtil.firebaseAuth;
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fUtil.firebaseUser = firebaseAuth.getCurrentUser();
                if (fUtil.firebaseUser != null) {
                    // My is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + fUtil.firebaseUser.getUid());
                } else {
                    // My is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                bar.setVisibility(View.GONE);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]

        // Fields

        // Click listeners
        findViewById(R.id.button_anonymous_sign_in).setOnClickListener(this);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    private void signInAnonymously() {
        bar.setVisibility(View.VISIBLE);
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(AnonymousAuthActivity.this, getString(R.string.auth_sigin_failed),
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            finish();
                            SharedPreferences pref = getSharedPreferences("USERINFO", MODE_PRIVATE);
                            pref.edit().putInt("loginType",3).apply();

                            fUtil.firebaseUser = task.getResult().getUser();
                            fUtil.getUserRef().child(fUtil.firebaseUser.getUid()).setValue(new User("anonymous",fUtil.getCurrentUserId(),FirebaseInstanceId.getInstance().getToken()));
                            startActivity(new Intent(AnonymousAuthActivity.this, MainActivity.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }

                        // [START_EXCLUDE]
                        bar.setVisibility(View.GONE);
                        // [END_EXCLUDE]
                    }
                });
        // [END signin_anonymously]
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_anonymous_sign_in:
                signInAnonymously();
                break;
        }
    }
}
