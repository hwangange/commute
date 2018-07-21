package com.interns.team3.openstax.myttsapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HOMEFragment.OnFragmentInteractionListener, LIBRARYFragment.OnFragmentInteractionListener, BookshelfFragment.OnFragmentInteractionListener, TableOfContentsFragment.OnFragmentInteractionListener, TextbookViewFragment.OnFragmentInteractionListener, PlayerBarFragment.OnFragmentInteractionListener, VolumeFragment.OnFragmentInteractionListener, NOWPLAYINGFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private SlidingUpPanelLayout mLayout;
    private RelativeLayout dragView;
    private TabLayout tabLayout;
    private TabLayout.Tab textbookViewTab, nowPlayingTab;

    private TextView dragViewText;
    private ImageView dragViewPlayButton, dragViewFavorite;

    public FragmentManager fragmentManager;
    public HOMEFragment homeFragment;
    public LIBRARYFragment libraryFragment;
    public NOWPLAYINGFragment nowPlayingFragment;
    public TextbookViewFragment textbookViewFragment;

    public BottomNavigationView bottomNavigationView;


    public int /*uiOptions= View.SYSTEM_UI_FLAG_IMMERSIVE
            // set the content to appear under system bars
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // hide nav bar
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN; */

    uiOptions =View.SYSTEM_UI_FLAG_FULLSCREEN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        libraryFragment = LIBRARYFragment.newInstance("","");
        nowPlayingFragment = NOWPLAYINGFragment.newInstance("Select a module to play!","","", getApplicationContext());
        textbookViewFragment = TextbookViewFragment.newInstance("Select a module~", "", "", this);

        //nowPlayingFragment = (NOWPLAYINGFragment) fragmentManager.findFragmentById(R.id.nowPlayingFragment);
        //nowPlayingFragment.setNewModule("Select a module to play!", "", "");

        // add nowPlayingFragment to the scroll up panel
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.nowPlayingContainer, nowPlayingFragment);
        ft.commit();

        ft = fragmentManager.beginTransaction();
        homeFragment = HOMEFragment.newInstance(getApplicationContext());
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.fragmentContainer, homeFragment);
        ft.commit();

        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);
        //if(nowPlayingFragment == null) bottomNavigationView.findViewById(R.id.navigation_player).setEnabled(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // hide the actual bottom navigation
        View decorView = getWindow().getDecorView();
        // Hide the navigation bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.

        // sticky immersive mode: https://developer.android.com/training/system-ui/immersive
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                decorView.setSystemUiVisibility(uiOptions);
                ActionBar actionBar = getSupportActionBar();
                actionBar.show();

            }
        });


        dragView = (RelativeLayout) findViewById(R.id.dragView);
        dragViewText = (TextView) findViewById(R.id.dragViewText);
        dragViewPlayButton = (ImageView) findViewById(R.id.dragViewPlayButton);
        dragViewPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dragViewFavorite = (ImageView) findViewById(R.id.dragViewFavorite);
        dragViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);

                if(String.valueOf(newState).equals("DRAGGING")){
                    Log.i(TAG, "It is dragging");
                }

                if(String.valueOf(newState).equals("EXPANDED")){
                    dragView.setVisibility(View.GONE);
                }

                if(String.valueOf(newState).equals("COLLAPSED")){
                    dragView.setVisibility(View.VISIBLE);
                }


            }
        });

        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });



        // Hide the drag view
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        // Tabs
        tabLayout = findViewById(R.id.tabLayout);
        textbookViewTab = tabLayout.newTab();
        textbookViewTab.setText("Text");
        nowPlayingTab = tabLayout.newTab();
        nowPlayingTab.setText("Play");

        tabLayout.addTab(textbookViewTab);
        tabLayout.addTab(nowPlayingTab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){

                //TextbookView
                if(tab.getText().equals("Text")){
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.nowPlayingContainer, textbookViewFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                }

                else if(tab.getText().equals("Play")) {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.nowPlayingContainer, nowPlayingFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab){ }

            @Override
            public void onTabUnselected(TabLayout.Tab tab){ }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.fragmentContainer, homeFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                    Log.i("home","there");
                    return true;
                }
                case R.id.navigation_player: {

                   /* FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.fragmentContainer, nowPlayingFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                    Log.i("now playing","there"); */
                    return true;
                }
                case R.id.navigation_library: {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.fragmentContainer, libraryFragment);
                    //ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                    Log.i("library","there");
                    return true;
                }
            }
            return false;
        }
    };

    public void onFragmentInteraction(Uri uri){
        Log.i("onFragmentInteraction", uri.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if(bottomNavigationView.getSelectedItemId() == R.id.navigation_home){
                int count = homeFragment.getChildFragmentManager().getBackStackEntryCount();

                if(count ==0)
                {
                    //super.onBackPressed();
                    onBackPressed();
                }
                else homeFragment.getChildFragmentManager().popBackStack();

            }

            else {

                int count = getFragmentManager().getBackStackEntryCount();

                if (count == 0) {
                    onBackPressed();
                    //super.onBackPressed();
                    //additional code
                } else {
                    getFragmentManager().popBackStack();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        super.onBackPressed();
    }


    @Override
    public void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();
        }
    }


    public void sendBookInfo(String bookID, String bookTitle){
        // from Bookshelf fragment to Table of Contents fragment
        homeFragment.sendBookInfo(bookID, bookTitle);
    }

    public void sendModuleInfo(String bookTitle, String bookID, String modID, String modTitle){
        // from Table of Contents fragment to TextbookView Fragment
        homeFragment.sendModuleInfo(bookTitle, bookID, modID, modTitle);
    }

    public void playMergedFile(String bookTitle, String modID, String modTitle){

        Log.i("is nowPlayingFragment null?", String.valueOf(nowPlayingFragment == null));
        dragViewText.setText(Html.fromHtml("<b>"+modTitle+"</b><br/>"+bookTitle, Html.FROM_HTML_MODE_COMPACT));


        if(nowPlayingFragment != null)
        {
            String s = nowPlayingFragment.getModule();
            Log.i("getModule() results", s);
        }

        if(!nowPlayingFragment.getModule().equals(modID))
        {
            dragViewPlayButton.setImageResource(R.drawable.pause);
            // stop the media player
            if(!nowPlayingFragment.getModule().equals("")) nowPlayingFragment.stopTTS();

            // make a new nowPlayingFragment
            nowPlayingFragment.setNewModule(bookTitle, modID, modTitle);

            String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output" + modID + ".mp3";
            nowPlayingFragment.playMergedFile(output);
        }

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        /*FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragmentContainer, nowPlayingFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit(); */
    }


}
