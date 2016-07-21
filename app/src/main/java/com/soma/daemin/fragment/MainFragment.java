package com.soma.daemin.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.soma.daemin.R;
import com.soma.daemin.common.GlideUtil;
import com.soma.daemin.common.My;
import com.soma.daemin.data.PictureData;
import com.soma.daemin.firebase.fUtil;
import com.yongchun.library.utils.GridSpacingItemDecoration;

import java.util.Calendar;


/**
 * Created by user on 2016-06-14.
 */
public class MainFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<PictureData, PictureViewHolder> mAdapter;
    private FloatingActionButton fab;
    private ProgressBar bar;
    Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.nav_home));
        My.INFO.backKeyName ="MainFragment";
        bar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_submit_post);
        mAdapter = new FirebaseRecyclerAdapter<PictureData, PictureViewHolder>(PictureData.class, R.layout.listitem_picture,
                PictureViewHolder.class, fUtil.databaseReference.child("picture").limitToLast(100)) {

            @Override
            protected void populateViewHolder(final PictureViewHolder viewHolder, final PictureData pic, final int position) {
                bar.setVisibility(View.GONE);
                final DatabaseReference postRef = getRef(position);
                final String postKey = postRef.getKey();
                final String date = (String) DateUtils.getRelativeTimeSpanString(pic.time);
                viewHolder.tvTitle.setText(pic.title);
                viewHolder.tvName.setText(pic.uName);
                calendar.setTimeInMillis(pic.time);
                viewHolder.tvDate.setText(date);
                GlideUtil.loadImage(pic.thumbURL,viewHolder.ivThumb);
                viewHolder.ivThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PictureDetailActivity.class);
                        intent.putExtra(PictureDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(PictureDetailActivity.EXTRA_DATE, date);
                        intent.putExtra(PictureDetailActivity.EXTRA_UID, pic.uid);
                        startActivity(intent);
                    }
                });
                viewHolder.ivOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(fUtil.getCurrentUserId().equals(pic.uid)) {
                            showPopupMenu(viewHolder.ivOverflow,postKey);
                        }
                    }
                });
            }
        };
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(mAdapter.getItemCount()-1);
                mAdapter.notifyDataSetChanged();
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewPictureActivity.class));
            }
        });
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }
    private void showPopupMenu(View view, String key) {
        // inflate menu
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_picture, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(key));
        popup.show();
    }
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        String key;
        public MyMenuItemClickListener(String key) {
            this.key=key;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_remove:
                    fUtil.databaseReference.child("picture/" + key).removeValue();
                    fUtil.databaseReference.child("picture-comments/" + key).removeValue();
                    fUtil.databaseReference.child("picture-urls/" + key).removeValue();
                    fUtil.getStorePictureRef().child(key).delete();
                    return true;
                default:
            }
            return false;
        }
    }
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }
}
