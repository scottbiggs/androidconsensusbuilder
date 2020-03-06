package com.consensus_builder.consensusbuilder.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;
import com.consensus_builder.consensusbuilder.ui.CheckboxQuestionView;
import com.consensus_builder.consensusbuilder.ui.CreateQuestionCheckboxDialogActivity;
import com.consensus_builder.consensusbuilder.ui.CreateQuestionFreeformDialogActivity;
import com.consensus_builder.consensusbuilder.ui.CreateQuestionRadioDialogActivity;
import com.consensus_builder.consensusbuilder.ui.CreateQuestionRankDialogActivity;
import com.consensus_builder.consensusbuilder.ui.FreeformQuestionView;
import com.consensus_builder.consensusbuilder.ui.RadioQuestionView;
import com.consensus_builder.consensusbuilder.ui.RankQuestionView;
import com.consensus_builder.consensusbuilder.ui.TouchHelperInterface;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Constructing questionnaires is done here.
 *
 * The main gist of the Fragment is a RecyclerView with a series of Cards,
 * each representing an item of the questionnaire.  The types of Cards are:
 *
 *      Checkbox        Answerer can select a number of answers
 *      Radio           May select just one answer
 *      Freeform        Just a question, answered with text
 *      Ranking         Rank each item from top to bottom
 */
public class QuestionnaireFragment extends Fragment {

    //----------------------
    //  Constants
    //----------------------

    private static final String TAG = QuestionnaireFragment.class.getSimpleName();

    /** The requestCode that signals creating a new question. */
    private static final int NEW_QUESTION_IDENTIFIER = 281;

    /** Request code signaling that a Question has been edited. */
    private static final int EDIT_QUESTION_IDENTIFIER = 924;

    /** Key for accessing the number of Bundles transferred via onSaveInstanceState & onRestoreInstanceState */
    private static final String KEY_BUNDLE_COUNT = "key_bundle_count";

    /**
     * Base for the keys for all the Bundles stored in the SaveInstanceState Bundle.
     * This is kind of hackey, but the key for each bundle is its array postion concatenated
     * at the end of this key.
     *
     * For example:
     *      The 3rd bundle (array element 2) will use the key
     *      key_bundle_prefix2
     *
     *      That's the String below + the array element number.
     */
    private static final String KEY_BUNDLE_PREFIX = "key_bundle_prefix";

    /** Number of milliseconds for a Send lock to timeout. */
    private static final long LOCK_TIMEOUT = 2000L;


    //----------------------
    //  Widgets
    //----------------------

    private RecyclerView mRecycler;

    /** Assists with managing the RecyclerView */
    private LinearLayoutManager mLayoutMgr;

    /** The FAB that the user presses to send their response to the server. */
    private FloatingActionButton mSendQuestionnaireFAB;


    //----------------------
    //  Class Data
    //----------------------

    /** Adapter.  The parent actually holds the data. */
    private QuestionnaireFragmentAdapter mAdapter;

    /** A locking mechanism to prevent duplicate sends. Locks timeout in LOCK_TIMEOUT milliseconds. */
    private long mSendLock = 0L;


    //----------------------
    //  Methods
    //----------------------


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.questionnaire, container, false);

        // Get the RecyclerView.  We'll attend to it once the Activity is ready
        mRecycler = (RecyclerView) v.findViewById(R.id.questionnaire_rv);


        Button new_radio = (Button) v.findViewById(R.id.questionnaire_add_radio);
        new_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itt = new Intent(getActivity(), CreateQuestionRadioDialogActivity.class);
                startActivityForResult(itt, NEW_QUESTION_IDENTIFIER);
            }
        });

        Button new_check = (Button) v.findViewById(R.id.questionnaire_add_checkbox);
        new_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itt = new Intent(getActivity(), CreateQuestionCheckboxDialogActivity.class);
                startActivityForResult(itt, NEW_QUESTION_IDENTIFIER);
            }
        });

        Button new_rank = (Button) v.findViewById(R.id.questionnaire_add_rank);
        new_rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itt = new Intent(getActivity(), CreateQuestionRankDialogActivity.class);
                startActivityForResult(itt, NEW_QUESTION_IDENTIFIER);
            }
        });

        Button new_freeform = (Button) v.findViewById(R.id.questionnaire_add_freeform);
        new_freeform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itt = new Intent(getActivity(), CreateQuestionFreeformDialogActivity.class);
                startActivityForResult(itt, NEW_QUESTION_IDENTIFIER);
            }
        });


        mSendQuestionnaireFAB = (FloatingActionButton) v.findViewById(R.id.questionnaire_fab);
        mSendQuestionnaireFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prevent accidental multiple clicks
                if (!test_lock()) {
                    return;
                }

                send_questionnaire();

            }
        });

        return v;
    }


    /**
     * Sends the data displayed in this Fragment to the server to be distributed
     * to the email list (which is included in the data sent).
     */
    private void send_questionnaire() {

        Snackbar.make(getView(), R.string.sending_questionnaire_msg, Snackbar.LENGTH_LONG).show();

        // todo: send questionnaire to server (including the recipient list)

        // Don't need to send this again!  But don't despair, it'll be turned back
        // on once anything changes in the questionnaire.
        mSendQuestionnaireFAB.setVisibility(View.GONE);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The RecyclerView and all its attending bits
        mLayoutMgr = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mLayoutMgr);

        mAdapter = new QuestionnaireFragmentAdapter();
        mRecycler.setAdapter(mAdapter);

        // Get the touch stuff working
        ItemTouchHelper.Callback callback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecycler);

        // Restore any previously saved data
        restore_saved_data (savedInstanceState);

    } // onActivityCreate(.)


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
        int question_count = savedInstanceState.getInt(KEY_BUNDLE_COUNT, 0);

        // Just in case, make sure that the question data list is ready.
        mAdapter.mQuestionData.clear();

        for (int i = 0; i < question_count; i++) {
            Question q = new Question(savedInstanceState.getBundle(KEY_BUNDLE_PREFIX + i));
            mAdapter.mQuestionData.add(q);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the questions (if there are any).
        ArrayList<Question> questions = mAdapter.mQuestionData;
        if ((questions != null) && (questions.size() > 0)) {

            // First, store the count of the Bundles
            outState.putInt(KEY_BUNDLE_COUNT, questions.size());

            // Save all the bundles with their unique key
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                Bundle bundle = q.make_into_bundle();
                outState.putBundle(KEY_BUNDLE_PREFIX + i, bundle);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_CANCELED) {
            // do nothing
            return;
        }

        // A change has been detected, turn the FAB on (in case it had been turned off).
        mSendQuestionnaireFAB.setVisibility(View.VISIBLE);


        switch (requestCode) {
            case NEW_QUESTION_IDENTIFIER:
                // add question at end of RecyclerView.
                Bundle bundle = data.getExtras();
                Question q = new Question(bundle);
                Log.d(TAG, "onActivityResult(), bundle = " + q.toString());

                mAdapter.mQuestionData.add(q);
                mAdapter.notifyItemInserted(mAdapter.mQuestionData.size() - 1);     // Added item at the end.
                mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);        // Scroll to end
                break;

            case EDIT_QUESTION_IDENTIFIER:
                // todo be able to edit a question.
                break;

            default:
                Log.e (TAG, "Illegal requestCode in onActivityResult()!  requestCode = " + requestCode);
                break;
        }
    }

    /**
     * Used for testing
     */
    private ArrayList<Question> make_questions() {
        ArrayList<Question> questions = new ArrayList<>();

/*      TEST DATA

        Question checkbox1 = new Question(QUESTION_TYPE_CHECKBOX);
        checkbox1.set_checkbox_prompt("Who do you like?");
        checkbox1.addCheckbox("I like ike", false);
        checkbox1.addCheckbox("jfk", false);
        checkbox1.addCheckbox("and lbj", false);
        questions.add(checkbox1);

        Question checkbox2 = new Question(QUESTION_TYPE_CHECKBOX);
        checkbox2.addCheckbox("Do you love me?", false);
        questions.add(checkbox2);

        Question radio1 = new Question(QUESTION_TYPE_RADIO);
        radio1.setRadioPrompt("Who's your favorite Republican president?");
        radio1.addRadioButton("Ike");
        radio1.addRadioButton("Nixon");
        radio1.addRadioButton("Ford");
        radio1.addRadioButton("Reagan");
        radio1.addRadioButton("Bush 41");
        radio1.addRadioButton("Bush 43");
        questions.add(radio1);

        Question ranking1 = new Question(QUESTION_TYPE_RANK);
        ranking1.setRankingPrompt("Please rank the following:");
        ranking1.addRanking("eggs");
        ranking1.addRanking("pizza");
        ranking1.addRanking("manure");
        ranking1.addRanking("wonton");
        ranking1.addRanking("nails");
        questions.add(ranking1);

        Question freeform = new Question(QUESTION_TYPE_FREEFORM);
        freeform.set_freeform_prompt("Are you happy and know it?");
        questions.add(freeform);
*/
        return questions;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Adapter for the RecyclerView of this Fragment.  See the
     * parent for more info.
     */
    class QuestionnaireFragmentAdapter
            extends RecyclerView.Adapter<QuestionnaireFragmentViewHolder>
            implements TouchHelperInterface {

        //---------------------------
        //  Constants
        //---------------------------

        private final String TAG = QuestionnaireFragmentAdapter.class.getSimpleName();

        //---------------------------
        //  Data
        //---------------------------

        /**
         * Holds all the data, an ArrayList of Questions.
         */
        public ArrayList<Question> mQuestionData;


        //---------------------------
        //  Methods
        //---------------------------

        /**
         * Constructor.
         *
         * @param questions     You may initialize this with a list of Questions to
         *                      display.
         */
        public QuestionnaireFragmentAdapter(ArrayList<Question> questions) {
            mQuestionData = questions;
        }

        public QuestionnaireFragmentAdapter() {
            mQuestionData = new ArrayList<>();
        }


        @Override
        public int getItemCount() {
            return mQuestionData.size();
        }


        @Override
        public QuestionnaireFragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.questionnaire_fragment_recycler_card, parent, false);

            return new QuestionnaireFragmentViewHolder(v);
        }


        @Override
        public void onBindViewHolder(QuestionnaireFragmentViewHolder holder, int pos) {

            setTitle(holder, pos);

            holder.set_question(mQuestionData.get(pos));

        } // onBindViewHolder(holder, pos)


        /**
         * Given a ViewHolder and its position, this method sets the correct title
         * for this card (it depends on the position--duh!).
         *
         * @param holder    The ViewHolder to modify.
         *
         * @param pos       The position of the ViewHolder in the Adapter (0 based).
         */
        protected void setTitle (QuestionnaireFragmentViewHolder holder, int pos) {
            // Set the title of each card.  It's just the card's position with a prefix
            String title = getActivity().getText(R.string.qtype_number_prefix).toString();
            title += (pos + 1);
            holder.m_title.setText(title);
        }


        //----------
        //  These methods pertain to the moving and dismissing of items.
        //

        @Override
        public void onItemDismiss(final RecyclerView.ViewHolder viewHolder, final int pos) {

            // Before actually deleting the entry, give the user a chance to change their mind.

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.delete_confirmation);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Tell the adapter to go ahead and delete the item.
                    mQuestionData.remove(pos);

                    // Now each item AFTER needs to have its title updated (so the number
                    // in the title is correct).
                    for (int i = pos; i < mQuestionData.size(); i++) {
                        // get the ViewHolder at that position
                        QuestionnaireFragmentViewHolder vh = (QuestionnaireFragmentViewHolder) mRecycler.findViewHolderForAdapterPosition(i);
                        if (vh != null) {
                            setTitle(vh, i);
                        }
                    }

                    // The item swiped and everything past it needs updating.
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos, getItemCount());
                }
            });

            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // I can't believe that this is all we need to do to
                    // make the View pop back into place!  Wow!
                    notifyItemChanged(pos);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        @Override
        public void onItemMove(RecyclerView.ViewHolder viewHolder,
                               RecyclerView.ViewHolder viewHolderTarget,
                               int fromPos, int toPos) {

            // Shift all the items that were moved over.
            Collections.swap(mQuestionData, fromPos, toPos);

            // Any time a Card is moved, it's title needs updating.
            setTitle((QuestionnaireFragmentViewHolder)viewHolder, toPos);
            setTitle((QuestionnaireFragmentViewHolder) viewHolderTarget, fromPos);

            notifyItemMoved(fromPos, toPos);
        }


    } // class QuestionRecylerAdapter



    /**
     * ViewHolder for the QuestionnaireFragment's RecyclerView.
     *
     * As usual, the data in this class MUST correspond to the widgets in
     * questionnaire_fragment_recycler_card.xml.
     */
    public class QuestionnaireFragmentViewHolder extends RecyclerView.ViewHolder {

        //---------------------------
        //  Constants
        //---------------------------

        private final String TAG = QuestionnaireFragmentViewHolder.class.getSimpleName();

        //---------------------------
        //  Widgets
        //---------------------------

        //
        // These MUST correspond to the Views in questionnaire_fragment_recycler_card.xml!
        //

        public TextView m_title;

        public CheckboxQuestionView m_checkboxView;
        public RankQuestionView m_rankView;
        public RadioQuestionView m_radioView;
        public FreeformQuestionView m_freeform;


        //---------------------------
        //  Methods
        //---------------------------

        public QuestionnaireFragmentViewHolder (View v) {
            super(v);

            // Do the expensive operations here, in the constructor.
            m_title = (TextView) v.findViewById(R.id.questionnaire_card_title_tv);

            m_checkboxView = (CheckboxQuestionView) v.findViewById(R.id.questionnaire_card_checkbox);
            m_rankView = (RankQuestionView) v.findViewById(R.id.questionnaire_card_rank);
            m_radioView = (RadioQuestionView) v.findViewById(R.id.questionnaire_card_radio);
            m_freeform = (FreeformQuestionView) v.findViewById(R.id.questionnaire_card_freeform);
        }

        /**
         * Load up the Widgets with data from the given Question.  Any previous data will
         * be removed and/or turned off.
         */
        public void set_question (Question q) {

            hide_all_views();

            switch (q.get_type()) {
                case Question.QUESTION_TYPE_CHECKBOX:
                    m_checkboxView.setQuestion(q, false);   // No scrolling here--might slow things down
                    m_checkboxView.setEditable(true);
                    m_checkboxView.setVisibility(View.VISIBLE);
                    break;

                case Question.QUESTION_TYPE_RADIO:
                    m_radioView.setQuestion (q, false);
                    m_radioView.setVisibility(View.VISIBLE);
                    break;

                case Question.QUESTION_TYPE_RANK:
                    m_rankView.setQuestion (q, false);
                    m_rankView.setVisibility(View.VISIBLE);
                    break;

                case Question.QUESTION_TYPE_FREEFORM:
                    m_freeform.setFromQuestion(q);
                    m_freeform.setVisibility(View.VISIBLE);
                    m_freeform.setCreatingQuestion(true);
                    break;

                default:
                    Log.e(TAG, "Unknown Question type in set_question()!");
                    break;
            }
        } // set_question (q)


        private void hide_all_views() {
            m_checkboxView.setVisibility(View.GONE);
            m_radioView.setVisibility(View.GONE);
            m_rankView.setVisibility(View.GONE);
            m_freeform.setVisibility(View.GONE);
        }

    } // class QuestionnaireFragmentViewHolder


    /**
     * An implementation of {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}
     * that enables basic drag & drop and swipe-to-dismiss.  Drag events are automatically
     * started by an item long-press.<br>
     * <br>
     * Expects the <code>QuestionRecyclerAdapter</code> to listen for {@link TouchHelperInterface}
     * callbacks and the <code>QuestionViewHolder</code> to implement
     * {@link QuestionnaireFragmentViewHolder}.
     */
    public class TouchHelperCallback
            extends android.support.v7.widget.helper.ItemTouchHelper.Callback {

        //---------------------------
        //  Data
        //---------------------------

        private final String TAG = TouchHelperCallback.class.getSimpleName();

        //---------------------------
        //  Methods
        //---------------------------

        public TouchHelperCallback() {}

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = android.support.v7.widget.helper.ItemTouchHelper.UP | android.support.v7.widget.helper.ItemTouchHelper.DOWN;
            int swipeFlags = android.support.v7.widget.helper.ItemTouchHelper.START | android.support.v7.widget.helper.ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder, target, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder, viewHolder.getAdapterPosition());
        }

    } // class TouchHelperCallback


}
