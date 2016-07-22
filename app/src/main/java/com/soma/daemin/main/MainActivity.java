package com.soma.daemin.main;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.soma.daemin.R;
import com.soma.daemin.auth.SignInActivity;
import com.soma.daemin.common.BackPressCloseHandler;
import com.soma.daemin.common.My;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.fragment.CalendarFragment;
import com.soma.daemin.fragment.ChatFragment;
import com.soma.daemin.fragment.FriendsFragment;
import com.soma.daemin.fragment.MainFragment;
import com.soma.daemin.fragment.MemberFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 1;
    private BackPressCloseHandler backPressCloseHandler;
    private String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3586590119792360~9039100136");
        SharedPreferences pref = getSharedPreferences("USERINFO", MODE_PRIVATE);
        My.INFO.loginType = pref.getInt("loginType", 0);
        if (My.INFO.loginType == 1) FacebookSdk.sdkInitialize(getApplicationContext());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new MainFragment()).commit();
        backPressCloseHandler = new BackPressCloseHandler(this);
        myId = fUtil.getCurrentUserId();
        fUtil.databaseReference.child("users").child(myId).child("startTime").setValue(System.currentTimeMillis()+"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fUtil.databaseReference.child("users").child(myId).child("endTime").setValue(System.currentTimeMillis()+"").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent i = new Intent(MainActivity.this, UserDetailActivity.class);
                i.putExtra("uId", fUtil.getCurrentUserId());
                startActivity(i);
                return true;
            case R.id.sign_out_menu:
                fUtil.firebaseAuth.signOut();
                if(My.INFO.loginType==0)
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                else if(My.INFO.loginType==1)
                    LoginManager.getInstance().logOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            case R.id.invite_menu:
                sendInvitation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
            }
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fm = getFragmentManager();
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_home:
                My.INFO.backKeyName ="MainFragment";
                fm.beginTransaction().replace(R.id.content_frame,new MainFragment()).commit();
                break;
            case R.id.nav_chat:
                My.INFO.backKeyName ="ChatFragment";
                fm.beginTransaction().replace(R.id.content_frame,new ChatFragment()).commit();
                break;
            case R.id.nav_member:
                My.INFO.backKeyName ="MemberFragment";
                fm.beginTransaction().replace(R.id.content_frame,new MemberFragment()).commit();
                break;
            case R.id.nav_friends:
                My.INFO.backKeyName ="FriendsFragment";
                fm.beginTransaction().replace(R.id.content_frame,new FriendsFragment()).commit();
                break;
            case R.id.nav_calendar:
                My.INFO.backKeyName ="CalendarFragment";
                fm.beginTransaction().replace(R.id.content_frame,new CalendarFragment()).commit();
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backPressCloseHandler.onBackPressed(My.INFO.backKeyName);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
