package com.socialreport.srpublisher;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.Publication;

public class RecyclerAdapterPublication extends RecyclerView.Adapter<RecyclerAdapterPublication.ViewHolder> {

    final static String LOG_TAG = RecyclerAdapterPublication.class.getName();

    public interface OnPublicationItemRecyclerViewClickListener {
        public void onPublicationSelected(Project project, int arg1, RecyclerAdapterPublication mAdapter);
    }

    private Project mCurrProject;
    private OnPublicationItemRecyclerViewClickListener mClickListener;
    private Publication mCurrentSelection = null;

    public RecyclerAdapterPublication(Project project, OnPublicationItemRecyclerViewClickListener clickListener) {

        Log.i(LOG_TAG, "RecyclerAdapter");

        mCurrProject = project;

        Log.i(LOG_TAG, "RecyclerAdapter mCurrProject.getAccounts().get(0): " + mCurrProject.getAccounts().get(0));

        this.mClickListener = clickListener;

        for (int i = 0; i < mCurrProject.getPublications().size(); i++) {
            Publication currPublication = mCurrProject.getPublications().get(i);

            if (currPublication.isChecked()) {
                mCurrentSelection = currPublication;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView_publication_name;
        public TextView mTextView_publication_type;

        public ViewHolder(View v) {
            super(v);

            Log.i(LOG_TAG, "ViewHolder");

            mTextView_publication_name = (TextView) v.findViewById(R.id.publication_item_name);
            mTextView_publication_type = (TextView) v.findViewById(R.id.publication_item_type);
        }
    }

    @Override
    public int getItemCount() {
        Log.i(LOG_TAG, "getItemCount");
        return mCurrProject.getPublications().size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder arg0, final int arg1) {

        Log.i(LOG_TAG, "onBindViewHolder");

        arg0.mTextView_publication_name.setText(mCurrProject.getPublications().get(arg1).getName());
        //arg0.mTextView_publication_type.setText("" + mCurrProject.getPublications().get(arg1).getType());
        arg0.itemView.setTag(arg1);

        arg0.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(LOG_TAG, "click on: " + arg1 + " mCurrentSelection: " + mCurrentSelection + " ((int)v.getTag()): " + ((int)v.getTag()));

                if (mCurrentSelection != null) {
                    mCurrentSelection.setIsChecked(false);
                }

                mCurrentSelection = mCurrProject.getPublications().get(arg1);

                mCurrentSelection.setIsChecked(true);

                mClickListener.onPublicationSelected(mCurrProject, arg1, RecyclerAdapterPublication.this);
            }
        });

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {

        Log.i(LOG_TAG, "onCreateViewHolder");

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.publications_list_tem, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
}
