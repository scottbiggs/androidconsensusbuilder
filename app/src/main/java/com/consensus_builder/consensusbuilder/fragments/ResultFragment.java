package com.consensus_builder.consensusbuilder.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.consensus_builder.consensusbuilder.R;

/**
 * As results trickle in, they will show up here.  Stats and
 * related stuff can be done here as well.
 */
public class ResultFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.analyze_results, container, false);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo:  may have to check the savedInstanceState to restore the widgets.
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // todo:  save widget information so that it'll be restored correctly later
    }

}
