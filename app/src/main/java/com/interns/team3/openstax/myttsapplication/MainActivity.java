package com.interns.team3.openstax.myttsapplication;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements HOMEFragment.OnFragmentInteractionListener, LIBRARYFragment.OnFragmentInteractionListener, BookshelfFragment.OnFragmentInteractionListener, TableOfContentsFragment.OnFragmentInteractionListener, TextbookViewFragment.OnFragmentInteractionListener, PlayerBarFragment.OnFragmentInteractionListener, VolumeFragment.OnFragmentInteractionListener, NOWPLAYINGFragment.OnFragmentInteractionListener {

    public FragmentManager fragmentManager;
    public HOMEFragment homeFragment;
    public LIBRARYFragment libraryFragment;
    public NOWPLAYINGFragment nowPlayingFragment;

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

        FragmentTransaction ft = fragmentManager.beginTransaction();
        homeFragment = HOMEFragment.newInstance(getApplicationContext());
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.fragmentContainer, homeFragment);
        ft.commit();

        bottomNavigationView = findViewById(R.id.navigation);
        if(nowPlayingFragment == null) bottomNavigationView.findViewById(R.id.navigation_player).setEnabled(false);

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

        decorView.setOnSystemUiVisibilityChangeListener (visibility -> {
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();
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
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.fragmentContainer, nowPlayingFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                    Log.i("now playing","there");
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

                if(count ==0) super.onBackPressed();
                else homeFragment.getChildFragmentManager().popBackStack();

            }

            else {

                int count = getFragmentManager().getBackStackEntryCount();

                if (count == 0) {
                    super.onBackPressed();
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
        bottomNavigationView.findViewById(R.id.navigation_player).setEnabled(true);
        bottomNavigationView.setSelectedItemId(R.id.navigation_player);
        Log.i("is nowPlayingFragment null?", String.valueOf(nowPlayingFragment == null));
        if(nowPlayingFragment != null)
        {
            String s = nowPlayingFragment.getModule();
            Log.i("getModule() results", s);
        }

        if(!nowPlayingFragment.getModule().equals(modID))
        {
            // stop the media player
            if(!nowPlayingFragment.getModule().equals("")) nowPlayingFragment.stopTTS();

            // make a new nowPlayingFragment
            nowPlayingFragment.setNewModule(bookTitle, modID, modTitle);
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragmentContainer, nowPlayingFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit();
    }


}
