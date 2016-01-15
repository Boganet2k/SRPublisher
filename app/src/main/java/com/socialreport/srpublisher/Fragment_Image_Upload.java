package com.socialreport.srpublisher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.recyclerviewProjects.FragmentProjectsList;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.socialreport.srpublisher.signin.SignInModel;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by bb on 21.09.15.
 */
public class Fragment_Image_Upload extends Fragment implements BackButtonPressed, SignInModel.Observer {

    final String LOG_TAG = "Fragment_Image_Upload";

    final static private String uploadRequest = "https://api.socialreport.com/mediaCreate.svc";

    Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();
    SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

    Uri receivedUri;
    Button projectsListSelect;
    ProjectDAO projectDAO;
    //Project selectedProject;

    private TextView textView_upload;
    private ImageView imageView_upload;

    private View mWebShadow;

    public static final int PROJECTS_FRAGMENT = 1;

    String projectsListTAG = "projectsListTAG";

//    private WeakReference<OnImageUploadedListener> mCallback;
    private SignInModel mSignInModel;

    @Override
    public boolean onBackPressed() {
        Log.i(LOG_TAG, "onBackPressed");

        Fragment projectsFragment = getFragmentManager().findFragmentByTag(projectsListTAG);

        if (projectsFragment != null) {
            getFragmentManager().beginTransaction().remove(projectsFragment).commit();
            return false;
        }

        return true;
    }

    @Override
    public void onSignInStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInStarted");
    }

    @Override
    public void onSignInSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInSucceeded");
    }

    @Override
    public void onSignInFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInFailed");
    }

    @Override
    public void onPublishStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishStarted");
    }

    @Override
    public void onPublishSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishSucceeded");
    }

    @Override
    public void onPublishFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishFailed");
    }

    @Override
    public void onUploadhStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadhStarted");

        mWebShadow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUploadSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadSucceeded");

        mWebShadow.setVisibility(View.GONE);
        //Toast.makeText(getView().getContext(), "Success! Image has been uploaded", Toast.LENGTH_SHORT).show();

        Dialog dialog = new AlertDialog.Builder(Fragment_Image_Upload.this.getActivity()).setTitle(R.string.success).setMessage(R.string.success_upload).setPositiveButton(R.string.dialog_button_text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Fragment_Image_Upload.this.getActivity().finish();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Fragment_Image_Upload.this.getActivity().finish();
            }
        }).create();

        dialog.show();
    }

    @Override
    public void onUploadFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadFailed");

        mWebShadow.setVisibility(View.GONE);
        //Toast.makeText(getView().getContext(), "Failed! Image has not been uploaded: " + signInModel.getResultMessage(), Toast.LENGTH_SHORT).show();

        Dialog dialog = new AlertDialog.Builder(Fragment_Image_Upload.this.getActivity()).setTitle(R.string.failed).setMessage(Fragment_Image_Upload.this.getActivity().getString(R.string.failed_upload) + ": " + signInModel.getResultMessage()).setPositiveButton(R.string.dialog_button_text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();

        dialog.show();
    }

    @Override
    public void onPublishWebLoadStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadStarted");
    }

    @Override
    public void onPublishWebLoadProgress(SignInModel signInModel, int progress) {
        Log.i(LOG_TAG, "onPublishWebLoadProgress");
    }

    @Override
    public void onPublishWebLoadSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadSucceeded");
    }

    @Override
    public void onPublishWebLoadFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadFailed");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Log.i(LOG_TAG, "onSaveInstanceState");

        outState.putString("description", textView_upload.getText().toString());
        mSignInModel.mTagDescription = textView_upload.getText().toString();

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        final SignInWorkerFragment retainedWorkerFragment = (SignInWorkerFragment) getFragmentManager().findFragmentByTag(MainActivity.TAG_WORKER);
        mSignInModel = retainedWorkerFragment.getSignInModel();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreateView");

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.image_upload, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onViewCreated mSignInModel: " + mSignInModel + " savedInstanceState: " + savedInstanceState);

        projectDAO = new ProjectDAO(view.getContext());

        projectsListSelect = (Button)view.findViewById(R.id.btn_projects_select_upload);
        projectsListSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click buttonProjectsSelect tapped");

                FragmentProjectsList fragmentProjectsList = new FragmentProjectsList();
                fragmentProjectsList.setTargetFragment(Fragment_Image_Upload.this, PROJECTS_FRAGMENT);

                getFragmentManager().beginTransaction().replace(R.id.frame_projects_upload, fragmentProjectsList, projectsListTAG).commit();

            }
        });

        if (mSignInModel.getSelectedProject() != null) {
            projectsListSelect.setText(mSignInModel.getSelectedProject().getName());
        }

        mWebShadow = view.findViewById(R.id.web_upload_shadow);

        textView_upload = (TextView)view.findViewById(R.id.textView_upload);
        textView_upload.setText(mSignInModel.mTagDescription);

        if (savedInstanceState != null) {
            textView_upload.setText(savedInstanceState.getString("description"));
        }

        imageView_upload = (ImageView)view.findViewById(R.id.imageView_upload);

        if (mSignInModel != null) {
            mSignInModel.registerObserver(this);
        } else {
            Log.i(LOG_TAG, "mSignInModel == null");
        }

        if (mSignInModel.getUploadImage() != null) {

            Log.i(LOG_TAG, "mSignInModel.getUploadImage() != null");

            imageView_upload.setImageBitmap(mSignInModel.getUploadImage());
        } else {
            Log.i(LOG_TAG, "mSignInModel.getUploadImage() == null");
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(LOG_TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);

        if (requestCode == PROJECTS_FRAGMENT) {
            mSignInModel.setSelectedProject(projectDAO.getProjectById(resultCode));

            projectsListSelect.setText(mSignInModel.getSelectedProject().getName());

            Toast.makeText(getView().getContext(), mSignInModel.getSelectedProject().getName() + " selected", Toast.LENGTH_SHORT).show();

            Fragment projectsFragment = getFragmentManager().findFragmentByTag(projectsListTAG);

            if (projectsFragment != null) {
                getFragmentManager().beginTransaction().remove(projectsFragment).commit();
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_upload, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            if (mSignInModel.getSelectedProject() != null) {
                mSignInModel.sendUploadRequest(textView_upload.getText().toString());
            } else {
                Log.i(LOG_TAG, "publish() projects empty");
                Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.project_not_selected), Toast.LENGTH_SHORT).show();
            }
            //upload(item);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSignInModel != null) {
            mSignInModel.unregisterObserver(this);
        } else {
            Log.i(LOG_TAG, "mSignInModel == null");
        }
    }

}
