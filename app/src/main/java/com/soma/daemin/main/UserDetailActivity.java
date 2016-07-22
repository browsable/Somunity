/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soma.daemin.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.soma.daemin.R;
import com.soma.daemin.common.CustomJSONObjectRequest;
import com.soma.daemin.common.MyVolley;
import com.soma.daemin.data.User;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.fragment.NewPicUploadTaskFragment;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class UserDetailActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks,
        NewPicUploadTaskFragment.TaskCallbacks {
    private final String TAG = "UserDetailActivity";
    private static final int THUMBNAIL_MAX_DIMENSION = 480;
    private static final int FULL_SIZE_MAX_DIMENSION = 960;
    private CircleImageView ivProfile;
    private static final int REQUEST_IMAGE_PIC = 1;
    private static final int REQUEST_IMAGE_UPLOAD = 3;
    public static final String TAG_TASK_FRAGMENT = "NewPicUploadTaskFragment";
    private String currentUserId;
    private Uri mFileUri;
    private Bitmap mResizedBitmap;
    private Bitmap mThumbnail;
    private NewPicUploadTaskFragment mTaskFragment;
    private static final int RC_CAMERA_PERMISSIONS = 102;
    public RequestManager mGlideRequestManager;
    private boolean imgDetail;
    private ProgressBar bar;
    private Button btAddProfile, btAddFriend;
    private TextView tvStudy, tvCareer, tvStartTime, tvEndTime;
    private static final String[] cameraPerms = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        imgDetail = false;
        final String uId = getIntent().getStringExtra("uId");
        mGlideRequestManager = Glide.with(UserDetailActivity.this);
        currentUserId = fUtil.getCurrentUserId();
        fUtil.databaseReference.child(currentUserId).child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if(dataSnapshot.getValue()!=null) {
                        //btAddFriend.setVisibility(View.INVISIBLE);//null인 경우만 제외
                    }
                } catch (NullPointerException e) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        btAddProfile = (Button) findViewById(R.id.btAddProfile);
        btAddFriend = (Button) findViewById(R.id.btAddFriend);
        tvStudy = (TextView) findViewById(R.id.tvStudy);
        tvCareer = (TextView) findViewById(R.id.tvCareer);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        if (uId.equals(currentUserId)) btAddFriend.setVisibility(View.INVISIBLE);

        try {
            fUtil.databaseReference.child("users").child(uId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        String study = user.getStudy();
                        String career = user.getCareer();
                        String startTime = user.getStartTime();
                        String endTime = user.getEndTime();
                        if (user.getThumbPhotoURL() == null) {
                            imgDetail = false;
                            collapsingToolbar.setTitle("");
                            ivProfile.setBackgroundResource(R.drawable.ic_account_circle_black_36dp);
                        } else {
                            mGlideRequestManager.load(user.getThumbPhotoURL())
                                    .placeholder(R.drawable.ic_account_circle_black_36dp)
                                    .dontAnimate()
                                    .fitCenter()
                                    .into(ivProfile);
                            imgDetail = true;
                        }
                        collapsingToolbar.setTitle(user.getuName());
                        if (study != null) tvStudy.setText(study);
                        if (career != null) tvCareer.setText(career);
                        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        if (startTime != null) {
                            String sTime = dayTime.format(new Date(Long.parseLong(startTime)));
                            tvStartTime.setText(sTime);
                        }
                        if (endTime != null) {
                            String eTime = dayTime.format(new Date(Long.parseLong(endTime)));
                            tvEndTime.setText(eTime);
                        }

                    } else {
                        collapsingToolbar.setTitle(fUtil.getCurrentUserName());
                        ivProfile.setBackgroundResource(R.drawable.ic_account_circle_black_36dp);
                        imgDetail = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    imgDetail = false;
                }
            });
        } catch (Exception e) {
            imgDetail = false;
        }
        btAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fUtil.databaseReference.child(currentUserId).child(uId).setValue(uId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        btAddFriend.setVisibility(View.INVISIBLE);
                    }
                });
                //친구추가푸시구현
                FcmPush("hi");
                /*fUtil.databaseReference.child("users").child(uId).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String token = (String) dataSnapshot.getValue();
                        FcmPush(token);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (NewPicUploadTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new NewPicUploadTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        Bitmap selectedBitmap = mTaskFragment.getSelectedBitmap();
        Bitmap thumbnail = mTaskFragment.getThumbnail();
        if (selectedBitmap != null) {
            ivProfile.setImageBitmap(selectedBitmap);
            mResizedBitmap = selectedBitmap;
        }
        if (thumbnail != null) {
            mThumbnail = thumbnail;
        }

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imgDetail) {
                    Intent i = new Intent(UserDetailActivity.this, DetailActivity.class);
                    i.putExtra("uId", uId);
                    startActivity(i);
                }
            }
        });
        if (fUtil.getCurrentUserId().equals(uId)) {
            btAddProfile.setVisibility(View.VISIBLE);
            btAddProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && !EasyPermissions.hasPermissions(UserDetailActivity.this, cameraPerms)) {
                        EasyPermissions.requestPermissions(this,
                                "사진 업로드를 위해 저장소에 접근합니다",
                                RC_CAMERA_PERMISSIONS, cameraPerms);
                        return;
                    }
                    if (currentUserId != null) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_IMAGE_PIC);
                    } else {
                        Toast.makeText(UserDetailActivity.this, getString(R.string.error_user_not_signed_in), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PIC:
                    mFileUri = data.getData();
                case REQUEST_IMAGE_UPLOAD:
                    bar.setVisibility(View.VISIBLE);
                    mTaskFragment.resizeBitmapWithPath(getPathFromUri(mFileUri), THUMBNAIL_MAX_DIMENSION);
                    mTaskFragment.resizeBitmapWithPath(getPathFromUri(mFileUri), FULL_SIZE_MAX_DIMENSION);
                    break;
            }
        }
    }

    public String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        return path;
    }

    @Override
    public void onDestroy() {
        // store the data in the fragment
        if (mResizedBitmap != null) {
            mTaskFragment.setSelectedBitmap(mResizedBitmap);
        }
        if (mThumbnail != null) {
            mTaskFragment.setThumbnail(mThumbnail);
        }
        super.onDestroy();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @Override
    public void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension) {
        if (resizedBitmap == null) {
            Log.e(TAG, "Couldn't resize bitmap in background task.");
            Toast.makeText(getApplicationContext(), "Couldn't resize bitmap.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMaxDimension == THUMBNAIL_MAX_DIMENSION) {
            mThumbnail = resizedBitmap;
        } else if (mMaxDimension == FULL_SIZE_MAX_DIMENSION) {
            mResizedBitmap = resizedBitmap;
            ivProfile.setImageBitmap(mResizedBitmap);
        }

        if (mThumbnail != null && mResizedBitmap != null) {
            ivProfile.setEnabled(true);
            StorageReference fullSizeRef = fUtil.getStoreFullProfileRef().child(currentUserId);
            StorageReference thumbnailRef = fUtil.getStoreThumbProfileRef().child(currentUserId);
            mTaskFragment.uploadProfile(mResizedBitmap, fullSizeRef, mThumbnail, thumbnailRef, "profile.jpg");
        }
    }

    @Override
    public void onPhotoUploaded(final String error, String fullURL, String thumbURL) {
        UserDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivProfile.setEnabled(true);
                bar.setVisibility(View.GONE);
                if (error != null) {
                    Toast.makeText(UserDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static final String PUSH_POST = "http://localhost:9000/fcm";
    public static final String KEY_SUCCESS ="success";

    public static void FcmPush(final String token) {
        CustomJSONObjectRequest rq = new CustomJSONObjectRequest(Request.Method.POST, PUSH_POST, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString(KEY_SUCCESS) != null) {
                                int success = Integer.parseInt(response.getString(KEY_SUCCESS));
                                if (success == 1) {
                                    Log.i("test",response.getString("message"));
                                }else {
                                    Log.i("test","Something went wrong.Please try again..");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Response Error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "x-www-form-urlencoded");
                return headers;
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                String body = "{\"registration_token\":\""+token
                        +"\"name\":\""+fUtil.getCurrentUserName()+"\"tag\":\"fcm\"}";
                return body.getBytes();
            }
        };
        MyVolley.getRequestQueue().add(rq);
    }
}
