package com.socialreport.srpublisher.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.socialreport.srpublisher.SRPublisherApplication;

import java.util.ArrayList;

/**
 * Created by aleksandrbogomolov on 11/30/15.
 */
public class BoardDAO {

    private static String LOG_TAG = BoardDAO.class.getName();

    private DBHelper dbHelper;

    public BoardDAO() {
        dbHelper = SRPublisherApplication.getDbHelper();
    }

    private void setContentValue(Board board, ContentValues values) {

        values.put(Board.KEY_ACCOUNT_ID, board.getAccountID());
        values.put(Board.KEY_SERVER_ID, board.getServerID());
        values.put(Board.KEY_NAME, board.getName());
        values.put(Board.KEY_IMAGE, board.getImage());
        values.put(Board.KEY_URL, board.getUrl());

    }

    private void getBoardFromCursor(Board board, Cursor cursor) {

        board.setID(cursor.getInt(cursor.getColumnIndex(Board.KEY_ID)));
        board.setAccountID(cursor.getInt(cursor.getColumnIndex(Board.KEY_ACCOUNT_ID)));
        board.setServerID(cursor.getString(cursor.getColumnIndex(Board.KEY_SERVER_ID)));
        board.setName(cursor.getString(cursor.getColumnIndex(Board.KEY_NAME)));
        board.setImage(cursor.getString(cursor.getColumnIndex(Board.KEY_IMAGE)));
        board.setUrl(cursor.getString(cursor.getColumnIndex(Board.KEY_URL)));

    }

    public int insert(Board board) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long board_Id = insert(db, board);

        db.close(); // Closing database connection

        return (int) board_Id;
    }

    public int insert(SQLiteDatabase db, Board board) {

        ContentValues values = new ContentValues();

        setContentValue(board, values);

        //Test for existing

        Board testForExist = getBoardByServerId(db, board.getServerID());

        Log.i(LOG_TAG, " testForExist.getID(): " + testForExist.getID() + " for board.getServerID(): " + board.getServerID());

        if (testForExist.getID() != -1) {
            return testForExist.getID();
        }

        // Inserting Row
        long board_Id = db.insert(Board.TABLE, null, values);

        return (int) board_Id;
    }

    public void update(Board board) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        update(db, board);

        db.close(); // Closing database connection
    }

    public void update(SQLiteDatabase db, Board board) {

        ContentValues values = new ContentValues();

        setContentValue(board, values);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Board.TABLE, values, Board.KEY_ID + "= ?", new String[]{String.valueOf(board.getID())});

    }

    public Board getBoardByServerId(SQLiteDatabase db, String Id){

        String selectQuery =  "SELECT  " +
                Board.ALL_FIELDS +
                " FROM " + Board.TABLE
                + " WHERE " +
                Board.KEY_SERVER_ID + " = ?";// It's a good practice to use parameter ?, instead of concatenate string

        Board board = new Board();

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Id)});

        if (cursor.moveToFirst()) {
            do {

                getBoardFromCursor(board, cursor);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return board;
    }

    public ArrayList<Board> getBoardsForAccount(int account_Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ArrayList<Board> result = getBoardsForAccount(db, account_Id);

        db.close();

        return result;
    }

    public ArrayList<Board> getBoardsForAccount(SQLiteDatabase db, int account_Id){

        String selectQuery =  "SELECT  " +
                Board.ALL_FIELDS +
                " FROM " + Board.TABLE
                + " WHERE "
                + Board.KEY_ACCOUNT_ID + " = ?";

        ArrayList<Board> boards = new ArrayList<Board>();

        Board board;

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(account_Id) } );

        Log.i(LOG_TAG, "getBoardsForAccount cursor.getCount(): " + cursor.getCount());

        if (cursor.moveToFirst()) {

            do {
                board = new Board();

                getBoardFromCursor(board, cursor);

                boards.add(board);

            } while (cursor.moveToNext());
        }

        cursor.close();

        return boards;
    }

    public void deleteAll() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        deleteAll(db);

        db.close(); // Closing database connection

    }

    public void deleteAll(SQLiteDatabase db) {

        db.delete(Board.TABLE, null, new String[]{});
    }
}
