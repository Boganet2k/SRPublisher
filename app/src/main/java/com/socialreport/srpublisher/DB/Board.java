package com.socialreport.srpublisher.DB;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aleksandrbogomolov on 11/30/15.
 *
 * {
 "id":"4575449013423",
 "name":"Reports",
 "image": "https://s-media-cache-ak0.pinimg.com/216x146/cd/7a/74/cd7a74f36849da482ffe1c267aa486c8.jpg",
 "url": "https://www.pinterest.com/socialreport/reports/"
 }
 */
public class Board implements Parcelable {

    public static final String TABLE = "Board";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_ACCOUNT_ID = "account_id";
    public static final String KEY_SERVER_ID = "server_id";

    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_URL = "url";

    public static final String ALL_FIELDS = KEY_ID + "," +
            KEY_ACCOUNT_ID + "," +
            KEY_SERVER_ID + "," +
            KEY_NAME + "," +
            KEY_IMAGE + "," +
            KEY_URL;

    private int ID;
    private int account_ID;
    private String server_ID;

    private String name;
    private String image;
    private String url;

    private int isChecked;

    public Board() {

        ID = -1;
        account_ID = -1;
        server_ID = "";

        name = "";
        image = "";
        url = "";

        isChecked = 0;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getAccountID() {
        return account_ID;
    }

    public void setAccountID(int account_ID) {
        this.account_ID = account_ID;
    }

    public String getServerID() {
        return server_ID;
    }

    public void setServerID(String server_ID) {
        this.server_ID = server_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getIsChecked() {
        return isChecked == 1 ? true : false;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked ? 1 : 0;
    }

    protected Board(Parcel in) {
        ID = in.readInt();
        account_ID = in.readInt();
        server_ID = in.readString();
        name = in.readString();
        image = in.readString();
        url = in.readString();
        isChecked = in.readInt();
    }

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        @Override
        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeInt(account_ID);
        dest.writeString(server_ID);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(url);
        dest.writeInt(isChecked);
    }
}
