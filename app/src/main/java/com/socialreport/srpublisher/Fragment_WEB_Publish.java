package com.socialreport.srpublisher;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.recyclerviewProjects.FragmentProjectsList;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.socialreport.srpublisher.signin.SignInModel;
import com.twitter.Extractor;
import com.twitter.Validator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import retrofit.Retrofit;

import static android.app.DatePickerDialog.*;


/**
 * Created by bb on 21.09.15.
 */
public class Fragment_WEB_Publish extends Fragment implements BackButtonPressed, SignInModel.Observer {

    private static final int RESULT_OK = -1;
    final String LOG_TAG = "Fragment_WEB_Publish";
    public static final int PROJECTS_FRAGMENT = 1;
    public static final int ACCOUNTS_ACTIVITY = 2;
    public static final int PUBLICATIONS_ACTIVITY = 3;
    public static final int PICK_IMAGE_REQUEST = 4;

    String projectsListTAG = "projectsListTAG";

    final static private String publishRequest = "https://api.socialreport.com/publications.svc";

    Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();
    SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

    private TextView textView_web;
    private ImageView imageView_web;

    private View mWebShadow;
    private ProgressBar mWebProgress;
    private ProgressBar mWebProgressCircle;
    private TextView mWebProgressTitle;
    private TextView mWebProgressText;

    private Button projectsListSelect;

    private ImageButton btnSelectPublications;
    private ImageButton btnSelectAccounts;
    private ImageButton btnSelectTime;
    private ImageButton btnSelectDate;

//    private Calendar publishDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat sdfPublish = new SimpleDateFormat("yyyyMMdd HH:mm");

    private ProjectDAO projectDAO;
    //private Project selectedProject;

    private SignInModel mSignInModel;
    private Button buttonCategorySelect;
    private Validator mValidator;
    private TextView mCharCount;
    private TextView mTwitterCharCount;

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

        mWebProgressText.setText("");
        mWebShadow.setVisibility(View.VISIBLE);
        mWebProgress.setVisibility(View.GONE);
        mWebProgressCircle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPublishSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishSucceeded");

        mWebShadow.setVisibility(View.GONE);
        //Toast.makeText(getView().getContext(), "Success! Publication has been scheduled", Toast.LENGTH_SHORT).show();

        Dialog dialog = new AlertDialog.Builder(Fragment_WEB_Publish.this.getActivity()).setTitle(R.string.success).setMessage(R.string.success_publish).setPositiveButton(R.string.dialog_button_text_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Fragment_WEB_Publish.this.getActivity().finish();
            }
        }).setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Fragment_WEB_Publish.this.getActivity().finish();
            }
        }).create();

        dialog.show();
    }

    @Override
    public void onPublishFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishFailed");

        mWebShadow.setVisibility(View.GONE);
        //Toast.makeText(getView().getContext(), "Failed! Publication has not been scheduled: " + signInModel.getResultMessage(), Toast.LENGTH_SHORT).show();

        Dialog dialog = new AlertDialog.Builder(Fragment_WEB_Publish.this.getActivity()).setTitle(R.string.failed).setMessage(Fragment_WEB_Publish.this.getActivity().getString(R.string.failed_publish) + ": " + signInModel.getResultMessage()).setPositiveButton(R.string.dialog_button_text_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();

        dialog.show();
    }

    @Override
    public void onUploadhStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadhStarted");
    }

    @Override
    public void onUploadSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadSucceeded");
    }

    @Override
    public void onUploadFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadFailed");
    }

    @Override
    public void onPublishWebLoadStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadStarted");

        mWebShadow.setVisibility(View.VISIBLE);
        mWebProgress.setProgress(mSignInModel.mLastProgress);
        mWebProgressText.setText("" + mSignInModel.mLastProgress + " %");
    }

    @Override
    public void onPublishWebLoadProgress(SignInModel signInModel, int progress) {
        Log.i(LOG_TAG, "onPublishWebLoadProgress");

        mWebProgress.setProgress(progress);
        mWebProgressText.setText("" + progress + " %");
    }

    @Override
    public void onPublishWebLoadSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadSucceeded");

        updateDataFromModel();

        mWebShadow.setVisibility(View.GONE);
    }

    @Override
    public void onPublishWebLoadFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishWebLoadFailed");

        mWebShadow.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        final SignInWorkerFragment retainedWorkerFragment = (SignInWorkerFragment) getFragmentManager().findFragmentByTag(MainActivity.TAG_WORKER);
        mSignInModel = retainedWorkerFragment.getSignInModel();
        mValidator = new Validator();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreateView");

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.web_publish, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onViewCreated mSignInModel: " + mSignInModel);

        projectDAO = new ProjectDAO(view.getContext());

        buttonCategorySelect = (Button) view.findViewById(R.id.buttonCategorySelect);

        buttonCategorySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click btnSelectPublications tapped");

                if (mSignInModel.getSelectedProject() != null) {
                    Intent intent = new Intent(getView().getContext(), PublicationsListActivity.class);
                    intent.putExtra("project", mSignInModel.getSelectedProject());

                    Log.i(LOG_TAG, "selectedProject.getPublications().size(): " + mSignInModel.getSelectedProject().getPublications().size());

                    startActivityForResult(intent, PUBLICATIONS_ACTIVITY);
                } else {
                    Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.project_not_selected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        textView_web = (TextView) view.findViewById(R.id.textView_web);

        TextWatcher txtWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int charResult = mValidator.getTweetLength(s.toString());

                Log.i(LOG_TAG, "Characters count: " + s.length() + " charResult: " + charResult);

                //Test for image
                if (mSignInModel.mIsWebImageRAW || mSignInModel.imgUrl != null) {
                    charResult += 23;
                }

                Log.i(LOG_TAG, "charResult: " + charResult);

                mCharCount.setTextColor(Color.LTGRAY);
                mCharCount.setText(s.length() + " " + view.getResources().getString(R.string.character));

                int remainTwitterChars = 140 - charResult;

                if (remainTwitterChars > 0) {
                    mTwitterCharCount.setTextColor(Color.LTGRAY);
                } else {
                    mTwitterCharCount.setTextColor(Color.RED);
                }

                mTwitterCharCount.setText(remainTwitterChars + " " + view.getResources().getString(R.string.character_remain_for_twitter));
            }

            public void afterTextChanged(Editable s) {
            }
        };

        textView_web.addTextChangedListener(txtWatcher);

        mCharCount = (TextView) view.findViewById(R.id.textView_char_count);
        mTwitterCharCount = (TextView) view.findViewById(R.id.textView_twitter_char_count);

        imageView_web = (ImageView) view.findViewById(R.id.imageView_web);

        if (mSignInModel.mIsWebImageRAW) {
            imageView_web.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            });
        }

        projectsListSelect = (Button) view.findViewById(R.id.btn_projects_select_publish);
        projectsListSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click buttonProjectsSelect tapped");

                FragmentProjectsList fragmentProjectsList = new FragmentProjectsList();
                fragmentProjectsList.setTargetFragment(Fragment_WEB_Publish.this, PROJECTS_FRAGMENT);

                getFragmentManager().beginTransaction().replace(R.id.frame_projects_publish, fragmentProjectsList, projectsListTAG).commit();

            }
        });

        btnSelectPublications = (ImageButton) view.findViewById(R.id.btnSelectPublications);
        btnSelectPublications.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click btnSelectPublications tapped");

                if (mSignInModel.getSelectedProject() != null) {
                    Intent intent = new Intent(getView().getContext(), PublicationsListActivity.class);
                    intent.putExtra("project", mSignInModel.getSelectedProject());

                    Log.i(LOG_TAG, "selectedProject.getPublications().size(): " + mSignInModel.getSelectedProject().getPublications().size());

                    startActivityForResult(intent, PUBLICATIONS_ACTIVITY);
                } else {
                    Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.project_not_selected), Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSelectAccounts = (ImageButton) view.findViewById(R.id.btnSelectAccounts);
        btnSelectAccounts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click btnSelectAccounts tapped");

                if (mSignInModel.getSelectedProject() != null) {
                    Intent intent = new Intent(getView().getContext(), AccountsListActivity.class);
                    intent.putExtra("project", mSignInModel.getSelectedProject());

                    Log.i(LOG_TAG, "selectedProject.getAccounts().size(): " + mSignInModel.getSelectedProject().getAccounts().size());

                    startActivityForResult(intent, ACCOUNTS_ACTIVITY);
                } else {
                    Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.project_not_selected), Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSelectTime = (ImageButton) view.findViewById(R.id.btnSelectTime);
        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click btnSelectTime tapped");

                TimePickerDialog picker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mSignInModel.mPublishDate.set(mSignInModel.mPublishDate.get(Calendar.YEAR), mSignInModel.mPublishDate.get(Calendar.MONTH), mSignInModel.mPublishDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                    }
                }, mSignInModel.mPublishDate.get(Calendar.HOUR_OF_DAY), mSignInModel.mPublishDate.get(Calendar.MINUTE), false);

                picker.show();
            }
        });

        btnSelectDate = (ImageButton) view.findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i(LOG_TAG, "click btnSelectDate tapped");

                DatePickerDialog picker = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mSignInModel.mPublishDate.set(year, monthOfYear, dayOfMonth);
                    }

                }, mSignInModel.mPublishDate.get(Calendar.YEAR), mSignInModel.mPublishDate.get(Calendar.MONTH), mSignInModel.mPublishDate.get(Calendar.DAY_OF_MONTH));

                picker.show();
            }
        });

        mWebProgress = (ProgressBar) view.findViewById(R.id.web_download_progress);
        mWebProgressCircle = (ProgressBar) view.findViewById(R.id.web_download_progress_circle);
        mWebProgressTitle = (TextView) view.findViewById(R.id.web_download_progress_title);
        mWebProgressText = (TextView) view.findViewById(R.id.web_download_progress_text);
        mWebShadow = view.findViewById(R.id.web_download_shadow);

        if (mSignInModel != null) {

            mSignInModel.registerObserver(this);

            updateDataFromModel();

            if (mSignInModel.getSelectedProject() != null) {

                buttonCategorySelect.setVisibility(View.VISIBLE);
                buttonCategorySelect.setText(Utils.getSelectedPublication(mSignInModel.getSelectedProject()) != null ? Utils.getSelectedPublication(mSignInModel.getSelectedProject()).getName() : getView().getContext().getString(R.string.publication_not_selected));

                if (Utils.getSelectedPublication(mSignInModel.getSelectedProject()) != null) {
                    btnSelectAccounts.setVisibility(View.VISIBLE);
                }


                projectsListSelect.setText(mSignInModel.getSelectedProject().getName());

            }
        } else {
            Log.i(LOG_TAG, "mSignInModel == null");
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void updateDataFromModel() {

        String descriptionWeb = mSignInModel.mTagDescription != null && mSignInModel.mTagDescription.length() > 0 ? mSignInModel.mTagDescription : mSignInModel.mTagTitle;

        Fragment_WEB_Publish.this.textView_web.setText((descriptionWeb.length() > 0 ? descriptionWeb + "\n" : "") + mSignInModel.mWebURL);

        if (mSignInModel.mWebImage != null && !mSignInModel.mIsWebImageRAW) {
            imageView_web.setImageBitmap(mSignInModel.mWebImage);
        } else {
            imageView_web.setImageBitmap(mSignInModel.getUploadImage());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(LOG_TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);

        if (requestCode == PROJECTS_FRAGMENT) {
            mSignInModel.setSelectedProject(projectDAO.getProjectById(resultCode));

            buttonCategorySelect.setText(Utils.getSelectedPublication(mSignInModel.getSelectedProject()) != null ? Utils.getSelectedPublication(mSignInModel.getSelectedProject()).getName() : getView().getContext().getString(R.string.publication_not_selected));
            buttonCategorySelect.setVisibility(View.VISIBLE);

            projectsListSelect.setText(mSignInModel.getSelectedProject().getName());

            Toast.makeText(getView().getContext(), mSignInModel.getSelectedProject().getName() + " selected", Toast.LENGTH_SHORT).show();

            Fragment projectsFragment = getFragmentManager().findFragmentByTag(projectsListTAG);

            if (projectsFragment != null) {
                getFragmentManager().beginTransaction().remove(projectsFragment).commit();
            }

        } else if (requestCode == ACCOUNTS_ACTIVITY) {

            if (data != null) {
                mSignInModel.setSelectedProject((Project) data.getParcelableExtra("project"));

                Log.i(LOG_TAG, "onActivityResult mCurrProject.getAccounts().get(0): " + mSignInModel.getSelectedProject().getAccounts().get(0));

                Log.i(LOG_TAG, "onActivityResult selectedProject: " + mSignInModel.getSelectedProject());
            }

        } else if (requestCode == PUBLICATIONS_ACTIVITY) {
            if (data != null) {
                mSignInModel.setSelectedProject((Project) data.getParcelableExtra("project"));

                Log.i(LOG_TAG, "onActivityResult selectedProject: " + mSignInModel.getSelectedProject() + " publicationIndex: " + resultCode);

                String result = "id: ";

                buttonCategorySelect.setText(Utils.getSelectedPublication(mSignInModel.getSelectedProject()) != null ? Utils.getSelectedPublication(mSignInModel.getSelectedProject()).getName() : getView().getContext().getString(R.string.publication_not_selected));

                //if (Utils.getSelectedPublication(mSignInModel.getSelectedProject()) != null) {
                    btnSelectAccounts.setVisibility(View.VISIBLE);
                //}

            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            mSignInModel.handleUploadImageData(uri);

            updateDataFromModel();
/*
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_publish, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_publish) {

            if (mSignInModel.getSelectedProject() == null) {
                Log.i(LOG_TAG, "publish() projects empty");
                Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.project_not_selected), Toast.LENGTH_SHORT).show();
                return true;
            }

            if (Utils.getSelectedPublication(mSignInModel.getSelectedProject()) == null) {
                Log.i(LOG_TAG, "publish() content category empty");
                Toast.makeText(getView().getContext(), getView().getContext().getString(R.string.publication_not_selected), Toast.LENGTH_SHORT).show();
                return true;
            }

            if (Utils.getSelectedPublication(mSignInModel.getSelectedProject()).getID() == -1 && !Utils.checkForValidPinterestBoards(mSignInModel.getSelectedProject())) {
                Intent intent = new Intent(getView().getContext(), PinterestAccountsListActivity.class);
                intent.putExtra("project", mSignInModel.getSelectedProject());

                Log.i(LOG_TAG, "selectedProject.getAccounts().size(): " + mSignInModel.getSelectedProject().getAccounts().size());

                startActivityForResult(intent, ACCOUNTS_ACTIVITY);

                return true;
            }

            mSignInModel.sendWebRequest(textView_web.getText().toString());

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
