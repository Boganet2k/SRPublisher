package com.socialreport.srpublisher;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.DB.Publication;
import com.socialreport.srpublisher.retrofit.SocialReportRestAPI;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit.Call;
import retrofit.Retrofit;

/**
 * Created by bb on 17.09.15.
 */
public class AccountIntentService extends IntentService {

    final String LOG_TAG = AccountIntentService.class.getCanonicalName();
    public static final String RESPONSE_MESSAGE = "AccountResponseMessage";

    private String URL = null;
    private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;

    ProjectDAO projectDAO = new ProjectDAO(this);

    public AccountIntentService() {
        super("AccountIntentService");
        // TODO Auto-generated constructor stub
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final ArrayList<Project> projects = intent.getParcelableArrayListExtra("projects");
        final String access_key = intent.getStringExtra("access_key");

        Log.d(LOG_TAG, "onHandleIntent projects.size(): " + projects.size());

        Retrofit retrofit = new Retrofit.Builder().baseUrl(SocialReportRestAPI.baseURL).build();
        final SocialReportRestAPI restAPIService = retrofit.create(SocialReportRestAPI.class);

        final ExecutorService executorService = Executors.newFixedThreadPool(5);

        Log.d(LOG_TAG, "onHandleIntent start request: " + new Date());

        ArrayList<Callable<Project>> tasks = new ArrayList<Callable<Project>>();

        for (int i = 0; i < projects.size(); i++) {

            final Project currProject = projects.get(i);

            Callable<Project> task = new Callable<Project>() {

                @Override
                public Project call() throws Exception {

                    Call<ResponseBody> c = restAPIService.accounts(access_key, currProject.getServerID());

                    String result = "";

                    ResponseBody response = c.execute().body();

                    result = new String(response.bytes());

                    Log.i(LOG_TAG, "after retrofit execute currProject.getServerID(): " + currProject.getServerID() + " response: " + result);

                    JSONArray accountsJSON = new JSONArray(result);

                    if (accountsJSON.length() == 1) {
                        try {
                            ((JSONObject) accountsJSON.get(0)).getString("error");
                            return null;
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }
                    }

                    Account currAccount;
                    JSONObject currAccountJSON;

                    for (int j = 0; j < accountsJSON.length(); j++) {

                        currAccountJSON = (JSONObject) accountsJSON.get(j);

                        currAccount = new Account();

                        currAccount.setProjectID(currProject.getID());

                        try {
                            currAccount.setServerID(currAccountJSON.getInt("id"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setName(currAccountJSON.getString("name"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setActive(currAccountJSON.getString("active") == "true" ? true : false);
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setType(currAccountJSON.getString("type"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setImage(currAccountJSON.getString("image"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setNetworkIcon(currAccountJSON.getString("networkIcon"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setAccess(currAccountJSON.getString("access") == "true" ? true : false);
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currAccount.setPublish(currAccountJSON.getString("publish") == "true" ? true : false);
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {

                            Log.i(LOG_TAG, "befor reading board for currAccount.getID(): " + currAccount.getID() + " currAccount.getName(): " + currAccount.getName());

                            JSONArray boardsJSON = currAccountJSON.getJSONArray("boards");

                            Log.i(LOG_TAG, "after reading board boardsJSON: " + boardsJSON + " boardsJSON.length(): " + boardsJSON.length());

                            JSONObject currBoardJSON;
                            Board currBoard;

                            for (int i = 0; i < boardsJSON.length(); i++) {

                                currBoardJSON = (JSONObject) boardsJSON.get(i);
                                currBoard = new Board();

                                try {
                                    currBoard.setServerID(currBoardJSON.getString("id"));
                                } catch (JSONException e) {
                                    Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                                }

                                try {
                                    currBoard.setName(currBoardJSON.getString("name"));
                                } catch (JSONException e) {
                                    Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                                }

                                try {
                                    currBoard.setImage(currBoardJSON.getString("image"));
                                } catch (JSONException e) {
                                    Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                                }

                                try {
                                    currBoard.setUrl(currBoardJSON.getString("url"));
                                } catch (JSONException e) {
                                    Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                                }

                                currAccount.getBoards().add(currBoard);

                                Log.i(LOG_TAG, "after handle boards currAccount.getBoards().size(): " + currAccount.getBoards().size());
                            }

                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        currProject.getAccounts().add(currAccount);
                    }

                    //Request publications for mProject
                    c = restAPIService.publications(access_key, currProject.getServerID());

                    result = "";

                    response = c.execute().body();

                    result = new String(response.bytes());

                    Log.i(LOG_TAG, "after retrofit execute publications currProject.getServerID(): " + currProject.getServerID() + " response: " + result);

                    JSONArray publicationsJSON = new JSONArray(result);

                    Publication currPublication;
                    JSONObject currPublicationJSON;

                    for (int j = 0; j < publicationsJSON.length(); j++) {

                        currPublicationJSON = (JSONObject) publicationsJSON.get(j);

                        currPublication = new Publication();

                        currPublication.setProjectID(currProject.getID());

                        try {
                            currPublication.setServerID(currPublicationJSON.getInt("id"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currPublication.setStatus(currPublicationJSON.getString("status"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currPublication.setName(currPublicationJSON.getString("name"));

                            if (currPublication.getName().trim().length() == 0) continue;

                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currPublication.setType(currPublicationJSON.getString("type"));
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        try {
                            currPublication.setApproved(currPublicationJSON.getString("approved") == "true" ? true : false);
                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "e.getMessage(): " + e.getMessage());
                        }

                        currProject.getPublications().add(currPublication);

                    }

                    return currProject;
                }
            };

            tasks.add(task);
        }

        List<Future<Project>> tasksResult = null;

        try {
            tasksResult = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result = "Accounts received SUCCESS !!!";

        if (tasksResult != null) {

            for (Future<Project> taskResult : tasksResult) {
                try {
                    Log.d(LOG_TAG, "onHandleIntent befor result: " + new Date());
                    Project taskProject = taskResult.get(120, TimeUnit.SECONDS);

                    if (taskProject != null) {
                        projectDAO.updateBulk(taskProject);
                    }

                    Log.d(LOG_TAG, "onHandleIntent after result: " + new Date());
                } catch (InterruptedException e) {
                    result = "Accounts received 1 FAILURE!!!";
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    result = "Accounts received 2 FAILURE!!!";
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    result = "Accounts received 3 FAILURE!!!";
                    e.printStackTrace();
                }
            }

        } else {
            result = "Accounts received FAILURE!!!";
        }

        Log.d(LOG_TAG, "onHandleIntent after request: " + new Date());

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AccountsRequestReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_MESSAGE, result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
/*
        //Send notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(this.getResources().getText(R.string.app_name))
                        .setContentText(result);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(MainActivity.NOTIFICATION_PROCESS_RESPONSE);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
*/

    }

}
