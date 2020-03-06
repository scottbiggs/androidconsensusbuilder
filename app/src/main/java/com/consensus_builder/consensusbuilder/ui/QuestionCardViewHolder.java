package com.consensus_builder.consensusbuilder.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;

/**
 * ViewHolder for any RecyclerView that wants to display cards defined in
 * questionnaire_card.xml.
 *
 * This class must match precisely with the elements of that layout.
 */
public class QuestionCardViewHolder extends RecyclerView.ViewHolder {


    //---------------------------
    //  Widgets
    //---------------------------

    /** Title at the top of the card. Probably has the question number and some other description */
    public TextView mTitle_tv;

    /** Encompasses the checkbox section */
    public LinearLayout mCheckBox_ll;
    public TextView mCheckBoxPrompt_tv;
    public TextView mCheckBoxDesc_tv;

    /** Encompasses the RadioGroup */
    public RadioGroup mRadioGroup_rg;
    public TextView mRadioPrompt_tv;
    public TextView mRadioDesc_tv;

    /** LinearLayout that encompasses a free-form question type */
    public LinearLayout mFreeForm_ll;
    public TextView mFreeFormDesc_tv;
    /** The question to ask in a free-form item */
    public TextView mFreeFormQuestion_tv;
    /** Answers go here */
    public EditText mFreeFormAnswer_et;

    /** Encompasses the ranking section */
    public LinearLayout mRank_ll;
    public TextView mRankingPrompt_tv;
    public TextView mRankingDesc_tv;


    //---------------------------
    //  Methods
    //---------------------------

    /** Required constructor.  You MUST fill in all the widgets here! */
    public QuestionCardViewHolder (View v) {
        super(v);

        mTitle_tv = (TextView) v.findViewById(R.id.card_title_tv);

        mCheckBox_ll = (LinearLayout) v.findViewById(R.id.checkboxes_ll);
        mCheckBoxPrompt_tv = (TextView) v.findViewById(R.id.checkboxes_prompt);
        mCheckBoxDesc_tv = (TextView) v.findViewById(R.id.checkboxes_desc_tv);

        mRadioGroup_rg = (RadioGroup) v.findViewById(R.id.radio_rg);
        mRadioPrompt_tv = (TextView) v.findViewById(R.id.radio_prompt);
        mRadioDesc_tv = (TextView) v.findViewById(R.id.radio_desc_tv);

        mFreeForm_ll = (LinearLayout) v.findViewById(R.id.freeform_ll);
        mFreeFormDesc_tv = (TextView) v.findViewById(R.id.freeform_desc_tv);
        mFreeFormQuestion_tv = (TextView) v.findViewById(R.id.freeform_ask_tv);
        mFreeFormAnswer_et = (EditText) v.findViewById(R.id.freeform_answer_et);

        mRank_ll = (LinearLayout) v.findViewById(R.id.rank_ll);
        mRankingDesc_tv = (TextView) v.findViewById(R.id.ranking_desc_tv);
        mRankingPrompt_tv = (TextView) v.findViewById(R.id.ranking_prompt);
    }

}
