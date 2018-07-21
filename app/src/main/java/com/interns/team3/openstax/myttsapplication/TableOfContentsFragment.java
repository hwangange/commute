package com.interns.team3.openstax.myttsapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class TableOfContentsFragment extends Fragment {

    private static final String ARG_BOOK_TITLE = "param1";
    private static final String ARG_BOOK_ID = "param2";
    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    private TextView mTextMessage;
    private ContentAdapter adapter;
    private ArrayList<Content> dataSet;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DecimalFormat df;

    private String bookId, bookTitle;

    // required empty constructor
    public TableOfContentsFragment(){}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TableOfContentsFragment newInstance(String param1, String param2) {
        TableOfContentsFragment fragment = new TableOfContentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_TITLE, param1);
        args.putString(ARG_BOOK_ID, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                bookTitle = getArguments().getString(ARG_BOOK_TITLE);
                bookId = getArguments().getString(ARG_BOOK_ID);
            }
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Log.i("Table of contents", "onCreateview");

            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_toc, container, false);


       /*  BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener); */


        // Construct the data source
        dataSet = new ArrayList<Content>();
        // Create the adapter to convert the array to views
        adapter = new ContentAdapter(dataSet, new ContentAdapter.ContentOnClickListener() {
            @Override
            public void onClick(View v) {
                String modId = ((TextView) v.findViewById(R.id.modID)).getText().toString();
                String modTitle = ((TextView) v.findViewById(R.id.modTitle)).getText().toString();
                //Toast.makeText(getApplicationContext(), targetId, Toast.LENGTH_SHORT).show();


                ((MainActivity)getActivity()).playMergedFile(bookTitle, modId, modTitle);
                //((HOMEFragment)getParentFragment()).sendModuleInfo(bookId, bookTitle, modId, modTitle);
            }
        });

        // Attach the adapter to a ListView
        recyclerView= (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setAdapter(adapter);

        layoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        (getActivity()).setTitle(bookTitle);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new AddItemsTask().execute("");


        /* JSON

        // Or even append an entire new collection
        // Fetching some data, data has now returned
        // If data was JSON, convert to ArrayList of User objects.
        JSONArray jsonArray = ...;
        ArrayList<User> newUsers = User.fromJson(jsonArray)
        adapter.addAll(newUsers);
         */

        return view;


    }


    private class AddItemsTask extends AsyncTask<String, Integer, String> {

        ArrayList<Content> temp = new ArrayList<Content>();

        @Override
        protected String doInBackground(String... ary) {

            try {
                StringBuilder buf=new StringBuilder();
                InputStreamReader inputStream = new InputStreamReader((getActivity()).getAssets().open("Books/"+bookId+"/collection.xml"));
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

                    temp.add(new Content.Chapter(subTitle, "0", chapNum));

                    Elements modules = sub.getElementsByTag("col:module");
                    for (Element mod : modules) {

                        String modTitle = mod.getElementsByTag("md:title").first().ownText();
                        if(!(modTitle.equals("Introduction"))) num += 0.1;
                        String modID = mod.attributes().get("document");

                        df = new DecimalFormat("0.#");
                        String modChapter = df.format(num);

                        temp.add(new Content.Module(modTitle, modID, modChapter));
                    }


                }
            } catch(IOException e ){
                e.printStackTrace();
            }


            return "done";
        }

        @Override
        protected void onProgressUpdate(Integer... result) {


        }

        @Override
        protected void onPostExecute(String result) {
            for(Content c : temp){
                if(c instanceof Content.Module) dataSet.add(new Content.Module((Content.Module) c));
                else if(c instanceof Content.Chapter) dataSet.add(new Content.Chapter((Content.Chapter)c));
                adapter.notifyDataSetChanged();
            }
            Log.i("onPostExecute", "Done");
        }
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
    }

}
