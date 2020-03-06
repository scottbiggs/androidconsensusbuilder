package com.consensus_builder.consensusbuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.json.Question;
import com.consensus_builder.consensusbuilder.json.QuestionRank;
import com.consensus_builder.consensusbuilder.ui.QuestionCardViewHolder;
import com.consensus_builder.consensusbuilder.ui.RecyclerAdapterBase;
import com.consensus_builder.consensusbuilder.ui.TouchHelperInterface;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Activity that works like a dialog. This is used to let the user
 * manipulate and rank a series of lines.
 */
public class RankDialogActivity extends Activity {

    //----------------------------
    //  Constants
    //----------------------------

    private static final String TAG = RankDialogActivity.class.getSimpleName();


    //----------------------------
    //  Widgets
    //----------------------------

    /** The main portion and point of this Activity is a RecyclerView. */
    private RecyclerView mRecycler;


    //----------------------------
    //  Data
    //----------------------------

    private QuestionRank mQuestionRankData;

    protected RankAdapter mAdapter;


    //----------------------------
    //  Methods
    //----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank_dialog);

        // Prevents touches outside the Activity/Dialog area from causing this
        // Activity to terminate.
        setFinishOnTouchOutside(false);

        // Load up our data.  NOTE: it's stored in the Intent, NOT the savedInstanceState, Scott!
        mQuestionRankData = new QuestionRank(getIntent().getExtras());
        Log.d(TAG, "onCreate(), savedInstanceState = " + mQuestionRankData.toString());

        Button cancel_butt = (Button) findViewById(R.id.rank_dialog_cancel_butt);
        cancel_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rank_cancel(v);
            }
        });

        Button ok_butt = (Button) findViewById(R.id.rank_dialog_ok_butt);
        ok_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rank_ok(v);
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.rank_dialog_rv);

        // The RecyclerView and all its attending bits
        LinearLayoutManager layoutMgr = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(layoutMgr);

        mAdapter = new RankAdapter(this, mQuestionRankData);
        mRecycler.setAdapter(mAdapter);

        // Get the touch stuff working
        ItemTouchHelper.Callback callback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecycler);
    }


    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }


    public void rank_cancel (View v) {
        // End dialog--return nothing.
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * User clicked OK, time to end this dialog and return any changes the user
     * made.
     *
     * @param v     The OK Button that was clicked--not used.
     */
    public void rank_ok (View v) {
        // Make Bundle from our data and stuff it into an Intent.
        Bundle bundle = mQuestionRankData.make_into_bundle();
        Intent itt = new Intent();
        itt.putExtras(bundle);

        setResult(RESULT_OK, itt);
        finish();
    }



    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    class RankAdapter extends RecyclerView.Adapter<RankViewHolder>
                        implements TouchHelperInterface {

        private final String TAG = RankAdapter.class.getSimpleName();


        /**
         * Constructor<br>
         * <br>
         * Always use this constructor.  It supplies variables needed by this class to
         * interact properly with the RecyclerView that uses this Adapter.
         *
         * @param activity          Always nice to have a reference to our Activity!
         *
         * @param a_question_rank   The data itself, a list of Questions.
         */
        public RankAdapter(Activity activity, QuestionRank a_question_rank) {
        }

        @Override
        public RankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.rank_card, parent, false);

            return new RankViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RankViewHolder holder, int pos) {
            holder.m_tv.setText(mQuestionRankData.mStrList.get(pos));

            // todo:  take care of the images
        }

        @Override
        public int getItemCount() {
            return mQuestionRankData.mStrList.size();
        }

        @Override
        public void onItemMove(RecyclerView.ViewHolder viewHolder,
                               RecyclerView.ViewHolder targetViewHolder,
                               int fromPos, int toPos) {
            Log.d (TAG, "onItemMove() from " + fromPos + " to " + toPos);
            // Shift all the items that were moved over.
            Collections.swap(mQuestionRankData.mStrList, fromPos, toPos);
            notifyItemMoved(fromPos, toPos);
        }

        @Override
        public void onItemDismiss(RecyclerView.ViewHolder viewHolder, int pos) {
            // Not used
        }
    } // class RankAdapter


    /**
     * ViewHolder for doing the Ranking thing.  This one's pretty simple as we only
     * need a String and an Image (TextView and ImageView).
     */
    class RankViewHolder extends RecyclerView.ViewHolder {

        /** Displays the string to slide around */
        TextView m_tv;

        /** Holds the image if applicable. */
        ImageView m_image;

        public RankViewHolder(View v) {
            super(v);
            m_tv = (TextView) v.findViewById(R.id.rank_card_tv);
            m_image = (ImageView) v.findViewById(R.id.rank_card_iv);
        }
    }



    /**
     * An implementation of {@link android.support.v7.widget.helper.ItemTouchHelper.Callback}
     * that enables basic drag & drop and swipe-to-dismiss.  Drag events are automatically

     * started by an item long-press.<br>
     * <br>
     * Expects the <code>QuestionRecyclerAdapter</code> to listen for {@link TouchHelperInterface}
     * callbacks and the <code>QuestionViewHolder</code> to implement
     * {@link QuestionCardViewHolder}.
     */
    public class TouchHelperCallback
            extends android.support.v7.widget.helper.ItemTouchHelper.Callback {

        private final String TAG = TouchHelperCallback.class.getSimpleName();


        public TouchHelperCallback() {}

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = android.support.v7.widget.helper.ItemTouchHelper.UP | android.support.v7.widget.helper.ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0); // I believe 0 means no movement direction at all (not documented)
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            Log.d(TAG, "onMove()");
            mAdapter.onItemMove(viewHolder, target, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            // not used
        }

    } // class TouchHelperCallback

}