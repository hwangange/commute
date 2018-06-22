package com.interns.team3.openstax.myttsapplication;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TextbookView extends AppCompatActivity {


    private RecyclerView recyclerView;
    private TextbookViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public static String modId, bookId;
    public static Document content;
    public ArrayList<String> dataSet;

    public TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_view);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // recyclerView.setHasFixedSize(true);

        // use a GRID layout manager
        layoutManager = new GridLayoutManager(this, 1);
       // layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //get content
        Intent intent = getIntent();
        modId = intent.getStringExtra("Module ID");
        bookId = intent.getStringExtra("Book ID");
        // Toast.makeText(getApplicationContext(), modId, Toast.LENGTH_SHORT).show();

        content = getContent();
        dataSet = new ArrayList<String>();

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
        // specify an adapter (see also next example)
        adapter = new TextbookViewAdapter(dataSet, new TextbookViewAdapter.TextOnClickListener(){
            @Override public void onClick(String text, View v){
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id");
                while(tts.isSpeaking()){
                    v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSelected));
                }
                v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.defaultGrey));
            }
        });
       // adapter.setContext(getApplicationContext()); // nOT NEEDED // will also setup TTS instance
        recyclerView.setAdapter(adapter);


        Elements elements = content.body().children().select("*");
        for (Element element : elements) {
            dataSet.add(element.ownText());

            //adapter.notifyItemInserted(dataSet.size()-1);
            adapter.notifyDataSetChanged();

        }




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
}
