package com.interns.team3.openstax.myttsapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TableOfContentsAdapter adapter;
    private ArrayList<Content> dataSet;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Construct the data source
        dataSet = new ArrayList<Content>();
        // Create the adapter to convert the array to views
        adapter = new TableOfContentsAdapter(dataSet);

        // Attach the adapter to a ListView
        recyclerView= (RecyclerView) findViewById(R.id.book_recycler_view);
        recyclerView.setAdapter(adapter);

        // use a GRID layout manager
        layoutManager = new GridLayoutManager(this, 1);
        // layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        addItems();
        adapter.notifyDataSetChanged();


    }

    public void addItems(){

        try{
            String[] bookTitles = getAssets().list("Books");
            for(String s : bookTitles){
                dataSet.add(new Content.Book(s, s));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
