package com.consensus_builder.consensusbuilder.json;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Holds data that will be associated with a checkbox question.
 * These questions will present the user with a number of options.
 * He may choose some, none, or all of the options (standard checkbox).<br>
 * <br>
 * This defines the data structure for such a question.
 */
public class QuestionCheckbox {

    //-----------------------
    //  Constants
    //-----------------------

    /** Constants for accessing Bundles of this data */
    private static final String
            QC_BUNDLE_STR_LIST = "question_checkbox_bundle_strings",
            QC_BUNDLE_CHECKED_LIST = "question_checkbox_bundle_checked";


    //-----------------------
    //  Data
    //-----------------------

    /** Strings to display with each checkbox. Again, null means no associated String. */
    public ArrayList<String> mCheckboxStrings = new ArrayList<>();

    /** Corresponds to mCheckboxStrings, indicating whether that item is checked or not. */
    public ArrayList<Boolean> mCheckboxChecked = new ArrayList<>();


    //-----------------------
    //  Methods
    //-----------------------

    /**
     * Basic constructor
     */
    QuestionCheckbox() {
    }

    /**
     * Copy constructor (creates a copy of the given instance).
     *
     * @param checkbox      The instance to copy.
     */
    QuestionCheckbox (QuestionCheckbox checkbox) {
        mCheckboxStrings = new ArrayList<>(checkbox.mCheckboxStrings);
        mCheckboxChecked = new ArrayList<>(checkbox.mCheckboxChecked);
    }

    /**
     * Constructor that turns a Bundle of data into a QuestionCheckbox instance.
     *
     * @param bundle    A normal android Bundle (preferably constructed using the
     *                  {@link #make_into_bundle()} method).
     */
    public QuestionCheckbox (Bundle bundle) {
        mCheckboxStrings = bundle.getStringArrayList(QC_BUNDLE_STR_LIST);

        // Because of the types, we have to do this the hard way.
        boolean[] tmp_bool_array = bundle.getBooleanArray(QC_BUNDLE_CHECKED_LIST);
        assert tmp_bool_array != null;
        for (boolean checked : tmp_bool_array) {
            mCheckboxChecked.add(checked);
        }
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
        bundle.putStringArrayList(QC_BUNDLE_STR_LIST, mCheckboxStrings);

        // Again, since bundles need a plain boolean[], we have to convert
        // the hard way.
        final boolean[] tmp_bool_array = new boolean[mCheckboxChecked.size()];
        for (int i = 0; i < mCheckboxChecked.size(); i++) {
            tmp_bool_array[i] = mCheckboxChecked.get(i);
        }
        bundle.putBooleanArray(QC_BUNDLE_CHECKED_LIST, tmp_bool_array);
    }


    @Override
    public String toString() {
        String str = getClass().getSimpleName();
        str += ": count = " + mCheckboxStrings.size() + " ,";
        for (int i = 0; i < mCheckboxStrings.size(); i++) {
            str += " [" + i + "] '" + mCheckboxStrings.get(i);
            str += (mCheckboxChecked.get(i) ? " (x) " : " ( ) ");
            str += ", ";
        }
        return str;
    }

}
