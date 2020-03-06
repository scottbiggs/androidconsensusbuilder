package com.consensus_builder.consensusbuilder.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.R;

/**
 * ViewHolder for cards defined in rank_card.xml.
 *
 * NOTE: this class must match the layout of rank_card.xml EXACTLY!!!!
 */
public class RankCardViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = RankCardViewHolder.class.getSimpleName();

    //-----------------------
    //  Widgets
    //-----------------------

    /** Primary String display for this card / item. */
    TextView mMainStr_tv;

    /** If a picture is to be displayed, it goes here. */
    ImageView mImage_iv;


    //-----------------------
    //  Methods
    //-----------------------

    /**
     * Required Constructor. Widgets are discovered here.
     */
    public RankCardViewHolder(View v) {
        super(v);
        mMainStr_tv = (TextView) v.findViewById(R.id.rank_card_tv);
        mImage_iv = (ImageView) v.findViewById(R.id.rank_card_iv);
    }

}
