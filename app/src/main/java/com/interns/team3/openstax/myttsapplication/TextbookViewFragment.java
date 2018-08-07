package com.interns.team3.openstax.myttsapplication;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nshmura.snappysmoothscroller.LinearLayoutScrollVectorDetector;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappySmoothScroller;

import org.apache.commons.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class TextbookViewFragment extends Fragment implements PlayerBarFragment.OnFragmentInteractionListener {

    private static final String ARG_MOD_TITLE = "param1";
    private static final String ARG_MOD_ID = "param2";
    private static final String ARG_BOOK_ID = "param3";
    // TODO: Rename and change types of parameters

    private RecyclerView recyclerView;
    private TextbookViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static String modId, bookId, modTitle;
    public Context context = getContext();
    public static Document content;
    public List<TextAudioChunk> tempDataSet, dataSet;

    public MediaPlayer player;


    private int length;

    private boolean is_paused;

    public ProgressBar progressBar;
    public LinearLayout progress;
    public TextView progressNumber;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public CustomScrollListener customScrollListener;

    public FragmentManager fragmentManager;
    public PlayerBarFragment playerBarFragment;

    public boolean makeDownloadAvailable = true;

    public boolean easyWay = false; // skip tts, utterance progress listener, progress bar
    public ArrayList<Integer> times;


    // required empty constructor
    public TextbookViewFragment(){}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TextbookViewFragment newInstance(String param1, String param2, String param3) {
        TextbookViewFragment fragment = new TextbookViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOD_TITLE, param1);
        args.putString(ARG_MOD_ID, param2);
        args.putString(ARG_BOOK_ID, param3);

        fragment.setArguments(args);
        return fragment;
    }

    // when user wants to listen to a new audiobook
    public void setNewModule(String bookId, String modId, String modTitle){
        TextbookViewFragment.bookId = bookId;
        TextbookViewFragment.modId = modId;
        TextbookViewFragment.modTitle = modTitle;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            modTitle = getArguments().getString(ARG_MOD_TITLE);
            modId = getArguments().getString(ARG_MOD_ID);
            bookId = getArguments().getString(ARG_BOOK_ID);
        }

        Log.i("modtitle", modTitle);

        // Populate dataSet
        tempDataSet = new ArrayList<>();
        dataSet = new ArrayList<>();

        playerBarFragment =  (PlayerBarFragment) getActivity().getSupportFragmentManager().findFragmentByTag("Player Bar");
        player = playerBarFragment.getPlayer();
        adapter = new TextbookViewAdapter(dataSet, position -> {

            if(position != getSelected() || (position == getSelected() && !player.isPlaying()))  // do not pause the audio if "selected" text is selected again
            {
                // If interrupted
                if(times != null)
                {
                    player.seekTo(getTimeUpTo(position));

                    // No matter what, makes sure player starts playing.
                    playerBarFragment.setPlayButton("Pause");
                    player.start();

                }
            }
        });

        if(getTimesList() == null || !((MainActivity) getContext()).isEntireModuleAvailable(bookId, modId)) {
            getContent(); // Populate tempDataSet
            storeConvertedTTSAudio();

            // look at UtteranceProgressListener class.
        } else {
            // if there is a recorded times list AND entire module has already been downloaded
            makeThingsEasy();
            getContent(); // Order matters bro
        }


        context = getContext();

        if(easyWay){
            for(TextAudioChunk tc : tempDataSet) {
                dataSet.add(new TextAudioChunk(tc));
            }

            Log.i("dataSet is good to go!", "finished populating");
        }

        setRetainInstance(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_textbook_view, container, false);

        recyclerView = view.findViewById(R.id.my_recycler_view);

        // Layout Manager
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

                SnappySmoothScroller scroller = new SnappySmoothScroller.Builder()
                        .setSnapType(SnapType.START) // SnapType.CENTER OR SnapType.START
                        .setSnapInterpolator(new DecelerateInterpolator())
                        .setSnapPaddingEnd(20)
                        .setPosition(position)
                        .setScrollVectorDetector(new LinearLayoutScrollVectorDetector(this))
                        .build(recyclerView.getContext());

                startSmoothScroll(scroller);

            }

            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

                final int result = super.scrollVerticallyBy(dy, recycler, state);
                int target = customScrollListener.getTarget();

                int front = findFirstVisibleItemPosition();
                int back = findLastVisibleItemPosition();

                // blubber
                //playerBarFragment.setSeekbarProgress(front);

                if ( target >=front && target <=back) {
                   // Log.i("Target is visible", "Target is not -1!!");
                    customScrollListener.setTarget(-1);
                    highlightText(target);

                }

                return result;

            }

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {



                super.onLayoutChildren(recycler, state);
                int target = customScrollListener.getTarget();
                //Log.i("excellent", "In onLayoutChildren\t"+ "Position: " + String.valueOf(target));

                int front = findFirstVisibleItemPosition();
                int back = findLastVisibleItemPosition();

                if ( target >=front && target <=back) {
                   // Log.i("Target is visible", "Target is not -1!!");
                    customScrollListener.setTarget(-1);
                    highlightText(target);

                }
            }
        };
        customScrollListener = new CustomScrollListener();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(customScrollListener);


        // After dataSet is completed
        //Progress bar to show progress of TTS file synthesis

        progress = view.findViewById(R.id.progress);
        if(easyWay) { progress.setVisibility(View.GONE); }
        else {
            if (dataSet.size() == tempDataSet.size() && !dataSet.contains(null)) {
                progress.setVisibility(View.GONE);
            } else {
                progressBar = view.findViewById(R.id.determinateBar);
                progressBar.setProgress(numNonNulls());
                progressBar.setMax(tempDataSet.size());

                progressNumber = view.findViewById(R.id.progressNumber);
            }

        }

        //Permissions for MediaPlayer
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        adapter.setContext(context);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return view;

    }

    public int numNonNulls(){
        List<TextAudioChunk> nonNulls = dataSet.parallelStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return nonNulls.size();
    }

    public void onFragmentInteraction(Uri uri){
        Log.i("onFragmentInteraction", uri.toString());
    }

    public void getContent(){

        try {
            Object[] voicePrefs = getVoicePreferences();
            int volume = (int) voicePrefs[1];

            AudioBook book = new AudioBook(getContext(), bookId);
            String moduleFile = book.getModuleFile(modId);
            Content.Module mod = new Content.Module(modId, moduleFile);

            tempDataSet = mod.initTextAudioChunks(volume);

            // fill dataset with temporary values; because chunks finish downloading out of order
            if (!easyWay) {
                Log.i("NOT the easyWay", "you better not be in here!");
                for(int x = 0; x < tempDataSet.size(); x ++)
                    dataSet.add(null);
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Convert every text block in dataSet to audio and store
     */
    public void storeConvertedTTSAudio() {
        Object[] voicePrefs = getVoicePreferences();
        String voice = (String) voicePrefs[0];

        File book = new File(getContext().getExternalCacheDir(), bookId);
        if (!book.isDirectory()) {
                if(book.mkdirs()) Log.i("Book directory created", "YAY!");
                else Log.i("FAILED: book directory not created", "rip");
        }

        tempDataSet.parallelStream().forEach(chunk -> {
            String ssml = chunk.getSsml();
            int id = chunk.getId();
            String folder = String.format("%s/%s/%s/", getContext().getExternalCacheDir().getAbsolutePath(), bookId, modId);
            AudioClient.AmazonClient client = new AudioClient.AmazonClient(folder, getContext(), voice);
            client.synthesizeAudio(String.valueOf(id), true, ssml, false);
            chunk.setAudioFile(folder + id + ".mp3");
            chunk.synthesized();
        });
    }


    /**
     * Find the first item whose top is underneath the action bar (that would be ok to read without needing to scroll.)
     * */
    public int getActualFirstVisibleItem(){
        int first = layoutManager.findFirstCompletelyVisibleItemPosition();
        int last = layoutManager.findLastCompletelyVisibleItemPosition();

        int index = layoutManager.findFirstVisibleItemPosition();
        int guaranteedLast = layoutManager.findLastVisibleItemPosition();

        while(index <= guaranteedLast){
            View v = layoutManager.findViewByPosition(index);
            // removed the last condition from that in checkIfVisible.
            if((v == null) || (v !=null && v.getTop() < 0))
                index ++;
            else return index;
        }

        return last;
    }


    /**
     * Scroll to selected text block if it is not visible. Otherwise, proceed to read it aloud (call readText).
     * @param position
     */
    public void checkIfVisible(final int position){
        adapter.setSelected(position);
        int first = layoutManager.findFirstCompletelyVisibleItemPosition();
        int last = layoutManager.findLastCompletelyVisibleItemPosition();

        View v = layoutManager.findViewByPosition(position);
        customScrollListener.setTarget(position);



        // should check if the position is the first visible item position but the top is nOT at the top: && !(first == -1 && last == -1 && position == layoutManager.findFirstVisibleItemPosition())
        // last condition checks if the view is visible, but isn't at the top. (too much at the bottom)
        if(     //((position < first || position > last)   &&     (first !=-1 && last !=-1)) ||
                (v == null) ||
                        (v !=null && v.getTop() < 0) ||
                        (v !=null && v.getTop() > 0 && position != last && position == layoutManager.findLastVisibleItemPosition()))
        {

            //  if(v != null) Log.i("Top", String.valueOf(v.getTop()));

            // Log.i("Position not visible?", "\tFirst: " + String.valueOf(first)+"\tPosition: " + String.valueOf(position) + "\tLast: " + String.valueOf(last));
            // Log.i("First Visible item position: ", String.valueOf(layoutManager.findFirstVisibleItemPosition()));

            //customScrollListener.setTarget(position);
            recyclerView.smoothScrollToPosition(position);

        }
        else {
            //Log.i("Selected: " + String.valueOf(getSelected()), "Position (Input): " + String.valueOf(position));
            highlightText(position);
        }
    }

    /**
     * Should only be called after the view is visible.
     * @param position
     */
    public void highlightText(final int position) {
        final View v = layoutManager.findViewByPosition(position);

        if (v != null) {
            getActivity().runOnUiThread(() ->
            {
                v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(context, R.color.colorHighlighted));
                ((TextView) v.findViewById(R.id.item)).setTextColor(ContextCompat.getColor(context, R.color.highlightedText));
            });

        } else {
            Log.i("View is null", "View: " + v + "\tPosition: " + position + "\nLast Visible Item: " + layoutManager.findLastVisibleItemPosition());
        }

    }

    /**
     * Will be useful for downloading the entire chapter.
     * @param string
     * @return String array representation of string (broken down into 3999 character blocks)
     */
    private String[] stringSplit(String string) {
        String a = WordUtils.wrap(string, 3999);
        String[] list = a.split(System.lineSeparator());
        Log.i("Split string array length: ", String.valueOf(list.length));
        for(String str : list) Log.i("\t\tElement", str +"\n");
        return list;
    }

    // Formerly in UtteranceProgressListener
    public void showProgress(int pos){
        // index number in dataSet

        dataSet.set(pos, new TextAudioChunk(tempDataSet.get(pos)));
        getActivity().runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            if(progressBar != null) {
                progressBar.incrementProgressBy(1);
                int fraction = progressBar.getProgress() * 100 / tempDataSet.size();
                progressNumber.setText(String.format("%s%%", fraction));

                if(progressBar.getProgress() == tempDataSet.size()){
                    Log.i("Completed converting all files", String.valueOf(pos));

                    storeTimeLengths();

                    if(makeDownloadAvailable) { makeDownloadAvailable = false; }
                    progress.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progress.setVisibility(View.GONE);
                        }
                    });
                }
            }

        });
    }


    /**
     * Change the view at the specified position back to its normal appearance
     * @param position
     */
    public void unhighlightText(final int position){


        getActivity().runOnUiThread(() -> {

            // old method
            //View v = recyclerView.getLayoutManager().findViewByPosition(Integer.parseInt(position));
            TextbookViewAdapter.ViewHolder vh = (TextbookViewAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            if (vh != null) {
                View v = vh.textView;
                v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(context, R.color.transparentGrey));
                ((TextView)v.findViewById(R.id.item)).setTextColor(ContextCompat.getColor(context, R.color.normalText));
            }
            else{Log.i("unhighlightText", "View is null, can't set bg color to grey");}
        });
    }

    /**
     * Shut down TTS and MediaPlayer instances
     */
    @Override
    public void onDestroy() {
        if(player !=null){
            try {
                player.stop();
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }


    //Called by player bar fragment
    public void pauseTTS() {
        is_paused = true;

        player.pause();
        length = player.getCurrentPosition();

        Log.i("Paused - Selected", String.valueOf(adapter.getSelected()));

        //backToNormal(String.valueOf(getSelected()));

        getActivity().runOnUiThread(() -> playerBarFragment.setPlayButton("Play"));
    }

    //Called by player bar fragment
    public void stopTTS(){
        player.stop();

        int former_position = getSelected();
        unhighlightText(former_position);
        is_paused = false;

        adapter.setSelected(0);
        getActivity().runOnUiThread(() -> playerBarFragment.setPlayButton("Play"));
    }

    //Called by player bar fragment
    public void forwardTTS(){

        is_paused = false; // if the player was paused before the user pressed "fast forward"

        int position = getSelected();

        unhighlightText(position);
        int posNext = position + 1;
        if (posNext < dataSet.size()) {
            checkIfVisible(posNext);

        } else {
            adapter.setSelected(0);
            getActivity().runOnUiThread(() -> {
                playerBarFragment.setPlayButton("Play");
                /* // blubber
                playerBarFragment.setStopButton(false);
                playerBarFragment.setForwardButton(false);
                playerBarFragment.setReverseButton(false); */
            });
        }
    }

    //Called by player bar fragment
    public void reverseTTS(){

        is_paused = false; // if the player was paused before the user pressed "fast rewind"

        Log.i("Reversed - Selected", String.valueOf(adapter.getSelected()) + " (should be the same)");

        int position = getSelected();

        unhighlightText(position);
        int posPrev= position - 1;
        if (posPrev >= 0) {
            checkIfVisible(posPrev);

        } else {
            adapter.setSelected(0);
            getActivity().runOnUiThread(() -> {
                playerBarFragment.setPlayButton("Play");
                //blubber
                /*
                playerBarFragment.setStopButton(false);
                playerBarFragment.setForwardButton(false);
                playerBarFragment.setReverseButton(false);  */
            });
        }
    }

    //Called by player bar fragment - seek bar
    public void onDragStart(int position){
        if (player.isPlaying())
            player.stop();
        int former_position = getSelected();
        unhighlightText(former_position);
        adapter.setSelected(-1);


    }

    //Called by player bar fragment - seek bar
    public void onDragStop(int position){
        int start = getActualFirstVisibleItem();
        //int start = layoutManager.findFirstVisibleItemPosition();
        if(start == -1) Log.e("Invalid position", "getActualFirstVisibleItem returned -1");
        adapter.setSelected(start);
        highlightText(start);

    }

    //Called by player bar fragment - seek bar
    public void showChange(int position){

        //adapter.setSelected(position);
        recyclerView.scrollToPosition(position);

    }

    //Called by volume fragment
    public void setVolume(float value){
        Log.i("Volume", String.valueOf(value));
        player.setVolume(value, value);
    }

    public int getSelected(){
        return adapter.getSelected();
    }

    public class CustomScrollListener extends RecyclerView.OnScrollListener {

        int target;

        public CustomScrollListener() {
            super();
            target = -1;
        }

        public void setTarget(int num) {target = num;}

        public int getTarget() {return target;}

        public boolean matchesTarget(int num) {return num == target;}

        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    //System.out.println("The RecyclerView is not scrolling");
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    //System.out.println("Scrolling now");
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:
                    //System.out.println("Scroll Settling");
                    //Log.i("ScrollStateChanged", "Scroll Settling\tTarget: " + String.valueOf(target));
                    /*if(target != -1) {
                        Log.i("ScrollStateChanged", "Target is not -1!!");
                        doneScrolling(target);
                        target = -1;
                    } */
                    break;

            }

        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dx > 0) {
                //System.out.println("Scrolled Right");
            } else if (dx < 0) {
                //System.out.println("Scrolled Left");
            } else {
                //System.out.println("No Horizontal Scrolled");
            }

            if (dy > 0) {
                //System.out.println("Scrolled Downwards");
            } else if (dy < 0) {
                // System.out.println("Scrolled Upwards");
            } else {
                //System.out.println("No Vertical Scrolled");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void sendModuleInfo(String bookTitle, String bookID, String modID, String modTitle);

        void playEntireModule(String bookTitle, String modID, String modTitle);

        void onRecyclerViewCreated(RecyclerView recyclerView);
    }

    public List<TextAudioChunk> getDataSet(){ return dataSet; }

    @Override
    public void onResume(){
        super.onResume();

        if(recyclerView !=null) ((MainActivity)getActivity()).onRecyclerViewCreated(recyclerView);
        else Log.i("Recyclerview is null", "lol");


//        playerBarFragment.setSeekbarProgress(getActualFirstVisibleItem());

    }

    /**
     * Called after the entire module has been converted.
     */
    public void storeTimeLengths(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("time lengths", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<Integer> times = new ArrayList<>();

        // get times from downloads as milliseconds
        dataSet.forEach(chunk -> {
            int time = getTimeLength(chunk.getAudioFile());
            times.add(time);
        });

        // convert arrayList to jSONArray to string
        JSONArray timesJSON = new JSONArray(times);
        String timesString = timesJSON.toString();
        // commit changes
        editor.putString(modId, timesString);
        editor.commit();
    }

    public int getTimeLength(String filePath){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getContext(), Uri.parse(filePath));
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //Log.i("Duration", duration);
        mmr.release();
        return Integer.parseInt(duration);
    }

    public ArrayList<Integer> getTimesList(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("time lengths", 0);
        String timesString = sharedPreferences.getString(modId, "Not Found");
        if(timesString.equals("Not Found")) return null;
        else {
            try {
//                String s = "";
                int total = 0;
                JSONArray timesJSON = new JSONArray(timesString);
                ArrayList<Integer> times = new ArrayList<>();
                for (int i  = 0; i < timesJSON.length(); i ++){
                    times.add(Integer.parseInt(timesJSON.getString(i)));
//                    s +=timesJSON.getString(i)+"\t";
                    total += Integer.parseInt(timesJSON.getString(i));
                }
                Log.i("======================>", "yeetz");
                Log.i("Calculated Duration", String.valueOf(total));
                playerBarFragment =  (PlayerBarFragment) getActivity().getSupportFragmentManager().findFragmentByTag("Player Bar");
                Log.i("Actual Duration", String.valueOf(playerBarFragment.getDuration()));
                return times;

            } catch(JSONException e){ Log.e("JSONException", "SharedPreferences string cannot be converted to JSONArray");}


        }

        return null;
    }

    public int getTimeUpTo(int position){
        int sum = 0;
        for(int i = 0; i < position; i ++){
            sum += times.get(i);
        }
        return sum;
    }

    public int getPositionAt(int time){
        int sum = 0;
        if (times != null) {
            for(int i = 0; i < times.size(); i ++){
                if(sum <= time && time < sum + times.get(i))
                    return i;
                sum += times.get(i);
            }

            return times.size() - 1;
        }
        // means times hasn't been instantiated yet; give it some t i m e
        return -1;

    }

    public void setSelected(int i){
        adapter.setSelected(i);
    }

    public void makeThingsEasy(){
        easyWay = true;
        times= getTimesList();
    }

    public Object[] getVoicePreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("settings", 0);
        String selectedVoice = sharedPreferences.getString("voice", "None");
        int selectedVolume = sharedPreferences.getInt("volume", -6); // -6 to 6.
        return new Object[]{selectedVoice, selectedVolume};
    }

}
