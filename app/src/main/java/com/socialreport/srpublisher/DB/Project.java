package com.socialreport.srpublisher.DB;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Project JSON format:
 *
 {
 "id": "56783",
 "name": "SocialReport.com",
 "user": 5,
 "timezone": "America/New_York",
 "created": "02/08/2012 18:30 -0500",
 }
 */

public class Project implements Parcelable {



    // Labels table name
    public static final String TABLE = "Project";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_SERVER_ID = "server_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER = "user";
    public static final String KEY_TIME_ZONE = "timezone";
    public static final String KEY_CREATED = "created";

    public static final String ALL_FIELDS = Project.KEY_ID + "," +
            Project.KEY_SERVER_ID + "," +
            Project.KEY_NAME + "," +
            Project.KEY_USER + "," +
            Project.KEY_TIME_ZONE + "," +
            Project.KEY_CREATED;

    // property help us to keep data
    private int ID;
    private int server_ID;

    private String name;

    private int user;
    private String timezone;
    private Date created;

    private ArrayList<Account> accounts;
    private ArrayList<Publication> publications;

    public Project() {
        ID = -1;
        server_ID = -1;
        name = "";

        user = -1;
        timezone = "";
        created = new Date(0);

        accounts = new ArrayList<Account>();
        publications = new ArrayList<Publication>();
    }

    protected Project(Parcel in) {
        ID = in.readInt();
        server_ID = in.readInt();
        name = in.readString();

        user = in.readInt();
        timezone = in.readString();
        created = new Date(in.readLong());

        accounts = in.readArrayList(Account.class.getClassLoader());
        publications = in.readArrayList(Publication.class.getClassLoader());

    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeInt(server_ID);
        dest.writeString(name);

        dest.writeInt(user);
        dest.writeString(timezone);
        dest.writeLong(created.getTime());

        dest.writeList(accounts);
        dest.writeList(publications);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
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

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    public ArrayList<Publication> getPublications() {
        return publications;
    }

    public void setPublications(ArrayList<Publication> publications) {
        this.publications = publications;
    }

    @Override
    public String toString() {
        return "id: " + ID + " server_ID: " + server_ID + " name: " + name + " accounts.size(): " + accounts.size();
    }
}