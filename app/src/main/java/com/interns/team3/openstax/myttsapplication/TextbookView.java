package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.nshmura.snappysmoothscroller.LinearLayoutScrollVectorDetector;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager;
import com.nshmura.snappysmoothscroller.SnappySmoothScroller;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextbookView extends AppCompatActivity {


    private RecyclerView recyclerView;
    private TextbookViewAdapter adapter;
    private LinearLayoutManager layoutManager;

    public static String modId, bookId;
    public static Document content;
    public ArrayList<TextChunk> dataSet;

    public TextToSpeech tts;
    public MyUtteranceProgressListener myUtteranceProgressListener;

    public ExecutorService executorService;

    public CustomScrollListener customScrollListener;

   // public static Bus bus;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_view);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        executorService = Executors.newSingleThreadExecutor();

        //bus = new Bus(ThreadEnforcer.ANY);
        //bus.register(this);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // recyclerView.setHasFixedSize(true);

        // use a GRID layout manager
       // layoutManager = new GridLayoutManager(this, 1);

        // OPTION 1
        /*layoutManager = new SnappyLinearLayoutManager(getApplicationContext());
        // Set the SnapType
        layoutManager.setSnapType(SnapType.CENTER);

        // Set the Interpolator
        layoutManager.setSnapInterpolator(new DecelerateInterpolator());
        layoutManager.setSnapPaddingEnd(20);
        recyclerView.setLayoutManager(layoutManager); */

        //OPTION 2
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false) {

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                Log.i("on smooth scroll", "hmm");
                SnappySmoothScroller scroller = new SnappySmoothScroller.Builder()
                        .setSnapType(SnapType.CENTER)
                        .setSnapInterpolator(new DecelerateInterpolator())
                        .setSnapPaddingEnd(20)
                        .setPosition(position)
                        .setScrollVectorDetector(new LinearLayoutScrollVectorDetector(this))
                        .build(recyclerView.getContext());

                startSmoothScroll(scroller);
                //bus.post(position);

            }

            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

                final int result = super.scrollVerticallyBy(dy, recycler, state);
                int target = customScrollListener.getTarget();
                Log.i("excellent", "In scrollVerticallyBy\t" + "Position: " + String.valueOf(target));

                int front = findFirstVisibleItemPosition();
                int back = findLastVisibleItemPosition();

                if ( target >=front && target <=back) {
                    Log.i("Target is visible", "Target is not -1!!");
                    doneScrolling(target);
                    customScrollListener.setTarget(-1);
                }

                return result;

            }

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {



                super.onLayoutChildren(recycler, state);
                int target = customScrollListener.getTarget();
                Log.i("excellent", "In onLayoutChildren\t"+ "Position: " + String.valueOf(target));

                int front = findFirstVisibleItemPosition();
                int back = findLastVisibleItemPosition();

                if ( target >=front && target <=back) {
                    Log.i("Target is visible", "Target is not -1!!");
                    doneScrolling(target);
                    customScrollListener.setTarget(-1);
                }
            }
        };

        customScrollListener = new CustomScrollListener();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(customScrollListener);

        Toast.makeText(getApplicationContext(), "I'm creating...", Toast.LENGTH_SHORT).show();

        //get content
        Intent intent = getIntent();
        modId = intent.getStringExtra("Module ID");
        bookId = intent.getStringExtra("Book ID");
        // Toast.makeText(getApplicationContext(), modId, Toast.LENGTH_SHORT).show();

        content = getContent();
        dataSet = new ArrayList<TextChunk>();

        myUtteranceProgressListener = new MyUtteranceProgressListener();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    //mButtonSpeak.setEnabled(true);
                    Log.e("Initialization", "Initialization succeeded");

                } else {
                    Log.e("Initialization", "Initialization failed");
                }

            }
        });
        tts.setOnUtteranceProgressListener(myUtteranceProgressListener);

        // specify an adapter (see also next example)
        adapter = new TextbookViewAdapter(dataSet, new TextbookViewAdapter.TextOnClickListener(){

            @Override public void onClick(String text, View v, int position){

                readText(text, v, position);
            }

        });

        adapter.setContext(getApplicationContext()); // nOT NEEDED // will also setup TTS instance
        recyclerView.setAdapter(adapter);


        Elements elements = content.body().children().select("*");
        for (Element element : elements) {
            dataSet.add(new TextChunk(element.ownText()));

            adapter.notifyItemInserted(dataSet.size()-1);
            //adapter.notifyDataSetChanged();

        }

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                Log.e("LayoutChangeListener", "updated");
            }
        });
        adapter.notifyDataSetChanged();




    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public void readText(final String text, final View v, final int position) {

        TextChunk tc = dataSet.get(position);
        tc.setSelected(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorHighlighted));
            }
        });

        Thread readTextThread = new Thread()
        {
            public void run() {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(position));
            }

        };

        readTextThread.start();

    }

    public Document getContent() {

        try {

            String fileName = "Books/"+bookId+"/"+modId+"/index.cnxml.html";
            StringBuilder buf = new StringBuilder();
            InputStreamReader inputStream = new InputStreamReader(getAssets().open(fileName));
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buf.append(str);
            }
            Document doc = Jsoup.parse(buf.toString());

            return doc;



        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ArrayList<String> getData(Document doc, TextbookViewAdapter adapter) {
        Element body = doc.body();
        ArrayList<String> lst = new ArrayList<String>();

        Elements elements = doc.body().children().select("*");
        for (Element element : elements) {
            lst.add(element.ownText());
        }

        return lst;
    }

    public class MyUtteranceProgressListener extends UtteranceProgressListener{

        public MyUtteranceProgressListener(){
            super();
            //bus.register(this);
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
            backToNormal(pos);

            // new Autoplay().execute(pos);

            final int posn = Integer.parseInt(pos) + 1;
            if (posn < dataSet.size()) {
                View v = layoutManager.findViewByPosition(posn);

                if (v == null) {

                    customScrollListener.setTarget(posn);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(posn);

                            //adapter.notifyDataSetChanged();

                        }
                    });

                }
                else{
                    Log.i("About to Read: ", String.valueOf(posn));
                    doneScrolling(posn);
                    //bus.post(posn);
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

    //@Subscribe
    public void doneScrolling(int posn){
        Log.i("GOOD", "IN DONE SCROLLING");
        //Toast.makeText(getApplicationContext(), "In doneScrolling", Toast.LENGTH_SHORT).show();
        View v = layoutManager.findViewByPosition(posn);

        if(v != null){
            TextView tv = v.findViewById(R.id.item);
            String text = (String) tv.getText();
            readText(text, v, posn);
        }
        else {
            Log.i("View is null", "View: " + v + "\tPosition: " + posn + "\nLast Visible Item: " + layoutManager.findLastVisibleItemPosition());
        }

    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if(executorService !=null) {
            executorService.shutdownNow();
        }

        super.onDestroy();
    }

    @Override
    protected void onStop(){
        super.onStop();
        //bus.unregister(this);
    }


    public class Autoplay extends AsyncTask<String, Void, View> {

        private int position;

        protected View doInBackground(String... pos){
            TextbookViewAdapter.ViewHolder vh;

            position = Integer.parseInt(pos[0]) +1;
            if(position < dataSet.size()) {
               // vh = (TextbookViewAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                View v = layoutManager.findViewByPosition(position);

                if (v == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(position);
                            //adapter.notifyDataSetChanged();

                        }
                    });
                    //vh = (TextbookViewAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
                    v = layoutManager.findViewByPosition(position);
                    Log.i("1", "View: " + v);
                }

                return v;
            }

            return null;
        }

        protected void onProgressUpdate(){

        }

        protected void onPostExecute(View v){

            if(v != null){
                TextView tv = v.findViewById(R.id.item);
                String text = (String) tv.getText();
                readText(text, v, position);
            }
            else {
                Log.i("View is null", "View: " + v + "\tPosition: " + position + "\nLast Visible Item: " + layoutManager.findLastVisibleItemPosition());
            }



        }

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

}
