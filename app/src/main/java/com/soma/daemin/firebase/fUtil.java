package com.soma.daemin.firebase;/*
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class fUtil {
    public static FirebaseAuth firebaseAuth;
    public static FirebaseUser firebaseUser;
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static FirebaseRemoteConfig firebaseRemoteConfig;
    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;
    public static FirebaseMessaging firebaseMessaging;
    //Auth
    public static void FirebaseInstanceInit(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseMessaging = FirebaseMessaging.getInstance();
    }
    public static String getCurrentUserName() {
        FirebaseUser user = firebaseUser;
        if (user != null) {
            return user.getDisplayName();
        }else
            return "anonymous";
    }
    public static String getCurrentUserId() {
        FirebaseUser user = firebaseUser;
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    //RealTimeDatabase
    public static DatabaseReference getUserRef() {
        return databaseReference.child("users");
    }
    public static DatabaseReference getPictureRef() {
        return databaseReference.child("picture");
    }
    public static DatabaseReference getPictureDetailRef() {
        return databaseReference.child("picture-urls");
    }

    //Remote Config
    public static FirebaseRemoteConfig getRemoteConfig() {
        return firebaseRemoteConfig;
    }

    //Storage
    public static StorageReference getStorePictureRef() {
        return storageReference.child("photo/picture/");
    }
    public static StorageReference getStoreFullProfileRef() {
        return storageReference.child("photo/profile/full");
    }
    public static StorageReference getStoreThumbProfileRef() {
        return storageReference.child("photo/profile/thumb");
    }
}
