package com.socialreport.srpublisher;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import com.socialreport.srpublisher.DB.Account;
import com.socialreport.srpublisher.DB.Board;
import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.Publication;
import com.socialreport.srpublisher.DB.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bb on 17.09.15.
 */
public class Utils {

    public static final String PREFS_NAME = "PrefsFile";

    public static void saveUser(Context context, User currUser) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("user_token", currUser.getToken());

        editor.putInt("user_id", currUser.getId());
        editor.putString("user_firstName", currUser.getFirstName());
        editor.putString("user_lastName", currUser.getLastName());
        editor.putString("user_email", currUser.getEmail());
        editor.putLong("user_created", currUser.getCreated().getTime());
        editor.putLong("user_lastLogin", currUser.getLastLogin().getTime());
        editor.putString("user_photo", currUser.getPhoto());
        editor.putBoolean("user_deleted", currUser.getDeleted());

        // Commit the edits!
        editor.commit();

        MainService.updateUserData();
    }

    public static User getUser(Context context) {
        User currUser = new User();

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        currUser.setToken(settings.getString("user_token", ""));
        currUser.setId(settings.getInt("user_id", 0));
        currUser.setFirstName(settings.getString("user_firstName", ""));
        currUser.setLastName(settings.getString("user_lastName", ""));
        currUser.setEmail(settings.getString("user_email", ""));
        currUser.setCreated(new Date(settings.getLong("user_created", 0)));
        currUser.setLastLogin(new Date(settings.getLong("user_lastLogin", 0)));
        currUser.setPhoto(settings.getString("user_photo", ""));
        currUser.setDeleted(settings.getBoolean("user_deleted", false));

        return currUser;
    }

    public static byte[] getByteArrayFromFile(String filePath) throws IOException, URISyntaxException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];

        int n;
        while (-1 != (n = fis.read(buf))) baos.write(buf, 0, n);

        return baos.toByteArray();
    }

    public static byte[] getByteArrayFromFile(InputStream is) throws IOException, URISyntaxException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];

        int n;
        while (-1 != (n = is.read(buf))) baos.write(buf, 0, n);

        return baos.toByteArray();
    }

    public static String getPath(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static boolean checkForValidPinterestBoards(Project mProject) {

        //Check boards selection for accounts
        Account currAccount;
        Board currBoard;

        for (int i = 0; i < mProject.getAccounts().size(); i++) {

            currAccount = mProject.getAccounts().get(i);

            if (currAccount.getBoards().size() == 0 || !currAccount.isChecked()) continue;

            boolean isSelectionValid = false;
            for (int j = 0; j < currAccount.getBoards().size(); j ++) {
                currBoard = currAccount.getBoards().get(j);

                if (currBoard.getIsChecked()) {
                    isSelectionValid = true;
                    break;
                }
            }

            if (!isSelectionValid) {
                return false;
            }

        }

        return true;
    }

    public static Board getFirstSelectedBoard(ArrayList<Board> boards) {

        Board selectedBoard = null;

        for (Board currBoard : boards) {

            if (currBoard.getIsChecked()) {
                selectedBoard = currBoard;
                break;
            }

        }

        return selectedBoard;
    }

    public static void uncheckBoards(Account currAccount) {

        for (Board currBoard : currAccount.getBoards()) {
            currBoard.setIsChecked(false);
        }

    }

    public static Publication getSelectedPublication(Project selectedProject) {

        Publication result = null;

        if (selectedProject != null) {

            for (Publication currPublication : selectedProject.getPublications()) {
                if (currPublication.isChecked()) {

                    result = currPublication;

                    break;
                }
            }
        }

        return result;
    }

    public static Bitmap decodeFile(File f,int WIDTH,int HIGHT) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth/scale/2 >= REQUIRED_WIDTH && o.outHeight/scale/2 >= REQUIRED_HIGHT) {
                scale *= 2;
            }

            o.inJustDecodeBounds = false;
            o.inSampleSize = scale;

            return BitmapFactory.decodeStream(new FileInputStream(f), null, o);

        } catch (FileNotFoundException e) {}

        return null;
    }
}
