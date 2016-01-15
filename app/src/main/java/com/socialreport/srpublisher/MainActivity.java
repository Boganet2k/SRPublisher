package com.socialreport.srpublisher;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.net.Uri;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.charting.PieChart;
import com.socialreport.srpublisher.signin.SignInModel;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SignInModel.Observer{

    final String LOG_TAG = MainActivity.class.getCanonicalName();

    public static final String NOTIFICATION_PROCESS_RESPONSE = "com.socialreport.intent.action.NOTIFICATION_ACCOUNT_RESPONSE";
    public static final String ACTION_COMPOSE_MESSAGE = "com.socialreport.intent.action.ACTION_COMPOSE_MESSAGE";
    String fragmentWebTAG = "fragmentWebTAG";
    String fragmentUploadTAG = "fragmentUploadTAG";

    private Object lockMain = new Object();

    //private String username = "boganet2000@gmail.com";
    private TextView username;
    private String password = "123456";

    final static private String loginRequest = "https://api.socialreport.com/login.svc";
    final static private String projectsRequest = "https://api.socialreport.com/projects.svc";
    final static private String publishRequest = "https://api.socialreport.com/publications.svc";
    //final static private String publishRequest = "http://requestb.in/1a4ibkx1";

    final Context mContext = this;

    private AccountsRequestReceiver receiver;

    public static final String TAG_WORKER = "TAG_WORKER";

    private EditText mUserName;
    private EditText mPassword;
    private View mSubmit;
    private View mProgress;
    private View mLogout;

    ProjectDAO projectDAO = new ProjectDAO(this);

    private SignInModel mSignInModel;
    private RelativeLayout loginLayout;
    private RelativeLayout logoutLayout;

    Fragment_WEB_Publish fragmentWEB;
    Fragment_Image_Upload fragmentUpload;
    private CircleImageView imageUserLogo;
    private Toolbar myToolbar;
    private View mLogoutHelp;
    private Button mLoginForgotPassword;
    private Button mLoginNewAccount;
    private View mButtonShareWeb;
    private View mButtonShareImg;
    private View mButtonComposeMessage;
    private ImageView mLogo;
    private RelativeLayout mSplashScreen;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);

        Log.i(LOG_TAG, "onNewIntent intent: " + intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(LOG_TAG, "onOptionsItemSelected id: " + item.getItemId());



        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.menu_accounts_select, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate savedInstanceState: " + savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        //toolbar.setNavigationIcon(R.drawable.ic_add_white_48dp);
        //toolbar.setTitle("Test Title");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Navigation icon clicked");
            }
        });

        setSupportActionBar(toolbar);

        mLogo = (ImageView) findViewById(R.id.logo_main);
        mSplashScreen = (RelativeLayout) findViewById(R.id.splash_screen);
/*
        AppCompatSpinner navSpinner = (AppCompatSpinner) findViewById(R.id.spinner_nav);

        ArrayList<String> list = new ArrayList<String>();
        list.add("Top News");
        list.add("Politics");
        list.add("Business");
        list.add("Sports");
        list.add("Movies");

        // Custom ArrayAdapter with spinner item layout to set popup background

        CustomSpinnerAdapter spinAdapter = new CustomSpinnerAdapter(getApplicationContext(), list);

        navSpinner.setAdapter(spinAdapter);

        navSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                String item = adapter.getItemAtPosition(position).toString();

                // Showing selected spinner item
                Toast.makeText(getApplicationContext(), "Selected  : " + item,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
*/
        //get the received intent
        Intent receivedIntent = getIntent();
        //get the action
        String receivedAction = receivedIntent.getAction();
        //find out what we are dealing with
        String receivedType = receivedIntent.getType();

        Log.i(LOG_TAG, "receivedAction: " + receivedAction + " receivedType: " + receivedType);

        final SignInWorkerFragment retainedWorkerFragment = (SignInWorkerFragment) getFragmentManager().findFragmentByTag(TAG_WORKER);

        Log.i(LOG_TAG, "retainedWorkerFragment: " + retainedWorkerFragment);

        if (retainedWorkerFragment != null) {
            mSignInModel = retainedWorkerFragment.getSignInModel();
        } else {
            final SignInWorkerFragment workerFragment = new SignInWorkerFragment();

            getFragmentManager().beginTransaction().add(workerFragment, TAG_WORKER).commit();

            mSignInModel = workerFragment.getSignInModel();
            mSignInModel.setContext(getApplicationContext());

        }

        //detect: new activity, recreated after rotate or recreated after kill process
        boolean isNeedToHandleData = false;

        if (savedInstanceState == null) {
            isNeedToHandleData = true;
        } else {
            mSplashScreen.setVisibility(View.GONE);
        }
/*
        if ((receivedIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            isNeedToHandleData = false;
        }
*/

        Log.i(LOG_TAG, "isNeedToHandleData: " + isNeedToHandleData);

        //make sure it's an action and type we can handle
        if(receivedAction.startsWith(ACTION_COMPOSE_MESSAGE)) {

            mLogo.setVisibility(View.GONE);

            Log.i(LOG_TAG, "onCreate ACTION_COMPOSE_MESSAGE isNeedToHandleData: " + isNeedToHandleData);

            if (isNeedToHandleData) {
                mSignInModel.mIsWebImageRAW = true;

                openWebFragment(false);
            }

        } else if(receivedAction.equals(Intent.ACTION_SEND)){

            mLogo.setVisibility(View.GONE);

            //content is being shared
            if(receivedType.startsWith("text/")){

                String webURL = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);

                if (webURL != null && isNeedToHandleData) {
                    mSignInModel.handleWebPublishData(webURL);
                    openWebFragment(true);
                }

            } else if(receivedType.startsWith("image/")) {

                final Uri receivedUri = (Uri)receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);

                Log.i(LOG_TAG, "onCreate receivedUri: " + receivedUri + " isNeedToHandleData: " + isNeedToHandleData);

                if (isNeedToHandleData) {

                    //Ask user for upload image method
                    final boolean finalIsNeedToHandleData = isNeedToHandleData;
                    AlertDialog methodDialog = new AlertDialog.Builder(this).setTitle(R.string.alert_caption).setMessage(R.string.image_upload_method).setPositiveButton(R.string.upload_caption, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (finalIsNeedToHandleData) {
                                mSignInModel.handleUploadImageData(receivedUri);
                            }

                            fragmentUpload = new Fragment_Image_Upload();

                            getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, fragmentUpload, fragmentUploadTAG).commit();

                            hideSPlashScreen(true);

                        }
                    }).setNegativeButton(R.string.publish_caption, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            mSignInModel.mIsWebImageRAW = true;

                            if (finalIsNeedToHandleData) {
                                mSignInModel.handleUploadImageData(receivedUri);
                            }

                            openWebFragment(true);
                        }
                    }).create();
                    methodDialog.show();
                }

            }
        } else if(receivedAction.equals(Intent.ACTION_MAIN) || receivedAction.equals(MainActivity.NOTIFICATION_PROCESS_RESPONSE)){

            toolbar.setVisibility(View.GONE);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();

            Resources res = getResources();

            final PieChart pie = (PieChart) this.findViewById(R.id.Pie);
            pie.addItem("Agamemnon", 1, res.getColor(R.color.seafoam));
            pie.addItem("Bocephus", 1, res.getColor(R.color.chartreuse));
            pie.addItem("Calliope", 1, res.getColor(R.color.emerald));
            pie.addItem("Daedalus", 1, res.getColor(R.color.bluegrass));
            pie.addItem("Euripides", 1, res.getColor(R.color.turquoise));
            pie.addItem("Ganymede", 1, res.getColor(R.color.slate));

            loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
            mLoginForgotPassword = (Button) findViewById(R.id.login_button_forgot_password);
            mLoginForgotPassword.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Uri uriUrl = Uri.parse("https://www.socialreport.com/password.htm");
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });
            mLoginNewAccount = (Button) findViewById(R.id.login_button_new_account);
            mLoginNewAccount.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Uri uriUrl = Uri.parse("https://www.socialreport.com/register.htm");
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });

            logoutLayout = (RelativeLayout) findViewById(R.id.logout_layout);
            imageUserLogo = (CircleImageView) findViewById(R.id.imageUserLogo);
            username = (TextView) findViewById(R.id.textView_user_name);

            User currUser = Utils.getUser(this);

            if (currUser.getId() > 0) {
                logoutLayout.setVisibility(RelativeLayout.VISIBLE);
                imageUserLogo.setImageBitmap(Utils.base64ToBitmap(currUser.getPhoto()));
                username.setText(currUser.getFirstName() + " " + currUser.getLastName());

            } else {
                loginLayout.setVisibility(RelativeLayout.VISIBLE);
            }

            mUserName = (EditText) findViewById(R.id.view_username);
            mPassword = (EditText) findViewById(R.id.view_password);
            mSubmit = findViewById(R.id.view_submit);
            mLogoutHelp = findViewById(R.id.texView_logout_help);
            mLogout = findViewById(R.id.view_logout);
            mProgress = findViewById(R.id.view_progress);

            mButtonShareWeb = findViewById(R.id.imageView_logout_shareWeb);
            mButtonShareWeb.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Uri uriUrl = Uri.parse("about:blank");
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);

                }
            });

            mButtonShareImg = findViewById(R.id.imageView_logout_sharePhoto);
            mButtonShareImg.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setType("image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


                }
            });

            mButtonComposeMessage = findViewById(R.id.imageView_logout_composeMessage);
            mButtonComposeMessage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setAction(ACTION_COMPOSE_MESSAGE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mSignInModel.signIn(mUserName.getText().toString(), mPassword.getText().toString());
                }
            });

            mLogoutHelp.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Uri uriUrl = Uri.parse("http://www.socialreport.com/mobile.html");
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });

            mLogout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    logout();
                    logoutLayout.setVisibility(RelativeLayout.INVISIBLE);
                    loginLayout.setVisibility(RelativeLayout.VISIBLE);
                }
            });

            hideSPlashScreen(true);
        }

        if (mSignInModel != null) {
            mSignInModel.registerObserver(this);
        } else {
            Log.i(LOG_TAG, "mSignInModel == null");
        }
    }

    private void hideSPlashScreen(boolean isHidingSplashScreen) {

        if (isHidingSplashScreen) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSplashScreen.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mSplashScreen.startAnimation(anim);
        } else {
            mSplashScreen.setVisibility(View.GONE);
        }

    }

    public void openWebFragment(boolean isHidingSplashScreen) {
        fragmentWEB = new Fragment_WEB_Publish();

        getFragmentManager().beginTransaction().replace(R.id.fragment_placeholder, fragmentWEB, fragmentWebTAG).commit();

        hideSPlashScreen(isHidingSplashScreen);
    }

    @Override
    public void onBackPressed() {

        BackButtonPressed fragmentWeb = (BackButtonPressed) getFragmentManager().findFragmentByTag(fragmentWebTAG);
        BackButtonPressed fragmentUpload = (BackButtonPressed) getFragmentManager().findFragmentByTag(fragmentUploadTAG);

        boolean invokeSuper = true;

        if (fragmentWeb != null) {
            invokeSuper = fragmentWeb.onBackPressed();
        } else if (fragmentUpload != null) {
            invokeSuper = fragmentUpload.onBackPressed();
        }

        if (invokeSuper) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        super.onDestroy();

        if (mSignInModel != null) {
            mSignInModel.unregisterObserver(this);

            if (isFinishing()) {
                mSignInModel.stopSignIn();
            }
        }

    }

    private void logout() {
        Log.i("MainActivity", "logout()");

        User currUser = new User();
        Utils.saveUser(mContext, currUser);
    }

    /////SignIn Observers
    @Override
    public void onSignInStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInStarted");
        showProgress(true);
    }

    @Override
    public void onSignInSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInSucceeded");
        showProgress(false);
        Toast.makeText(this, R.string.sign_in_succeeded, Toast.LENGTH_SHORT).show();

        loginLayout.setVisibility(RelativeLayout.INVISIBLE);
        logoutLayout.setVisibility(RelativeLayout.VISIBLE);

        User currUser = Utils.getUser(this);

        imageUserLogo.setImageBitmap(Utils.base64ToBitmap(currUser.getPhoto()));
        username.setText(currUser.getFirstName() + " " + currUser.getLastName());
    }

    @Override
    public void onSignInFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onSignInFailed");

        logout();

        showProgress(false);
        Toast.makeText(this, R.string.sign_in_error + ": " + signInModel.getResultMessage(), Toast.LENGTH_SHORT).show();
    }

    private void showProgress(final boolean show) {
        mUserName.setEnabled(!show);
        mPassword.setEnabled(!show);
        mSubmit.setEnabled(!show);
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPublishStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishStarted");
    }

    @Override
    public void onPublishSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishSucceeded");

        //finish();
    }

    @Override
    public void onPublishFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onPublishFailed");
    }

    @Override
    public void onUploadhStarted(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadhStarted");
    }

    @Override
    public void onUploadSucceeded(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadSucceeded");

        //finish();
    }

    @Override
    public void onUploadFailed(SignInModel signInModel) {
        Log.i(LOG_TAG, "onUploadFailed");
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

}
