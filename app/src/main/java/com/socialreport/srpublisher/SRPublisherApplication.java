package com.socialreport.srpublisher;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.socialreport.srpublisher.DB.DBHelper;

/**
 * Created by aleksandrbogomolov on 10/26/15.
 */
public class SRPublisherApplication extends Application {

    private static String LOG_TAG = SRPublisherApplication.class.getCanonicalName();

    private static volatile DBHelper mDbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(LOG_TAG, "onCreate");

        mDbHelper = new DBHelper(getApplicationContext());

        // Запускаем свой MainService
        MainService.start(getApplicationContext());

    }

    public static DBHelper getDbHelper() {

        //Log.i(LOG_TAG, "getDbHelper");

        return mDbHelper;
    }
}
