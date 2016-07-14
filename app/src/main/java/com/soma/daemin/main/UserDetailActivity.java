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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.soma.daemin.R;
import com.soma.daemin.data.User;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.fragment.NewPicUploadTaskFragment;

import java.util.List;

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
    private TextView tvPos, tvKo, tvEn;
    private String bibleNum;
    private ProgressBar bar;
    private Button btAddProfile;
    private static final String[] cameraPerms = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        final String uId = getIntent().getStringExtra("uId");
        mGlideRequestManager = Glide.with(UserDetailActivity.this);
        currentUserId = fUtil.getCurrentUserId();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(fUtil.getCurrentUserName());
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        btAddProfile = (Button) findViewById(R.id.btAddProfile);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tvPos = (TextView) findViewById(R.id.tvPos);
        tvKo = (TextView) findViewById(R.id.tvKo);
        tvEn = (TextView) findViewById(R.id.tvEn);
        try {
            fUtil.databaseReference.child("users").child(uId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        mGlideRequestManager.load(user.getThumbPhotoURL())
                                .placeholder(R.drawable.ic_account_circle_black_36dp)
                                .dontAnimate()
                                .fitCenter()
                                .into(ivProfile);
                        collapsingToolbar.setTitle(user.getuName());
                        /*fUtil.databaseReference.child("bible").child(bibleNum).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    HashMap<String, String> hash = (HashMap) dataSnapshot.getValue();
                                    tvPos.setText(hash.get("본문"));
                                    tvKo.setText(hash.get("한글"));
                                    tvEn.setText(hash.get("영어"));
                                }catch(NullPointerException e){
                                    Random r = new Random();
                                    String bibleNum = String.valueOf(r.nextInt(239));
                                    fUtil.databaseReference.child("bible").child(bibleNum).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            try {
                                                HashMap<String, String> hash = (HashMap) dataSnapshot.getValue();
                                                tvPos.setText(hash.get("본문"));
                                                tvKo.setText(hash.get("한글"));
                                                tvEn.setText(hash.get("영어"));
                                            }catch(NullPointerException e){
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });*/
                    } else {
                        collapsingToolbar.setTitle(fUtil.getCurrentUserName());
                        ivProfile.setBackgroundResource(R.drawable.ic_account_circle_black_36dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
        }


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
                Intent i = new Intent(UserDetailActivity.this,DetailActivity.class);
                i.putExtra("uId",uId);
                startActivity(i);
            }
        });
        if(fUtil.getCurrentUserId().equals(uId)) {
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
}
