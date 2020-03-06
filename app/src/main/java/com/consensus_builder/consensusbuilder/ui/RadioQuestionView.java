package com.consensus_builder.consensusbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Using the BaseQuestionView as a model, this is specific to radio questions.
 *
 * Created on 9/22/16.
 */
public class RadioQuestionView extends LinearLayout {

    //-----------------------------
    //  Constants
    //-----------------------------

    /** Keys for the various data in the instance state bundle. */
    private static String
            KEY_SUPER_CLASS = "RadioSuperClass",
            KEY_EDITABLE = "RadioQuestionView_Editable",
            KEY_PROMPT = "RadioQuestionView_prompt";

    //-----------------------------
    //  Widgets
    //-----------------------------

    /** The main prompt for this question, static for question answerers. */
    private TextView mPromptStatic_tv;
    /** Main prompt, editable version for question makers. */
    private EditText mPromptEditable_et;

    /** Main image for this question. Right now it's just a background. */
    private ImageView mImage_iv;

    /** The RecyclerView for this Question. Optional. */
    private RecyclerView mRecycler;


    //-----------------------------
    //  Data
    //-----------------------------

    /** Prompt string for this question */
    private String mPrompt_str = null;

    /** Hint to be displayed when the prompt is editable and emtpy */
    private String mPromptHint_str = null;

    /** Background image for this question */
    private int mBackgroundImageResource;

    /** Layout manager for RecyclerView. Unused if no recyclerView. */
    private LinearLayoutManager mLayoutMgr;

    /** The name pretty much says it all. */
    private ItemTouchHelper.Callback mTouchHelperCallback;

    /** Adapter for the RecyclerView */
    private RadioQuestionAdapter mAdapter;

    /**
     * The primary question of this View: are we creating a question or answering a question?
     * The user who makes the questionnaire is creating questions.
     * The users who receive a questionnaire are answering questions.
     * How this View displays and responds depends on the situation.
     * Default is ANSWERING (which is false).
     */
    protected boolean mCreatingQuestion = false;

    /** Temporarily holds the input dat for a new RecyclerView item. */
    protected String mNewRecyclerLine;


    //-----------------------------
    //  Methods
    //-----------------------------

    public RadioQuestionView(Context context) {
        super(context);
        init (context);
    }

    public RadioQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context, attrs);
    }

    public RadioQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init (context, attrs);
    }

    private void init (Context ctx) {
        mLayoutMgr = new LinearLayoutManager(ctx);
        inflate_view(ctx);
    }

    private void init (Context ctx, AttributeSet attrs) {
        init (ctx);
        load_attrs(ctx, attrs);
    }


    /**
     * This is a great time to inflate our view!
     */
    private void inflate_view (Context ctx) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        inflater.inflate(R.layout.radio_question_view, this);
    }


    /**
     * Load the attributes from the XML layout of this BaseQuestionView and
     * modify our data correspondingly.
     */
    private void load_attrs (Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.RadioQuestionView);
        mPrompt_str = typedArray.getString(R.styleable.RadioQuestionView_setRadioPrompt);
        mPromptHint_str = typedArray.getString(R.styleable.RadioQuestionView_setRadioPromptHint);
        mBackgroundImageResource = typedArray.getResourceId(R.styleable.RadioQuestionView_setRadioBackground, 0);
        mCreatingQuestion = typedArray.getBoolean(R.styleable.RadioQuestionView_createRadioQuestion, false);

        typedArray.recycle();   // Shared resource--remember to let others play!
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setup_widgets();
    }

    /**
     * Since this layout is a little different from our parent, override this
     * to properly set up for the radio layout.
     */
    private void setup_widgets() {

        // Load up the widgets
        mImage_iv = (ImageView) findViewById(R.id.radio_question_image_iv);
        mImage_iv.setImageResource(mBackgroundImageResource);

        mPromptStatic_tv = (TextView) findViewById(R.id.radio_question_prompt_tv);
        mPromptEditable_et = (EditText) findViewById(R.id.radio_question_prompt_et);
        mPromptEditable_et.setHint(mPromptHint_str);
        setPrompt(mPrompt_str);

        // The RecyclerView and all its attending bits
        mRecycler = (RecyclerView) findViewById(R.id.radio_question_recycler);
        mRecycler.setLayoutManager(mLayoutMgr);

        mAdapter = new RadioQuestionAdapter(new ArrayList<String>());
        mRecycler.setAdapter(mAdapter);

        setCreatingQuestion(mCreatingQuestion);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.add_radio_prompt);

                final EditText newLine_et = new EditText(view.getContext());
                newLine_et.setHint(R.string.add_radio_hint);
                newLine_et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(newLine_et);   // Make the EditText the entire View of the AlertDialog.

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNewRecyclerLine = newLine_et.getText().toString();

                        // Add this line to our recyclerView
                        mAdapter.add (mNewRecyclerLine);
                        dialog.dismiss();
                        mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);    // Scroll to end
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });

        // Get the touch stuff working
        mTouchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(mTouchHelperCallback);
        touchHelper.attachToRecyclerView(mRecycler);

        // If we're just answering (and not Creating) a question, then we should
        // turn off the recycler's editing capabilities.
        ((TouchHelperCallback) mTouchHelperCallback).mDeletable = mCreatingQuestion;
        ((TouchHelperCallback) mTouchHelperCallback).mRearrangeable = mCreatingQuestion;

    } // setup_widgets()


    /**
     * Adds another radio button to this View.
     *
     * @param newline   The String to display with the new radio button. Use null
     *                  if no message is needed.
     */
    public void addRadioButton (String newline, boolean scrollTo) {
        mAdapter.add(newline);

        if (scrollTo) {
            mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }


    /**
     * Replaces any data in this instance with the contents of the given Question.
     *
     * @param q         The Radio Button Question to display.
     *
     * @param scrollto  Set this to TRUE if you want the recycler to scroll
     *                  to this item after added.  Don't bother if you're
     *                  adding a whole bunch of things at the same time as
     *                  that will slow everything down.
     */
    public void setQuestion (Question q, boolean scrollto) {
        if (q.get_type() != Question.QUESTION_TYPE_RADIO) {
            return;
        }

        setPrompt(q.getPrompt());
        for (int i = 0; i < q.numRadioButtons(); i++) {
            String str = q.getRadioButtonString(i);
            addRadioButton(str, false);
        }
    }


    /**
     * Handles the logic and UI for modifying the state of the View, which
     * entirely depends on how this View is being used (creating a question
     * by the questionnaire maker or answering a question by someone receiving
     * the questionnaire).
     *
     * @param creatingQuestion <br>
     *          True = the prompt may be edited (for creating and editing
     *          questions.<br>
     *          False = The prompt may not be changed, used when the
     *                 question is simply being answered.
     *
     * side effects:
     *      mCreateQuestion is set to the input's value.
     *      Several widgets are turned off/on depending on this value.
     */
    public void setCreatingQuestion (boolean creatingQuestion) {

        // If we're switching states, save the currently displaying
        // prompt, just in case it hasn't been recorded yet.
        if (mCreatingQuestion != creatingQuestion) {
            mPrompt_str = mCreatingQuestion ?
                    mPromptEditable_et.getText().toString() :
                    mPromptStatic_tv.getText().toString();
            mCreatingQuestion = creatingQuestion;
        }

        if (mCreatingQuestion) {
            mPromptStatic_tv.setVisibility(GONE);
            mPromptEditable_et.setVisibility(VISIBLE);
            mPromptEditable_et.setText(mPrompt_str);
        }
        else {
            mPromptStatic_tv.setVisibility(VISIBLE);
            mPromptEditable_et.setVisibility(GONE);
            mPromptStatic_tv.setText(mPrompt_str);
        }
    }

    /**
     * Returns whether or not this View is creating / editing a question or
     * just displaying a question.
     */
    public boolean isCreatingQuestion() {
        return mCreatingQuestion;
    }


    public void setPrompt (String prompt) {
        mPrompt_str = prompt;
        if (mCreatingQuestion) {
            mPromptEditable_et.setText(mPrompt_str);
        }
        else {
            mPromptStatic_tv.setText(mPrompt_str);
        }
    }

    public String getPrompt() {
        mPrompt_str = mCreatingQuestion ?
                mPromptEditable_et.getText().toString() :
                mPromptStatic_tv.getText().toString();
        return mPrompt_str;
    }


    /**
     * Necessary to preserve the state of the widget during configuration changes.
     */
    @Override
    protected Parcelable onSaveInstanceState() {

        // In case the prompt hasn't been saved, go ahead and grab it.
        mPrompt_str = mCreatingQuestion
                ? mPromptEditable_et.getText().toString()
                : mPromptStatic_tv.getText().toString();

        Bundle bundle = new Bundle();

        // Saves whatever the super wants to save in our Bundle.  Nice!
        bundle.putParcelable(KEY_SUPER_CLASS, super.onSaveInstanceState());

        // Save our data.
        bundle.putBoolean(KEY_EDITABLE, mCreatingQuestion);
        bundle.putString(KEY_PROMPT, mPrompt_str);

        return bundle;
    }

    /**
     * Necessary to preserve the state of the widget during configuration changes.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_CLASS));

            mCreatingQuestion = bundle.getBoolean(KEY_EDITABLE, false);
            mPrompt_str = bundle.getString(KEY_PROMPT);
        }
        else {
            super.onRestoreInstanceState(state);
        }

        setCreatingQuestion(mCreatingQuestion);
    }

    /**
     * Necessary to preserve the state of the widget during configuration changes.<br>
     * <br>
     * Makes sure that the state of the child views are not saved since
     * we handle the state in the onSaveInstanceState [sic].
     */
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }


    /**
     * Turns all the data into a Question and returns it in one big piece.
     */
    public Question getQuestion() {
        Question q = new Question();
        q.set_type(Question.QUESTION_TYPE_RADIO);
        q.setPrompt(getPrompt());

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            String radio_str = mAdapter.mStringData.get(i);
            q.addRadioButton(radio_str);
        }

        // Using the default of none selected.
        q.setRadioButtonSelected(-1);

        return q;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    public class RadioQuestionAdapter
            extends RecyclerView.Adapter<RadioQuestionViewHolder>
            implements TouchHelperInterface {

        //---------------------
        //  Constants
        //---------------------

        private final String TAG = RadioQuestionAdapter.class.getSimpleName();

        //---------------------
        //  Data
        //---------------------

        /**
         * The strings for all the radio buttons.  Each item in the list is the
         * text for a radio button.
         * todo: corresponding list for images (or a list of a string & image class)
         */
        protected List<String> mStringData;


        //---------------------
        //  Methods
        //---------------------

        /**
         * Constructor.
         *
         * @param data  The data to display.  Right now it's simply a bunch
         *              of Strings.
         */
        public RadioQuestionAdapter (ArrayList<String> data) {
            mStringData = data;
        }

        /**
         * Adds the given String to the RecyclerView. This new string will
         * take up a brand-new item at the end of the RecyclerView.
         *
         * todo:  Modify this for images too.
         *
         * @param str   Data to be added.
         */
        public void add (String str) {
            mStringData.add(str);
            mAdapter.notifyItemInserted(mStringData.size() - 1);     // Added item at the end.
        }


        @Override
        public RadioQuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            Log.d(TAG, "onCreateViewholder()");

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.radio_view_recycler_card, parent, false);

            return new RadioQuestionViewHolder(v);
        }


        @Override
        public void onBindViewHolder(RadioQuestionViewHolder holder, int position) {
            holder.m_et.setText(mStringData.get(position));

            // todo: set the image
        }

        @Override
        public int getItemCount() {
            return mStringData.size();
        }


        @Override
        public void onItemMove(RecyclerView.ViewHolder viewHolder,
                               RecyclerView.ViewHolder targetViewHolder,
                               int fromPos, int toPos) {
            Collections.swap(mStringData, fromPos, toPos);
            notifyItemMoved(fromPos, toPos);
        }

        @Override
        public void onItemDismiss(RecyclerView.ViewHolder viewHolder, final int pos) {
            // Before actually deleting the entry, give the user a chance to change their mind.

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Really delete?");

            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delete_yes(pos);
                }
            });

            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delete_no(pos);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        /**
         * Action to perform when the user clicks the OK button when deleting
         * an item from the recyclerView.
         *
         * @param pos   The position of the item within the recyclerView.
         */
        protected void delete_yes (int pos) {
//            Log.d(TAG, "position = " + pos);

            // Tell the adapter to go ahead and delete the item.
            mStringData.remove(pos);

            // The item swiped and everything past it needs updating.
            notifyItemRemoved(pos);
        }

        /**
         * Action to take when the use cancels a delete.
         *
         * @param pos   Position of the item in question.
         */
        protected void delete_no (int pos) {
            // I can't believe that this is all we need to do to
            // make the View pop back into place!  Wow!
            notifyItemChanged(pos);
        }

    } // class RadioQuestionAdapter


    /**
     * Viewholder for all the radio buttons for this radio question.
     *
     * Matches radio_view_recycler_card.xml
     *
     * todo: modify this to handle images
     */
    private class RadioQuestionViewHolder extends RecyclerView.ViewHolder {

        //-----------------
        //  Widgets
        //-----------------

        /** Used to get the default checked state of this item */
        protected RadioButton m_rb;
        protected EditText m_et;
        protected ImageView m_iv;

        //-----------------
        //  Methods
        //-----------------

        public RadioQuestionViewHolder(View v) {
            super(v);
            m_rb = (RadioButton) v.findViewById(R.id.radio_view_card_rb);
            m_et = (EditText) v.findViewById(R.id.radio_view_card_et);
            m_iv = (ImageView) v.findViewById(R.id.radio_view_card_iv);
        }
    } // class RadioQuestionViewHolder


    /**
     * An implementation of {@link ItemTouchHelper.Callback}
     * that enables basic drag & drop and swipe-to-dismiss.  Drag events are automatically
     * started by an item long-press.<br>
     */
    public class TouchHelperCallback
            extends ItemTouchHelper.Callback {

        /** Determines if the user can drag and rearrange items */
        public boolean mRearrangeable = true;

        /** User can swipe to delete items */
        public boolean mDeletable = true;


        //-------------------------
        //  Methods
        //-------------------------

        public TouchHelperCallback() {}

        @Override
        public boolean isLongPressDragEnabled() {
            return mRearrangeable;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return mDeletable;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0, swipeFlags = 0;

            if (mRearrangeable) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            if (mDeletable) {
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }

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