package com.socialreport.srpublisher;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.RelativeLayout;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import com.socialreport.srpublisher.DB.User;
import com.socialreport.srpublisher.MainActivity;
import com.socialreport.srpublisher.R;

/**
 * Created by aleksandrbogomolov on 10/21/15.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private final static String TAGLOG = MainActivityTest.class.getName();

    private MainActivity mActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        Intent mIntent = new Intent();
        mIntent.setAction("android.intent.action.MAIN");
        setActivityIntent(mIntent);

        mActivity = getActivity();
    }

    public void testEnterLoginPassword() throws Throwable {

        Log.i(TAGLOG, "testEnterLoginPassword");

        assertNotNull(mActivity);

        onView(withId(R.id.view_username)).perform(typeText("UserName"));
        onView(withId(R.id.view_password)).perform(typeText("UserPassword"));

        onView(withId(R.id.view_username)).check(matches(withText("UserName")));
        onView(withId(R.id.view_password)).check(matches(withText("UserPassword")));

        //onView(withId(R.id.view_submit)).perform(click());
    }

    public void testLoginRequest() throws Throwable {

        assertNotNull(mActivity);

        onView(withId(R.id.view_username)).check(matches(withText("")));
        onView(withId(R.id.view_password)).check(matches(withText("")));
    }
}
