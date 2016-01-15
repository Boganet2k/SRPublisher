package com.socialreport.srpublisher.retrofit;

import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by bb on 09.10.15.
 */
public interface SocialReportRestAPI {

    final static String baseURL = "https://api.socialreport.com";
    final static String loginRequest = "/login.svc";
    final static String projectsRequest = "/projects.svc";
    final static String accountsRequest = "/accounts.svc";
    final static String publishRequest = "/publications.svc";
    final static String uploadRequest = "/mediaCreate.svc";
    final static String publicationDetail = "/publicationDetail.svc";

    @FormUrlEncoded
    @POST(loginRequest)
    Call<ResponseBody> login(@Field("username") String username, @Field("password") String password);

    @POST(projectsRequest)
    Call<ResponseBody> projects(@Query("access_key") String access_key);

    @POST(publishRequest)
    Call<ResponseBody> publish(@Query("access_key") String access_key, @Body RequestBody body);

    @POST(uploadRequest)
    Call<ResponseBody> upload(@Query("access_key") String access_key, @Body RequestBody body);

    @GET(accountsRequest)
    Call<ResponseBody> accounts(@Query("access_key") String access_key, @Query("project") int project_id);

    @GET(publishRequest)
    Call<ResponseBody> publications(@Query("access_key") String access_key, @Query("project") int project_id);

    @GET(publicationDetail)
    Call<ResponseBody> categoryAccounts(@Query("access_key") String access_key, @Query("accounts_only") boolean onlyAccounts, @Query("id") int publication_id);
}
