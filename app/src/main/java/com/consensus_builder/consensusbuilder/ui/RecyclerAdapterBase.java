package com.consensus_builder.consensusbuilder.ui;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

import java.util.ArrayList;

import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_CHECKBOX;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_FREEFORM;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_RADIO;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_RANK;
import static com.consensus_builder.consensusbuilder.json.Question.QUESTION_TYPE_UNKNOWN;

/**
 * The base Adapter for RecyclerViews that display Questions.<br>
 * <br>
 * Inherit (or simply use) this class instead of {@link RecyclerView.Adapter}
 * as this holds all sorts of goodies that will make life easy.  This class also encapsulates
 * a lot of code to prevent redundancies and make debugging/maintaining much easier.
 */
public class RecyclerAdapterBase extends RecyclerView.Adapter<QuestionCardViewHolder> {

    //---------------------
    //  Constants
    //---------------------

    private static final String TAG = RecyclerAdapterBase.class.getSimpleName();


    //---------------------
    //  Data
    //---------------------

    /** A reference to the Activity is useful! Remember to kill it when done though. */
    protected Activity mActivity;

    public ArrayList<Question> mQuestionData;

    /** Determines if this recyclerView is enabled or disabled */
    private boolean mEnabled = true;


    //---------------------
    //  Methods
    //---------------------


    /**
     * Making the default (parent) constructor private to force using the constructor with
     * parameters.
     */
    private RecyclerAdapterBase() {
        super();
        mQuestionData = new ArrayList<>();
    }

    /**
     * Constructor<br>
     * <br>
     * Always use this constructor.  It supplies variables needed by this class to
     * interact properly with the RecyclerView that uses this Adapter.
     *
     * @param activity      Always nice to have a reference to our Activity!
     *
     * @param questions     The data itself, a list of Questions.
     *                      todo: find a way to maintain data integrity as this class and the Fragment modify this list.
     */
    public RecyclerAdapterBase (Activity activity, ArrayList<Question> questions) {
        mActivity = activity;
        mQuestionData = questions;
    }

    /**
     * Yes, you MUST call this when this class needs to go away (like when the Fragment is
     * destroyed) to prevent memory leaks.
     */
    public void close() {
        // todo  Remove references created in the constructor to prevent memory leaks

        mActivity = null;
        mQuestionData = null;
    }


    @Override
    public QuestionCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.questionnaire_card, parent, false);

        return new QuestionCardViewHolder(v);
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


    /**
     * Use this to enable or disable this RecyclerView.  When Enabled (default),
     * user may modify the contents of each recycler item.  Disabling allows
     * the user to scroll, but they can't change any items. Also, when disabled
     * the view should change and look unaccessible.
     *
     * @param on    When true (default) this RecyclerView functions normally.
     *              False means that user may scroll but can't activate any
     *              items.
     */
    public void setEnabled(boolean on) {
        if (mEnabled == on) {
            return; // nothing to do
        }

        mEnabled = on;
        notifyDataSetChanged();
    }

    /**
     * Returns whether or not the Adapter thinks this RecyclerView is enabled.
     */
    public boolean getEnabled() {
        return mEnabled;
    }


    /**
     * See {@link #setup_freeform(Question, QuestionCardViewHolder, int)}.
     */
    protected void setup_checkbox (final Question q, final QuestionCardViewHolder holder, int pos) {

        // We're rebuilding all the Checkbox views.  But a couple will be reused, so
        // save those (the TextViews).
        TextView tmp_checkbox_desc_tv = holder.mCheckBoxDesc_tv;
        TextView tmp_checkbox_prompt_tv = holder.mCheckBoxPrompt_tv;

        // Set the text while we're at it.
        tmp_checkbox_prompt_tv.setText(q.getPrompt());

        // Remove any previous checkboxes
        holder.mCheckBox_ll.removeAllViews();

        // Putting the required views back
        holder.mCheckBox_ll.addView(tmp_checkbox_desc_tv);
        holder.mCheckBox_ll.addView(tmp_checkbox_prompt_tv);

        // Add any checkboxes as needed.
        for (int i = 0; i < q.numCheckboxes(); i++) {
            String prompt = q.getCheckboxString(i);
            CheckBox cb = new CheckBox(mActivity);  // Just using default attributes
            cb.setText(prompt);
            cb.setTag(i);       // NOTE: the Tag of the CheckBox (View) denotes the position
            // in the list of checkboxes.  Yeah, I know: HACK!!!

            cb.setChecked(mQuestionData.get(pos).isCheckboxChecked(i));

            // Set check listener
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox v_cb = (CheckBox) v;
                    Question current_q =  mQuestionData.get(holder.getAdapterPosition());
                    if (v_cb.isChecked()) {
                        current_q.setCheckboxChecked((Integer) v.getTag(), true);
                    }
                    else {
                        current_q.setCheckboxChecked((Integer) v.getTag(), false);
                    }
                }
            });

            holder.mCheckBox_ll.addView(cb);
        }

        holder.mCheckBox_ll.setVisibility(View.VISIBLE);

        // Enable/Disable each View according to the state.
        disableEnableViews(mEnabled, holder.mCheckBox_ll);
    }


    /**
     * Recursively enables or disables an entire tree of Views.
     * Modified from eric cochran's answer at http://stackoverflow.com/questions/7068873/how-can-i-disable-all-views-inside-the-layout
     *
     * @param enable    Whether the view should be enabled or disabled
     *
     * @param v         The top-level View or ViewGroup.
     */
    public void disableEnableViews (boolean enable, View v) {

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                child.setEnabled(enable);
                disableEnableViews(enable, child);
            }
        }

        else {
            // Just a View.
            v.setEnabled(enable);
        }
    }

    /**
     * See {@link #setup_freeform(Question, QuestionCardViewHolder, int)}.
     */
    protected void setup_radio (final Question q, final QuestionCardViewHolder holder, int pos) {
        // Like checkboxes, save the important, reused views before clearning everything.
        TextView tmp_radio_desc_tv = holder.mRadioDesc_tv;
        TextView tmp_radio_prompt_tv = holder.mRadioPrompt_tv;

        tmp_radio_prompt_tv.setText(q.getPrompt());

        holder.mRadioGroup_rg.removeAllViews();

        holder.mRadioGroup_rg.addView(tmp_radio_desc_tv);
        holder.mRadioGroup_rg.addView(tmp_radio_prompt_tv);

        // Add the radio buttons
        for (int i = 0; i < q.numRadioButtons(); i++) {
            String str = q.getRadioButtonString(i);
            RadioButton rb = new RadioButton(mActivity);
            rb.setText(str);
            rb.setId(i);        // The ID is how each button is represented. I'm using it's position as ID.
            holder.mRadioGroup_rg.addView(rb);
        }

        // Set the checked from the data
        int selected_radio_butt = mQuestionData.get(pos).getRadioButtonSelected();
        holder.mRadioGroup_rg.check(selected_radio_butt);

        // Add a listener to save changes by the user
        holder.mRadioGroup_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mQuestionData.get(holder.getAdapterPosition()).setRadioButtonSelected(checkedId);      // The id corresponds to the position of the selected radio button
            }
        });

        holder.mRadioGroup_rg.setVisibility(View.VISIBLE);

        // Enable/Disable each View according to the state.
        disableEnableViews(mEnabled, holder.mRadioGroup_rg);

    }

        /**
         * See {@link #setup_freeform(Question, QuestionCardViewHolder, int)}.
         */
    protected void setup_rank (final Question q, final QuestionCardViewHolder holder, int pos) {
        Log.d(TAG, "setup_rank()");

        TextView tmp_ranking_desc = holder.mRankingDesc_tv;
        TextView tmp_ranking_prompt = holder.mRankingPrompt_tv;

        holder.mRank_ll.removeAllViews();

        holder.mRank_ll.addView(tmp_ranking_desc);
        holder.mRank_ll.addView(tmp_ranking_prompt);

        for (int i = 0; i < q.numRankings(); i++) {
            String str = q.get_rank().mStrList.get(i);
            TextView tv = new TextView(mActivity);
            tv.setText(str);
            holder.mRank_ll.addView(tv);
        }

        holder.mRankingPrompt_tv.setText(q.getPrompt());

        holder.mRank_ll.setVisibility(View.VISIBLE);

        // Enable/Disable each View according to the state.
        disableEnableViews(mEnabled, holder.mRank_ll);

    } // setup_rank (q, holder, pos)


    /**
     * Helper method for {@link #onBindViewHolder(QuestionCardViewHolder, int)}.
     * Handles the specific case of a freeform Question type.
     *
     * @param   q       The actual question that we're dealing with in mQuestionData.
     *
     * @param   holder  The ViewHolder that we're working with.
     *
     * @param   pos     The position of this ViewHolder and q (Question) within our
     *                  mQuestionData ArrayList.
     */
    protected void setup_freeform (final Question q, final QuestionCardViewHolder holder, int pos) {
        String prompt = q.getPrompt();
        String response = q.get_freeform_response();

        holder.mFreeFormQuestion_tv.setText(prompt);
        holder.mFreeFormAnswer_et.setText(response);

        // Setting the Tag to be the position will be important later.
        holder.mFreeFormAnswer_et.setTag(pos);

        holder.mFreeFormAnswer_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String response = holder.mFreeFormAnswer_et.getText().toString();

                // Important to use getAdapterPosition() as pos is no longer valid
                // in this scope and getLayoutPosition() doesn't play well with ViewHolders. I think.
                mQuestionData.get(holder.getAdapterPosition()).set_freeform_response(response);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // not used
            }
        });

        // todo: is this still needed?
        holder.mFreeForm_ll.setVisibility(View.VISIBLE);

        // Enable/Disable each View according to the state.
        disableEnableViews(mEnabled, holder.mFreeForm_ll);
    }


    /**
     * Given a ViewHolder and its position, this method sets the correct title
     * for this card (it depends on the position--duh!).
     *
     * @param holder    The ViewHolder to modify.
     *
     * @param pos       The position of the ViewHolder in the Adapter (0 based).
     */
    protected void setTitle (QuestionCardViewHolder holder, int pos) {
        // Set the title of each card.  It's just the card's position with a prefix
        String title = mActivity.getText(R.string.qtype_number_prefix).toString();
        title += (pos + 1);
        holder.mTitle_tv.setText(title);
    }


    /**
     * Sets the visibility of all the widget sections of the given
     * QuestionnaireViewHolder to GONE.  Useful for starting out with
     * a clean slate before turning on the correct widget section.
     */
    protected void turn_off_widget_sections(QuestionCardViewHolder holder) {
        holder.mCheckBox_ll.setVisibility(View.GONE);
        holder.mRadioGroup_rg.setVisibility(View.GONE);
        holder.mFreeForm_ll.setVisibility(View.GONE);
        holder.mRank_ll.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mQuestionData.size();
    }
}
