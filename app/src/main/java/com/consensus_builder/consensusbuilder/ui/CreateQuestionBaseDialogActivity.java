package com.consensus_builder.consensusbuilder.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;
import com.consensus_builder.consensusbuilder.json.Question;

/**
 * Since creating questions all follow a general form, this is a base class
 * that does the brunt of the work that is similar to all the forms.
 *
 * Inherit from this class to make an actual question creator dialog/activity
 * to communicate with the {@link com.consensus_builder.consensusbuilder.fragments.QuestionnaireFragment}.
 */
public class CreateQuestionBaseDialogActivity extends Activity {

    //----------------------------
    //  Constants
    //----------------------------

    private static final String TAG = CreateQuestionBaseDialogActivity.class.getSimpleName();

    /** Id used to identify this particular Activity when it returns values to the caller */
    public static final int CREATE_QUESTION_BASE_ID = "CREATE_QUESTION_BASE_ID".hashCode();


    //----------------------------
    //  Widgets
    //----------------------------

    /** The primary View for the question the user is filling out */
    private BaseQuestionView mQuestionView;


    //----------------------------
    //  Data
    //----------------------------

    /** The Question that the user is creating--the whole point of this Activity. */
    protected Question mQuestion = new Question();


    //----------------------------
    //  Methods
    //----------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_question_base);

        mQuestionView = (BaseQuestionView) findViewById(R.id.create_question_base_view);

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

    /**
     * User exits this Activity by signalling OK.
     * Collect all data and return it to caller.
     */
    protected void user_ok() {

        mQuestion.setPrompt(mQuestionView.getPrompt());
        Log.d(TAG, "mQuestion.getPrompt() = " + mQuestion.getPrompt());

        // todo get the recycler data...

        // Make Bundle from our data and stuff it into an Intent.
        Bundle bundle = mQuestion.make_into_bundle();
        Intent itt = new Intent();
        itt.putExtras(bundle);

        setResult(RESULT_OK, itt);
        finish();
    }



    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}


