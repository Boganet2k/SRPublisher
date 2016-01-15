package com.socialreport.srpublisher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.recyclerviewAccounts.FragmentAccountsList;

public class AccountsListActivity extends AppCompatActivity implements FragmentAccountsList.OnAccountsSelectedListener {

    final String LOG_TAG = "AccountsListActivity";
    Project mProject;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "onSaveInstanceState");

        outState.putParcelable("project", mProject);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(LOG_TAG, "onRestoreInstanceState");

        mProject = savedInstanceState.getParcelable("project");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//        toolbar.setNavigationIcon(R.drawable.ic_add_white_48dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Navigation icon clicked");
            }
        });

        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mProject = savedInstanceState.getParcelable("project");
        } else {
            mProject = getIntent().getParcelableExtra("project");
        }

        Log.i(LOG_TAG, "onCreate mProject: " + mProject);

        FragmentAccountsList fragmentAccountsList = new FragmentAccountsList();

        Bundle arg = new Bundle();
        arg.putParcelable("project", mProject);
        fragmentAccountsList.setArguments(arg);

        getFragmentManager().beginTransaction().replace(R.id.frame_accounts_list, fragmentAccountsList).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_accounts_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_account_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountsSelected(Project project) {

        Log.i(LOG_TAG, "onAccountsSelected mCurrProject.getAccounts().get(0): " + project.getAccounts().get(0));

        Intent resultIntent = new Intent();
        resultIntent.putExtra("project", project);
        setResult(1, resultIntent);
        finish();

    }

    @Override
    public void onAccountsSelectedCancel() {
        Log.i(LOG_TAG, "onAccountsSelectedCancel");

        finish();
    }

    @Override
    public void onProjectUpdate(Project project) {
        Log.i(LOG_TAG, "onProjectUpdate");

        mProject = project;
    }
}
