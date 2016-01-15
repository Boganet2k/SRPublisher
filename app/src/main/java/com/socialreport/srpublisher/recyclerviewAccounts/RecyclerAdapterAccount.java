package com.socialreport.srpublisher.recyclerviewAccounts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.R;
import com.socialreport.srpublisher.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RecyclerAdapterAccount extends RecyclerView.Adapter<RecyclerAdapterAccount.ViewHolder> {

    final static String LOG_TAG = "RecyclerAdapterAccount";

    private Project mCurrProject;
    private boolean mIsSelectingEnable;
    OnAccountItemRecyclerViewClickListener mOnAccountItemRecyclerViewClickListener;

    public RecyclerAdapterAccount(Project myDataset, boolean isSelectingEnable, OnAccountItemRecyclerViewClickListener mOnAccountItemRecyclerViewClickListener) {

        Log.i(LOG_TAG, "RecyclerAdapter");

        mCurrProject = myDataset;
        mIsSelectingEnable = isSelectingEnable;

        this.mOnAccountItemRecyclerViewClickListener = mOnAccountItemRecyclerViewClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView_account_name;
        public TextView mTextView_account_type;
        public ImageView mImageView;
        public CheckBox mCheckBox_account_checked;
        public Button mButtonPinterestBoardBlue;
        public Button mButtonPinterestBoardRed;

        public ViewHolder(View v) {
            super(v);

            Log.i(LOG_TAG, "ViewHolder");

            mCheckBox_account_checked = (CheckBox) v.findViewById(R.id.account_item_checked);
            mCheckBox_account_checked.setClickable(false);
            mTextView_account_name = (TextView) v.findViewById(R.id.account_item_name);
            mTextView_account_type = (TextView) v.findViewById(R.id.account_item_type);
            mImageView = (ImageView) v.findViewById(R.id.account_item_image);
            mButtonPinterestBoardBlue = (Button) v.findViewById(R.id.button_pinterest_board_blue);
            mButtonPinterestBoardRed = (Button) v.findViewById(R.id.button_pinterest_board_red);
        }
    }
    @Override
    public int getItemCount() {
        Log.i(LOG_TAG, "getItemCount");
        return mCurrProject.getAccounts().size();
    }
    @Override
    public void onBindViewHolder(final ViewHolder arg0, final int arg1) {

        final Account currAccount = mCurrProject.getAccounts().get(arg1);

        Log.i(LOG_TAG, "onBindViewHolder arg1: " + arg1 + " currAccount.isImageDataLoading(): " + currAccount.isImageDataLoading());

        arg0.mTextView_account_type.setVisibility(View.GONE);
        arg0.mButtonPinterestBoardBlue.setVisibility(View.GONE);
        arg0.mButtonPinterestBoardRed.setVisibility(View.GONE);

        if (currAccount.isChecked() && currAccount.getBoards().size() > 0) {

            Board selectedBoard = Utils.getFirstSelectedBoard(currAccount.getBoards());

            View.OnClickListener onPinterestBoardClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.i(LOG_TAG, "mButtonPinterestBoard clicked");

                    mOnAccountItemRecyclerViewClickListener.onSelectBoards(RecyclerAdapterAccount.this);
                }
            };

            if (selectedBoard != null) {
                arg0.mButtonPinterestBoardBlue.setPaintFlags(arg0.mButtonPinterestBoardBlue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                arg0.mButtonPinterestBoardBlue.setText("Pinterest board: " + selectedBoard.getName());
                arg0.mButtonPinterestBoardBlue.setVisibility(View.VISIBLE);
                arg0.mButtonPinterestBoardBlue.setOnClickListener(onPinterestBoardClick);

            } else {
                arg0.mButtonPinterestBoardRed.setPaintFlags(arg0.mButtonPinterestBoardRed.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                arg0.mButtonPinterestBoardRed.setText("Please select board");
                arg0.mButtonPinterestBoardRed.setVisibility(View.VISIBLE);
                arg0.mButtonPinterestBoardRed.setOnClickListener(onPinterestBoardClick);

            }

        } else {
            arg0.mTextView_account_type.setVisibility(View.VISIBLE);
            arg0.mTextView_account_type.setText(currAccount.getType());
        }

        arg0.mCheckBox_account_checked.setChecked(currAccount.isChecked());
        arg0.mTextView_account_name.setText(currAccount.getName());

        arg0.mImageView.setImageResource(R.drawable.ic_account_circle_black_48dp);

        if (currAccount.getImageData() != null) {
            arg0.mImageView.setImageBitmap(currAccount.getImageData());
        } else if (currAccount.getImage() != null && currAccount.getImage().length() > 0 && !currAccount.isImageDataLoading()) {

            AsyncTask<String, Void, Bitmap> downloadImage = new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {

                    Log.i(LOG_TAG, "asyncTask downloadImage url: " + params[0]);

                    Bitmap bitmap = null;

                    try {
                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(params[0]).getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {

                    if (bitmap != null) {
                        currAccount.setImageData(bitmap);
                        //currAccount.setIsImageDataLoading(false);
                    }

                    notifyItemChanged(arg1);
                    //notifyDataSetChanged();

                }
            };

            downloadImage.execute(currAccount.getImage());
            currAccount.setIsImageDataLoading(true);
        }

        arg0.itemView.setTag(arg1);

        if (mIsSelectingEnable) {
            arg0.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean newState = !arg0.mCheckBox_account_checked.isChecked();
                    arg0.mCheckBox_account_checked.setChecked(newState);
                    mCurrProject.getAccounts().get(arg1).setIsChecked(newState);

                    Log.i(LOG_TAG, "onBindViewHolder mCurrProject.getAccounts().get(0): " + mCurrProject.getAccounts().get(0));

                    mOnAccountItemRecyclerViewClickListener.onAccountsSelected(mCurrProject, RecyclerAdapterAccount.this);

                    if (currAccount.getBoards().size() > 0) {

                        if (!newState) Utils.uncheckBoards(currAccount);

                        notifyItemChanged((Integer) v.getTag());
                    }

                }
            });
        }

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {

        Log.i(LOG_TAG, "onCreateViewHolder");

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.accounts_list_item, parent, false);
        //v.setBackgroundColor(0xc0000000);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
}
