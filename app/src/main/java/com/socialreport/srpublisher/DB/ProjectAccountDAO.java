package com.socialreport.srpublisher.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.socialreport.srpublisher.SRPublisherApplication;

/**
 * Created by aleksandrbogomolov on 11/29/15.
 */
public class ProjectAccountDAO {

    private static String LOG_TAG = ProjectAccountDAO.class.getName();

    private final DBHelper dbHelper;

    public ProjectAccountDAO(Context context) {
        dbHelper = SRPublisherApplication.getDbHelper();
    }

    public void insert(long projectId, long accountId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        insert(db, projectId, accountId);

        db.close();
    }

    public void insert(SQLiteDatabase db, long projectId, long accountId) {

        Log.i(LOG_TAG, "insert");

        int count = isExist(db, projectId, accountId);

        Log.i(LOG_TAG, "insert count: " + count);

        if ( count > 0) {
            return;
        }

        ContentValues values = new ContentValues();

        values.put(ProjectAccount.KEY_PROJECT_ID, projectId);
        values.put(ProjectAccount.KEY_ACCOUNT_ID, accountId);

        Log.i(LOG_TAG, "insert values: " + values);

        // Inserting Row
        long projectAccountId = db.insert(ProjectAccount.TABLE, null, values);

        Log.i(LOG_TAG, "insert projectAccountId: " + projectAccountId);

    }

    public int isExist(SQLiteDatabase db, long projectId, long accountId){

        String selectQuery =  "SELECT  " +
                ProjectAccount.ALL_FIELDS +
                " FROM " + ProjectAccount.TABLE
                + " WHERE " +
                ProjectAccount.KEY_PROJECT_ID + " = ? AND " + ProjectAccount.KEY_ACCOUNT_ID + " = ?";// It's a good practice to use parameter ?, instead of concatenate string

        int result = 0;

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(projectId), String.valueOf(accountId)} );

        result = cursor.getCount();

        cursor.close();

        return result;
    }

}
