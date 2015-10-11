package com.fabiosanto.currencyconverter;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;

import org.hamcrest.Matchers;
import org.junit.Before;

/**
 * Created by fabiosanto on 11/10/15.
 */
public class MainActivityTestClass extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;

    public MainActivityTestClass() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    public void testResultValueExist(){

        //get view
        ViewInteraction input = Espresso.onView(ViewMatchers.withId(R.id.value));

        //clear input edittext
        input.perform(ViewActions.clearText());

        //set text
        input.perform(ViewActions.typeText("1234"));

        //select a currency
        Espresso.onView(ViewMatchers.withId(R.id.viewpager))
                .perform(ViewActions.swipeRight());

        //check view not empty
        // it should be checked if the value also is 0.00 or displays the correct currency selected
        Espresso.onView(ViewMatchers.withId(R.id.result))
                .check(ViewAssertions.matches(ViewMatchers.withText(Matchers.not(Matchers.isEmptyOrNullString()))));

    }
}
