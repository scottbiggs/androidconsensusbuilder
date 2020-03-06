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
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Using the BaseQuestionView as a model, this is specific to freeform questions.
 *
 * Created on 9/22/16.
 */
public class FreeformQuestionView extends LinearLayout {

    //-----------------------------
    //  Constants
    //-----------------------------

    /** Keys for the various data in the instance state bundle. */
    private static String
            KEY_SUPER_CLASS = "FreeformSuperClass",
            KEY_EDITABLE = "FreeformQuestionView_Editable",
            KEY_PROMPT = "FreeformQuestionView_prompt";

    //-----------------------------
    //  Widgets
    //-----------------------------

    /** The main prompt for this question, static for question answerers. */
    private TextView mPromptStatic_tv;
    /** Main prompt, editable version for question makers. */
    private EditText mPromptEditable_et;

    /** The hint for the area where users ANSWER the question in mPromptStatic_tv */
    private EditText mAnswerHint_et;

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

    /** Hint to be displayed to the question ANSWERER when this freeform question pops up. */
    private String mAnswerHint_str = null;

    /** Background image for this question */
    private int mBackgroundImageResource;

    /** Layout manager for RecyclerView. Unused if no recyclerView. */
    private LinearLayoutManager mLayoutMgr;

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

    /** Temporarily holds the input dat for a new RecyclerView item. */
    protected String mNewRecyclerLine;


    //-----------------------------
    //  Methods
    //-----------------------------

    public FreeformQuestionView(Context context) {
        super(context);
        init (context);
    }

    public FreeformQuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init (context, attrs);
    }

    public FreeformQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        inflater.inflate(R.layout.freeform_question_view, this);
    }


    /**
     * Load the attributes from the XML layout of this BaseQuestionView and
     * modify our data correspondingly.
     */
    private void load_attrs (Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.FreeformQuestionView);
        mPrompt_str = typedArray.getString(R.styleable.FreeformQuestionView_setFreeformPrompt);
        mPromptHint_str = typedArray.getString(R.styleable.FreeformQuestionView_setFreeformPromptHint);
        mBackgroundImageResource = typedArray.getResourceId(R.styleable.FreeformQuestionView_setFreeformBackground, 0);
        mCreatingQuestion = typedArray.getBoolean(R.styleable.FreeformQuestionView_createFreeformQuestion, false);
        mAnswerHint_str = typedArray.getString(R.styleable.FreeformQuestionView_setFreeformAnswerHint);

        typedArray.recycle();   // Shared resource--remember to let others play!
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setup_widgets();
    }


    private void setup_widgets() {

        // Load up the widgets
        mImage_iv = (ImageView) findViewById(R.id.freeform_question_image_iv);
        mImage_iv.setImageResource(mBackgroundImageResource);

        mPromptStatic_tv = (TextView) findViewById(R.id.freeform_question_prompt_tv);
        mPromptEditable_et = (EditText) findViewById(R.id.freeform_question_prompt_et);
        mPromptEditable_et.setHint(mPromptHint_str);
        setPrompt(mPrompt_str);
        mAnswerHint_et = (EditText) findViewById(R.id.freeform_question_answer_et);

        setCreatingQuestion(mCreatingQuestion);
    } // setup_widgets()


    /**
     * Fills in this custom View from data supplied within a Question instance.
     * If this is NOT a freeform question, nothing is done.
     *
     * @param q     The Question loaded with a freeform question.
     */
    public void setFromQuestion (Question q) {
        if (q.get_type() != Question.QUESTION_TYPE_FREEFORM) {
            return;
        }
        setPrompt(q.getPrompt());
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

            mAnswerHint_et.setVisibility(GONE);
        }
        else {
            mPromptStatic_tv.setVisibility(VISIBLE);
            mPromptEditable_et.setVisibility(GONE);
            mPromptStatic_tv.setText(mPrompt_str);

            mAnswerHint_et.setVisibility(VISIBLE);
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
        q.set_type(Question.QUESTION_TYPE_FREEFORM);
        q.setPrompt(getPrompt());

        // todo

        return q;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}