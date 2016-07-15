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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.soma.daemin.data.MessageData;
import com.soma.daemin.firebase.fUtil;
import com.soma.daemin.main.UserDetailActivity;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by user on 2016-06-14.
 */
public class ChatFragment extends Fragment {
    public static final String MESSAGES_CHILD = "messages";
    private String mUsername;
    private String mPhotoUrl;
    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private AdView mAdView;
    private String tmpThumbPhotoURL;
    public CircleImageView mImageView;
    private FirebaseRecyclerAdapter<MessageData,MessageViewHolder> mFirebaseAdapter;
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat,container,false);
        My.INFO.backKeyName ="ChatFragment";
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.nav_home);

        mUsername = fUtil.firebaseUser.getDisplayName();
        if(fUtil.firebaseUser.getPhotoUrl() != null){
            mPhotoUrl = fUtil.firebaseUser.getPhotoUrl().toString();
        }else{
            fUtil.databaseReference.child("users").child(fUtil.firebaseUser.getUid()).child("thumbPhotoURL").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mPhotoUrl = (String) dataSnapshot.getValue();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });{

            }
        }
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<MessageData, MessageViewHolder>(
                MessageData.class,
                R.layout.item_message,
                MessageViewHolder.class,
                fUtil.databaseReference.child(MESSAGES_CHILD)
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, MessageData messageData, int position) {
                final MessageViewHolder viewHol = viewHolder;
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.messageTextView.setText(messageData.getText());
                viewHolder.messengerTextView.setText(messageData.getName());
                mImageView = viewHolder.messengerImageView;
                final String uId = messageData.getuId();
                fUtil.databaseReference.child("users/" + uId + "/thumbPhotoURL/").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tmpThumbPhotoURL = (String) dataSnapshot.getValue();
                        if(tmpThumbPhotoURL==null){
                            viewHol.messengerImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    R.drawable.ic_account_circle_black_36dp));
                        }else{
                            try {
                                Glide.with(getActivity())
                                        .load(tmpThumbPhotoURL)
                                        .placeholder(R.drawable.ic_account_circle_black_36dp)
                                        .dontAnimate()
                                        .fitCenter()
                                        .into(viewHol.messengerImageView);
                            }catch(NullPointerException e){}
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                viewHol.messengerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), UserDetailActivity.class);
                        i.putExtra("uId",uId);
                        startActivity(i);
                    }
                });
            }
        };
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(lastVisiblePosition == -1 ||
                        (positionStart>=(friendlyMessageCount-1) &&
                                lastVisiblePosition == (positionStart-1))){
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) rootView.findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) rootView.findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send messages on click.
                MessageData friendlyMessage = new MessageData(mMessageEditText.getText().toString(),
                        mUsername,mPhotoUrl,null,fUtil.getCurrentUserId());
                fUtil.databaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return rootView;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.clear(mImageView);
    }
}
