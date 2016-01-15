package com.socialreport.srpublisher;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.Publication;

public class PublicationsListActivity extends AppCompatActivity implements RecyclerAdapterPublication.OnPublicationItemRecyclerViewClickListener {

    private static String LOG_TAG = PublicationsListActivity.class.getName();

    RecyclerView mRecyclerView;
    private Project mProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publications_list);

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

        mRecyclerView = (RecyclerView) findViewById(R.id.publications_recycler_view);
        GridLayoutManager mGridLayout = new GridLayoutManager(this, 1);

        mRecyclerView.setLayoutManager(mGridLayout);
        mRecyclerView.setAdapter(new RecyclerAdapterPublication(mProject, this));
    }

    @Override
    public void onPublicationSelected(Project project, int index, RecyclerAdapterPublication mAdapter) {
        Log.i(LOG_TAG, "onPublicationSelected: " + index + "" + project.getPublications().get(index));

        Intent resultIntent = new Intent();
        resultIntent.putExtra("project", project);

        setResult(index, resultIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publications_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_publication_add) {

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
            final EditText editText = (EditText) dialogView.findViewById(R.id.dialog_edit_text_input);

            AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.new_content_group).setView(dialogView).setPositiveButton(R.string.continue_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    for (int i = 0; i < mProject.getPublications().size(); i++) {
                        Publication currPublication = mProject.getPublications().get(i);
                        currPublication.setIsChecked(false);
                    }

                    Publication newPublication = new Publication();
                    newPublication.setName(editText.getText().toString());
                    newPublication.setIsChecked(true);

                    mProject.getPublications().add(0, newPublication);

                    onPublicationSelected(mProject, 0, null);

//                    mRecyclerView.getAdapter().notifyDataSetChanged();
//                    mRecyclerView.scrollToPosition(0);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create();
            alert.show();

            return true;
        } else if (id == R.id.action_publication_cancel) {
            setResult(-1, null);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
