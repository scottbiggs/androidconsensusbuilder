package com.consensus_builder.consensusbuilder.json;


import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Defines the data structure for a Question.  Should
 * match the JSON.  Note that a question is of a certain TYPE
 * (see the constants below).<br>
 * <br>
 * NOTE: If you try to modify some aspect that is not relevant
 * to the stated TYPE (see {@link #set_type}, this WILL crash!
 * You've been warned.
 */
public class Question {

    //--------------------------
    //  Constants
    //--------------------------

    private static final String TAG = Question.class.getSimpleName();

    /** Constants for accessing Bundles of this data */
    private static final String
            Q_BUNDLE_TYPE = "question_bundle_type",
            Q_BUNDLE_PROMPT = "question_bundle_prompt";

    /**
     * The different types of Questions.  Most of these
     * are intuitive and reflect the widget used to display
     * the question.
     */
    public static final int
        QUESTION_TYPE_ERROR = -1,
        QUESTION_TYPE_UNKNOWN = 0,
        QUESTION_TYPE_CHECKBOX = 1,
        QUESTION_TYPE_RADIO = 2,
        QUESTION_TYPE_FREEFORM = 3,
        QUESTION_TYPE_RANK = 4;

    //--------------------------
    //  Data
    //--------------------------

    /** There are different types of questions.  See the constants above. */
    private int mType;

    /** Prompt for this question.  Use null if not needed. */
    private String mPrompt;


    //-----------------------
    //  Pointers to the actual data.  The data that corresponds to the TYPE
    //  is the only one that should NOT be null.
    //

    private QuestionCheckbox mCheckbox;

    private QuestionRadio mRadio;

    private QuestionFreeform mFreeform;

    private QuestionRank mRank;


    //--------------------------
    //  Classes
    //--------------------------


    //--------------------------
    //  Methods
    //--------------------------

    /** Basic constructor */
    public Question() {
        mType = QUESTION_TYPE_UNKNOWN;
    }

    /**
     * Constructor that defines a type for this Question.
     */
    public Question (int type) {
        set_type(type);
    }

    /**
     * Constructor that creates a copy of the given Question.
     * For now this is merely copying the Type and the subclass.
     */
    public Question (Question question) {
        set_type(question.get_type());
        switch (get_type()) {
            case QUESTION_TYPE_CHECKBOX:
                mCheckbox = new QuestionCheckbox(question.mCheckbox);
                break;

            case QUESTION_TYPE_FREEFORM:
                mFreeform = new QuestionFreeform(question.mFreeform);
                break;

            case QUESTION_TYPE_RADIO:
                mRadio = new QuestionRadio(question.mRadio);
                break;

            case QUESTION_TYPE_RANK:
                mRank = new QuestionRank(question.mRank);
                break;
        }
    }

    /**
     * Constructor that turns a Bundle in an instance of Question.
     *
     * @param bundle    A Bundle holding info that will be a Question.
     *                  Best way to do this is to use make_into_bundle().
     */
    public Question (Bundle bundle) {
        mType = bundle.getInt(Q_BUNDLE_TYPE);
        mPrompt = bundle.getString(Q_BUNDLE_PROMPT);

        switch (get_type()) {
            case QUESTION_TYPE_CHECKBOX:
                mCheckbox = new QuestionCheckbox(bundle);
                break;

            case QUESTION_TYPE_FREEFORM:
                mFreeform = new QuestionFreeform(bundle);
                break;

            case QUESTION_TYPE_RADIO:
                mRadio = new QuestionRadio(bundle);
                break;

            case QUESTION_TYPE_RANK:
                mRank = new QuestionRank(bundle);
                break;
        }
    }


    /**
     * Clears all data within this instance. Will appear just as if it
     * were newly instantiated.
     */
    public void clear() {
        set_type(QUESTION_TYPE_UNKNOWN);
        null_all_types();
        setPrompt(null);
    }

    /**
     * Creates a Bundle suitable for passing through in Intent out of all the data
     * in this class.
     *
     * @return  A newly-created Bundle with all the relevant data of this instance.
     */
    public Bundle make_into_bundle() {
        Bundle bundle = new Bundle();
        add_to_bundle(bundle);
        return bundle;
    }


    /**
     * Puts the data contents of this instance into the given Bundle.
     * Note that this works on the ACTIVE subclass as well, forcing it to
     * add its data to the Bundle too.  The other types are ignored.
     *
     * @param bundle    An active Bundle waiting to have some data thrown in it.
     *                  Must already be instantiated!
     */
    public void add_to_bundle (Bundle bundle) {
        bundle.putInt(Q_BUNDLE_TYPE, mType);
        bundle.putString(Q_BUNDLE_PROMPT, mPrompt);

        switch (get_type()) {
            case QUESTION_TYPE_CHECKBOX:
                mCheckbox.add_to_bundle(bundle);
                break;

            case QUESTION_TYPE_FREEFORM:
                mFreeform.add_to_bundle(bundle);
                break;

            case QUESTION_TYPE_RADIO:
                mRadio.add_to_bundle(bundle);
                break;

            case QUESTION_TYPE_RANK:
                mRank.add_to_bundle(bundle);
                break;
        }
    }


    public void set_type (int type) {
        switch (type) {
            case QUESTION_TYPE_CHECKBOX:
                null_all_types();
                mCheckbox = new QuestionCheckbox();
                break;

            case QUESTION_TYPE_FREEFORM:
                null_all_types();
                mFreeform = new QuestionFreeform();
                break;

            case QUESTION_TYPE_RADIO:
                null_all_types();
                mRadio = new QuestionRadio();
                break;

            case QUESTION_TYPE_RANK:
                null_all_types();
                mRank = new QuestionRank();
                break;

            case QUESTION_TYPE_UNKNOWN:
                null_all_types();
                break;

            default:
                Log.e(TAG, "Illegal type in set_type(). Value is " + type);
                return;
        }
        mType = type;
    }

    /**
     * Returns the current type of this Question.  See {@link #QUESTION_TYPE_UNKNOWN}.
     */
    public int get_type() {
        return mType;
    }


    /**
     * Sets the question prompt. Use null if no prompt is needed.
     */
    public void setPrompt (String prompt) {
        mPrompt = prompt;
    }

    /**
     * Returns the current prompt.
     */
    public String getPrompt() {
        return mPrompt;
    }


    /** Little helper method--clears all the types (sets them all to null). */
    private void null_all_types() {
        mCheckbox = null;
        mFreeform = null;
        mRank = null;
        mRadio = null;
    }

    //--------
    //  Freeform methods

    public void set_freeform_response_wanted (boolean want_response) {
        mFreeform.mResponseWanted = want_response;
    }

    public boolean get_freeform_response_wanted() {
        return mFreeform.mResponseWanted;
    }


    public void set_freeform_response (String response) {
        mFreeform.mResponse = response;
    }

    /**
     * Returns the user's response to the freeform prompt.  May be null or empty.
     */
    public String get_freeform_response() {
        return mFreeform.mResponse;
    }

    //--------
    //  Checkbox methods

    /**
     * Simply returns the current number of checkboxes in this Question.
     */
    public int numCheckboxes() {
        return mCheckbox.mCheckboxStrings.size();
    }

    /**
     * Returns the number of the checkboxes that are actually checked.
     */
    public int num_checkboxes_checked() {
        int count = 0;
        for (int i = 0; i < mCheckbox.mCheckboxChecked.size(); i++) {
            if (mCheckbox.mCheckboxChecked.get(i) == true) {
                count++;
            }
        }
        return count;
    }


    /**
     * Adds a checkbox to the Question.  Assumes that the type is
     * QUESTION_TYPE_CHECKBOX.  The new checkbox will be added at
     * the end.
     *
     * @param str       The message to display with this checkbox.  Null for no msg.
     *
     * @param checked   Whether this checkbox is checked or not.
     *
     * @return      The total number of checkboxes AFTER this has been
     *              added.  Returns -1 on error.
     */
    public int addCheckbox (String str, boolean checked) {
        mCheckbox.mCheckboxStrings.add(str);
        mCheckbox.mCheckboxChecked.add(checked);

        // Sanity check
        if (mCheckbox.mCheckboxStrings.size() != mCheckbox.mCheckboxChecked.size()) {
            Log.e(TAG, "Error in addCheckbox()!  String list and checked list are NOT the same size!!!!");
            throw new NullPointerException();   // This should get their attention!
        }

        return mCheckbox.mCheckboxStrings.size();
    }

    /**
     * Returns all the checkbox strings (in order).
     */
    public ArrayList<String> getCheckboxStrings() {
        return mCheckbox.mCheckboxStrings;
    }

    /**
     * Returns a list of the checkbox items and whether they are checked or not.
     */
    public ArrayList<Boolean> getCheckboxChecks() {
        return mCheckbox.mCheckboxChecked;
    }

    /**
     * Removes all checkboxes from the Question.
     */
    public void clearCheckboxes() {
        mCheckbox.mCheckboxStrings.clear();
        mCheckbox.mCheckboxChecked.clear();
    }

    /**
     * Returns the checkbox String for the given position.  Don't know
     * what will happen if the index is out of range (whatever normally
     * happens for any ArrayList).
     */
    public String getCheckboxString(int pos) {
        return mCheckbox.mCheckboxStrings.get(pos);
    }

    public boolean isCheckboxChecked(int pos) {
        return mCheckbox.mCheckboxChecked.get(pos);
    }

    public void setCheckboxChecked (int pos, boolean checked) {
        mCheckbox.mCheckboxChecked.set(pos, checked);
    }

    //--------
    //  Radio methods

    public int numRadioButtons() {
        return mRadio.mRadioButtonStrings.size();
    }


    /**
     * Adds a radio button to the Question.  Assumes that the type is
     * QUESTION_TYPE_RADIO.  The new radio button will be added at
     * the end.
     *
     * @param str    The message to display with this radio button.  Use null if no String wanted.
     *
     * @return      The total number of radio buttons AFTER this has been
     *              added.
     */
    public int addRadioButton (String str) {
        mRadio.mRadioButtonStrings.add(str);
        return mRadio.mRadioButtonStrings.size();
    }

    /**
     * Removes all radio buttons from the Question.
     */
    public void clearRadioButtons() {
        mRadio.mRadioButtonStrings.clear();
        mRadio.mWhichSelected = -1;
    }

    /**
     * Returns the radio button message at the given position.
     */
    public String getRadioButtonString(int pos) {
        return mRadio.mRadioButtonStrings.get(pos);
    }

    /**
     * Which of the radio buttons has been selected.  Returns
     * -1 if none.
     */
    public int getRadioButtonSelected() {
        return mRadio.mWhichSelected;
    }

    /**
     * Set which radio button has been selected.  -1 for
     * none.
     */
    public void setRadioButtonSelected (int selected) {
        mRadio.mWhichSelected = selected;
    }

    //--------
    //  Ranking methods

    public int numRankings() {
        return mRank.mStrList.size();
    }

    /**
     * Returns the rank message at the given position.
     */
    public String getRankString(int pos) {
        return mRank.mStrList.get(pos);
    }

    /**
     * Returns the rank portion of a question.  Yep, that's ALL the
     * rank data.
     */
    public QuestionRank get_rank() {
        return mRank;
    }

    /**
     * Sets our ranking question to the given QuestionRank object.
     */
    public void set_rank (QuestionRank rank) {
        mRank = rank;
    }


    /**
     * Adds a ranking line.  Assumes that the type is QUESTION_TYPE_RANK.
     * This new line will be added at the end.
     *
     * @param msg   The line to display for this ranking line.
     *
     * @return      The total number of ranking lines after this has been added.
     */
    public int addRanking (String msg) {
        mRank.mStrList.add(msg);
        return mRank.mStrList.size();
    }

    /**
     * Removes all the ranking lines for this Question.
     */
    public void clearRanking() {
        mRank.mStrList.clear();
    }


    @Override
    public String toString() {
        String str = getClass().getSimpleName() + " : type = ";
        switch (mType) {
            case QUESTION_TYPE_CHECKBOX:
                str += "CHECKBOX ";
                str += mCheckbox.toString();
                break;

            case QUESTION_TYPE_FREEFORM:
                str += "FREEFORM ";
                str += mFreeform.toString();
                break;

            case QUESTION_TYPE_RADIO:
                str += "RADIO ";
                str += mRadio.toString();
                break;

            case QUESTION_TYPE_RANK:
                str += "RANK ";
                str += mRank.toString();
                break;

            case QUESTION_TYPE_ERROR:
                str += "ERROR";
                break;

            case QUESTION_TYPE_UNKNOWN:
                str += "UNKNOWN";
                break;
        }
        return str;
    }


}
