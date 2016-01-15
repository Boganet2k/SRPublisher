package com.socialreport.srpublisher.DB;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 8;

    // Database Name
    private static final String DATABASE_NAME = "SRPublisher.db";

    public DBHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //super(context, Environment.getExternalStorageDirectory() + File.separator + "/DataBase/" + File.separator + DATABASE_NAME, null, DATABASE_VERSION);

        //super(new DatabaseContext(context), DATABASE_NAME, null, DATABASE_VERSION);

        Log.i("DBHelper", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
        Log.i("DBHelper", "onCreate");

        String CREATE_TABLE_PROJECT = "CREATE TABLE " + Project.TABLE  + "("
                + Project.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Project.KEY_SERVER_ID + " INTEGER, "
                + Project.KEY_NAME + " TEXT, "
                + Project.KEY_USER + " INTEGER, "
                + Project.KEY_TIME_ZONE + " TEXT, "
                + Project.KEY_CREATED + " INTEGER)";

        String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + Account.TABLE  + "("
                + Account.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Account.KEY_PROJECT_ID + " INTEGER, "
                + Account.KEY_SERVER_ID + " INTEGER, "
                + Account.KEY_NAME + " TEXT, "
                + Account.KEY_ACTIVE + " INTEGER, "
                + Account.KEY_TYPE + " TEXT, "
                + Account.KEY_IMAGE + " TEXT, "
                + Account.KEY_NETWORK_ICON + " TEXT, "
                + Account.KEY_ACCESS + " INTEGER, "
                + Account.KEY_PUBLISH + " INTEGER)";

        String CREATE_TABLE_PROJECT_ACCOUNT = "CREATE TABLE " + ProjectAccount.TABLE  + "("
                + ProjectAccount.KEY_PROJECT_ID + " INTEGER, "
                + ProjectAccount.KEY_ACCOUNT_ID + " INTEGER)";

        String CREATE_TABLE_PUBLICATION = "CREATE TABLE " + Publication.TABLE  + "("
                + Publication.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Publication.KEY_PROJECT_ID + " INTEGER, "
                + Publication.KEY_SERVER_ID + " INTEGER, "
                + Publication.KEY_STATUS + " TEXT, "
                + Publication.KEY_NAME + " TEXT, "
                + Publication.KEY_TYPE + " TEXT, "
                + Publication.KEY_APPROVED + " INTEGER)";

        String CREATE_TABLE_BOARD = "CREATE TABLE " + Board.TABLE  + "("
                + Board.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Board.KEY_ACCOUNT_ID + " INTEGER, "
                + Board.KEY_SERVER_ID + " TEXT, "
                + Board.KEY_NAME + " TEXT, "
                + Board.KEY_IMAGE + " TEXT, "
                + Board.KEY_URL + " INTEGER)";

        db.execSQL(CREATE_TABLE_PROJECT);
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_PROJECT_ACCOUNT);
        db.execSQL(CREATE_TABLE_PUBLICATION);
        db.execSQL(CREATE_TABLE_BOARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DBHelper", "onUpgrade");

        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + ProjectAccount.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Project.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Account.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Publication.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Board.TABLE);

        // Create tables again
        onCreate(db);

    }

}

class DatabaseContext extends ContextWrapper {

    private static final String DEBUG_CONTEXT = "DatabaseContext";

    public DatabaseContext(Context base) {
        super(base);
        Log.i("DatabaseContext", "DatabaseContext");
    }

    @Override
    public File getDatabasePath(String name)
    {
        Log.i("DatabaseContext", "getDatabasePath");

        File sdcard = Environment.getExternalStorageDirectory();
        //String dbfile = sdcard.getAbsolutePath() + File.separator + "databases" + File.separator + name;
        String dbfile = "/storage/extSdCard/Data" + File.separator + "databases" + File.separator + name;

        Log.i(DEBUG_CONTEXT, "getDatabasePath dbfile: " + dbfile);

        if (!dbfile.endsWith(".db"))
        {
            dbfile += ".db" ;
        }

        File result = new File(dbfile);

        if (!result.getParentFile().exists())
        {
            result.getParentFile().mkdirs();
        }

        if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN))
        {
            Log.w(DEBUG_CONTEXT, "getDatabasePath(" + name + ") = " + result.getAbsolutePath());
        }

        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler)
    {
        Log.i("DatabaseContext", "openOrCreateDatabase befor name: " + name);

        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);

        Log.i("DatabaseContext", "openOrCreateDatabase after name: " + name);

        // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
        if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN))
        {
            Log.w(DEBUG_CONTEXT, "openOrCreateDatabase(" + name + ",,) = " + result.getPath());
        }
        return result;
    }

}

