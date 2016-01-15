package com.socialreport.srpublisher;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.socialreport.srpublisher.signin.SignInModel;

/**
 * Created by bb on 07.10.15.
 */
public class SignInWorkerFragment  extends Fragment {

    final String LOG_TAG = "SignInWorkerFragment";

    private SignInModel mSignInModel;

    public SignInWorkerFragment() {
        mSignInModel = new SignInModel();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");

        setRetainInstance(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(LOG_TAG, "onAttach activity");

    }

    public SignInModel getSignInModel() {

        Log.i(LOG_TAG, "getSignInModel");

        return mSignInModel;
    }
}
