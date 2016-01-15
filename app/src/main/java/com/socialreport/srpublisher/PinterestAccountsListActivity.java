package com.socialreport.srpublisher;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PinterestAccountsListActivity extends AppCompatActivity {

    private static String LOG_TAG = PinterestAccountsListActivity.class.getName();

    private LinearLayout customContainer;
    private Project mProject;
    private TextView account_name;

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("project", mProject);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mProject = savedInstanceState.getParcelable("project");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinterest_accounts_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//        toolbar.setNavigationIcon(R.drawable.ic_add_white_48dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Navigation icon clicked");
            }
        });

        setSupportActionBar(toolbar);

        Intent mIntent = getIntent();

        mProject = mIntent.getParcelableExtra("project");

        //Create custom components

        customContainer = (LinearLayout) findViewById(R.id.pinterest_accountCards_container);
        LinearLayout cardItem;

        RadioGroup radioGroup;
        Switch account_switch;

        Log.i(LOG_TAG, "mProject.getAccounts().size(): " + mProject.getAccounts().size());

        for (int a = 0; a < mProject.getAccounts().size(); a++) {

            final Account currAccount = mProject.getAccounts().get(a);

            Log.i(LOG_TAG, "currAccount.getID(): " + currAccount.getID() + " currAccount.isChecked(): " + currAccount.isChecked() + " currAccount.getBoards().size(): " + currAccount.getBoards().size());

            if (currAccount.getBoards().size() == 0) continue;

            cardItem = (LinearLayout) getLayoutInflater().inflate(R.layout.cards_item, null);

            account_name = (TextView) cardItem.findViewById(R.id.account_name);
            account_name.setText(currAccount.getName());

            account_switch = (Switch) cardItem.findViewById(R.id.account_switch);
            account_switch.setTag(R.id.pinterest_account_id, currAccount.getID());
            account_switch.setChecked(currAccount.isChecked());

            radioGroup = (RadioGroup) cardItem.findViewById(R.id.card_view_radio_group);

            for (int b = 0; b < currAccount.getBoards().size(); b++) {
                final Board currBoard = currAccount.getBoards().get(b);

                LinearLayout boardLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.card_item_radio_item, null);

                final ImageView boardImage = (ImageView) boardLayout.getChildAt(0);

                boardImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uriUrl = Uri.parse(currBoard.getUrl());
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                });

                if (currBoard.getImage() != null && currAccount.getImage().length() > 0) {

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
                                boardImage.setImageBitmap(bitmap);
                                //currBoard.setImageData(bitmap);
                                //currAccount.setIsImageDataLoading(false);
                            }

                        }
                    };

                    downloadImage.execute(currBoard.getImage());
                }

                //radioItem = (RadioButton) getLayoutInflater().inflate(R.layout.card_item_radio_item, null);
                final RadioButton radioItem = (RadioButton) boardLayout.getChildAt(1);

                radioItem.setText(currBoard.getName());
                radioItem.setTag(R.id.pinterest_board_id, currBoard.getID());

                //Drawable icon = getDrawable(R.drawable.ic_launcher);
                //Bitmap drawable = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                //radioItem.setCompoundDrawables(new BitmapDrawable(this.getResources(), drawable), null, null, null);
                //ButtonDrawable(new BitmapDrawable(this.getResources(), drawable));

                final RadioGroup finalRadioGroup = radioGroup;

                radioItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        int board_id = (int) buttonView.getTag(R.id.pinterest_board_id);

                        currBoard.setIsChecked(isChecked);

                        Log.i(LOG_TAG, "board_id: " + board_id + " isChecked: " + isChecked);

                        if (isChecked) {
                            for (int j = 0; j < finalRadioGroup.getChildCount(); j++) {
                                LinearLayout boardLayout = (LinearLayout) finalRadioGroup.getChildAt(j);
                                RadioButton radioButton = (RadioButton) boardLayout.getChildAt(1);

                                if (board_id != (int) radioButton.getTag(R.id.pinterest_board_id)) {
                                    radioButton.setChecked(false);
                                }

                            }
                        }

                    }
                });

                //radioGroup.addView(radioItem);
                radioGroup.addView(boardLayout);

                radioItem.setChecked(currBoard.getIsChecked());

                if (!currAccount.isChecked()) {
                    boardLayout.setVisibility(View.GONE);
                }

                account_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        int account_id = (int) buttonView.getTag(R.id.pinterest_account_id);

                        Log.i(LOG_TAG, "account_id: " + account_id + "isChecked: " + isChecked);

                        currAccount.setIsChecked(isChecked);

                        for (int j = 0; j < finalRadioGroup.getChildCount(); j++) {
                            LinearLayout boardLayout = (LinearLayout) finalRadioGroup.getChildAt(j);
                            //RadioButton radioButton = (RadioButton) finalRadioGroup.getChildAt(j);
                            RadioButton radioButton = (RadioButton) boardLayout.getChildAt(1);

                            if (!isChecked) {
                                radioButton.setChecked(false);
                                boardLayout.setVisibility(View.GONE);
                            } else {
                                boardLayout.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                });

            }

            customContainer.addView(cardItem);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pinterest_accounts_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_pinterest_select) {

            if (Utils.checkForValidPinterestBoards(mProject)) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("project", mProject);
                setResult(1, resultIntent);

                finish();

            } else {
                AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.alert_caption).setMessage(R.string.select_board_for_pinterest_account).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();

                            }
                        }).create();
                alert.show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
