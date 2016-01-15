package com.socialreport.srpublisher.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.socialreport.srpublisher.SRPublisherApplication;

import java.util.ArrayList;

/**
 * Created by aleksandrbogomolov on 11/30/15.
 */
public class PublicationDAO {

    private static String LOG_TAG = PublicationDAO.class.getName();

    private final DBHelper dbHelper;

    public PublicationDAO() {

        dbHelper = SRPublisherApplication.getDbHelper();
    }

    private void setContentValue(Publication publication, ContentValues values) {

        values.put(Publication.KEY_PROJECT_ID, publication.getProjectID());
        values.put(Publication.KEY_SERVER_ID, publication.getServerID());
        values.put(Publication.KEY_STATUS, publication.getStatus());
        values.put(Publication.KEY_NAME, publication.getName());
        values.put(Publication.KEY_TYPE, publication.getType());
        values.put(Publication.KEY_APPROVED, publication.isApproved() ? 1 : 0);

    }

    private void getPublicationFromCursor(Publication publication, Cursor cursor) {

        publication.setID(cursor.getInt(cursor.getColumnIndex(publication.KEY_ID)));
        publication.setProjectID(cursor.getInt(cursor.getColumnIndex(publication.KEY_PROJECT_ID)));
        publication.setServerID(cursor.getInt(cursor.getColumnIndex(publication.KEY_SERVER_ID)));
        publication.setStatus(cursor.getString(cursor.getColumnIndex(publication.KEY_STATUS)));
        publication.setName(cursor.getString(cursor.getColumnIndex(publication.KEY_NAME)));
        publication.setType(cursor.getString(cursor.getColumnIndex(publication.KEY_TYPE)));
        publication.setApproved(cursor.getInt(cursor.getColumnIndex(publication.KEY_APPROVED)) == 1 ? true : false);

    }

    public int insert(Publication publication) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long publication_Id = insert(db, publication);

        db.close(); // Closing database connection

        return (int) publication_Id;
    }

    public int insert(SQLiteDatabase db, Publication publication) {

        ContentValues values = new ContentValues();

        setContentValue(publication, values);

        //Test for existing

        Publication testForExist = getPublicationByServerId(db, publication.getServerID());

        if (testForExist.getID() != -1) {
            return testForExist.getID();
        }

        // Inserting Row
        long publication_Id = db.insert(Publication.TABLE, null, values);

        return (int) publication_Id;
    }

    public Publication getPublicationByServerId(SQLiteDatabase db, int Id) {

        String selectQuery =  "SELECT  " +
                Publication.ALL_FIELDS +
                " FROM " + Publication.TABLE
                + " WHERE " +
                Publication.KEY_SERVER_ID + " = ?";// It's a good practice to use parameter ?, instead of concatenate string

        Publication publication = new Publication();

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Id)});

        if (cursor.moveToFirst()) {
            do {

                getPublicationFromCursor(publication, cursor);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return publication;
    }

    public void update(Publication publication) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        update(db, publication);

        db.close(); // Closing database connection

    }

    public void update(SQLiteDatabase db, Publication publication) {

        ContentValues values = new ContentValues();

        setContentValue(publication, values);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Publication.TABLE, values, Publication.KEY_ID + "= ?", new String[]{String.valueOf(publication.getID())});
    }

    public void deleteAll() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        deleteAll(db);

        db.close(); // Closing database connection
    }

    public void deleteAll(SQLiteDatabase db) {
        db.delete(Publication.TABLE, null, new String[]{});
    }

    public ArrayList<Publication> getPublicationsForProject(int project_Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        /*
        select * from Account where Account.id in (select ProjectAccount.account_id from ProjectAccount where ProjectAccount.project_id = ?)
        */
        String selectQuery =  "SELECT  " +
                Publication.ALL_FIELDS +
                " FROM " + Publication.TABLE
                + " WHERE "
                + Publication.KEY_PROJECT_ID + " = ?";

        ArrayList<Publication> publications = new ArrayList<Publication>();

        Publication publication;

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(project_Id) } );

        Log.i(LOG_TAG, "getPublicationsForProject cursor.getCount(): " + cursor.getCount());

        if (cursor.moveToFirst()) {

            do {
                publication = new Publication();

                getPublicationFromCursor(publication, cursor);

                publications.add(publication);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return publications;
    }
}
