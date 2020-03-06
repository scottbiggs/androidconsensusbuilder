package com.consensus_builder.consensusbuilder.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.RankDialogActivity;
import com.consensus_builder.consensusbuilder.json.Question;
import com.consensus_builder.consensusbuilder.json.QuestionRank;
import com.consensus_builder.consensusbuilder.ui.QuestionCardViewHolder;
import com.consensus_builder.consensusbuilder.ui.RecyclerAdapterBase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_CHECKBOX;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_FREEFORM;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_RADIO;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_RANK;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_UNKNOWN;

/**
 * Want to respond to a questionnaire? This is the right place.
 *
 * This Fragment displays an entire questionnaire, allows people to view it,
 * respond to it, and send their responses to the server.
 */
public class RespondFragment extends Fragment {

    //----------------------
    //  Constants
    //----------------------

    private static final String TAG = RespondFragment.class.getSimpleName();

    /** Number of milliseconds for a Send lock to timeout. */
    private static final long LOCK_TIMEOUT = 2000L;

    /** Key to find out if the recyclerView is enabled/disabled. */
    private static final String KEY_RESPOND_BUNDLE_ENABLED = "key_respond_bundle_enabled";

    /** Key for how many Questions have been saved in the bundle. */
    private static final String KEY_RESPOND_BUNDLE_COUNT = "key_respond_bundle_count";

    /**
     * Prefix for the keys for all the Bundles stored in the SaveInstanceState Bundle.
     * This is kind of hackey, but the key for each bundle is its array postion concatenated
     * at the end of this key.
     *
     * Note: each question is represented as a Bundle.
     *
     * For example:
     *      The 3rd bundle (array element 2) will use the key
     *      key_bundle_prefix2
     *
     *      That's the String below + the array element number.
     */
    private static final String KEY_RESPOND_BUNDLE_QUESTION_PREFIX = "key_respond_bundle_question_prefix";


    //----------------------
    //  Widgets
    //----------------------

    private RecyclerView mRecycler;

    /** Assists with managing the RecyclerView */
    private LinearLayoutManager mLayoutMgr;

    /** The FAB that the user presses to send their response to the server. */
    private FloatingActionButton mSendFAB;


    //----------------------
    //  Class Data
    //----------------------

    private RespondAdapter mAdapter;

    /** A locking mechanism to prevent duplicate sends. Locks timeout in LOCK_TIMEOUT milliseconds. */
    private long mSendLock = 0L;


    //----------------------
    //  Methods
    //----------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.respond, container, false);

        mSendFAB = (FloatingActionButton) v.findViewById(R.id.groups_respond_fab);
        mSendFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prevent accidental multiple clicks
                if (!test_lock()) {
                    return;
                }

                if (is_all_questions_answered()) {
                    Snackbar.make(v, R.string.sending_response_msg, Snackbar.LENGTH_LONG).show();
                    send_results();
                    clear_view();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.send_incomplete_data);
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Snackbar.make(v, R.string.sending_response_msg, Snackbar.LENGTH_LONG).show();
                            send_results();
                            dialog.dismiss();
                            clear_view();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);    // Prevents the dialog from being cancelled on touch outside.
                    dialog.show();
                }
            }
        });

        // Get the RecyclerView.  We'll attend to it once the Activity is ready
        mRecycler = (RecyclerView) v.findViewById(R.id.respond_rv);

        return v;
    }


    /**
     * This is usually called when the user has completed responding to a questionnaire.
     * The questions should all be grayed out and no longer respond to touch events other than
     * scrolling.
     */
    private void clear_view() {
        mSendFAB.setVisibility(View.GONE);
        mAdapter.setEnabled(false);
    }

    /**
     * Goes through all the Questions in our RecyclerView.  If any have not been touched by the
     * user yet, then this will return FALSE.  If all questions have been touched, then TRUE
     * is returned.<br>
     * <br>
     * NOTE: Rankings do not register a touch as the user may be happy with the given ranking.
     */
    private boolean is_all_questions_answered() {

        for (int i = 0; i < mAdapter.mQuestionData.size(); i++) {
            Question q = mAdapter.mQuestionData.get(i);
            switch (q.get_type()) {
                case QUESTION_TYPE_RANK:        // Ranking questions are always considered touched.
                    break;
                case QUESTION_TYPE_CHECKBOX:    // Checkboxes may be untouched as well.
                    break;
                case QUESTION_TYPE_RADIO:
                    if (q.getRadioButtonSelected() == -1) {
                        return false;           // The MUST select a radio button
                    }
                    break;
                case QUESTION_TYPE_FREEFORM:    // They have to type something
                    String str = q.get_freeform_response();
                    if ((str == null) || (str.length() == 0)) {
                        return false;
                    }
            }
        }
        // Made it this far, then they must have answered all the necessary questions.
        return true;
    }


    /**
     * Sends the Question data in our RecyclerView to the server.
     */
    private void send_results() {
        Log.d(TAG, "send_results()");
        // todo:
    }


    /**
     * This method will return TRUE only if a certain amount of time has passed
     * since this has last returned TRUE.  The time is determined in {@link #LOCK_TIMEOUT}.
     *
     * The point of this method is to prevent accidental multiple clicks.  The user
     * may only click once every LOCK_TIMEOUT period.  Yes, it is possible for
     * quick (or unlucky) users to tap before a button can respond and turn itself
     * off!
     *
     * side effects:
     *      mSendLock       Assumes that this is properly initialized (say to 0) for the
     *                      first time it's run.
     *
     * @return  True: yes, enough time has elapsed.
     *          False: no, not enough time has elapsed. Try again later!
     */
    private boolean test_lock() {
        long current_time = System.currentTimeMillis();
        if (mSendLock + LOCK_TIMEOUT > current_time) {
            return false;
        }

        mSendLock = current_time;
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the questions (if there are any).
        ArrayList<Question> questions = mAdapter.mQuestionData;
        if ((questions != null) && (questions.size() > 0)) {

            // First, store the count of the Bundles
            outState.putInt(KEY_RESPOND_BUNDLE_COUNT, questions.size());

            // Save all the bundles with their unique key
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                Bundle bundle = q.make_into_bundle();
                outState.putBundle(KEY_RESPOND_BUNDLE_QUESTION_PREFIX + i, bundle);
            }
        }

        // Save enabled state
        outState.putBoolean(KEY_RESPOND_BUNDLE_ENABLED, mAdapter.getEnabled());
    }


    /**
     * Restores the data within the RecyclerView's adapter.  Presumably this data
     * has been stored from a previous instance of this Fragment via onSaveInstanceState().
     *
     * side effects:
     *      mAdapter.mQuestionData will hold the new data.  If any data already exists, it
     *      will be replaced.
     *
     * @param savedInstanceState    Holds the data to be restored.
     */
    private void restore_saved_data (@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;     // don't do anything
        }

        // Load up the saved data
        int question_count = savedInstanceState.getInt(KEY_RESPOND_BUNDLE_COUNT, 0);

        // Just in case, make sure that the question data list is ready.
        mAdapter.mQuestionData.clear();

        for (int i = 0; i < question_count; i++) {
            Question q = new Question(savedInstanceState.getBundle(KEY_RESPOND_BUNDLE_QUESTION_PREFIX + i));
            mAdapter.mQuestionData.add(q);
        }

        boolean enabled = savedInstanceState.getBoolean(KEY_RESPOND_BUNDLE_ENABLED, true);  // defaults to true
        if (!enabled) {
            clear_view();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The RecyclerView and all its attending bits
        mLayoutMgr = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutMgr);

        mAdapter = new RespondAdapter(getActivity(), new ArrayList<Question>());
        mRecycler.setAdapter(mAdapter);

        // Load up the data from a previous instance of this Fragment.
        if (savedInstanceState != null) {
            restore_saved_data (savedInstanceState);
        }
        else {
//            Log.d(TAG, "start_data_gathering()");
            start_data_gathering();
        }

    }


    /**
     * For now, generate the data by hand.  Eventually this will request data
     * from the server (which will will be broadcast and received).
     */
    private void start_data_gathering() {

        // Init data structures
        Gson gson = new Gson();
        mAdapter.mQuestionData = new ArrayList<>();

        new RespondAsyncTask().execute();   // Initializations happen here
    }


    @Override
    public void onDestroyView() {

        // This signals to the AsyncTask that an orientation changes has happened
        mAdapter.close();
        mAdapter.mQuestionData = null;

        super.onDestroyView();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Ignore anything other than an OK result.
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            QuestionRank qr = new QuestionRank(bundle);
//            Log.d(TAG, "onActivityResult(), bundle = " + qr.toString());

            Question q = mAdapter.mQuestionData.get(requestCode);
            q.set_rank(qr);

            // NOTE: the requestCode is also the position in the RecyclerView of the
            // item that was modified!
            mAdapter.update_adapter(requestCode);
        }

    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    class RespondAdapter extends RecyclerAdapterBase {

        /**
         * Constructor<br>
         * <br>
         * Always use this constructor.  It supplies variables needed by this class to
         * interact properly with the RecyclerView that uses this Adapter.
         *
         * @param activity      Always nice to have a reference to our Activity!
         *
         * @param questions     The data itself, a list of Questions.
         */
        public RespondAdapter(Activity activity, ArrayList<Question> questions) {
            super(activity, questions);
        }


        /**
         * Overriding the parent as we need to do some extra work with the rank system.
         *
         * Specificially, we will add a click listener to the entire rank layout, which then
         * pops up a dialog where the user can manipulate the listings.
         *
         * @param q         The question data itself.
         *
         * @param holder    The ViewHolder
         *
         * @param pos       The position of the ViewHolder within the data (also the position
         *                  of q too).
         */
        @Override
        protected void setup_rank(final Question q, final QuestionCardViewHolder holder, int pos) {
            TextView tmp_ranking_prompt = holder.mRankingPrompt_tv;

            holder.mRank_ll.removeAllViews();

            holder.mRank_ll.addView(tmp_ranking_prompt);

            for (int i = 0; i < q.numRankings(); i++) {
                String str = q.get_rank().mStrList.get(i);
//                Log.d(TAG, "setup_rank(), pos = " + pos + ", i = " + i + ", str = " + str);
                TextView tv = new TextView(mActivity);
                tv.setText(str);
                holder.mRank_ll.addView(tv);
            }

            holder.mRankingPrompt_tv.setText(q.getPrompt());

            holder.mRank_ll.setVisibility(View.VISIBLE);

            // Need a listener for a Rank widget to throw up a dialog.
            if (getEnabled()) {
                holder.mRank_ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent itt = new Intent(getActivity(), RankDialogActivity.class);
                        Bundle bundle = q.get_rank().make_into_bundle();
                        itt.putExtras(bundle);
                        startActivityForResult(itt, holder.getAdapterPosition());   // We use this position to tell
                                                                                    // onActivityForResult which item
                                                                                    // was modified.
                    }
                });
            }
            else {
                holder.mRank_ll.setOnClickListener(null);
            }

            // Enable/Disable each View according to the state.
            disableEnableViews(getEnabled(), holder.mRank_ll);

        } // setup_rank(q, holder, pos)


        /**
         * Call this to update an item of data in the RecyclerView that has changed.<br>
         * <br>
         * <b>preconditions</b>:
         *      <ul><li>The data in mQuestionData ArrayList should already be modified!</li></ul>
         * <br>
         * @param pos       The position of the item that was modified.
         */
        public void update_adapter (int pos) {
            notifyItemChanged(pos);
        }


        @Override
        public void onBindViewHolder(final QuestionCardViewHolder holder, final int pos) {

            // Extracting to make the code cleaner
            Question q = mQuestionData.get(pos);

            // Set the title for this card.
            setTitle(holder, pos);

            turn_off_widget_sections(holder);

            switch (q.get_type()) {
                case QUESTION_TYPE_CHECKBOX:
                    setup_checkbox (q, holder, pos);
                    break;

                case QUESTION_TYPE_RADIO:
                    setup_radio (q, holder, pos);
                    break;

                case QUESTION_TYPE_FREEFORM:
                    setup_freeform(q, holder, pos);
                    break;

                case QUESTION_TYPE_RANK:
                    setup_rank(q, holder, pos);
                    break;

                case QUESTION_TYPE_UNKNOWN:
                    // Append an unknown message.
                    String old_title = holder.mTitle_tv.getText().toString();
                    String unknown_msg = mActivity.getResources().getString(R.string.qtype_unknown);
                    String separator = mActivity.getResources().getString(R.string.separator);
                    String new_title = old_title + separator + unknown_msg;
                    holder.mTitle_tv.setText(new_title);
                    break;

                default:
                    break;
            }
        } // onBindViewHolder (holder, pos)

    } // class RespondAdapter



    /**
     * Works completely by side-effect.  To avoid a dangling reference during
     * orientation changes, set mQuestions to NULL during onDestroy().
     */
    class RespondAsyncTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            // todo:  send data to server  (NOTE: change to a broadcast/receiver paradigm???)

            // todo:  wait for data to come in from server.

            // simulate wait
            SystemClock.sleep(1000);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // todo:  notify parent Fragment that the data is ready.

            // for now, just make some data.
            convert_data();

            display_data();
        }

        /**
         * This method is Atomic and cannot be interrupted.  This prevents
         * any dangling pointers from happening during an orientation change.
         *
         * That's important because it modifies mQuestions of the Fragment parent.
         * If that Fragment is destroyed while this async is still running we
         * could have a serious problem!
         */
        protected synchronized void convert_data() {
            if (mAdapter.mQuestionData == null) {
                return;
            }

            Question ranking1 = new Question(QUESTION_TYPE_RANK);
            ranking1.setPrompt("Favorite to unfavorite meals");
            ranking1.addRanking("breakfast");
            ranking1.addRanking("brunch");
            ranking1.addRanking("lunch");
            ranking1.addRanking("afternoon snack");
            ranking1.addRanking("dinner");
            ranking1.addRanking("supper");
            ranking1.addRanking("late-night snack");
            Collections.swap(ranking1.get_rank().mStrList, 0, 1);   // for testing, swap breakfast and brunch
            mAdapter.mQuestionData.add(ranking1);

            Question ranking2 = new Question(QUESTION_TYPE_RANK);
            ranking2.setPrompt("This is the second one. You know what to do.");
            ranking2.addRanking("apple");
            ranking2.addRanking("banana");
            ranking2.addRanking("carrot");
            ranking2.addRanking("dewberry");
            ranking2.addRanking("eggplant");
            mAdapter.mQuestionData.add(ranking2);

            Question fq = new Question (QUESTION_TYPE_FREEFORM);
            fq.setPrompt("Type in something first!");
            fq.set_freeform_response("no!");
            mAdapter.mQuestionData.add(fq);

            Question q1 = new Question(QUESTION_TYPE_CHECKBOX);
            q1.setPrompt("Fave restaurant?");
            q1.addCheckbox("Jim's", false);
            q1.addCheckbox("Mandola's", false);
            q1.addCheckbox("Luby's", false);
            q1.addCheckbox("Dog & Duck", false);
            q1.addCheckbox("Paco's", true);
            mAdapter.mQuestionData.add(q1);

            Question checkbox2 = new Question(QUESTION_TYPE_CHECKBOX);
            checkbox2.addCheckbox("Are you free tonight?", false);
            mAdapter.mQuestionData.add(checkbox2);

            Question radio1 = new Question(QUESTION_TYPE_RADIO);
            radio1.setPrompt("Pick a place");
            radio1.addRadioButton("White House");
            radio1.addRadioButton("Devil's Tower");
            radio1.addRadioButton("Four Corners");
            radio1.addRadioButton("Eiffel Tower");
            mAdapter.mQuestionData.add(radio1);

            Question freeform = new Question(QUESTION_TYPE_FREEFORM);
            freeform.setPrompt("Is it safe?");
            freeform.set_freeform_response("peaches!");
            mAdapter.mQuestionData.add(freeform);

            // Now do a bunch
            for (int i = 0; i < 3; i++) {
                Question freeform2 = new Question(QUESTION_TYPE_FREEFORM);
                freeform2.setPrompt("Is " + (i + 1) + " a lucky number?");
                mAdapter.mQuestionData.add(freeform2);
            }

            // How about a bunch of radios?
            for (int i = 0; i < 3; i++) {
                Question q = new Question(QUESTION_TYPE_RADIO);
                q.setPrompt("# " + (i + 1) + ": Select yes or no");
                q.addRadioButton("yes");
                q.addRadioButton("no");
                mAdapter.mQuestionData.add(q);
            }

        } // convert_data()

        /**
         * Another synchronized method.  This calls the data to be displayed.
         */
        private synchronized void display_data() {
            mAdapter.notifyDataSetChanged();
        }

    } // class RespondAsyncTaskl


}
