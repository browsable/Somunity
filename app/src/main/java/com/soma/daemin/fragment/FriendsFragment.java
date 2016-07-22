package com.soma.daemin.fragment;

/**
 * Created by hernia on 2016-07-14.
 */

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.soma.daemin.R;
import com.soma.daemin.common.My;
import com.soma.daemin.data.User;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.main.UserDetailActivity;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by user on 2016-06-14.
 */
public class FriendsFragment extends Fragment {

    public static final String USERS = "users";
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private AdView mAdView;
    private FirebaseRecyclerAdapter<String,MessageViewHolder> mFirebaseAdapter;
    public CircleImageView mImageView;
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messengerTextView;
        public TextView tvLogin;
        public CircleImageView messengerImageView;
        public LinearLayout btUser;

        public MessageViewHolder(View v) {
            super(v);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            tvLogin = (TextView) itemView.findViewById(R.id.tvLogin);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            btUser = (LinearLayout) itemView.findViewById(R.id.btUser);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_member,container,false);
        My.INFO.backKeyName ="FriendsFragment";
        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        //mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, MessageViewHolder>(
                String.class,
                R.layout.listitem_user,
                MessageViewHolder.class,
                fUtil.databaseReference.child(fUtil.getCurrentUserId())
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, String uId, int position) {
                final MessageViewHolder vHolder = viewHolder;
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                mImageView = viewHolder.messengerImageView;
                fUtil.databaseReference.child(USERS).child(uId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        vHolder.messengerTextView.setText(user.getuName());
                        String eTime = user.getEndTime();
                        String sTime = user.getStartTime();
                        if(eTime!=null&&sTime!=null) {
                            if (Long.parseLong(eTime) - Long.parseLong(sTime) < 0) {
                                vHolder.tvLogin.setTextColor(getResources().getColor(R.color.colorPrimary));
                            }else{
                                vHolder.tvLogin.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(eTime)));
                            }
                        }else{
                            vHolder.tvLogin.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        final String uId = user.getuId();
                        if(user.getThumbPhotoURL()==null){
                            vHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    R.drawable.ic_account_circle_black_36dp));
                        }else{
                            try{
                                Glide.with(getActivity())
                                        .load(user.getThumbPhotoURL())
                                        .into(vHolder.messengerImageView);
                            }catch(NullPointerException e){}
                        }
                        vHolder.btUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getActivity(), UserDetailActivity.class);
                                i.putExtra("uId",uId);
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                try {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.nav_friends) + " (" + mFirebaseAdapter.getItemCount() + ")");
                }catch (NullPointerException e){};
            }
        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        return rootView;
    }
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if(mImageView!=null)
            Glide.clear(mImageView);
        super.onDestroy();

    }
}
