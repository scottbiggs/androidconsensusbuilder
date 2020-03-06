package com.consensus_builder.consensusbuilder.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

/**
 * This Activity (which looks like a Dialog [correction--now is full-screen])
 * allows the user to make a new ranking Question as part of their questionnaire.
 *
 * This Activity communicates with and is started by the QuestionnaireFragment.
 */
public class CreateQuestionRankDialogActivity extends Activity {

    //----------------------------
    //  Constants
    //----------------------------

    private static final String TAG = CreateQuestionRankDialogActivity.class.getSimpleName();

    /** Id used to identify this particular Activity when it returns values to the caller */
    public static final int CREATE_QUESTION_RANK_ID = "CREATE_QUESTION_RANK_ID".hashCode();


    //----------------------------
    //  Widgets
    //----------------------------

    /** The primary View for the question the user is filling out */
    private RankQuestionView mQuestionView;


    //----------------------------
    //  Data
    //----------------------------


    //----------------------------
    //  Methods
    //----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_question_rank);

        mQuestionView = (RankQuestionView) findViewById(R.id.rank_view);

        final Button cancel_butt = (Button) findViewById(R.id.cancel_butt);
        cancel_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_cancel();
            }
        });

        Button ok_butt = (Button) findViewById(R.id.ok_butt);
        ok_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_ok();
            }
        });

    } // onCreate(.)


    @Override
    public void onBackPressed() {
        user_cancel();
    }

    /**
     * The user has cancelled this Activity.  Return to the caller indicating so.
     */
    protected void user_cancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }


    protected void user_ok() {

        Question q = mQuestionView.getQuestion();

        // testing to make sure everything has been filled out
        if ((q.getPrompt() == null) || (q.getPrompt().length() == 0)) {
            // todo:  dialog warning that there is no prompt
        }

        if (q.numRankings() == 0) {
            // todo:  dialog warning that no rankable items have been added.
        }


        // Make Bundle from our data and stuff it into an Intent.
        Bundle bundle = q.make_into_bundle();
        Intent itt = new Intent();
        itt.putExtras(bundle);

        setResult(RESULT_OK, itt);
        finish();
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}


