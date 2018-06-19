package com.interns.team3.openstax.myttsapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

//import static com.interns.team3.openstax.myttsapplication.demo.*;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView listView;
    private CustomAdapter adapter;
    private ArrayList<Module> list;

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
        list = new ArrayList<Module>();
        // Create the adapter to convert the array to views
        adapter = new CustomAdapter(list, this);

        // Attach the adapter to a ListView
        listView= (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        // Add items to adapter
        /*Module newModule = new Module("Angela", "Hwang");
        adapter.add(newModule);

        newModule = new Module("Connie", "Wang");
        adapter.add(newModule);*/

        addItems(adapter);

        /* JSON

        // Or even append an entire new collection
        // Fetching some data, data has now returned
        // If data was JSON, convert to ArrayList of User objects.
        JSONArray jsonArray = ...;
        ArrayList<User> newUsers = User.fromJson(jsonArray)
        adapter.addAll(newUsers);
         */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
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
        });


    }

    public void addItems(CustomAdapter adapter){


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
            for (Element sub : subcollections) {
                String subTitle = sub.getElementsByTag("md:title").first().ownText();
                System.out.println("Title: " + subTitle);
                if (sub.hasText())
                    System.out.println(sub.ownText() + "\t" + sub.attributes());

                Elements modules = sub.getElementsByTag("col:module");
                for (Element mod : modules) {

                    String modTitle = mod.getElementsByTag("md:title").first().ownText();
                    String modID = mod.attributes().get("document");
                    adapter.add(new Module(modTitle, modID));

                }


            }
        } catch(IOException e ){
            e.printStackTrace();
        }


    }

}
