package com.socialreport.srpublisher.DB;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aleksandrbogomolov on 11/29/15.
 * {
 "id": "1061",
 "status": "pending",
 "name": "sr blog",
 "type": "RSS",
 "approved": "yes"
 }
 */
public class Publication implements Parcelable {

    public static final String TABLE = "Publication";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_PROJECT_ID = "project_id";
    public static final String KEY_SERVER_ID = "server_id";

    public static final String KEY_STATUS = "status";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_APPROVED = "approved";

    public static final String ALL_FIELDS = KEY_ID + "," +
            KEY_PROJECT_ID + "," +
            KEY_SERVER_ID + "," +
            KEY_STATUS + "," +
            KEY_NAME + "," +
            KEY_TYPE + "," +
            KEY_APPROVED;

    private int ID;
    private int project_ID;
    private int server_ID;

    private String status;
    private String name;
    private String type;
    private boolean approved;

    private int isChecked;

    public Publication() {
        ID = -1;
        project_ID = -1;
        server_ID = -1;

        status = "";
        name = "";
        type = "";
        approved = false;

        isChecked = 0;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isChecked() {
        return isChecked == 1 ? true : false;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked ? 1 : 0;
    }

    // Parcelable implementation

    protected Publication(Parcel in) {
        ID = in.readInt();
        project_ID = in.readInt();
        server_ID = in.readInt();
        status = in.readString();
        name = in.readString();
        type = in.readString();
        approved = in.readInt() == 1 ? true : false;
        isChecked = in.readInt();
    }

    public static final Creator<Publication> CREATOR = new Creator<Publication>() {
        @Override
        public Publication createFromParcel(Parcel in) {
            return new Publication(in);
        }

        @Override
        public Publication[] newArray(int size) {
            return new Publication[size];
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
        dest.writeString(status);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeInt(approved ? 1 : 0);
        dest.writeInt(isChecked);
    }

    //
}
