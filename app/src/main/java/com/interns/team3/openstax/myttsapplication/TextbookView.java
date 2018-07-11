package com.interns.team3.openstax.myttsapplication;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.nshmura.snappysmoothscroller.LinearLayoutScrollVectorDetector;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappySmoothScroller;


import org.apache.commons.text.WordUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;


public class TextbookView extends AppCompatActivity implements PlayerBarFragment.OnFragmentInteractionListener, VolumeFragment.OnFragmentInteractionListener {


    private RecyclerView recyclerView;
    private TextbookViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static String modId, bookId, modTitle;
    public static Document content;
    public ArrayList<TextChunk> tempDataSet, dataSet;

    public TextToSpeech tts;
    public MyUtteranceProgressListener myUtteranceProgressListener;
    public MediaPlayer player = new MediaPlayer();

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

    public Button download, playMerged;
    public boolean makeDownloadAvailable = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_view);

        is_paused = false;

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // Layout Manager
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {

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
                //Log.i("excellent", "In scrollVerticallyBy\t" + "Position: " + String.valueOf(target));

                int front = findFirstVisibleItemPosition();
                int back = findLastVisibleItemPosition();

                if ( target >=front && target <=back) {
                   // Log.i("Target is visible", "Target is not -1!!");
                    readText(target);
                    customScrollListener.setTarget(-1);
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
                    readText(target);
                    customScrollListener.setTarget(-1);
                }
            }
        };
        customScrollListener = new CustomScrollListener();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(customScrollListener);

        // Get book and module info
        Intent intent = getIntent();
        modId = intent.getStringExtra("Module ID");
        bookId = intent.getStringExtra("Book ID");
        modTitle= intent.getStringExtra("Module Title");

        // Customize action bar
        setTitle(modTitle);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Utterance Progress Listener
        myUtteranceProgressListener = new MyUtteranceProgressListener();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    //mButtonSpeak.setEnabled(true);
                    storeConvertedTTSAudio();
                    Log.e("Initialization", "Initialization succeeded");

                } else {
                    Log.e("Initialization", "Initialization failed");
                }

            }
        });
        tts.setOnUtteranceProgressListener(myUtteranceProgressListener);


        // Populate dataSet
        tempDataSet = new ArrayList<TextChunk>();
        dataSet = new ArrayList<TextChunk>();
        adapter = new TextbookViewAdapter(dataSet, new TextbookViewAdapter.TextOnClickListener(){

            @Override public void onClick(int position){
                if(position != getSelected() || (position == getSelected() && !player.isPlaying()))  // do not pause the audio if "selected" text is selected again
                {
                    // If interrupted

                    int former_position = getSelected();
                    TextChunk former_selected = dataSet.get(former_position);
                    //if(former_selected.isSelected())
                    backToNormal(String.valueOf(former_position));


                    is_paused = false;

                    checkIfVisible(position);
                }
            }

        });

        try {
            getContent();
        } catch (IOException e){
            Log.e("IOException", e.toString());
        } catch(JSONException e){
            Log.e("JSONException", e.toString());
        }

        // After dataSet is completed
        //Progress bar to show progress of TTS file synthesis
        progressBar = (ProgressBar) findViewById(R.id.determinateBar);
        progressBar.setMax(tempDataSet.size());
        progress = (LinearLayout) findViewById(R.id.progress);
        progressNumber = (TextView) findViewById(R.id.progressNumber);

        adapter.setContext(getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //Permissions for MediaPlayer
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }


        // Audio Player Bar
        fragmentManager = getSupportFragmentManager();

        //Add player bar fragment to this activity
        FragmentTransaction ft = fragmentManager.beginTransaction();
        playerBarFragment = PlayerBarFragment.newInstance("","");
        ft.add(R.id.playbar_container, playerBarFragment);
        ft.commit();

        String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output"+modId+".mp3";
        playMerged = (Button) findViewById(R.id.playMerged);
        playMerged.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playMergedFile(output);
            }
        });


        File f = new File(output);
        if(f.exists()) { makeDownloadAvailable = false; }
        else { playMerged.setEnabled(false);}

        //Download button
        download = (Button) findViewById(R.id.download);
        download.setEnabled(false);
        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{ download(output); } catch (IOException e){Log.i("IOException", "Can't download");}
            }
        });

        // ffmpeg merge audio
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        if (ffmpeg.isSupported()) {
            // ffmpeg is supported
            Log.i("FFmpeg is supported", "Yay!");
            if(makeDownloadAvailable) download.setEnabled(true);
        } else {
            // ffmpeg is not supported
            Log.i("FFmpeg is not supported", "Darn ;(");
        }




    }

    public void replaceFragment(){

    }

    public void onFragmentInteraction(Uri uri){
        Log.i("onFragmentInteraction", uri.toString());
    }

    /**
     * Build data set
     * @throws IOException
     * @throws JSONException
     */
    public void getContent() throws IOException, JSONException {

        String fileName = "Books/"+bookId+"/"+modId+"/index.cnxml.html";
        StringBuilder buf = new StringBuilder();
        InputStreamReader inputStream = new InputStreamReader(getAssets().open(fileName));
        BufferedReader bufferedReader = new BufferedReader(inputStream);
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buf.append(str);
        }

        Document doc = Jsoup.parse(buf.toString());

        String title = doc.body().getElementsByTag("div").first().attr("document-title");
        Content.Module mod= new Content.Module(title, modId, doc);


        // eventually just replace with mod.buildModuleSSMl()
        ArrayList<String> temp = mod.returnPrintOpening();
        if(temp != null) temp.forEach( (stringo) -> tempDataSet.add(new TextChunk(stringo)));
        temp = mod.returnPrintReadingSections();
        if(temp != null) temp.forEach( (stringo) -> tempDataSet.add(new TextChunk(stringo)));
        temp = mod.returnPrintEoc();
        if(temp != null) temp.forEach( (stringo) -> tempDataSet.add(new TextChunk(stringo)));
        //adapter.notifyDataSetChanged();

        /*Elements elements = doc.body().children().select("*");
        for (Element element : elements) {
            dataSet.add(new TextChunk(element.ownText()));
            adapter.notifyItemInserted(dataSet.size() - 1);
                //adapter.notifyDataSetChanged();
        } */

    }


    /**
     * Convert every text block in dataSet to audio and store
     */
    public void storeConvertedTTSAudio() {


        for (TextChunk tc : tempDataSet) {
            String s = Jsoup.parse(tc.getText()).text(); // might have to modify this once text comes with SSML tags
            String id = "textbookaudio" + tempDataSet.indexOf(tc);


            String filename = "/textbookaudio" + String.valueOf(tempDataSet.indexOf(tc)) + ".wav";
            File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
            tts.synthesizeToFile(s, null, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename), id);

        }


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
            customScrollListener.setTarget(position);
            recyclerView.smoothScrollToPosition(position);

        }
        else {
            //Log.i("Selected: " + String.valueOf(getSelected()), "Position (Input): " + String.valueOf(position));
            readText(position);
        }
    }

    /**
     * Should only be called after the view is visible.
     * @param position
     */
    public void readText(final int position) {
        final View v = layoutManager.findViewByPosition(position);

        if (v != null) {
            TextView tv = v.findViewById(R.id.item);
            TextChunk tc = dataSet.get(position);
            final String text = Jsoup.parse(tc.getText()).text();

            tc.setSelected(true);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorHighlighted));
                    playerBarFragment.setPlayButton("Pause");
                    playerBarFragment.setStopButton(true);
                    playerBarFragment.setForwardButton(true);
                    playerBarFragment.setReverseButton(true);

                }
            });

            Thread readTextThread = new Thread()
            {
                public void run() {

                    if (is_paused) {
                        is_paused = false;
                        player.seekTo(length - 1);
                        player.start();
                    }

                    else {

                        player.reset();

                        // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(position));

                        String filename2 = "/textbookaudio" + String.valueOf(position) + ".wav";
                        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + filename2;

                        Log.i("File name", fileName);

                        Uri uri = Uri.parse("file://" + fileName);

                        // player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build());

                        try {
                            player.setDataSource(getApplicationContext(), uri);
                            player.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                player.start();
                            }
                        });

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                player.stop();

                                backToNormal(String.valueOf(position));
                                int posn = position + 1;
                                if (posn < dataSet.size()) {
                                    checkIfVisible(posn);

                                } else {
                                    adapter.setSelected(0);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            playerBarFragment.setPlayButton("Play");
                                            playerBarFragment.setStopButton(false);
                                            playerBarFragment.setForwardButton(false);
                                            playerBarFragment.setReverseButton(false);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            };

            readTextThread.start();
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

    /**
     * Useful for performing certain actions only after TTS finishes a request.
     */
    public class MyUtteranceProgressListener extends UtteranceProgressListener{

        public MyUtteranceProgressListener(){
            super();
        }

        @Override
        public void onStart(String position){
            //Log.i("onStart", position);
        }

        @Override
        public void onError(String position){
            //Log.i("onError", position);
        }

        @Override
        public void onDone(final String pos) {

            if(pos.contains("textbookaudio")) {

                // index number in dataSet
                int number = Integer.parseInt(pos.replaceAll("textbookaudio",""));
                dataSet.add(new TextChunk(tempDataSet.get(number).getText()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(dataSet.size()-1);
                        progressBar.incrementProgressBy(1);
                        int fraction = (int) (progressBar.getProgress() * 100 / tempDataSet.size());
                        progressNumber.setText(String.valueOf(fraction) + "%");

                    }
                });

                if(pos.contains(String.valueOf(tempDataSet.size()-1))){
                    Log.i("Completed converting all files", pos);
                    progress.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progress.setVisibility(View.GONE);
                        }
                    });

                }

            }

        }

        @Override
        public void onStop(final String position, boolean interrupted){
            //if(interrupted) Log.i("onStop", "Interrupted");
            //else Log.i("onStop", "Completed");

            /*if(!position.contains("textbookaudio"))
                backToNormal(position); */

        }
    }

    /**
     * Change the view at the specified position back to its normal appearance
     * @param pos
     */
    public void backToNormal(String pos){

        final int position = Integer.parseInt(pos);
        dataSet.get(position).setSelected(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // old method
                //View v = recyclerView.getLayoutManager().findViewByPosition(Integer.parseInt(position));

                TextbookViewAdapter.ViewHolder vh = (TextbookViewAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                if (vh != null) {
                    View v = vh.textView;
                    v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.defaultGrey));
                }
                else{Log.i("backToNormal", "View is null, can't set bg color to grey");}
            }


        });
    }

    /**
     * Shut down TTS and MediaPlayer instances
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        player.stop();
        player.release();

        super.onDestroy();
    }


    //Called by player bar fragment
    public void pauseTTS() {

        is_paused = true;

        player.pause();
        length = player.getCurrentPosition();

        //backToNormal(String.valueOf(getSelected()));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerBarFragment.setPlayButton("Play");
            }
        });
    }

    //Called by player bar fragment
    public void stopTTS(){
        player.stop();


        int former_position = getSelected();
        backToNormal(String.valueOf(former_position));

        adapter.setSelected(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerBarFragment.setPlayButton("Play");
            }
        });
    }

    //Called by player bar fragment
    public void forwardTTS(){
        int position = getSelected();
        player.stop();

        backToNormal(String.valueOf(position));
        int posn = position + 1;
        if (posn < dataSet.size()) {
            checkIfVisible(posn);

        } else {
            adapter.setSelected(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerBarFragment.setPlayButton("Play");
                    playerBarFragment.setStopButton(false);
                    playerBarFragment.setForwardButton(false);
                    playerBarFragment.setReverseButton(false);
                }
            });
        }
    }

    //Called by player bar fragment
    public void reverseTTS(){

        int position = getSelected();
        player.stop();

        backToNormal(String.valueOf(position));
        int posn = position - 1;
        if (posn >= 0) {
            checkIfVisible(posn);

        } else {
            adapter.setSelected(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerBarFragment.setPlayButton("Play");
                    playerBarFragment.setStopButton(false);
                    playerBarFragment.setForwardButton(false);
                    playerBarFragment.setReverseButton(false);
                }
            });
        }
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
            onBackPressed();
            return true;
        }
        return false;
    }

    public void download(String outputFilename) throws IOException {

        /*String[] uris = new String[]{
                "/storage/emulated/0/textbookaudio0.wav",
             */

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

        // String[] cmd = new String[]{"-version"};


        // to execute "ffmpeg -version" command you just need to pass "-version"
        // for more info, check out this link:
        // https://superuser.com/questions/1298891/ffmpeg-merge-multiple-audio-files-into-single-audio-file-with-android
        // CORRECT dependency that fixes "relocation" problems: https://github.com/bravobit/FFmpeg-Android
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
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
                playMerged.setEnabled(true);
            }

        });
    }

    public void playMergedFile(String output) {

        player.reset();

        // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(position));

        Uri uri = Uri.parse("file://" + output);

        // player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.stop();

            }
        });
    }
}
