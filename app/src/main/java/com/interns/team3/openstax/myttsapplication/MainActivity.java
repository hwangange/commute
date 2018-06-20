package com.interns.team3.openstax.myttsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

//import static com.interns.team3.openstax.myttsapplication.demo.*;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TableOfContentsAdapter adapter;
    private ArrayList<Module> dataSet;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DecimalFormat df;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        // Construct the data source
        dataSet = new ArrayList<Module>();
        // Create the adapter to convert the array to views
        adapter = new TableOfContentsAdapter(dataSet);

        // Attach the adapter to a ListView
        recyclerView= (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setAdapter(adapter);

        // use a GRID layout manager
        layoutManager = new GridLayoutManager(this, 1);
        // layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Add items to adapter
        /*Module newModule = new Module("Angela", "Hwang");
        adapter.add(newModule);

        newModule = new Module("Connie", "Wang");
        adapter.add(newModule);*/

        addItems();


        /* JSON

        // Or even append an entire new collection
        // Fetching some data, data has now returned
        // If data was JSON, convert to ArrayList of User objects.
        JSONArray jsonArray = ...;
        ArrayList<User> newUsers = User.fromJson(jsonArray)
        adapter.addAll(newUsers);
         */


        // ON CLICK LISTENER
        /*recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter, View v, int position, long id){
                // what each variable means: https://developer.android.com/reference/android/widget/AdapterView.OnItemClickListener


                String targetId = ((TextView) v.findViewById(R.id.itemID)).getText().toString();
                //Toast.makeText(getApplicationContext(), targetId, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), TextbookView.class);
                intent.putExtra("Module ID", targetId);
                startActivity(intent);
               // to put extra information: intent.putExtra(ImageTextListViewActivity.EXTRA_KMLSUMMARY, summary.get(position));
            }
        }); */


    }

    public void addItems(){


        try {
            StringBuilder buf=new StringBuilder();
            InputStreamReader inputStream = new InputStreamReader(getAssets().open("col11629_1.7_complete/collection.xml"));
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            String str;
            while ((str=bufferedReader.readLine()) != null) {
                buf.append(str);
            }
            Document doc = Jsoup.parse(buf.toString());


            String title = doc.title();

            Element body = doc.body();
            Elements subcollections = body.getElementsByTag("col:subcollection");

            float num= 0; // track chapter number

            for (Element sub : subcollections) {

                num = Math.round(Math.floor(num+1));

                String subTitle = sub.getElementsByTag("md:title").first().ownText();
                df = new DecimalFormat("#");
                String chapNum = df.format(num);

                dataSet.add(new Module(subTitle, "0", "chapter", chapNum));
                adapter.notifyDataSetChanged();
                //System.out.println("Title: " + subTitle);

                if (sub.hasText())
                    System.out.println(sub.ownText() + "\t" + sub.attributes());

                Elements modules = sub.getElementsByTag("col:module");
                for (Element mod : modules) {

                    String modTitle = mod.getElementsByTag("md:title").first().ownText();
                    if(!(modTitle.equals("Introduction"))) num += 0.1;
                    String modID = mod.attributes().get("document");

                    df = new DecimalFormat("0.#");
                    String modChapter = df.format(num);

                    dataSet.add(new Module(modTitle, modID, "module", modChapter));
                    adapter.notifyDataSetChanged();
                }


            }
        } catch(IOException e ){
            e.printStackTrace();
        }


    }

}
