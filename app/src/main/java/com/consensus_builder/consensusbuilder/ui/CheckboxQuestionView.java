package com.consensus_builder.consensusbuilder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Using the BaseQuestionView as a model, this is specific to Checkbox questions.
 *
 * Created on 9/22/16.
 */
public class CheckboxQuestionView extends LinearLayout {

    //-----------------------------
    //  Constants
    //-----------------------------

    /** Keys for the various data in the instance state bundle. */
    private static String
            KEY_SUPER_CLASS = "CheckboxSuperClass",
            KEY_EDITABLE = "CheckboxQuestionView_Editable",
            KEY_PROMPT = "CheckboxQuestionView_prompt";

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

    /** FAB for adding new checkbox lines */
    private FloatingActionButton mAddFAB;


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
    private CheckboxQuestionAdapter mAdapter;

    /**
     * Can this question be edited?  True for when you are creating or modifying the question.
     * False for when responding to a question.
     */
    protected boolean mEditable = false;


    //-----------------------------
    //  Methods
    //-----------------------------

    public CheckboxQuestionView(Context context) {
        super(context);
        init (context);
    }

    public CheckboxQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context, attrs);
    }

    public CheckboxQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        inflater.inflate(R.layout.checkbox_question_view, this);
    }


    /**
     * Load the attributes from the XML layout of this BaseQuestionView and
     * modify our data correspondingly.
     */
    private void load_attrs (Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.CheckboxQuestionView);
        mPrompt_str = typedArray.getString(R.styleable.CheckboxQuestionView_setCheckboxPrompt);
        mPromptHint_str = typedArray.getString(R.styleable.CheckboxQuestionView_setCheckboxPromptHint);
        mBackgroundImageResource = typedArray.getResourceId(R.styleable.CheckboxQuestionView_setCheckboxBackground, 0);
        mEditable = typedArray.getBoolean(R.styleable.CheckboxQuestionView_setEditable, false);

        typedArray.recycle();   // Shared resource--remember to let others play!
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setup_widgets();
    }

    /**
     * Since this layout is a little different from our parent, override this
     * to properly set up for the checkbox layout.
     */
    private void setup_widgets() {

        // Load up the widgets
        mImage_iv = (ImageView) findViewById(R.id.checkbox_question_image_iv);
        mImage_iv.setImageResource(mBackgroundImageResource);

        mPromptStatic_tv = (TextView) findViewById(R.id.checkbox_question_prompt_tv);
        mPromptEditable_et = (EditText) findViewById(R.id.checkbox_question_prompt_et);
        mPromptEditable_et.setHint(mPromptHint_str);
        setPrompt(mPrompt_str);

        // The RecyclerView and all its attending bits
        mRecycler = (RecyclerView) findViewById(R.id.checkbox_question_recycler);
        mRecycler.setLayoutManager(mLayoutMgr);

        mAdapter = new CheckboxQuestionAdapter(new ArrayList<String>());
        mRecycler.setAdapter(mAdapter);

        mAddFAB = (FloatingActionButton) findViewById(R.id.checkbox_add_fab);
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.add_checkbox_prompt);

                final EditText newLine_et = new EditText(view.getContext());
                newLine_et.setHint(R.string.add_checkbox_hint);
                newLine_et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(newLine_et);   // Make the EditText the entire View of the AlertDialog.

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Add this line to our recyclerView
                        addCheckbox(newLine_et.getText().toString(), false, true);
                        dialog.dismiss();
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

        setEditable(mEditable);

    } // setup_widgets()


    /**
     * Replaces any data in this instance with the contents of the given Question.
     * Assumes that this Question IS a checkbox type.  If not, then nothing is
     * done.
     *
     * @param q         The Checkbox Question to display.
     *
     * @param scrollto  Set this to TRUE if you want the recycler to scroll
     *                  to this item after added.  Don't bother if you're
     *                  adding a whole bunch of things at the same time as
     *                  that will slow everything down.
     */
    public void setQuestion (Question q, boolean scrollto) {
        if (q.get_type() != Question.QUESTION_TYPE_CHECKBOX) {
            return;
        }

        setPrompt(q.getPrompt());
        for (int i = 0; i < q.numCheckboxes(); i++) {
            String str = q.getCheckboxString(i);
            Boolean checked = q.isCheckboxChecked(i);
            addCheckbox(str, false, false);
        }
    }


    /**
     * Add a new checkbox to this CheckboxQuestionView. That is just ONE checkbox to
     * this CheckboxQuestionView (does NOT include the prompt!).
     *
     * @param text      Text of the Checkbox. Use null if no text needed.
     *
     * todo: Add ability to handle images.
     *
     * @param checked   Is this item checked or not?
     *
     * @param scrollto  Set this to TRUE if you want the recycler to scroll
     *                  to this item after added.  Don't bother if you're
     *                  adding a whole bunch of things at the same time as
     *                  that will slow everything down.
     */
    public void addCheckbox (String text, boolean checked, boolean scrollto) {
        mAdapter.add (text);

        // todo:  handle the checks!

        if (scrollto) {
            mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }


    /**
     * Handles the logic and UI for modifying the state of the View, which
     * entirely depends on how this View is being used (creating a question
     * by the questionnaire maker or responding to a question).
     *
     * @param editable <br>
     *          True = the CheckboxView is now editable. You may modify
     *          all aspects.<br>
     *          False = The prompt may not be changed, used when the
     *                 question is simply being answered.
     *
     * side effects:
     *      mCreateQuestion is set to the input's value.
     *      Several widgets are turned off/on depending on this value.
     */
    public void setEditable (boolean editable) {

        // If we're switching states, save the currently displaying
        // prompt, just in case it hasn't been recorded yet.
        if (mEditable != editable) {
            mPrompt_str = mEditable ?
                    mPromptEditable_et.getText().toString() :
                    mPromptStatic_tv.getText().toString();
            mEditable = editable;

            // Force a re-evaluation of the data.
            mAdapter.resetCheckboxData();
        }

        if (mEditable) {
            mPromptStatic_tv.setVisibility(GONE);
            mPromptEditable_et.setVisibility(VISIBLE);
            mPromptEditable_et.setText(mPrompt_str);
            mAddFAB.setVisibility(VISIBLE);
        }
        else {
            mPromptStatic_tv.setVisibility(VISIBLE);
            mPromptEditable_et.setVisibility(GONE);
            mPromptStatic_tv.setText(mPrompt_str);
            mAddFAB.setVisibility(GONE);
        }


        // Recycler's editing capabalilities depend on the editable state.
        ((TouchHelperCallback) mTouchHelperCallback).mDeletable = mEditable;
        ((TouchHelperCallback) mTouchHelperCallback).mRearrangeable = mEditable;

        // When creating a question, the checkboxes are inactive
        mAdapter.setCheckboxesEnabled(!mEditable);

        // And the editability of the text for a checkbox is the exact opposite
        // of the usability of the checkbox itself (yet is the same as mEditable).
        mAdapter.setCheckboxTextEditable(mEditable);

        // And we change the look of the checkboxes as well.
        mAdapter.setCheckboxCardViewVisible(mEditable);
    }

    /**
     * Returns whether or not this View is creating / editing a question or
     * just displaying a question.
     */
    public boolean getEditable() {
        return mEditable;
    }


    public void setPrompt (String prompt) {
        mPrompt_str = prompt;
        if (mEditable) {
            mPromptEditable_et.setText(mPrompt_str);
        }
        else {
            mPromptStatic_tv.setText(mPrompt_str);
        }
    }

    public String getPrompt() {
        mPrompt_str = mEditable ?
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
        mPrompt_str = mEditable
                ? mPromptEditable_et.getText().toString()
                : mPromptStatic_tv.getText().toString();

        Bundle bundle = new Bundle();

        // Saves whatever the super wants to save in our Bundle.  Nice!
        bundle.putParcelable(KEY_SUPER_CLASS, super.onSaveInstanceState());

        // Save our data.
        bundle.putBoolean(KEY_EDITABLE, mEditable);
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

            mEditable = bundle.getBoolean(KEY_EDITABLE, false);
            mPrompt_str = bundle.getString(KEY_PROMPT);
        }
        else {
            super.onRestoreInstanceState(state);
        }

        setEditable(mEditable);
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
        mAdapter.resetCheckboxData();   // Make sure all checkboxes are updated!

        Question q = new Question();
        q.set_type(Question.QUESTION_TYPE_CHECKBOX);
        q.setPrompt(getPrompt());

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            String checkbox_str = mAdapter.mStringData.get(i);
            q.addCheckbox(checkbox_str, false);     // todo: make this read in the checkmark!
        }

        return q;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    /******************************************
     * Since the names of the widgets are the same in both checkbox_view_recycler_card.xml
     * and base_question_view_recycler_card.xml, we save ourselves a lot of work.  Just
     * change the layout and the type, and we're in business!
     */
    public class CheckboxQuestionAdapter
            extends RecyclerView.Adapter<CheckboxQuestionViewHolder>
            implements TouchHelperInterface {

        //---------------------
        //  Constants
        //---------------------

        private final String TAG = CheckboxQuestionAdapter.class.getSimpleName();

        //---------------------
        //  Data
        //---------------------

        /**
         * The strings for all the checkboxes.  Each item in the list is the
         * text for a checkbox.
         * todo: corresponding list for images (or a list of a string & image class)
         */
        protected List<String> mStringData;

        /** Flag for whether the checkboxes are enabled (default) or disabled. */
        protected boolean mCheckboxesEnabled = true;

        /** Can the text for each checkbox be edited?  Default is false (no). */
        protected boolean mCheckboxTextEditable = false;

        /** Do we display the cardview for each checkbox? Default is false (no). */
        protected boolean mCardViewVisible = false;

        /** Save the default elevation so it can be restored later */
        private float mDefaultElevation;


        //---------------------
        //  Methods
        //---------------------

        /**
         * Constructor.
         *
         * @param data  The data to display.  Right now it's simply a bunch
         *              of Strings.
         */
        public CheckboxQuestionAdapter (ArrayList<String> data) {
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
        public CheckboxQuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.checkbox_view_recycler_card, parent, false);

            CheckboxQuestionViewHolder holder = new CheckboxQuestionViewHolder(v);

            // Save info about the original CardView. It'll be necessary to restore these
            // numbers later.
            //
            // NOTE: The default is the Editable state!
            //
            // NOTE: the View v is the exact same as holder.m_cardview in this case.
            //
            mDefaultElevation = v.getElevation();

            return holder;
        }


        @Override
        public void onBindViewHolder(CheckboxQuestionViewHolder holder, int position) {
            holder.m_et.setText(mStringData.get(position));

            // Enable or disable the checkbox
            holder.m_cb.setEnabled(mCheckboxesEnabled);

            // Enable or disable the EditText for the checkbox line
            holder.m_et.setEnabled(mCheckboxTextEditable);

            if (mCardViewVisible) {
                // Restore elevation and padding
                holder.m_cardview.setElevation(mDefaultElevation);

                // Using the side effect of forcing this padding as a way of providing
                // separation when in editing mode.
                holder.m_cardview.setUseCompatPadding(true);
                holder.m_cardview.setPreventCornerOverlap(true);
            }
            else {
                // Remove the elevation and margin
                holder.m_cardview.setElevation(0);

                // Restore defaults (no padding) to make the checkboxes fit tightly
                // when in response mode.
                holder.m_cardview.setUseCompatPadding(false);
                holder.m_cardview.setPreventCornerOverlap(false);
            }


            // todo: set the image
        }

        @Override
        public int getItemCount() {
            return mStringData.size();
        }


        /**
         * There are times where you want to disable (and later re-enable) the checkboxes
         * in the recyclerview.  This methods lets you do that!
         *
         * @param enabled   When true, checkboxes work normally. False disables
         *                  the checkboxes.
         */
        public void setCheckboxesEnabled(boolean enabled) {
            // Only bother if this is a change
            if (mCheckboxesEnabled != enabled) {
                mCheckboxesEnabled = enabled;
                notifyDataSetChanged();
            }
        }

        /**
         * In case your curious, this tells the caller whether the adapter thinks
         * checkboxes should be enabled or disabled.
         */
        public boolean getCheckboxesEnabled() {
            return mCheckboxesEnabled;
        }

        public void setCheckboxTextEditable (boolean editable) {
            if (mCheckboxTextEditable != editable) {
                mCheckboxTextEditable = editable;
                notifyDataSetChanged();
            }
        }

        public boolean getCheckboxTextEditable() {
            return mCheckboxTextEditable;
        }

        /**
         * Sometimes we want to see the CardView behind each checkbox, and sometimes it
         * just looks complicated. Use this method to change the look.
         *
         * @param cardViewVisible   When TRUE, the CardView is visible behind each and
         *                          every checkbox.
         */
        public void setCheckboxCardViewVisible(boolean cardViewVisible) {
            if (mCardViewVisible != cardViewVisible) {
                mCardViewVisible = cardViewVisible;
                notifyDataSetChanged();
            }
        }

        public boolean getCheckboxCardViewVisible() {
            return mCardViewVisible;
        }


        /**
         * Because users can change the texts of the checkboxes willy-nilly, the content
         * of the EditTexts can veer away from the content in mStringData. Call this
         * method to scan all the EditTexts and set mStringData appropriately.
         *
         * When should this be called?
         * - Any time this class goes out of scope, but before we need to grab its
         *   data (like during an orientation change).
         * - Before any re-arrangement of the recyclerItems.
         * - And of course it should be called before any data is passed out of this class!
         *
         * Assumes that the quantity of data items and their order has not changed!
         */
        public void resetCheckboxData() {
            for (int i = 0; i < mStringData.size(); i++) {
                CheckboxQuestionViewHolder holder = (CheckboxQuestionViewHolder) mRecycler.findViewHolderForAdapterPosition(i);
                String str = holder.m_et.getText().toString();
                mStringData.set(i, str);
            }
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

    } // class CheckboxQuestionAdapter


    /******************************************
     * Viewholder for all the checkboxes for this checkbox question.
     *
     * Matches checkbox_view_recycler_card.xml
     *
     * todo: modify this to handle images
     */
    private class CheckboxQuestionViewHolder extends RecyclerView.ViewHolder {

        //-----------------
        //  Widgets
        //-----------------

        protected CardView m_cardview;
        protected CheckBox m_cb;
        protected EditText m_et;
        protected ImageView m_iv;


        //-----------------
        //  Methods
        //-----------------

        public CheckboxQuestionViewHolder(View v) {
            super(v);
            m_cardview = (CardView) v.findViewById(R.id.checkbox_view_card_parent_cv);
            m_cb = (CheckBox) v.findViewById(R.id.checkbox_view_card_cb);
            m_et = (EditText) v.findViewById(R.id.checkbox_view_card_et);
            m_iv = (ImageView) v.findViewById(R.id.checkbox_view_card_iv);
        }
    } // class CheckboxQuestionViewHolder


    /******************************************
     * An implementation of {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}
     * that enables basic drag & drop and swipe-to-dismiss.  Drag events are automatically
     * started by an item long-press.<br>
     */
    public class TouchHelperCallback
            extends android.support.v7.widget.helper.ItemTouchHelper.Callback {

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
                dragFlags = android.support.v7.widget.helper.ItemTouchHelper.UP | android.support.v7.widget.helper.ItemTouchHelper.DOWN;
            }
            if (mDeletable) {
                swipeFlags = android.support.v7.widget.helper.ItemTouchHelper.START | android.support.v7.widget.helper.ItemTouchHelper.END;
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