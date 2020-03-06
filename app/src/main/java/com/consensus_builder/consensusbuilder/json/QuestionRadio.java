package com.consensus_builder.consensusbuilder.json;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * The data structure that defines a Question that is essentially a
 * group of radio buttons.  Users may choose one (or sometimes none)
 * button only.
 */
public class QuestionRadio {

    //-----------------------
    //  Constants
    //-----------------------

    /** Constants for accessing Bundles of this data */
    private static final String
            QRAD_BUNDLE_STR_LIST = "question_radio_bundle_strings",
            QRAD_BUNDLE_SELECTED = "question_radio_bundle_selected";


    //-----------------------
    //  Data
    //-----------------------

    /** Strings to display with each checkbox. Again, null means no associated String. */
    public ArrayList<String> mRadioButtonStrings = new ArrayList<>();

    /**
     * Indicates with of the button strings (in mRadioButtonStrings ArrayList)
     * has been selected.  -1 means no seletion at all.
     */
    public int mWhichSelected = -1;


    //-----------------------
    //  Methods
    //-----------------------

    /**
     * Basic constructor
     */
    QuestionRadio() {
    }

    /**
     * Copy constructor (creates a copy of the given instance).
     *
     * @param radio     The instance to copy.
     */
    QuestionRadio (QuestionRadio radio) {
        mRadioButtonStrings = new ArrayList<>(radio.mRadioButtonStrings);
        mWhichSelected = radio.mWhichSelected;
    }

    /**
     * Constructor that turns a Bundle of data into a QuestionRadio instance.
     *
     * @param bundle    A normal android Bundle (preferably constructed using the
     *                  {@link #make_into_bundle()} method).
     */
    public QuestionRadio (Bundle bundle) {
        mRadioButtonStrings = bundle.getStringArrayList(QRAD_BUNDLE_STR_LIST);
        mWhichSelected = bundle.getInt(QRAD_BUNDLE_SELECTED);
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
        bundle.putStringArrayList(QRAD_BUNDLE_STR_LIST, mRadioButtonStrings);
        bundle.putInt(QRAD_BUNDLE_SELECTED, mWhichSelected);
    }


    @Override
    public String toString() {
        String str = getClass().getSimpleName();
        str += ": count = " + mRadioButtonStrings.size() + ", ";
        for (int i = 0; i < mRadioButtonStrings.size(); i++) {
            str += " [" + i;
            str += (i == mWhichSelected ? " -X- ] '" : "] '");
            str += mRadioButtonStrings.get(i) + "', ";
        }
        return str;
    }



}
