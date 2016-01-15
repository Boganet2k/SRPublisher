package com.socialreport.srpublisher.DB;

import android.util.Log;

import java.util.Date;

/**
 * Created by bb on 16.09.15.
 */
public class User {

    private int id;

    private String firstName;
    private String lastName;
    private String email;
    private Date created;
    private Date lastLogin;
    private Boolean deleted;
    private String photo;
    private String token;

    public User() {
        id = -1;
        firstName = "";
        lastName = "";
        email = "";
        created = new Date();
        lastLogin = new Date();
        deleted = false;
        photo = "";
        token = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
//        Log.i("User", "setId: " + id);
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
//        Log.i("User", "setFirstName: " + firstName);
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
//        Log.i("User", "setLastName: " + lastName);
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
//        Log.i("User", "setEmail: " + email);
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
//        Log.i("User", "setCreated: " + created);
        this.created = created;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
//        Log.i("User", "setLastLogin: " + lastLogin);
        this.lastLogin = lastLogin;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
//        Log.i("User", "setDeleted: " + deleted);
        this.deleted = deleted;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
//        Log.i("User", "setPhoto: " + photo);
        this.photo = photo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
//        Log.i("User", "setToken: " + token);
        this.token = token;
    }

    @Override
    public String toString() {
        return "id: " + id + " firstName: " + firstName + " lastName: " + lastName + " token: " + token;
    }
}
