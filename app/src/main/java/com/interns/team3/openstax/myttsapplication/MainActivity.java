package com.interns.team3.openstax.myttsapplication;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity implements BookshelfFragment.OnFragmentInteractionListener, TableOfContentsFragment.OnFragmentInteractionListener, TextbookViewFragment.OnFragmentInteractionListener, PlayerBarFragment.OnFragmentInteractionListener, VolumeFragment.OnFragmentInteractionListener, LibraryFragment.OnFragmentInteractionListener {

    public FragmentManager fragmentManager;
    public BookshelfFragment bookshelfFragment;
    public LibraryFragment libraryFragment;


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
        libraryFragment = LibraryFragment.newInstance("","");

        FragmentTransaction ft = fragmentManager.beginTransaction();
        bookshelfFragment = BookshelfFragment.newInstance("","");
        ft.add(R.id.fragmentContainer, bookshelfFragment);
        ft.commit();

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

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
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_player:
                    return true;
                case R.id.navigation_library: {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.fragmentContainer, libraryFragment);
                    ft.addToBackStack(null); // allow user to go back
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

    public void sendBookInfo(String bookID, String bookTitle){
        // from Bookshelf fragment to Table of Contents fragment
        Log.i("sendBookInfo", bookID + ", " + bookTitle);
        TableOfContentsFragment tableOfContentsFragment = TableOfContentsFragment.newInstance(bookTitle, bookID);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragmentContainer, tableOfContentsFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.commit();
    }

    public void sendModuleInfo(String bookTitle, String bookID, String modID, String modTitle){
        // from Table of Contents fragment to TextbookView Fragment
        Log.i("sendModuleInfo", bookTitle + ", " + bookID + ", " + modID + ", " + modTitle);
        TextbookViewFragment textbookViewFragment = TextbookViewFragment.newInstance(modTitle, modID, bookID, getApplicationContext());
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragmentContainer, textbookViewFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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


}
