package com.socialreport.srpublisher.signin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.socialreport.srpublisher.AccountIntentService;
import com.socialreport.srpublisher.AccountsRequestReceiver;
import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.DB.Publication;
import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.MainService;
import com.socialreport.srpublisher.R;
import com.socialreport.srpublisher.Utils;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.squareup.okhttp.ResponseBody;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Observer;
import java.util.logging.Handler;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.google.android.gms.internal.zzip.runOnUiThread;

public class SignInModel {
    private static final String LOG_TAG = SignInModel.class.getCanonicalName();

    final static private String loginRequest = "https://api.socialreport.com/login.svc";
    final static private String projectsRequest = "https://api.socialreport.com/projects.svc";

    private final SignInObservable mObservable = new SignInObservable();

    Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();

    SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

    private ModelState mModelState = ModelState.NONE;
    private WebView webview;
    private boolean mWebPageFinished = false;
    private volatile boolean mIsWebParsed = false;

    enum ModelState {
        NONE,
        LOGIN,
        WEB_CONTENT_PARSE,
        PUBLISH_WEB,
        UPLOAD_IMAGE
    }

    //private SignInTask mSignInTask;
    //private boolean mIsWorking;
    private Context mContext;
    ProjectDAO projectDAO;
    private AccountsRequestReceiver receiver;
    private AccountsRequestReceiver receiverMainService;

    private String mUsername;
    private String mPassword;
    private String mResultMessage;

    private Project selectedProject;

    // Publish web data
    public String mWebURL = "";
    public String imgUrl;
    public Bitmap mWebImage;
    public boolean mIsWebImageRAW = false;
    public String mTagTitle = "";
    public String mTagDescription = "";
    private boolean mFacebookDefaultImage = false;
    private ArrayList<String> mImages = new ArrayList<String>();
    public int mLastProgress = 0;
    public Calendar mPublishDate = Calendar.getInstance();

    // Upload image data
    Uri receivedUri;
    byte[] imageRAW = new byte[0];
    private Bitmap uploadImage;

    public SignInModel() {
        Log.i(LOG_TAG, "new Instance");

        mPublishDate.setTime(new Date());
    }

    public void setContext(Context context) {
        Log.i(LOG_TAG, "new setContext");

        if (mContext != null) return;

        mContext = context;
        projectDAO = new ProjectDAO(context);

        //Create listeners from main service

        //Publish WEB
        IntentFilter filter = new IntentFilter();

        filter.addAction(MainService.ACTION_PUBLISH_WEB_CONTENT_RESULT);
        filter.addAction(MainService.ACTION_UPLOAD_IMAGE_CONTENT_RESULT);

        filter.addCategory(Intent.CATEGORY_DEFAULT);

        receiverMainService = new AccountsRequestReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);

                String action = intent.getAction();

                Log.i(LOG_TAG, "receive action: " + action);

                if (action.equals(MainService.ACTION_PUBLISH_WEB_CONTENT_RESULT)) {
                    String responseMessage = intent.getStringExtra(MainService.ACTION_PUBLISH_WEB_CONTENT_RESULT);
                    Boolean actionError = intent.getBooleanExtra(MainService.ACTION_ERROR, false);

                    Log.i(LOG_TAG, "responseMessage: " + responseMessage + " actionError: " + actionError);

                    if (actionError) {
                        mObservable.notifyPublishWebFailed(responseMessage);
                    } else {
                        mObservable.notifyPublishWebSucceeded();
                    }

                } else if (action.equals(MainService.ACTION_UPLOAD_IMAGE_CONTENT_RESULT)) {
                    String responseMessage = intent.getStringExtra(MainService.ACTION_UPLOAD_IMAGE_CONTENT_RESULT);
                    Boolean actionError = intent.getBooleanExtra(MainService.ACTION_ERROR, false);

                    Log.i(LOG_TAG, "responseMessage: " + responseMessage + " actionError: " + actionError);

                    if (actionError) {
                        mObservable.notifyUploadImageFailed(responseMessage);
                    } else {
                        mObservable.notifyUploadImageSucceeded();
                    }
                }

            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiverMainService, filter);

        Log.i(LOG_TAG, "receiverMainService register");

    }

    public void signIn(final String userName, final String password) {
        if (mModelState != ModelState.NONE) {
            return;
        }

        mUsername = userName;
        mPassword = password;

        mObservable.notifyStarted();

//        mIsWorking = true;

        login();
//        mSignInTask = new SignInTask(userName, password);
//        mSignInTask.execute();
    }

    public void sendWebRequest(String description) {
        Log.i(LOG_TAG, "sendWebRequest description: " + description);

        if (mModelState != ModelState.NONE) {
            return;
        }

        SimpleDateFormat sdfPublish = new SimpleDateFormat("yyyyMMdd HH:mm");

        Publication selectedPublication = null;

        for (Publication currPub : selectedProject.getPublications()) {
            if (currPub.isChecked()) {
                selectedPublication = currPub;
                break;
            }
        }

        if (selectedPublication == null) {
            mObservable.notifyPublishWebFailed(mContext.getString(R.string.category_not_selected));
            return;
        }

        try {
            //Construct JSON to post
            JSONObject jsonObject = new JSONObject();

            JSONObject schedule = new JSONObject();
            JSONObject scheduleItem = new JSONObject();
            JSONObject jsonImageRAW = new JSONObject();
            JSONObject customization = new JSONObject();

            jsonObject.put("project", selectedProject.getServerID());
            scheduleItem.put("message", description);
            scheduleItem.put("twitterMessage", "");

            schedule.put(sdfPublish.format(mPublishDate.getTime()), scheduleItem);
            jsonObject.put("schedule", schedule);

            if (selectedPublication.getServerID() == -1) {

                ArrayList<Integer> accountsID = new ArrayList<Integer>();

                if (selectedProject.getAccounts().size() > 0) {

                    Account currAccount;

                    for (int i = 0; i < selectedProject.getAccounts().size(); i++) {
                        currAccount = selectedProject.getAccounts().get(i);

                        if (currAccount.isChecked()) {
                            accountsID.add(currAccount.getServerID());

                            for (Board currBoard : currAccount.getBoards()) {

                                if (currBoard.getIsChecked()) {
                                    customization.put(String.valueOf(currAccount.getServerID()), new JSONObject().put("pinterest_board_id", currBoard.getServerID()));
                                    break;
                                }

                            }

                        }
                    }
                }

                if (accountsID.size() == 0) {
                    mObservable.notifyPublishWebFailed(mContext.getString(R.string.accounts_not_present));
                    return;
                }

                JSONArray jsonAccounts = new JSONArray(accountsID);

                Log.i(LOG_TAG, "accounts to string: " + jsonAccounts);

                jsonObject.put("name", selectedPublication.getName());
                jsonObject.putOpt("accounts", jsonAccounts);
                jsonObject.put("customization", customization);

            } else {
                jsonObject.putOpt("campaign", selectedPublication.getServerID());
            }


            if (mIsWebImageRAW) {
                if (imageRAW != null) {
                    String imgBase64 = Base64.encodeToString(imageRAW, Base64.DEFAULT);

                    jsonImageRAW.put("name", "image.jpg");
                    jsonImageRAW.put("image_data", imgBase64);

                    jsonObject.put("image_raw", jsonImageRAW);
                }
            } else {
                if (imgUrl != null) {
                    jsonObject.put("image", imgUrl);
                }
            }

            String resultJSON = jsonObject.toString();

            Log.i(LOG_TAG, "JSON resultJSON.length(): " + resultJSON.length());
            Log.i(LOG_TAG, "JSON to string:" + resultJSON);

            MainService.publishWebContent(mContext, resultJSON);

            mObservable.notifyPublishWebStarted();

        } catch (Exception e) {

            e.printStackTrace();

            mObservable.notifyPublishWebFailed(e.getMessage());

        }

    }

    public void stopSignIn() {
        if (mModelState != ModelState.NONE) {
            mModelState = ModelState.NONE;
        }

        if (receiver != null)
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
        if (receiverMainService != null)
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiverMainService);
    }

    // Methods for upload image

    public void sendUploadRequest(String description) {
        Log.i(LOG_TAG, "sendUploadRequest mModelState: " + mModelState);

        if (mModelState != ModelState.NONE) {
            return;
        }

        User currUser = Utils.getUser(mContext);

        ArrayList<Integer> accountsID = new ArrayList<Integer>();

        if (selectedProject.getAccounts().size() > 0) {

            Account currAccount;

            for (int i = 0; i < selectedProject.getAccounts().size(); i++) {
                currAccount = selectedProject.getAccounts().get(i);
                accountsID.add(currAccount.getServerID());
            }
        }

        //Construct JSON to post
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonImageRAW = new JSONObject();

        try {
            jsonObject.put("project", selectedProject.getServerID());
            jsonObject.put("title", description);
            jsonObject.put("description", description);

            Log.i(LOG_TAG, "imageRAW: " + imageRAW);

            if (imageRAW != null) {
                String imgBase64 = Base64.encodeToString(imageRAW, Base64.DEFAULT);


                jsonImageRAW.put("name", "image.jpg");
                jsonImageRAW.put("image_data", imgBase64);

                jsonObject.put("image_raw", jsonImageRAW);
            }

            String resultJSON = jsonObject.toString();

            Log.i(LOG_TAG, "JSON resultJSON.length(): " + resultJSON.length());
            Log.i(LOG_TAG, "JSON to string:" + resultJSON);

            MainService.uploadImage(mContext, resultJSON);

            mObservable.notifyUploadImageStarted();

        } catch (JSONException e) {

            e.printStackTrace();

            mObservable.notifyUploadImageFailed(e.getMessage());

        }

    }

    public void handleUploadImageData(Uri receivedUri) {

        Log.i(LOG_TAG, "handleUploadImageData receivedUri: " + receivedUri);

        if (receivedUri != null) {

            Bitmap bmp = null;
/*
            try {
                imageRAW = Utils.getByteArrayFromFile(Utils.getPath(mContext, receivedUri));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeByteArray(imageRAW, 0, imageRAW.length);
*/

            //New variant
            File file;

            file = new File(Utils.getPath(mContext, receivedUri));

            bmp = Utils.decodeFile(file, 640, 480);

            if (bmp != null) {
                Log.i(LOG_TAG, "handleUploadImageData bmp.getWidth(): " + bmp.getWidth() + " bmp.getHeight(): " + bmp.getHeight());

/*Scale image
                int width = 640;
                int height = 480;

                double widthPart = (double) width / (double) bmp.getWidth();
                double heightPart = (double) height / (double) bmp.getHeight();

                Log.i(LOG_TAG, "handleUploadImageData widthPart: " + widthPart + " heightPart: " + heightPart);

                if (heightPart < widthPart) {
                    width = (int) (width * heightPart);
                } else if (heightPart > widthPart) {
                    height = (int) (height * widthPart);
                }

                uploadImage = Bitmap.createScaledBitmap(bmp, width, height, true);

                Log.i(LOG_TAG, "handleUploadImageData resized.getWidth(): " + uploadImage.getWidth() + " resized.getHeight(): " + uploadImage.getHeight());
*/
                uploadImage = bmp;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                uploadImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageRAW = stream.toByteArray();
            }

        }

    }

    public Bitmap getUploadImage() {
        return uploadImage;
    }

    public byte[] getUploadImageRAW() {
        return imageRAW;
    }

    // Method for publish Web

    public void handleWebPublishData(String webURL) {

        Log.i(LOG_TAG, "handleWebPublishData receivedUri: " + webURL + " mModelState: " + mModelState);

        if (mModelState != ModelState.NONE) {
            return;
        }

        mWebURL = webURL;

        webview = new WebView(mContext);
        webview.addJavascriptInterface(new JsObject(), "CallToAnAndroidFunction");
        webview.getSettings().setDomStorageEnabled(true);
//        webview.getSettings().setLoadsImagesAutomatically(false);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                Log.i(LOG_TAG, "WebView onProgressChanged: " + progress);

                if (progress > mLastProgress) {

                    mObservable.notifyPublishWebLoadProgress(progress);

                    mLastProgress = progress;
                }

            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.i(LOG_TAG, "WebView onReceivedError: " + errorCode + " description: " + description);
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                Log.i(LOG_TAG, "WebView onPageFinished url: " + url + " mWebPageFinished: " + mWebPageFinished);

                if (mIsWebParsed) return;

                mIsWebParsed = true;


                try {
                    InputStream is = view.getContext().getAssets().open("webContentParserNoJQuery.js");

                    String parserJS = new String(Utils.getByteArrayFromFile(is), "utf-8");

                    Log.i(LOG_TAG, "parserJS.length: " + parserJS.length());

                    webview.loadUrl("javascript:" + parserJS);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        });
        mLastProgress = 0;

        webview.loadUrl(mWebURL);

        mObservable.notifyPublishWebLoadStarted();
    }

    public class JsObject {
        @JavascriptInterface
        public void toJava(final String[] images, final String tagTitle, final String tagDescription, final boolean facebookDefaultImage) {

            runOnUiThread(new Runnable() {

                              @Override
                              public void run() {

                                  Log.i(LOG_TAG, "JsObject toJava success");

                                  mImages = new ArrayList<String>(Arrays.asList(images));
                                  mTagTitle = tagTitle;
                                  mTagDescription = tagDescription;
                                  mFacebookDefaultImage = facebookDefaultImage;

                                  Log.i(LOG_TAG, "JsObject toJava success mImages.size(): " + mImages.size());
                                  //Fragment_WEB_Publish.this.textView_web.setText((mTagDescription != null && mTagDescription.length() > 0 ? mTagDescription : mTagTitle) + "\n" + webURL);

                                  //mWebShadow.setVisibility(View.GONE);

                                  if (mImages.size() > 0) {
                                      imgUrl = mImages.get(0);
                                      Log.i(LOG_TAG, "try to load image: " + imgUrl);

                                      AsyncHttpClient client = new AsyncHttpClient();

                                      client.get(imgUrl, new AsyncHttpResponseHandler() {
                                          @Override
                                          public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                              if (responseBody != null) {
                                                  Log.i(LOG_TAG, "onSuccess responseBody.length: " + responseBody.length + " Thread.currentThread(): " + Thread.currentThread());

                                                  try {

                                                      File tmpFile = new File(mContext.getApplicationInfo().dataDir/*context.getFilesDir()*/, "webImgData.txt");

                                                      FileOutputStream fos = new FileOutputStream(tmpFile);
                                                      fos.write(responseBody, 0, responseBody.length);
                                                      fos.flush();
                                                      fos.close();

                                                      mWebImage = Utils.decodeFile(tmpFile, 640, 480);

                                                      if (mWebImage != null) {
                                                          Log.i(LOG_TAG, "bmp.getWidth(): " + mWebImage.getWidth() + " bmp.getHeight(): " + mWebImage.getHeight());
/*
                                                          if (mWebImage.getWidth() > 640 || mWebImage.getHeight() > 480) {
                                                              mWebImage = Bitmap.createScaledBitmap(mWebImage, 640, 480, true);
                                                              Log.i(LOG_TAG, "resized.getWidth(): " + mWebImage.getWidth() + " resized.getHeight(): " + mWebImage.getHeight());
                                                          }
*/
                                                          //
                                                          mObservable.notifyPublishWebLoadSucceeded();
                                                      } else {

                                                      }


                                                  } catch (IOException e) {
                                                      Log.i(LOG_TAG, e.getMessage());
                                                  }


                                              } else {
                                                  Log.i(LOG_TAG, "onSuccess responseBody is NULL");
                                              }

                                          }

                                          @Override
                                          public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                              Log.i(LOG_TAG, "onFailure error.getMessage(): " + error.getMessage());
                                          }
                                      });

                                  }

                                  mObservable.notifyPublishWebLoadSucceeded();

                              }
                          }

            );

        }
    }
    //

    public void registerObserver(final Observer observer) {
        mObservable.registerObserver(observer);

        if (mModelState == ModelState.LOGIN) {
            observer.onSignInStarted(this);
        } else if (mModelState == ModelState.WEB_CONTENT_PARSE) {
            observer.onPublishWebLoadStarted(this);
        } else if (mModelState == ModelState.PUBLISH_WEB) {
            observer.onPublishStarted(this);
        } else if (mModelState == ModelState.UPLOAD_IMAGE) {
            observer.onUploadhStarted(this);
        }
    }

    public void unregisterObserver(final Observer observer) {
        mObservable.unregisterObserver(observer);
    }

    public interface Observer {
        /// Login
        void onSignInStarted(SignInModel signInModel);

        void onSignInSucceeded(SignInModel signInModel);

        void onSignInFailed(SignInModel signInModel);

        /// Publish Web
        void onPublishStarted(SignInModel signInModel);

        void onPublishSucceeded(SignInModel signInModel);

        void onPublishFailed(SignInModel signInModel);


        /// Upload image
        void onUploadhStarted(SignInModel signInModel);

        void onUploadSucceeded(SignInModel signInModel);

        void onUploadFailed(SignInModel signInModel);

        void onPublishWebLoadStarted(SignInModel signInModel);

        void onPublishWebLoadProgress(SignInModel signInModel, int progress);

        void onPublishWebLoadSucceeded(SignInModel signInModel);

        void onPublishWebLoadFailed(SignInModel signInModel);
    }

    private class SignInObservable extends Observable<Observer> {

        //Login

        public void notifyStarted() {

            mModelState = ModelState.LOGIN;

            for (final Observer observer : mObservers) {
                observer.onSignInStarted(SignInModel.this);
            }
        }

        public void notifySucceeded() {

            mModelState = ModelState.NONE;

            for (final Observer observer : mObservers) {
                observer.onSignInSucceeded(SignInModel.this);
            }
        }

        public void notifyFailed(String errorMessage) {

            mModelState = ModelState.NONE;

            mResultMessage = errorMessage;

            for (final Observer observer : mObservers) {
                observer.onSignInFailed(SignInModel.this);
            }
        }

        ///Publish Web
        //Load
        public void notifyPublishWebLoadStarted() {

            mModelState = ModelState.WEB_CONTENT_PARSE;

            for (final Observer observer : mObservers) {
                observer.onPublishWebLoadStarted(SignInModel.this);
            }
        }

        public void notifyPublishWebLoadProgress(int progress) {
            for (final Observer observer : mObservers) {
                observer.onPublishWebLoadProgress(SignInModel.this, progress);
            }
        }

        public void notifyPublishWebLoadSucceeded() {

            mModelState = ModelState.NONE;

            for (final Observer observer : mObservers) {
                observer.onPublishWebLoadSucceeded(SignInModel.this);
            }
        }

        public void notifyPublishWebLoadFailed(String errorMessage) {

            mModelState = ModelState.NONE;

            mResultMessage = errorMessage;

            for (final Observer observer : mObservers) {
                observer.onPublishWebLoadFailed(SignInModel.this);
            }
        }

        //Publish
        public void notifyPublishWebStarted() {

            mModelState = ModelState.PUBLISH_WEB;

            for (final Observer observer : mObservers) {
                observer.onPublishStarted(SignInModel.this);
            }
        }

        public void notifyPublishWebSucceeded() {

            mModelState = ModelState.NONE;

            for (final Observer observer : mObservers) {
                observer.onPublishSucceeded(SignInModel.this);
            }
        }

        public void notifyPublishWebFailed(String errorMessage) {

            mModelState = ModelState.NONE;

            mResultMessage = errorMessage;

            for (final Observer observer : mObservers) {
                observer.onPublishFailed(SignInModel.this);
            }
        }

        ///Upload image
        public void notifyUploadImageStarted() {

            mModelState = ModelState.UPLOAD_IMAGE;

            for (final Observer observer : mObservers) {
                observer.onUploadhStarted(SignInModel.this);
            }
        }

        public void notifyUploadImageSucceeded() {

            mModelState = ModelState.NONE;

            for (final Observer observer : mObservers) {
                observer.onUploadSucceeded(SignInModel.this);
            }
        }

        public void notifyUploadImageFailed(String errorMessage) {

            mModelState = ModelState.NONE;

            mResultMessage = errorMessage;

            for (final Observer observer : mObservers) {
                observer.onUploadFailed(SignInModel.this);
            }
        }

    }

    public String getResultMessage() {
        return mResultMessage;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    private void login() {

        Log.i(LOG_TAG, "login()");

        User currUser = Utils.getUser(mContext);

        Log.i(LOG_TAG, "login() startAuthenticate");

        Call<ResponseBody> c = restAPIService.login(mUsername, mPassword);

        c.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {

                String result = "";

                try {
                    result = new String(response.body().bytes());

                    Log.i(LOG_TAG, "restAPIService.login onResponse: " + result);

                    JSONArray resultJSONArray = new JSONArray(result);

                    Log.i(LOG_TAG, "restAPIService.login resultJSONArray: " + resultJSONArray);

                    User currUser = new User();

                    // Pull out the first event on the public timeline
                    JSONObject firstItem = null;
                    try {
                        firstItem = (JSONObject) resultJSONArray.get(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String access_key = null;

                    access_key = firstItem.getString("token");
                    currUser.setToken(access_key);

                    // Do something with the response
                    Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() access_key: " + access_key);

                    JSONObject secondItem = null;
                    JSONObject userItem = null;

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

                    try {
                        secondItem = (JSONObject) resultJSONArray.get(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (secondItem != null) {

                        userItem = secondItem.getJSONObject("user");

                        try {
                            currUser.setId(userItem.getInt("id"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setFirstName(userItem.getString("firstName"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setLastName(userItem.getString("lastName"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setEmail(userItem.getString("email"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setCreated(dateFormat.parse(userItem.getString("created")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setLastLogin(dateFormat.parse(userItem.getString("lastLogin")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setPhoto(userItem.getString("photo"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            currUser.setDeleted(userItem.getString("deleted").equalsIgnoreCase("no") ? false : true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    if (currUser.getToken() != null && currUser.getToken().length() > 0) {
                        //Success
                        Utils.saveUser(mContext, currUser);
                        updateData(currUser.getToken());
                    }


                } catch (Exception e) {
                    mObservable.notifyFailed(e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i(LOG_TAG, "restAPIService.login onFailure: " + t.getMessage());
                mObservable.notifyFailed(t.getMessage());
            }
        });

    }

    private void updateData(String access_key) {

        final User currUser = Utils.getUser(mContext);

        Log.i(LOG_TAG, "updateData currUser.getToken(): " + currUser.getToken() + " currentThread: " + Thread.currentThread().getId() + " currUser: " + currUser);

        Call<ResponseBody> c = restAPIService.projects(currUser.getToken());

        c.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {

                String result = "";

                try {
                    result = new String(response.body().bytes());

                    Log.i(LOG_TAG, "restAPIService.projects onResponse: " + result);

                    JSONArray resultJSONArray = new JSONArray(result);

                    Log.i(LOG_TAG, "restAPIService.projects resultJSONArray: " + resultJSONArray);

                    //Delete previous projects data
                    projectDAO.deleteAll();

                    Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray length: " + resultJSONArray.length() + " JSONArray: " + resultJSONArray + " currentThread: " + Thread.currentThread().getId());

                    JSONObject item = null;
                    String project_name = null;
                    Project project = new Project();

                    if (resultJSONArray.length() == 0) {
                        mObservable.notifySucceeded();
                        return;
                    }

                    Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray try to extract projects_1");

                    item = (JSONObject) resultJSONArray.get(0);

                    Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray try to extract projects_2 item: " + item);

                    try {
                        if (item.getString("error") != null) {
                            Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray try to extract projects_3");
                            mObservable.notifyFailed(item.getString("error"));
                            return;
                        }
                    } catch (JSONException e) {
                        Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray try to extract projects_4 no error");
                    }

                    Log.i(LOG_TAG, "AsyncHttpClient.onSuccess() JSONArray try to extract projects");

                    for (int i = 0; i < resultJSONArray.length(); i++) {

                        project = new Project();

                        try {
                            item = (JSONObject) resultJSONArray.get(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            project.setServerID(item.getInt("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            project.setName(item.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            project.setUser(Integer.valueOf(item.getString("user")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            project.setTimezone(item.getString("timezone"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {

                            DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm Z", Locale.ENGLISH);

                            project.setCreated(format.parse(item.getString("created")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.i(LOG_TAG, "project.getServerID(): " + project.getServerID() + "project.getName(): " + project.getName());

                        project.setID(projectDAO.insert(project));

                        Log.i(LOG_TAG, "project.getID(): " + project.getID());

                    }

                    ArrayList<Project> projects = projectDAO.getProjectList();

                    Log.i(LOG_TAG, "intentAccountService");

                    //Subscribe to intent from AccountIntentService
                    IntentFilter filter = new IntentFilter();

                    filter.addAction(AccountsRequestReceiver.PROCESS_RESPONSE);

                    filter.addCategory(Intent.CATEGORY_DEFAULT);
                    receiver = new AccountsRequestReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            super.onReceive(context, intent);

                            String action = intent.getAction();

                            if (action.equals(AccountsRequestReceiver.PROCESS_RESPONSE)) {
                                String responseMessage = intent.getStringExtra(AccountIntentService.RESPONSE_MESSAGE);
                                Log.i(LOG_TAG, "AccountsRequestReceiver responseMessage: " + responseMessage);

                                ArrayList<Project> projectsAccounts = projectDAO.getProjectList();
                                Project currProject;

                                for (int i = 0; i < projectsAccounts.size(); i++) {
                                    currProject = projectsAccounts.get(i);
                                    Log.i(LOG_TAG, "i: " + i + " currProject: " + currProject);
                                }

                                if (receiver != null) {
                                    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
                                    receiver = null;
                                }

                                mObservable.notifySucceeded();
                            }
                        }
                    };
                    LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);

                    // Запускаем свой IntentService
                    Intent intentAccountService = new Intent(mContext, AccountIntentService.class);

                    Log.i(LOG_TAG, "intentAccountService_1 Thread.currentThread().getId(): " + Thread.currentThread().getId());

                    mContext.startService(intentAccountService.putExtra("projects", projects).putExtra("access_key", currUser.getToken()));

                    Log.i(LOG_TAG, "intentAccountService_2");

                } catch (Exception e) {
                    mObservable.notifyFailed(e.getMessage());
                }

            }

            @Override
            public void onFailure(Throwable t) {
                mObservable.notifyFailed(t.getMessage());
            }

        });

    }

}
