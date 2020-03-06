package com.consensus_builder.consensusbuilder.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.consensus_builder.consensusbuilder.R;


/**
 * Handles the managing of groups of people.
 */
public class GroupsFragment extends Fragment {

    //------------------------
    //  Constants
    //------------------------

    private static final String TAG = GroupsFragment.class.getSimpleName();

    private static final String[] SAMPLE_GROUPS = {
            "Wendy's Friends",
            "My Friends",
            "Scrum",
            "Department",
            "close family",
            "whole family",
            "john's bday party",
            "short mailing list",
            "Dexter's victims",
            "Nielson families",
            "Cats with iPhones",
            "Mojo players",
            "Spurs starters",
            "Marc's ex-girlfriends",
            "homeless homeys",
            "VCs",
            "movie friends",
            "foodies",
            "cast of Gilligan's Island",
            "James Bonds",
            "Comics",
            "Trump supporters and other idiots",
            "philosophers"
    };

    //------------------------
    //  Widgets
    //------------------------

    /** The list of already established groups */
    ListView mListView;


    //------------------------
    //  Data
    //------------------------


    //------------------------
    //  Methods
    //------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.groups, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.groups_respond_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Uh, groups really aren't working either, so there's nothing to send. Sorry!", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // todo:  make this dialog meaningful
                builder.setMessage("You are intending to send out the questionnaire to the recipients. If no questionnaire has been built, this button won't be visible.");
                builder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        // Now that the Activity is ready, we can populate the ListView.
        mListView = (ListView) v.findViewById(R.id.groups_lv);
        Context ctx = getActivity();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_list_item_1, SAMPLE_GROUPS);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "click!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // The add/edit/delete buttons
        ImageButton butt = (ImageButton) v.findViewById(R.id.groups_add_butt);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Add new group", Snackbar.LENGTH_SHORT).show();
            }
        });

        butt = (ImageButton) v.findViewById(R.id.groups_edit_butt);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Edit selected group", Snackbar.LENGTH_SHORT).show();
            }
        });

        butt = (ImageButton) v.findViewById(R.id.groups_delete_butt);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Delete selected group", Snackbar.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo:  may have to check the savedInstanceState to restore the widgets.
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // todo:  save widget information so that it'll be restored correctly later
    }


}
