package com.socialreport.srpublisher.DB;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by bb on 18.09.15.
 *
 * {
 "id": "63459",
 "name": "SocialReport.com",
 "active": true,
 "type": "Pinterest Profile",
 "image": "https://s-media-cache-ak0.pinimg.com/avatars/socialreport_1369751408_140.jpg",
 "networkIcon": "http://socialreport.com/images/icons/pinterest-32x32.png",
 "access": true,
 "publish": true,
 "boards": [
     {
         "id":"4575449013423",
         "name":"Reports",
         "image": "https://s-media-cache-ak0.pinimg.com/216x146/cd/7a/74/cd7a74f36849da482ffe1c267aa486c8.jpg",
         "url": "https://www.pinterest.com/socialreport/reports/"
     },
     {
         "id":"4744645556642",
         "name":"Social",
         "image": "https://s-media-cache-ak0.pinimg.com/216x146/7a/fb/c6/7afbc60e46da19838aa3ef4033fa040c.jpg",
         "url": "https://www.pinterest.com/manobyte/social/"
     }
 ]
 }
 */

public class Account implements Parcelable {

    // Labels table name
    public static final String TABLE = "Account";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_PROJECT_ID = "project_id";
    public static final String KEY_SERVER_ID = "server_id";

    public static final String KEY_NAME = "name";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_NETWORK_ICON = "network_icon";
    public static final String KEY_ACCESS = "access";
    public static final String KEY_PUBLISH = "publish";

    public static final String ALL_FIELDS = KEY_ID + "," +
            KEY_PROJECT_ID + "," +
            KEY_SERVER_ID + "," +
            KEY_NAME + "," +
            KEY_ACTIVE + "," +
            KEY_TYPE + "," +
            KEY_IMAGE + "," +
            KEY_NETWORK_ICON + "," +
            KEY_ACCESS + "," +
            KEY_PUBLISH;

    // property help us to keep data
    private int ID;
    private int project_ID;
    private int server_ID;
    private String name;

    private boolean active;
    private String type;
    private String image;
    private String networkIcon;
    private boolean access;
    private boolean publish;

    private int isChecked;

    private ArrayList<Board> boards;

    private Bitmap imageData;
    private boolean isImageDataLoading;

    public Account() {
        ID = -1;
        project_ID = -1;
        server_ID = -1;
        name = "";

        active = false;
        type = "";
        image = "";
        imageData = null;
        isImageDataLoading = false;

        networkIcon = "";
        access = false;
        publish = false;

        boards = new ArrayList<Board>();

        isChecked = 1;
    }

    protected Account(Parcel in) {
        ID = in.readInt();
        project_ID = in.readInt();
        server_ID = in.readInt();
        name = in.readString();

        active = in.readInt() == 1 ? true : false;
        type = in.readString();
        image = in.readString();
        networkIcon = in.readString();
        access = in.readInt() == 1 ? true : false;
        publish = in.readInt() == 1 ? true : false;

        boards = in.readArrayList(Board.class.getClassLoader());
        imageData = null;

        isChecked = in.readInt();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeInt(project_ID);
        dest.writeInt(server_ID);
        dest.writeString(name);

        dest.writeInt(active ? 1 : 0);
        dest.writeString(type);
        dest.writeString(image);
        dest.writeString(networkIcon);
        dest.writeInt(access ? 1 : 0);
        dest.writeInt(publish ? 1 : 0);
        dest.writeList(boards);

        dest.writeInt(isChecked);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getProjectID() {
        return project_ID;
    }

    public void setProjectID(int project_ID) {
        this.project_ID = project_ID;
    }

    public int getServerID() {
        return server_ID;
    }

    public void setServerID(int server_ID) {
        this.server_ID = server_ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNetworkIcon() {
        return networkIcon;
    }

    public void setNetworkIcon(String networkIcon) {
        this.networkIcon = networkIcon;
    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public ArrayList<Board> getBoards() {
        return boards;
    }

    public void setBoards(ArrayList<Board> boards) {
        this.boards = boards;
    }

    public boolean isChecked() {
        return isChecked == 1 ? true : false;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked ? 1 : 0;
    }

    public Bitmap getImageData() {
        return imageData;
    }

    public void setImageData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public boolean isImageDataLoading() {
        return isImageDataLoading;
    }

    public void setIsImageDataLoading(boolean isImageDataLoading) {
        this.isImageDataLoading = isImageDataLoading;
    }

    @Override
    public String toString() {
        return "id: " + ID + " project_ID: " + project_ID + " server_ID: " + server_ID + " name: " + name + " isChecked: " + isChecked;
    }

}
