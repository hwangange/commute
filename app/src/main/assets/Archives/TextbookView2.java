package com.interns.team3.openstax.myttsapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
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
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Arrays;

public class TextbookView2 extends AppCompatActivity implements PlayerBarFragment.OnFragmentInteractionListener {


    private RecyclerView recyclerView;
    private TextbookViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static String modId, bookId, modTitle;
    public static Document content;
    public ArrayList<TextChunk> dataSet;

    public TextToSpeech tts;
    public MyUtteranceProgressListener myUtteranceProgressListener;
    public MediaPlayer player = new MediaPlayer();

    Button readBtn;
    Button pauseBtn;
    Button convertBtn;
    private int length;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public CustomScrollListener customScrollListener;

    public FragmentManager fragmentManager;
    public PlayerBarFragment playerBarFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_view);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        readBtn = findViewById(R.id.readbtn);
        pauseBtn = findViewById(R.id.pause);
        convertBtn = findViewById(R.id.convert);

        convertBtn.setEnabled(false);
        pauseBtn.setEnabled(false);
        readBtn.setEnabled(false);

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

        Intent intent = getIntent();
        modId = intent.getStringExtra("Module ID");
        bookId = intent.getStringExtra("Book ID");
        modTitle= intent.getStringExtra("Module Title");
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
                    convertBtn.setEnabled(true);
                    Log.e("Initialization", "Initialization succeeded");

                } else {
                    Log.e("Initialization", "Initialization failed");
                }

            }
        });
        tts.setOnUtteranceProgressListener(myUtteranceProgressListener);



        dataSet = new ArrayList<TextChunk>();

        // specify an adapter (see also next example)
        adapter = new TextbookViewAdapter(dataSet, new TextbookViewAdapter.TextOnClickListener(){

            @Override public void onClick(int position){
                if(position != getSelected() || (position == getSelected() && !tts.isSpeaking()))  // do not pause the audio if "selected" text is selected again
                    checkIfVisible(position);
            }

        });

        try {
            getContent();
        } catch (IOException e){
            Log.e("IOException", e.toString());
        } catch(JSONException e){
            Log.e("JSONException", e.toString());
        }

        adapter.setContext(getApplicationContext()); // nOT NEEDED // will also setup TTS instance
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                Log.e("LayoutChangeListener", "updated");
            }
        });
        adapter.notifyDataSetChanged();

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

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()){
                    player.seekTo(0);
                    player.start();
                }
                else {

                    initializeMedia();
                    readBtn.setText("Restart");
                }


            }
        });


        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = "";

                for(TextChunk tc : dataSet){
                    temp = temp + tc.getText();
                }

                String[] list = stringSplit(temp);

                for (String s : list) {

                    String id = "textbookaudio" + Arrays.asList(list).indexOf(s);


                    String filename = "/textbookaudio" + Arrays.asList(list).indexOf(s) + s.substring(0,5).replaceAll("[^a-zA-Z ]", "") + ".wav";
                    File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
                    if (myFile.exists()){
                        Log.i("File Exists", filename);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                readBtn.setEnabled(true);
                                pauseBtn.setEnabled(true);
                                convertBtn.setText("Conversion completed");
                            }

                        });

                    }
                    else {
                        tts.synthesizeToFile(s, null, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename), id);
                        //tts.speak(s, TextToSpeech.QUEUE_ADD, null);
                    }

                }
            }


        });


        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!player.isPlaying()){
                    pauseBtn.setText("Pause");
                    player.seekTo(length-1);
                    player.start();
                }
                else{
                    player.pause();
                    pauseBtn.setText("Resume");
                    length = player.getCurrentPosition();

                }


            }
        });


        // Audio Player Bar
        fragmentManager = getSupportFragmentManager();

        //add
        FragmentTransaction ft = fragmentManager.beginTransaction();
        playerBarFragment = PlayerBarFragment.newInstance("","");
        ft.add(R.id.playbar_container, playerBarFragment);
        ft.commit();

        // handle 'replace' and 'remove' requests




    }

    public void onFragmentInteraction(Uri uri){
        Log.i("onFragmentInteraction", uri.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

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

    // Should only be called after the view is visible.
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
                }
            });

            Thread readTextThread = new Thread()
            {
                public void run() {

                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(position));
                }
            };

            readTextThread.start();
        } else {
            Log.i("View is null", "View: " + v + "\tPosition: " + position + "\nLast Visible Item: " + layoutManager.findLastVisibleItemPosition());
        }

    }

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
        if(temp != null) temp.forEach( (stringo) -> dataSet.add(new TextChunk(stringo)));
        temp = mod.returnPrintReadingSections();
        if(temp != null) temp.forEach( (stringo) -> dataSet.add(new TextChunk(stringo)));
        temp = mod.returnPrintEoc();
        if(temp != null) temp.forEach( (stringo) -> dataSet.add(new TextChunk(stringo)));
        adapter.notifyDataSetChanged();

        /*Elements elements = doc.body().children().select("*");
        for (Element element : elements) {
            dataSet.add(new TextChunk(element.ownText()));
            adapter.notifyItemInserted(dataSet.size() - 1);
                //adapter.notifyDataSetChanged();
        } */

    }

    private String[] stringSplit(String string) {
        String a = WordUtils.wrap(string, 3999);
        String[] list = a.split(System.lineSeparator());
        Log.i("Split string array length: ", String.valueOf(list.length));
        for(String str : list) Log.i("\t\tElement", str +"\n");
        return list;

    }

    private void initializeMedia() {
        String temp = "";
        for(TextChunk tc : dataSet)
            temp = temp + tc.getText();
        String[] list = stringSplit(temp);
        for (String s : list) {
            String filename2 = "/textbookaudio" + Arrays.asList(list).indexOf(s) + s.substring(0,5).replaceAll("[^a-zA-Z ]", "")+ ".wav";
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + filename2;

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
        }

    }

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
            // Log.i("onDone", pos);

            if(pos.contains("textbookaudio")) {
                Log.i("onDone", pos);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readBtn.setEnabled(true);
                        pauseBtn.setEnabled(true);
                        convertBtn.setText("Conversion completed");
                    }

                });
            }

            else{
                backToNormal(pos);
                int posn = Integer.parseInt(pos) + 1;
                if (posn < dataSet.size()) {
                    checkIfVisible(posn);

                } else {
                    adapter.setSelected(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerBarFragment.setPlayButton("Play");
                            playerBarFragment.setStopButton(false);
                        }
                    });
                }
            }

        }

        @Override
        public void onStop(final String position, boolean interrupted){
            //if(interrupted) Log.i("onStop", "Interrupted");
            //else Log.i("onStop", "Completed");

            backToNormal(position);

        }
    }

    public void backToNormal(String pos){

        final int position = Integer.parseInt(pos);

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
            }


        });

        dataSet.get(position).setSelected(false);
    }

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

    //Meant to be called by fragment
    public void pauseTTS() {
        tts.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerBarFragment.setPlayButton("Play");
            }
        });
    }

    //Meant to be called by fragment
    public void stopTTS(){
        tts.stop();
        adapter.setSelected(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerBarFragment.setPlayButton("Play");
            }
        });
    }

    public int getSelected(){
        return adapter.getSelected();
    }

    @Override
    protected void onStop(){
        super.onStop();
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

}
