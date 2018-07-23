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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class MainActivity extends AppCompatActivity implements HOMEFragment.OnFragmentInteractionListener, LIBRARYFragment.OnFragmentInteractionListener, BookshelfFragment.OnFragmentInteractionListener, TableOfContentsFragment.OnFragmentInteractionListener, TextbookViewFragment.OnFragmentInteractionListener, PlayerBarFragment.OnFragmentInteractionListener, NOWPLAYINGFragment.OnFragmentInteractionListener {

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
    public PlayerBarFragment playerBarFragment;

    public BottomNavigationView bottomNavigationView;

    public Fragment activeFragment;


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
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        fragmentManager = getSupportFragmentManager();
        libraryFragment = LIBRARYFragment.newInstance("","");
        nowPlayingFragment = NOWPLAYINGFragment.newInstance("Select a module to play!","","");
        textbookViewFragment = TextbookViewFragment.newInstance("Select a module~", "", "");
        playerBarFragment = PlayerBarFragment.newInstance("Select a module to play!", "","");

        //nowPlayingFragment = (NOWPLAYINGFragment) fragmentManager.findFragmentById(R.id.nowPlayingFragment);
        //nowPlayingFragment.setNewModule("Select a module to play!", "", "");

        FragmentTransaction ft = fragmentManager.beginTransaction();
        activeFragment = nowPlayingFragment;
        ft.replace(R.id.nowPlayingContainer, nowPlayingFragment);
        ft.commit();

        ft = fragmentManager.beginTransaction();
        ft.replace(R.id.playbarContainer, playerBarFragment, "Player Bar");
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
                playerBarFragment.handlePlayButtonClick();
            }
        });
        dragViewFavorite = (ImageView) findViewById(R.id.dragViewFavorite);
        dragViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setFavoriteIconColor(true);

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
                //Log.i(TAG, "onPanelStateChanged " + newState);

                if(String.valueOf(newState).equals("DRAGGING")){
                    //Log.i(TAG, "It is dragging");
                }

                if(String.valueOf(newState).equals("EXPANDED")){
                    //dragView.setVisibility(View.GONE);
                }

                if(String.valueOf(newState).equals("COLLAPSED")){
                    //dragView.setVisibility(View.VISIBLE);
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
                    activeFragment = textbookViewFragment;
                    ft.replace(R.id.nowPlayingContainer, textbookViewFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                }

                else if(tab.getText().equals("Play")) {
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    activeFragment = nowPlayingFragment;
                    ft.replace(R.id.nowPlayingContainer, nowPlayingFragment);
                    ft.addToBackStack(null); // allow user to go back
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.commit();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab){

            }

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

    public void playEntireModule(String bookTitle, String modID, String modTitle){

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
            if(!nowPlayingFragment.getModule().equals("")) playerBarFragment.stopTTS();

            // make a new nowPlayingFragment
            nowPlayingFragment.setNewModule(bookTitle, modID, modTitle);
            playerBarFragment.setNewModule(bookTitle, modID, modTitle);
            textbookViewFragment = TextbookViewFragment.newInstance(modTitle, modID, bookTitle);
            setFileTag(bookTitle, modTitle, modID);

            // set favorite
            setFavoriteIconColor(false);

            // add nowPlayingFragment to the scroll up panel
            nowPlayingTab.select();

            if(isEntireModuleAvailable(modID)){
                playerBarFragment.setVisible(true);
                //fragmentManager.beginTransaction().replace(R.id.playbarContainer, playerBarFragment, "Player Bar").commit();
                String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output" + modID + ".mp3";
                playerBarFragment.playMergedFile(output);
            }
            else {
                // hide playerBarFragment?
                playerBarFragment.setVisible(false);
                //fragmentManager.beginTransaction().remove(playerBarFragment).commit();
            }


        }


        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void onRecyclerViewCreated(RecyclerView recyclerView){
        mLayout.setScrollableView(recyclerView);
    }




    // Little icons
    public void setFavoriteIconColor(boolean shouldToggle){

        // Shared Preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("library", 0);
        HashSet<String> faves = (HashSet<String>) sharedPreferences.getStringSet("favorites", new HashSet<String>());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // create a duplicate HashSet of the current faves set
        HashSet<String> newFaves = new HashSet<String>(faves);

        if((faves.contains(tag) && shouldToggle) || (!faves.contains(tag) && !shouldToggle)) {
            // change the module to NOT favorited
            dragViewFavorite.setImageDrawable(getDrawable(R.drawable.ic_border_heart_24dp));
            Drawable icon = dragViewFavorite.getDrawable();
            icon.setColorFilter(this.getColor(R.color.darkBlack), PorterDuff.Mode.SRC_ATOP);
            newFaves.remove(tag);

            if(shouldToggle && bottomNavigationView.getSelectedItemId() == R.id.navigation_library)
            {
                // Update favorites list real time
                String title = tag.split("_")[1];
                libraryFragment.removeFavorite(title);
            }

        } else if((!faves.contains(tag) && shouldToggle) || (faves.contains(tag) && !shouldToggle )){
            // Change the module to favorited
            dragViewFavorite.setImageDrawable(getDrawable(R.drawable.ic_heart_24dp));
            Drawable icon = dragViewFavorite.getDrawable();
            icon.setColorFilter(this.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            newFaves.add(tag);

            if(shouldToggle && bottomNavigationView.getSelectedItemId() == R.id.navigation_library)
            {
                // Update favorites list real time
                String title = tag.split("_")[1];
                libraryFragment.addFavorite(title);
            }

        } else { Log.e("Why is it here.", faves.contains(tag) + " | " + shouldToggle);}
        String output = ":) \t";
        for(String s : newFaves) {
            output+=s+"\t";
        }
        newFaves.remove(null);
        Log.i("in NewFaves", output);
        editor.putStringSet("favorites", newFaves); // hopefully this replaces the old favorites set
        editor.commit();
    }

    public boolean isEntireModuleAvailable(String modId){
        SharedPreferences sharedPreferences = this.getSharedPreferences("library", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output"+modId+".mp3";
        File f = new File(output);
        if(f.exists()) {
            // show textbookviewfragment
            Log.i("Download available", "showing play options");
            dragViewPlayButton.setVisibility(View.VISIBLE);
            nowPlayingFragment.showPlaying();
            return true;
        }
        else {
            Log.i("Download unavailable", "hiding play options");
            // show download button
            // don't make player bar visible on the "Play" section.
            dragViewPlayButton.setVisibility(View.GONE);
            nowPlayingFragment.hidePlaying();

            return false;
        }
    }

    public void downloadEntireModule(String modId){
        String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output"+modId+".mp3";
        try{ download(output); } catch(IOException e){ Log.i("IOException", "Downloading entire module");};

        SharedPreferences sharedPreferences = this.getSharedPreferences("library", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        HashSet<String> downloads = (HashSet<String>) sharedPreferences.getStringSet("downloads", new HashSet<String>());
        HashSet<String> newDownloads = new HashSet<String>(downloads);
        newDownloads.add(tag); // same tag as favorites.
        editor.putStringSet("downloads", newDownloads); // hopefully this replaces the old downloads set
        editor.commit();
    }

    public void download(String outputFilename) throws IOException {

        /*String[] uris = new String[]{
                "/storage/emulated/0/textbookaudio0.wav",
             */

        ArrayList<TextChunk> dataSet = textbookViewFragment.getDataSet();

        String[] uris = new String[dataSet.size()];
        String s = "";

        for (int x = 0; x < dataSet.size(); x ++){
            uris[x] = "/storage/emulated/0/textbookaudio"+ x +".wav";
            s+="-i " + uris[x] + " ";
        }

        s+="-filter_complex ";

        for(int x = 0; x < dataSet.size(); x++){
            s+="["+x+":0]";
        }

        s+="concat=n="+dataSet.size()+":v=0:a=1[out] -map [out] " + outputFilename;

        // https://trac.ffmpeg.org/wiki/Concatenate
        // String s = "-i " + uris[0] + " -i " + uris[1] + " -filter_complex [0:0][1:0]concat=n=2:v=0:a=1[out] -map [out] " + output;
        Log.i("THE WHOLE THING", s);


        String[] cmd = s.split(" ");

        // checking that Ffmpeg works
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        if (ffmpeg.isSupported()) {
            // ffmpeg is supported
            Log.i("FFmpeg is supported", "Yay!");
            //if(makeDownloadAvailable) download.setEnabled(true);
        } else {
            // ffmpeg is not supported
            Log.i("FFmpeg is not supported", "Darn ;(");
        }


        // to execute "ffmpeg -version" command you just need to pass "-version"
        // for more info, check out this link:
        // https://superuser.com/questions/1298891/ffmpeg-merge-multiple-audio-files-into-single-audio-file-with-android
        // CORRECT dependency that fixes "relocation" problems: https://github.com/bravobit/FFmpeg-Android
        ffmpeg = FFmpeg.getInstance(this);
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {
                Log.i("ffmpeg execute - Start", "Hi");
            }

            @Override
            public void onProgress(String message) {
                Log.i("ffmpeg execute - Progress", message);
            }

            @Override
            public void onFailure(String message) {
                Log.i("ffmpeg execute - Failure", message);
            }

            @Override
            public void onSuccess(String message) {
                Log.i("ffmpeg execute - Success", message);
            }

            @Override
            public void onFinish() {
                Log.i("ffmpeg execute - Finish", "Bye");
                // show the original NowPlaying.
                if(getActiveFragment() instanceof NOWPLAYINGFragment){
                    nowPlayingFragment.showPlaying();
                }
                nowPlayingFragment.setShowPlaying(true); // in case nowPlayingFragment is not visibile on the screen at the time
                textbookViewFragment.makeThingsEasy();
                dragViewPlayButton.setVisibility(View.VISIBLE);
                playerBarFragment.setVisible(true);
                playerBarFragment.playMergedFile(outputFilename);

                if(bottomNavigationView.getSelectedItemId() == R.id.navigation_library)
                {
                    // update the list of downloads realtime
                    String title = tag.split("_")[1];
                    libraryFragment.addDownload(title);
                }
            }

        });
    }

    public TextbookViewFragment getTextbookViewFragment(){
        return textbookViewFragment;
    }

    public Fragment getActiveFragment() {
        return activeFragment;
    }

    public String tag;
    public void setFileTag(String bookId, String modTitle, String modId){

        tag = bookId+"_"+modTitle+"_"+modId;

    }

    public String getFileTag(){ return tag; }


}
