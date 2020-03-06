package com.consensus_builder.consensusbuilder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.consensus_builder.consensusbuilder.fragments.GroupsFragment;
import com.consensus_builder.consensusbuilder.fragments.QuestionnaireFragment;
import com.consensus_builder.consensusbuilder.fragments.RespondFragment;
import com.consensus_builder.consensusbuilder.fragments.ResultFragment;
import com.consensus_builder.consensusbuilder.ui.CheckboxQuestionView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //------------------------
    //  Constants
    //------------------------

    private static final String TAG = MainActivity.class.getSimpleName();

    /** The number of tabs total */
    private static final int NUM_VIEW_PAGER_ITEMS = 4;



    //------------------------
    //  Widgets
    //------------------------

    /** A tabbed ViewPager for scrolling left/right through the various screens. */
    private ViewPager mViewPager;


    //------------------------
    //  Data
    //------------------------

    /** Side-Effect warning!   Used by static subclass */
    protected static Context mCtx;


    //------------------------
    //  Methods
    //------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // testing...
/*
        setContentView(R.layout.test_layout);

        final CheckboxQuestionView check = (CheckboxQuestionView) findViewById(R.id.checkbox);

        final TextView state_tv = (TextView) findViewById(R.id.current_mode_tv);
        state_tv.setText("" + check.getEditable());

        Button butt = (Button) findViewById(R.id.mode_butt);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle the current state
                check.setEditable(!check.getEditable());
                state_tv.setText("" + check.getEditable());
            }
        });
*/
        // ...end test


        mCtx = this;    // Used by side-effect in the adapter

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        // Begin initialization of the ViewPager
        // NOTE:  The ViewPager creates an odd warning: Fragment state for [fragmentName] not updated inline, expected state 3 found 2
        //  The bug-tracker for Google seems to indicate that this warning isn't much of a problem
        //  and should be ignored.
        mViewPager = (ViewPager) findViewById(R.id.content_main_vp);
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new ViewPagerAdapter(fm));

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  Classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // I extend FragmentSTATEPagerAdapter because I have a strong
    // feeling that I'll need to maintain the state of the UIs as the
    // different fragments are swiped through (they
    static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private static final String TAG = ViewPagerAdapter.class.getSimpleName();


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RespondFragment();
                case 1:
                    return new GroupsFragment();
                case 2:
                    return new QuestionnaireFragment();
                case 3:
                    return new ResultFragment();

                default:
                    Log.e(TAG, "whoa, that position is completely unacceptable!");
                    return null;
            }
        } // getItem (position)


        //!!!!!!!!!
        // NEEDS TO MATCH the switch statement in getItem()!!!!!
        //!!!!!!!!!
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mCtx.getResources().getString(R.string.response_title);
                case 1:
                    return mCtx.getResources().getString(R.string.groups_title);
                case 2:
                    return mCtx.getResources().getString(R.string.questionnaire_title);
                case 3:
                    return mCtx.getResources().getString(R.string.result_title);
                default:
                    return "error in getPageTitle";

            }
        }

        @Override
        public int getCount() {
            return NUM_VIEW_PAGER_ITEMS;
        }

    }


}

