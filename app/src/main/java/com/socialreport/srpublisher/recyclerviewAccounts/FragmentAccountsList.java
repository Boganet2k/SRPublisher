package com.socialreport.srpublisher.recyclerviewAccounts;


import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.PinterestAccountsListActivity;
import com.socialreport.srpublisher.R;
import com.socialreport.srpublisher.Utils;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class FragmentAccountsList extends Fragment implements OnAccountItemRecyclerViewClickListener {

    final String LOG_TAG = "FragmentAccountsList";

    public static final int ACCOUNTS_ACTIVITY = 2;

    Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();
    SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

    RecyclerView mRecyclerView;
    private Project project;
    OnAccountsSelectedListener mCallback;

    private boolean isSelectingEnable = true;
    private RecyclerAdapterAccount mRecyclerAdapter;

    // Container Activity must implement this interface
    public interface OnAccountsSelectedListener {
        public void onAccountsSelected(Project project);

        void onAccountsSelectedCancel();

        void onProjectUpdate(Project project);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        Bundle arg = getArguments();
        project = arg.getParcelable("project");

        Log.i(LOG_TAG, "onCreate project: " + project);

        if (Utils.getSelectedPublication(project) != null && Utils.getSelectedPublication(project).getID() != -1) {
            //Reqest accounts for exist category and disable selecting accounts

            User currUser = Utils.getUser(getActivity().getApplicationContext());

            Call<ResponseBody> c = restAPIService.categoryAccounts(currUser.getToken(), true, Utils.getSelectedPublication(project).getServerID());

            c.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {

                    String result = "";

                    try {
                        result = new String(response.body().bytes());

                        Log.i(LOG_TAG, "restAPIService.login onResponse: " + result);

                        JSONObject resultJSONObject = new JSONObject(result);

                        Log.i(LOG_TAG, "restAPIService.login resultJSONArray: " + resultJSONObject);

                        ArrayList<Account> categoryAccounts;
                        JSONArray jsonAccounts;
                        Account currAccount;
                        JSONObject jsonAccount;
                        Board currBoard;
                        JSONObject jsonBoard;

                        try {
                            jsonAccounts = resultJSONObject.getJSONArray("accounts");

                            categoryAccounts = new ArrayList<Account>(jsonAccounts.length());

                            for (int i = 0; i < jsonAccounts.length(); i++) {
                                jsonAccount = (JSONObject) jsonAccounts.get(i);

                                currAccount = new Account();

                                try {
                                    currAccount.setServerID(jsonAccount.getInt("id"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setName(jsonAccount.getString("name"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setType(jsonAccount.getString("type"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setNetworkIcon(jsonAccount.getString("networkIcon"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setActive(jsonAccount.getBoolean("active"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setAccess(jsonAccount.getBoolean("access"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setPublish(jsonAccount.getBoolean("publish"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    currAccount.setImage(jsonAccount.getString("image"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                categoryAccounts.add(currAccount);
                            }

                            try {

                                JSONObject jsonCustomization = resultJSONObject.getJSONObject("customization");

                                //Test for boards for account
                                for (int i = 0; i < categoryAccounts.size(); i++) {

                                    try {

                                        jsonBoard = jsonCustomization.getJSONObject(String.valueOf(categoryAccounts.get(i).getServerID()));

                                        currBoard = new Board();

                                        currBoard.setIsChecked(true);

                                        try {

                                            currBoard.setServerID(jsonBoard.getString("pinterest_board_id"));

                                            currBoard.setName(jsonBoard.getString("pinterest_board_name"));

                                            categoryAccounts.get(i).getBoards().add(currBoard);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            project.setAccounts(categoryAccounts);

                            mRecyclerAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.i(LOG_TAG, "restAPIService.login onFailure: " + t.getMessage());
                }
            });

            project = new Project();
            isSelectingEnable = false;

        } else {
            //Show accounts for current project for selection

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnAccountsSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.accounts_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onViewCreated project.getAccounts().size(): " + project.getAccounts().size());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.accounts_recycler_view);
        GridLayoutManager mGridLayout = new GridLayoutManager(view.getContext(), 1);


        mRecyclerView.setLayoutManager(mGridLayout);

        mRecyclerAdapter = new RecyclerAdapterAccount(project, isSelectingEnable, this);

        mRecyclerView.setAdapter(mRecyclerAdapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_accounts_select, menu);

        if (!isSelectingEnable) {
            menu.removeItem(R.id.action_accounts_select_done);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_accounts_select_done) {

            mCallback.onAccountsSelected(project);

            return true;
        } else if (id == R.id.action_accounts_select_cancel) {
            mCallback.onAccountsSelectedCancel();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountsSelected(Project project, RecyclerAdapterAccount mAdapter) {
        Log.i(LOG_TAG, "onItemClicked mCurrProject.getAccounts().get(0): " + project.getAccounts().get(0));

//        mCallback.onAccountsSelected(project);
/*
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), projects.get(position).getID(), getActivity().getIntent());
        }
*/
    }

    @Override
    public void onSelectBoards(RecyclerAdapterAccount mAdapter) {

        if (isSelectingEnable) {
            Intent intent = new Intent(getView().getContext(), PinterestAccountsListActivity.class);
            intent.putExtra("project", project);

            Log.i(LOG_TAG, "selectedProject.getAccounts().size(): " + project.getAccounts().size());

            startActivityForResult(intent, ACCOUNTS_ACTIVITY);
        } else {
            final Dialog alert = new AlertDialog.Builder(getActivity()).setTitle(R.string.board_select_dialog_title).setMessage(R.string.board_select_dialog_message).setPositiveButton(R.string.dialog_button_text_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create();
            alert.show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(LOG_TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);

        if (requestCode == ACCOUNTS_ACTIVITY) {

            if (data != null) {
                project = (Project) data.getParcelableExtra("project");

                mCallback.onProjectUpdate(project);

                mRecyclerView.setAdapter(new RecyclerAdapterAccount(project, isSelectingEnable, this));
            }

        }
    }

}
