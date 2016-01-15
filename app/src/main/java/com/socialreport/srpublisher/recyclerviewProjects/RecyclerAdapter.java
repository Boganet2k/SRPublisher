package com.socialreport.srpublisher.recyclerviewProjects;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    final static String LOG_TAG = "RecyclerAdapter";

    private ArrayList<Project> mDataset;
    OnItemRecycleViewClickListener mOnItemRecycleViewClickListener;

    public RecyclerAdapter(ArrayList<Project> myDataset, OnItemRecycleViewClickListener mOnItemRecycleViewClickListener) {

        Log.i(LOG_TAG, "RecyclerAdapter");

        mDataset = myDataset;
        this.mOnItemRecycleViewClickListener = mOnItemRecycleViewClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView_project_name;
        public TextView mTextView_project_account_count;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);

            Log.i(LOG_TAG, "ViewHolder");

            mTextView_project_name = (TextView) v.findViewById(R.id.project_name);
            mTextView_project_account_count = (TextView) v.findViewById(R.id.project_account_count);
            mImageView = (ImageView) v.findViewById(R.id.imageView);
        }
    }
    @Override
    public int getItemCount() {
        Log.i(LOG_TAG, "getItemCount");
        return mDataset.size();
    }
    @Override
    public void onBindViewHolder(ViewHolder arg0, int arg1) {

        Log.i(LOG_TAG, "onBindViewHolder");

        arg0.mTextView_project_name.setText(mDataset.get(arg1).getName());
        arg0.mTextView_project_account_count.setText("" + mDataset.get(arg1).getAccounts().size());
        arg0.itemView.setTag(arg1);

        arg0.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemRecycleViewClickListener.onItemClicked(Integer.parseInt(v.getTag().toString()), RecyclerAdapter.this);
            }
        });

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {

        Log.i(LOG_TAG, "onCreateViewHolder");

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.projects_list_item, parent, false);
        v.setBackgroundColor(0xc0000000);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
}