package com.socialreport.srpublisher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.DB.Publication;
import com.socialreport.srpublisher.DB.PublicationDAO;
import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import retrofit.Call;
import retrofit.Retrofit;

public class MainService extends Service {

    private static String LOG_TAG = MainService.class.getCanonicalName();

    Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();
    SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

    private static final String ACTION_START_SERVICE_FROM_APPLICATION_CREATE = MainService.class.getCanonicalName() + ".ACTION_START_SERVICE_FROM_APPLICATION_CREATE";
    public static final String ACTION_ERROR = MainService.class.getCanonicalName() + ".ACTION_ERROR";

    private static final String ACTION_PUBLISH_WEB_CONTENT = MainService.class.getCanonicalName() + ".ACTION_PUBLISH_WEB_CONTENT";
    private static final String PUBLISH_WEB_CONTENT = MainService.class.getCanonicalName() + ".PUBLISH_WEB_CONTENT";
    public static final String ACTION_PUBLISH_WEB_CONTENT_RESULT = MainService.class.getCanonicalName() + ".ACTION_PUBLISH_WEB_CONTENT_RESULT";

    private static final String ACTION_UPLOAD_IMAGE_CONTENT = MainService.class.getCanonicalName() + ".ACTION_UPLOAD_IMAGE_CONTENT";
    private static final String UPLOAD_IMAGE_CONTENT = MainService.class.getCanonicalName() + ".UPLOAD_IMAGE_CONTENT";
    public static final String ACTION_UPLOAD_IMAGE_CONTENT_RESULT = MainService.class.getCanonicalName() + ".ACTION_UPLOAD_IMAGE_CONTENT_RESULT";

    private static String dirPath = "/storage/extSdCard/Data";

    static Context mContext;

    static Handler mServiceThreadHandler;
    HandlerThread mServiceThread;

    static Handler UIHandler;
    static User currUser;
    ProjectDAO projectDAO = new ProjectDAO(this);

    boolean startServiceThread() {

        Log.i(LOG_TAG, "startServiceThread");

        UIHandler = new Handler();

        mServiceThread = new HandlerThread("MainServiceThreadNotMain");
        mServiceThread.start();

        mServiceThreadHandler = new Handler(mServiceThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {

                Log.i(LOG_TAG, "mServiceThreadHandler handleMessage msg.getData(): " + msg.getData());
                String task = msg.getData().getString("task");

                if (task.equals(ACTION_PUBLISH_WEB_CONTENT)) {
                    String jsonData = msg.getData().getString("taskData");

                    Log.i(LOG_TAG, "jsonData: " + jsonData + " Thread.currentThread(): " + Thread.currentThread().getId());

                    String resultJsonData = readDataFromFile(jsonData);

                    JSONObject resultJSONObject = null;
                    String nameForNewPublication = null;
                    int projectID = -1;

                    try {
                        resultJSONObject = new JSONObject(resultJsonData);
                        nameForNewPublication = resultJSONObject.getString("name");
                        projectID = resultJSONObject.getInt("project");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i(LOG_TAG, "Restore JSONObject Exception: " + e.getMessage());
                    }

                    Log.i(LOG_TAG, "resultJsonObject: " + resultJSONObject);
                    Log.i(LOG_TAG, "resultJsonData.length(): " + resultJsonData.length() + " resultJsonData: " + resultJsonData);

                    //Make request
                    Call<ResponseBody> c = restAPIService.publish(currUser.getToken(), RequestBody.create(MediaType.parse(""), resultJsonData));

                    ResponseBody response = null;
                    try {
                        response = c.execute().body();
                        String result = new String(response.bytes());

                        JSONArray resultJSONArray = new JSONArray(result);

                        Log.i(LOG_TAG, "restAPIService.publish resultJSONArray: " + resultJSONArray);

                        boolean isError = false;
                        String resultMessage = "Result ACTION_PUBLISH_WEB_CONTENT from MainService to model";

                        if (resultJSONArray != null && resultJSONArray.length() > 0) {
                            JSONObject firstItem = (JSONObject) resultJSONArray.get(0);

                            try {
                                resultMessage = (String) firstItem.get("error");
                                isError = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.i(LOG_TAG, "isError: " + isError + " projectID: " + projectID + " nameForNewPublication: " + nameForNewPublication);

                            if (!isError && projectID != -1) {
                                try {
                                    String serverIDForNewPublication = (String) firstItem.get("id");

                                    Publication newPublication = new Publication();
                                    newPublication.setName(nameForNewPublication);
                                    newPublication.setServerID(Integer.valueOf(serverIDForNewPublication));

                                    Project currProject = projectDAO.getProjectByServerId(projectID);
                                    currProject.getPublications().add(newPublication);

                                    projectDAO.updateBulk(currProject);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        //Send notification about request result

                        final String finalResultMessage = resultMessage;
                        final boolean finalIsError = isError;

                        Log.i(LOG_TAG, "Post request result " + " Thread.currentThread(): " + Thread.currentThread().getId());

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(ACTION_PUBLISH_WEB_CONTENT_RESULT);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(ACTION_PUBLISH_WEB_CONTENT_RESULT, finalResultMessage);
                        broadcastIntent.putExtra(ACTION_ERROR, finalIsError);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);

                    } catch (final Exception e) {
                        e.printStackTrace();

                        //Send notification error about request result

                        Log.i(LOG_TAG, "Post error request result " + " Thread.currentThread(): " + Thread.currentThread().getId());

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(ACTION_PUBLISH_WEB_CONTENT_RESULT);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(ACTION_PUBLISH_WEB_CONTENT_RESULT, e.getMessage());
                        broadcastIntent.putExtra(ACTION_ERROR, true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);

                    }

                } else if (task.equals(ACTION_UPLOAD_IMAGE_CONTENT)) {
                    String jsonData = msg.getData().getString("taskData");

                    Log.i(LOG_TAG, "jsonData: " + jsonData + " Thread.currentThread(): " + Thread.currentThread().getId());

                    String resultJsonData = readDataFromFile(jsonData);

                    Log.i(LOG_TAG, "resultJsonData.length(): " + resultJsonData.length() + " resultJsonData: " + resultJsonData);

                    Call<ResponseBody> c = restAPIService.upload(currUser.getToken(), RequestBody.create(MediaType.parse(""), resultJsonData));

//                    Call<ResponseBody> c = restAPIService.upload(currUser.getToken(), RequestBody.create(MediaType.parse(""), jsonData));

                    ResponseBody response = null;
                    try {
                        response = c.execute().body();
                        String result = new String(response.bytes());

                        Log.i(LOG_TAG, "restAPIService.upload result: " + result);

                        JSONObject resultJSON = new JSONObject(result);

                        boolean isError = false;
                        String resultMessage = "Result ACTION_UPLOAD_IMAGE_CONTENT_RESULT from MainService to model";

                        try {
                            resultMessage = (String) resultJSON.get("error");
                            isError = true;
                        } catch (Exception e) {
                        }

                        //Send notification about request result

                        final String finalResultMessage = resultMessage;
                        final boolean finalIsError = isError;

                        Log.i(LOG_TAG, "Post request result " + " Thread.currentThread(): " + Thread.currentThread().getId());

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(ACTION_UPLOAD_IMAGE_CONTENT_RESULT);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(ACTION_UPLOAD_IMAGE_CONTENT_RESULT, finalResultMessage);
                        broadcastIntent.putExtra(ACTION_ERROR, finalIsError);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);

                    } catch (final Exception e) {
                        e.printStackTrace();

                        //Send notification error about request result
                        Log.i(LOG_TAG, "Post error request result " + " Thread.currentThread(): " + Thread.currentThread().getId());

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(ACTION_UPLOAD_IMAGE_CONTENT_RESULT);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra(ACTION_UPLOAD_IMAGE_CONTENT_RESULT, e.getMessage());
                        broadcastIntent.putExtra(ACTION_ERROR, true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);

                    }

                }

            }
        };

        return true;
    }

    private String readDataFromFile(String fileName) {
        Reader reader = null;
        StringBuffer strBuffer = new StringBuffer();
        char[] buffer = new char[1024];

        int len;
        try {
            reader = new InputStreamReader(new FileInputStream(new File(fileName)));

            while ((len = reader.read(buffer)) != -1) {
                strBuffer.append(buffer, 0, len);
            }
            ;

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();

            Log.i(LOG_TAG, e.getMessage());
        }

        String resultJsonData = strBuffer.toString();

        return resultJsonData;
    }

    @Override
    public void onCreate() {
        //super.onCreate();

        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(LOG_TAG, "onStartCommand  flag: " + flags + " startId: " + startId + " intent: " + intent + " mServiceThread: " + mServiceThread);

        dirPath = getApplicationInfo().dataDir;

        if (mServiceThread == null) {
            startServiceThread();
        }

        if (intent != null) {
            String action = intent.getAction();
            Log.i(LOG_TAG, "onStartCommand action: " + action);

            if (action.equals(ACTION_PUBLISH_WEB_CONTENT)) {
                String jsonData = intent.getStringExtra(PUBLISH_WEB_CONTENT);

                Message task = mServiceThreadHandler.obtainMessage();

                Bundle taskData = new Bundle();
                taskData.putString("task", ACTION_PUBLISH_WEB_CONTENT);
                taskData.putString("taskData", jsonData);

                task.setData(taskData);

                mServiceThreadHandler.sendMessage(task);

            } else if (action.equals(ACTION_UPLOAD_IMAGE_CONTENT)) {
                String jsonData = intent.getStringExtra(UPLOAD_IMAGE_CONTENT);

                Message task = mServiceThreadHandler.obtainMessage();

                Bundle taskData = new Bundle();
                taskData.putString("task", ACTION_UPLOAD_IMAGE_CONTENT);
                taskData.putString("taskData", jsonData);

                task.setData(taskData);

                mServiceThreadHandler.sendMessage(task);

            } else if (action.equals(ACTION_START_SERVICE_FROM_APPLICATION_CREATE)) {
                String message = intent.getStringExtra("message");
                Log.i(LOG_TAG, "onStartCommand message: " + message);

            }

        } else {
            Log.i(LOG_TAG, "onStartCommand restarted after close");
        }

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();

        Log.i(LOG_TAG, "onDestroy");

        mServiceThread.quit();
    }

    public MainService() {

        Log.i(LOG_TAG, "MainService()");

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void start(Context context) {

        mContext = context;
        currUser = Utils.getUser(mContext);

        Intent intentMainService = new Intent(context, MainService.class);
        intentMainService.setAction(ACTION_START_SERVICE_FROM_APPLICATION_CREATE);

        Log.i(LOG_TAG, "MainService Thread.currentThread().getId(): " + Thread.currentThread().getId() + " Thread.currentThread().getName(): " + Thread.currentThread().getName());

        context.startService(intentMainService.putExtra("message", "message_for_start_service"));
    }

    public static void updateUserData() {
        currUser = Utils.getUser(mContext);
    }

    public static void publishWebContent(Context context, String jsonData) {
        Log.i(LOG_TAG, "publishWebContent: " + jsonData);

        try {
            //FileWriter out = new FileWriter(new File(/*context.getFilesDir()*/"/storage/extSdCard/Data", "uploadData.txt"));
            OutputStreamWriter bos = new OutputStreamWriter(new FileOutputStream(new File(/*context.getFilesDir()*/dirPath, "publishData.txt")));
            bos.write(jsonData);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            Log.i(LOG_TAG, e.getMessage());
        }

        Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_PUBLISH_WEB_CONTENT);
//        intent.putExtra(PUBLISH_WEB_CONTENT, jsonData);
        intent.putExtra(PUBLISH_WEB_CONTENT, dirPath + "/publishData.txt");
        context.startService(intent);

    }

    public static void uploadImage(Context context, String jsonData) {
        Log.i(LOG_TAG, "uploadImage jsonData: " + jsonData);

        try {
            //FileWriter out = new FileWriter(new File(/*context.getFilesDir()*/"/storage/extSdCard/Data", "uploadData.txt"));
            OutputStreamWriter bos = new OutputStreamWriter(new FileOutputStream(new File(/*context.getFilesDir()*/dirPath, "uploadData.txt")));
            bos.write(jsonData);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            Log.i(LOG_TAG, e.getMessage());
        }

        Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_UPLOAD_IMAGE_CONTENT);
//        intent.putExtra(UPLOAD_IMAGE_CONTENT, jsonData);
        intent.putExtra(UPLOAD_IMAGE_CONTENT, /*context.getFilesDir()*/dirPath + "/uploadData.txt");
        context.startService(intent);

    }
}
