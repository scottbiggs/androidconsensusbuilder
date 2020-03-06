package com.consensus_builder.consensusbuilder.json;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Data structure for a question that asks users to rank a list
 * of items.
 */
public class QuestionRank {

    //----------------------------
    //  Constants
    //----------------------------

    /** Constants for accessing Bundles of this data */
    private static final String
            QR_BUNDLE_STR_LIST = "question_rank_bundle_orig_list_prompt";


    //----------------------------
    //  Data
    //----------------------------

    /**
     * Ordered list of items to rank.
     */
    public ArrayList<String> mStrList = new ArrayList<>();


    //----------------------------
    //  Methods
    //----------------------------

    /**
     * Basic Constructor
     */
    public QuestionRank() {
    }


    /**
     * Constructor that creates a copy of the given QuestionRank.
     *
     * @param questionRank      An already-instantiated QuestionRank.  It will be copied
     *                          to make this new instance.
     */
    public QuestionRank (QuestionRank questionRank) {
        mStrList = new ArrayList<>(questionRank.mStrList);
    }


    /**
     * Constructor that turns a Bundle of data into a QuestionRank instance.
     *
     * @param bundle    A normal android Bundle (preferably constructed using the
     *                  {@link #make_into_bundle()} method).
     */
    public QuestionRank (Bundle bundle) {
        mStrList = bundle.getStringArrayList(QR_BUNDLE_STR_LIST);
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
        bundle.putStringArrayList(QR_BUNDLE_STR_LIST, mStrList);
    }


    @Override
    public String toString() {
        String str = getClass().getSimpleName();
        str += ": count = " + mStrList.size() + ", ";

        for (int i = 0; i < mStrList.size(); i++) {
            str += " [" + i + "] '" + mStrList.get(i) + "', ";
        }

        return str;
    }

}
