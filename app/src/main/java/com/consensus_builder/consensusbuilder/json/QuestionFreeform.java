package com.consensus_builder.consensusbuilder.json;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Data structure for a free-form question.  This is pretty simple:
 * just a question and an answer.  If the user wants a yes-no
 * question/answer, they should use radio buttons instead.
 */
public class QuestionFreeform {

    //-----------------------
    //  Constants
    //-----------------------

    /** Constants for accessing Bundles of this data */
    private static final String
            QF_BUNDLE_RESPONSE = "question_freeform_bundle_response",
            QF_BUNDLE_RESPONSE_WANTED = "question_freeform_bundle_response_wanted";


    //-----------------------
    //  Data
    //-----------------------

    /**
     * There are times where you don't want a response--you just want to send a message
     * to inform people.  Set this to FALSE to indicate that no response is wanted at
     * all (the users will NOT even have a place to respond).  Default is TRUE.
     */
    public boolean mResponseWanted = true;

    /** The response to the question. */
    public String mResponse;


    //-----------------------
    //  Methods
    //-----------------------

    /**
     * Basic constructor
     */
    QuestionFreeform() {
    }

    /**
     * Copy constructor (creates a copy of the given instance).
     *
     * @param freeform      The instance to copy.
     */
    QuestionFreeform (QuestionFreeform freeform) {
        mResponseWanted = freeform.mResponseWanted;
        mResponse = freeform.mResponse;
    }

    /**
     * Constructor that turns a Bundle of data into a QuestionRadio instance.
     *
     * @param bundle    A normal android Bundle (preferably constructed using the
     *                  {@link #make_into_bundle()} method).
     */
    public QuestionFreeform (Bundle bundle) {
        mResponseWanted = bundle.getBoolean(QF_BUNDLE_RESPONSE_WANTED);
        mResponse = bundle.getString(QF_BUNDLE_RESPONSE);
    }


    /**
     * Turns the data contents of this instance into a Bundle suitable for
     * android data transfers.
     */
    public Bundle make_into_bundle() {
        Bundle bundle = new Bundle();
        add_to_bundle(bundle);
        return bundle;
    }

    /**
     * Puts the data contents of this instance into the given Bundle.
     *
     * @param bundle    An active Bundle waiting to have some data thrown in it.
     *                  Must already be instantiated!
     */
    public void add_to_bundle (Bundle bundle) {
        bundle.putBoolean(QF_BUNDLE_RESPONSE_WANTED, mResponseWanted);
        bundle.putString(QF_BUNDLE_RESPONSE, mResponse);
    }


    @Override
    public String toString() {
        String str = getClass().getSimpleName();
        str += ": response? " + (mResponseWanted ? "YES" : "no") + ": ";
        str += mResponse + ", ";
        return str;
    }


}
