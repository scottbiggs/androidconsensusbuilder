package com.consensus_builder.consensusbuilder.ui;

import com.consensus_builder.consensusbuilder.R;

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
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is the base class for the four question Views.  Since they all have
 * many similar functions, a parent makes sense to avoid repeating myself.
 *
 * One interesting note: if this class is considered Editable, then the text is
 * displayed using EditTexts.  Otherwise text is displayed with TextViews.
 *
 * See {@link com.consensus_builder.consensusbuilder.R.styleable#BaseQuestionView BaseQuestionView Attributes},
 */
public class BaseQuestionView extends LinearLayout {

    //-----------------------------
    //  Constants
    //-----------------------------

    /** Keys for the various data in the instance state bundle. */
    private static String
            KEY_SUPER_CLASS = "SuperClass",
            KEY_EDITABLE = "BaseQuestionView_Editable",
            KEY_PROMPT = "BaseQuestionView_prompt";


    //-----------------------------
    //  Widgets
    //-----------------------------

    /** The main prompt for this question, static for question answerers. */
    private TextView mPromptStatic_tv;
    /** Main prompt, editable version for question makers. */
    private EditText mPromptEditable_et;

    /** Main image for this question. Right now it's just a background. */
    private ImageView mImage_iv;

    /** All the recyclerView operations happen within this ViewGroup. Set visibility here. */
    private RelativeLayout mRecyclerRelativeLayout;

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

    /** Adapter for the RecyclerView. Unused if no recyclerView. */
    private BaseQuestionAdapter mAdapter;

    /** The name pretty much says it all. */
    private ItemTouchHelper.Callback mTouchHelperCallback;

    /**
     * The primary question of this View: are we creating a question or answering a question?
     * The user who makes the questionnaire is creating questions.
     * The users who receive a questionnaire are answering questions.
     * How this View displays and responds depends on the situation.
     * Default is ANSWERING (which is false).
     */
    protected boolean mCreatingQuestion = false;

    /** The visibility of the RecyclerView portion of this Question. Default is true. */
    protected  boolean mRecyclerVisible;

    /** Temporarily holds the input dat for a new RecyclerView item. */
    protected String mNewRecyclerLine;

    /** When true, the RecyclerView may add items. */
    protected boolean mRecyclerAddable;

    /** True means that user may delete items from the RecyclerView */
    protected boolean mRecyclerDeletable;

    /** When true, user may rearrange items in the recyclerView */
    protected boolean mRecyclerRearrangeable;


    //-----------------------------
    //  Methods
    //-----------------------------

    public BaseQuestionView(Context context) {
        super(context);
        init (context);
    }

    public BaseQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context, attrs);
    }

    public BaseQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        inflater.inflate(R.layout.base_question_view, this);
    }

    /**
     * Load the attributes from the XML layout of this BaseQuestionView and
     * modify our data correspondingly.
     */
    private void load_attrs (Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.BaseQuestionView);
        mPrompt_str = typedArray.getString(R.styleable.BaseQuestionView_setPrompt);
        mPromptHint_str = typedArray.getString(R.styleable.BaseQuestionView_setPromptHint);
        mBackgroundImageResource = typedArray.getResourceId(R.styleable.BaseQuestionView_setBackground, 0);
        mCreatingQuestion = typedArray.getBoolean(R.styleable.BaseQuestionView_createQuestion, false);

        // Recycler properties
        mRecyclerVisible = typedArray.getBoolean(R.styleable.BaseQuestionView_recyclerVisible, true);
        mRecyclerAddable = typedArray.getBoolean(R.styleable.BaseQuestionView_recyclerAddable, true);
        mRecyclerDeletable = typedArray.getBoolean(R.styleable.BaseQuestionView_recyclerDeletable, true);
        mRecyclerRearrangeable = typedArray.getBoolean(R.styleable.BaseQuestionView_recyclerRearrangeable, true);

        typedArray.recycle();   // Shared resource--remember to let others play!
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setup_widgets();
    }


    /**
     * Once the inflation is finished, this should be called to take care of the
     * UI details that need to play with the layout.
     */
    private void setup_widgets() {
        // Load up the widgets
        mImage_iv = (ImageView) findViewById(R.id.base_question_image_iv);
        mImage_iv.setImageResource(mBackgroundImageResource);

        mPromptStatic_tv = (TextView) findViewById(R.id.base_question_prompt_tv);
        mPromptEditable_et = (EditText) findViewById(R.id.base_question_prompt_et);
        mPromptEditable_et.setHint(mPromptHint_str);
        setPrompt(mPrompt_str);


        // The RecyclerView and all its attending bits
        mRecyclerRelativeLayout = (RelativeLayout) findViewById(R.id.recycler_rl);
        mRecycler = (RecyclerView) findViewById(R.id.base_question_recycler);
        mRecycler.setLayoutManager(mLayoutMgr);


        // initialize recyclerview with some temp stuff. todo: remove these once tested
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("mars");
        arrayList.add("venus");
        arrayList.add("mongo");
        arrayList.add("smerth");
        arrayList.add("earth 2");
        arrayList.add("earth");
        arrayList.add("florida");
        mAdapter = new BaseQuestionAdapter(arrayList);
        mRecycler.setAdapter(mAdapter);

        setCreatingQuestion(mCreatingQuestion);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add Line");

                final EditText newLine_et = new EditText(view.getContext());
                newLine_et.setHint("Enter a new line (it will be added at the end).");
                newLine_et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(newLine_et);   // Make the EditText the entire View of the AlertDialog.

                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNewRecyclerLine = newLine_et.getText().toString();

                        // Add this line to our recyclerView
                        mAdapter.add (mNewRecyclerLine);
                        dialog.dismiss();
                        mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);    // Scroll to end
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
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

        setRecyclerVisibile (mRecyclerVisible);
        setRecyclerDeletable (mRecyclerDeletable);
        setRecyclerRearrangeable (mRecyclerRearrangeable);

    } // setup_widgets()


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


    public void setRecyclerVisibile (boolean visible) {
        // do nothing if this is no change
        if (mRecyclerVisible == visible) {
            return;
        }

        mRecyclerRelativeLayout.setVisibility(visible ? VISIBLE : GONE);
        mRecyclerVisible = visible;
    }

    public boolean isRecyclerVisible() {
        return mRecyclerVisible;
    }


    public void setRecyclerDeletable (boolean deletable) {
        if (mRecyclerDeletable == deletable) {
            return; // do nothing if this isn't a change
        }
        ((TouchHelperCallback) mTouchHelperCallback).mDeletable = deletable;
    }

    public boolean isRecyclerDeletable() {
        return mRecyclerDeletable;
    }


    public void setRecyclerRearrangeable (boolean rearrangeable) {
        if (mRecyclerRearrangeable == rearrangeable) {
            return;
        }
        ((TouchHelperCallback) mTouchHelperCallback).mRearrangeable = rearrangeable;
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


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public class BaseQuestionAdapter
            extends RecyclerView.Adapter<BaseQuestionViewHolder>
            implements TouchHelperInterface {

        //---------------------
        //  Constants
        //---------------------

        //---------------------
        //  Data
        //---------------------

        /**
         * The data to fill in the RecyclerView.  You'll probably have to replace this
         * with your own item in child classes.  For now, it's just a bunch of Strings.
         *
         * todo: implement this to handle images, too.
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
        public BaseQuestionAdapter (ArrayList<String> data) {
            mStringData = data;
        }


        /**
         * Adds the given String to the RecyclerView. This new string will
         * take up a brand-new item at the end of the RecyclerView.
         *
         * @param str   Data to be added.
         */
        public void add (String str) {
            mStringData.add(str);
            mAdapter.notifyItemInserted(mStringData.size() - 1);     // Added item at the end.
        }


        @Override
        public BaseQuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.base_question_view_recycler_card, parent, false);

            return new BaseQuestionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(BaseQuestionViewHolder holder, int position) {
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
            // Tell the adapter to go ahead and delete the item.
            Log.d("scott", "position = " + pos);
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

    } // class BaseQuestionAdapter


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * The widget data must correspond exactly with the elements of
     * base_question_view_recycler_card.xml.
     */
    protected class BaseQuestionViewHolder extends RecyclerView.ViewHolder {

        //---------------------------
        //  Widgets
        //---------------------------

        /** Holds data entered for this card item */
        protected EditText m_et;

        /** Background image for this card item */
        protected ImageView m_iv;


        public BaseQuestionViewHolder(View v) {
            super(v);

            // Just load up the widgets!
            m_et = (EditText) v.findViewById(R.id.base_question_card_et);
            m_iv = (ImageView) v.findViewById(R.id.base_question_card_iv);
        }
    } // class BaseQuestionViewHolder


    /**
     * An implementation of {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}
     * that enables basic drag & drop and swipe-to-dismiss.  Drag events are automatically
     * started by an item long-press.<br>
     */
    public class TouchHelperCallback
            extends android.support.v7.widget.helper.ItemTouchHelper.Callback {

        //-------------------------
        //  Data
        //-------------------------

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
