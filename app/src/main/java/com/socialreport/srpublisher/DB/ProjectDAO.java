package com.socialreport.srpublisher.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.socialreport.srpublisher.SRPublisherApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
/**
 * Created by bb on 15.09.15.
 */
public class ProjectDAO {

    private static String LOGTAG = ProjectDAO.class.getName();

    private DBHelper dbHelper;
    private AccountDAO accountDAO;
    private ProjectAccountDAO projectAccountDAO;
    private PublicationDAO publicationDAO;

    public ProjectDAO(Context context) {
        //dbHelper = new DBHelper(context);
        dbHelper = SRPublisherApplication.getDbHelper();
        accountDAO = new AccountDAO(context);
        projectAccountDAO = new ProjectAccountDAO(context);
        publicationDAO = new PublicationDAO();

    }

    private void setContentValue(Project project, ContentValues values) {
        values.put(Project.KEY_SERVER_ID, project.getServerID());
        values.put(Project.KEY_NAME, project.getName());
        values.put(Project.KEY_USER, project.getUser());
        values.put(Project.KEY_TIME_ZONE, project.getTimezone());
        values.put(Project.KEY_CREATED, project.getCreated().getTime());
    }

    private void getProjectFromCursor(Project project, Cursor cursor) {
        project.setID(cursor.getInt(cursor.getColumnIndex(Project.KEY_ID)));
        project.setServerID(cursor.getInt(cursor.getColumnIndex(Project.KEY_SERVER_ID)));
        project.setName(cursor.getString(cursor.getColumnIndex(Project.KEY_NAME)));
        project.setUser(cursor.getInt(cursor.getColumnIndex(Project.KEY_USER)));
        project.setTimezone(cursor.getString(cursor.getColumnIndex(Project.KEY_TIME_ZONE)));
        project.setCreated(new Date(cursor.getInt(cursor.getColumnIndex(Project.KEY_CREATED))));
    }

    public int insert(Project project) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        ContentValues values = new ContentValues();
        setContentValue(project, values);

        // Inserting Row
        long project_Id = db.insert(Project.TABLE, null, values);

        Account currAccount;
        for (int i = 0; i < project.getAccounts().size(); i++) {
            currAccount = project.getAccounts().get(i);
            currAccount.setProjectID((int) project_Id);

            currAccount.setID(accountDAO.insert(db, currAccount));

            projectAccountDAO.insert(db, project_Id, currAccount.getID());

        }

        Publication currPublication;
        for (int i = 0; i < project.getPublications().size(); i++) {
            currPublication = project.getPublications().get(i);
            currPublication.setProjectID((int) project_Id);

            currPublication.setID(publicationDAO.insert(db, currPublication));

        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection

        return (int) project_Id;
    }

    public void delete(int project_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Project.TABLE, Project.KEY_ID + "= ?", new String[]{String.valueOf(project_Id)});
        db.close(); // Closing database connection

        accountDAO.deleteFoProject(project_Id);
    }

    public void deleteAll() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(Project.TABLE, Project.KEY_ID + " > 0", new String[]{});

        db.delete(ProjectAccount.TABLE, null, new String[]{});

        accountDAO.deleteAll(db);

        publicationDAO.deleteAll(db);

        db.close(); // Closing database connection

        //accountDAO.deleteAll();
    }

    public void update(Project project) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        setContentValue(project, values);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Project.TABLE, values, Project.KEY_ID + "= ?", new String[]{String.valueOf(project.getID())});
        db.close(); // Closing database connection

        Account currAccount;
        for (int i = 0; i < project.getAccounts().size(); i++) {
            currAccount = project.getAccounts().get(i);

            if (currAccount.getID() == -1) {
                currAccount.setProjectID((int) project.getID());
                currAccount.setID(accountDAO.insert(currAccount));
            } else {
                accountDAO.update(currAccount);
            }

            projectAccountDAO.insert(project.getID(), currAccount.getID());
        }

        Publication currPublication;
        for (int i = 0; i < project.getPublications().size(); i++) {
            currPublication = project.getPublications().get(i);

            if (currPublication.getID() == -1) {
                currPublication.setProjectID((int) project.getID());
                currPublication.setID(publicationDAO.insert(currPublication));
            } else {
                publicationDAO.update(currPublication);
            }

        }
    }

    public void updateBulk(Project project) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        setContentValue(project, values);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(Project.TABLE, values, Project.KEY_ID + "= ?", new String[]{String.valueOf(project.getID())});


        Account currAccount;
        for (int i = 0; i < project.getAccounts().size(); i++) {
            currAccount = project.getAccounts().get(i);

            if (currAccount.getID() == -1) {
                currAccount.setProjectID((int) project.getID());
                currAccount.setID(accountDAO.insert(db, currAccount));
            } else {
                accountDAO.update(db, currAccount);
            }

            projectAccountDAO.insert(db, project.getID(), currAccount.getID());
        }

        Publication currPublication;
        for (int i = 0; i < project.getPublications().size(); i++) {
            currPublication = project.getPublications().get(i);

            if (currPublication.getID() == -1) {
                currPublication.setProjectID((int) project.getID());
                currPublication.setID(publicationDAO.insert(db, currPublication));
            } else {
                publicationDAO.update(db, currPublication);
            }

        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    public ArrayList<Project>  getProjectList() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Project.ALL_FIELDS +
                " FROM " + Project.TABLE;

        ArrayList<Project> projectList = new ArrayList<Project>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();

                getProjectFromCursor(project, cursor);

                projectList.add(project);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Project project;

        for (int i = 0; i < projectList.size(); i++) {

            project = projectList.get(i);

            project.setAccounts(accountDAO.getAccountsForProject(project.getID()));

            project.setPublications(publicationDAO.getPublicationsForProject(project.getID()));

        }

        return projectList;

    }

    public Project getProjectById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Project.ALL_FIELDS +
                " FROM " + Project.TABLE
                + " WHERE " +
                Project.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        Project project = new Project();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {

                getProjectFromCursor(project, cursor);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        project.setAccounts(accountDAO.getAccountsForProject(project.getID()));

        project.setPublications(publicationDAO.getPublicationsForProject(project.getID()));

        return project;
    }

    public Project getProjectByServerId(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Project.ALL_FIELDS +
                " FROM " + Project.TABLE
                + " WHERE " +
                Project.KEY_SERVER_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        Project project = new Project();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {

                getProjectFromCursor(project, cursor);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        project.setAccounts(accountDAO.getAccountsForProject(project.getID()));

        project.setPublications(publicationDAO.getPublicationsForProject(project.getID()));

        return project;
    }
}
