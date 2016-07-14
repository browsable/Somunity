package com.soma.daemin.fragment;

/**
 * Created by hernia on 2016-07-14.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.soma.daemin.R;
import com.soma.daemin.common.My;
import com.soma.daemin.data.User;
import com.soma.daemin.firebase.fUtil;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by user on 2016-06-14.
 */
public class MemberFragment extends Fragment {

    public static final String USERS = "users";
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private AdView mAdView;
    private FirebaseRecyclerAdapter<User,MessageViewHolder> mFirebaseAdapter;
    private int memCnt;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_member,container,false);
        My.INFO.backKeyName ="MemberFragment";
        memCnt=0;
        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        //mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, MessageViewHolder>(
                User.class,
                R.layout.listitem_user,
                MessageViewHolder.class,
                fUtil.databaseReference.child(USERS)
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, User user, int position) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.nav_mem)+" ("+(++memCnt)+")");
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.messengerTextView.setText(user.getuName());
                if(user.getThumbPhotoURL()==null){
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_account_circle_black_36dp));
                }else{
                    Glide.with(getActivity())
                            .load(user.getThumbPhotoURL())
                            .into(viewHolder.messengerImageView);
                }
            }
        };
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

            }
        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
       /* Intent i = new Intent(getActivity(), UserDetailActivity.class);
        i.putExtra("uId", fUtil.getCurrentUserId());
        startActivity(i);*/

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
        super.onDestroy();
    }
}
