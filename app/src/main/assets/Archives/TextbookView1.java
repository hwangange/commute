package com.interns.team3.openstax.myttsapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TextbookView1 extends AppCompatActivity {


    public static String modId;
    public static Document content;
    public static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textbook_view1);

        Intent intent = getIntent();
        modId = intent.getStringExtra("Module ID");
       // Toast.makeText(getApplicationContext(), modId, Toast.LENGTH_SHORT).show();

        content = getContent();

        textView = (TextView) findViewById(R.id.content);
        textView.setText(Html.fromHtml(content.html(), Html.FROM_HTML_MODE_COMPACT));


    }


    public Document getContent() {

        try {

            String fileName = "col11629_1.7_complete/"+modId+"/index.cnxml.html";
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


}
