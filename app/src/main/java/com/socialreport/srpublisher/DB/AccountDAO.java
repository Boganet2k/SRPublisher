package com.socialreport.srpublisher.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.socialreport.srpublisher.SRPublisherApplication;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by bb on 18.09.15.
 */
public class AccountDAO {

    private static String LOG_TAG = AccountDAO.class.getName();

    private DBHelper dbHelper;
    private BoardDAO boardDAO;

    public AccountDAO(Context context) {
        //dbHelper = new DBHelper(context);
        dbHelper = SRPublisherApplication.getDbHelper();
        boardDAO = new BoardDAO();
    }

    private void setContentValue(Account account, ContentValues values) {

        values.put(Account.KEY_PROJECT_ID, account.getProjectID());
        values.put(Account.KEY_SERVER_ID, account.getServerID());
        values.put(Account.KEY_NAME, account.getName());
        values.put(Account.KEY_ACTIVE, account.isActive() ? 1 : 0);
        values.put(Account.KEY_TYPE, account.getType());
        values.put(Account.KEY_IMAGE, account.getImage());
        values.put(Account.KEY_NETWORK_ICON, account.getNetworkIcon());
        values.put(Account.KEY_ACCESS, account.isAccess() ? 1 : 0);
        values.put(Account.KEY_PUBLISH, account.isPublish() ? 1 : 0);

    }

    private void getAccountFromCursor(Account account, Cursor cursor) {

        account.setID(cursor.getInt(cursor.getColumnIndex(Account.KEY_ID)));
        account.setProjectID(cursor.getInt(cursor.getColumnIndex(Account.KEY_PROJECT_ID)));
        account.setServerID(cursor.getInt(cursor.getColumnIndex(Account.KEY_SERVER_ID)));
        account.setName(cursor.getString(cursor.getColumnIndex(Account.KEY_NAME)));
        account.setActive(cursor.getInt(cursor.getColumnIndex(Account.KEY_ACTIVE)) == 1 ? true : false);
        account.setType(cursor.getString(cursor.getColumnIndex(Account.KEY_TYPE)));
        account.setImage(cursor.getString(cursor.getColumnIndex(Account.KEY_IMAGE)));
        account.setNetworkIcon(cursor.getString(cursor.getColumnIndex(Account.KEY_NETWORK_ICON)));
        account.setAccess(cursor.getInt(cursor.getColumnIndex(Account.KEY_ACCESS)) == 1 ? true : false);
        account.setPublish(cursor.getInt(cursor.getColumnIndex(Account.KEY_PUBLISH)) == 1 ? true : false);

    }

    public int insert(Account account) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long account_Id = insert(db, account);

        db.close(); // Closing database connection

        return (int) account_Id;
    }

    public int insert(SQLiteDatabase db, Account account) {

        ContentValues values = new ContentValues();

        setContentValue(account, values);

        //Test for existing

        Account testForExist = getAccountByServerId(db, account.getServerID());

        if (testForExist.getID() != -1) {
            return testForExist.getID();
        }

        // Inserting Row
        long account_Id = db.insert(Account.TABLE, null, values);

        Board currBoard;
        for (int i = 0; i < account.getBoards().size(); i++) {
            currBoard = account.getBoards().get(i);
            currBoard.setAccountID((int) account_Id);

            currBoard.setID(boardDAO.insert(db, currBoard));

        }

        return (int) account_Id;
    }

    public void update(Account account) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        update(db, account);

        db.close(); // Closing database connection
    }

    public void update(SQLiteDatabase db, Account account) {

        ContentValues values = new ContentValues();

        setContentValue(account, values);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Account.TABLE, values, Account.KEY_ID + "= ?", new String[]{String.valueOf(account.getID())});

        Board currBoard;
        for (int i = 0; i < account.getBoards().size(); i++) {
            currBoard = account.getBoards().get(i);

            if (currBoard.getID() == -1) {
                currBoard.setAccountID((int) account.getID());
                currBoard.setID(boardDAO.insert(db, currBoard));
            } else {
                boardDAO.update(db, currBoard);
            }


        }
    }

    public void delete(int account_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Account.TABLE, Account.KEY_ID + "= ?", new String[]{String.valueOf(account_Id)});
        db.close(); // Closing database connection
    }

    public void deleteFoProject(int project_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(Account.TABLE, Account.KEY_PROJECT_ID + " = ?", new String[]{String.valueOf(project_Id)});
        db.close(); // Closing database connection
    }

    public void deleteAll() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        deleteAll(db);

        db.close(); // Closing database connection

    }

    public void deleteAll(SQLiteDatabase db) {

        db.delete(Account.TABLE, Account.KEY_ID + " > 0", new String[]{});

        boardDAO.deleteAll(db);

    }

    public ArrayList<Account>  geAll() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Account.ALL_FIELDS +
                " FROM " + Account.TABLE;

        ArrayList<Account> accountsList = new ArrayList<Account>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                Account account = new Account();

                getAccountFromCursor(account, cursor);

                account.setBoards(boardDAO.getBoardsForAccount(db, account.getID()));

                accountsList.add(account);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return accountsList;
    }

    public Account getAccountById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Account.ALL_FIELDS +
                " FROM " + Account.TABLE
                + " WHERE " +
                Account.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        Account account = new Account();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {

                getAccountFromCursor(account, cursor);

                account.setBoards(boardDAO.getBoardsForAccount(db, account.getID()));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return account;
    }

    public Account getAccountByServerId(SQLiteDatabase db, int Id){

        String selectQuery =  "SELECT  " +
                Account.ALL_FIELDS +
                " FROM " + Account.TABLE
                + " WHERE " +
                Account.KEY_SERVER_ID + " = ?";// It's a good practice to use parameter ?, instead of concatenate string

        Account account = new Account();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {

                getAccountFromCursor(account, cursor);

                account.setBoards(boardDAO.getBoardsForAccount(db, account.getID()));

            } while (cursor.moveToNext());
        }

        cursor.close();

        return account;
    }

    public ArrayList<Account> getAccountsForProject(int project_Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        /*
        select * from Account where Account.id in (select ProjectAccount.account_id from ProjectAccount where ProjectAccount.project_id = ?)
        */
        String selectQuery =  "SELECT  " +
                Account.ALL_FIELDS +
                " FROM " + Account.TABLE
                + " WHERE "
                + Account.KEY_ID + " IN ("
                + " SELECT " + ProjectAccount.KEY_ACCOUNT_ID
                + " FROM "
                + ProjectAccount.TABLE
                + " WHERE "
                + ProjectAccount.KEY_PROJECT_ID + " = ?)";

        ArrayList<Account> accounts = new ArrayList<Account>();

        Account account;

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(project_Id) } );

        Log.i(LOG_TAG, "getAccountsForProject cursor.getCount(): " + cursor.getCount());

        if (cursor.moveToFirst()) {

            do {
                account = new Account();

                getAccountFromCursor(account, cursor);

                account.setBoards(boardDAO.getBoardsForAccount(db, account.getID()));

                accounts.add(account);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return accounts;
    }
}
