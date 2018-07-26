package com.interns.team3.openstax.myttsapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.interns.team3.openstax.myttsapplication.demo.*;


public class TableOfContentsFragment extends Fragment {

    private static final String ARG_BOOK_TITLE = "param1";
    private static final String ARG_BOOK_ID = "param2";
    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    private LinearLayout loading, doneLoading;
    private TextView details, bookTitleTv, bookDate;
    private ImageView bookImg;
    private ContentAdapter adapter;
    private List<Content> dataSet;

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
        dataSet = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new ContentAdapter(dataSet, new ContentAdapter.ContentOnClickListener() {
            @Override
            public void onClick(View v) {
                String modId = ((TextView) v.findViewById(R.id.modID)).getText().toString();
                String modTitle = ((TextView) v.findViewById(R.id.modTitle)).getText().toString();
                //Toast.makeText(getApplicationContext(), targetId, Toast.LENGTH_SHORT).show();


                ((MainActivity)getActivity()).playEntireModule(bookTitle, modId, modTitle);
                //((HOMEFragment)getParentFragment()).sendModuleInfo(bookId, bookTitle, modId, modTitle);
            }
        });

        // Attach the adapter to a ListView
        recyclerView= view.findViewById(R.id.my_recycler_view);
        recyclerView.setAdapter(adapter);

        layoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false); // fix unsmooth scrolling?

        (getActivity()).setTitle(bookTitle);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // miscellaneous things
        loading = view.findViewById(R.id.loading);
        doneLoading = view.findViewById(R.id.doneLoading);
        details = view.findViewById(R.id.details);
        bookTitleTv = view.findViewById(R.id.bookTitleTv);
        bookImg = view.findViewById(R.id.bookImg);
        bookDate = view.findViewById(R.id.bookDate);

        loading.setVisibility(View.VISIBLE);
        doneLoading.setVisibility(View.GONE);
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


    private class AddItemsTask extends AsyncTask<String, Integer, Map <String, String>> {

        List<Content> temp = new ArrayList<Content>();

        @Override
        protected Map<String, String> doInBackground(String... ary) {
            try {
                AudioBook book = new AudioBook(getContext(), bookId);
                Map<String, String> metadata = book.metadata();
                Elements chapters = book.getChapters();

                int chapterNum = 1;
                for (Element chapter : chapters) {
                    String subTitle = chapter.getElementsByTag("md:title").first().ownText();
                    temp.add(new Content.Chapter(subTitle, "0", String.valueOf(chapterNum)));
                    Elements modules = book.getChapterModules(chapter);

                    int modNum = 0;
                    for (Element mod : modules) {
                        String chapModNum = chapterNum + "." + modNum;
                        temp.add(book.getModule(mod, chapModNum));
                        modNum++;
                    }
                    chapterNum++;
                }

                return metadata;
            } catch(Exception e ){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... result) {


        }

        @Override
        protected void onPostExecute(Map<String, String> metadata) {
            for(Content c : temp) {
                if (c instanceof Content.Module) {
                    dataSet.add(new Content.Module((Content.Module) c));
                } else if (c instanceof Content.Chapter) {
                    dataSet.add(new Content.Chapter((Content.Chapter) c));
                }
                adapter.notifyDataSetChanged();
            }

            String revised = "Last Revised: " + metadata.get("revised");
            String title = metadata.get("title");
            String summary = metadata.get("summary");
            bookDate.setText(revised);
            bookTitleTv.setText(title);
            details.setText(summary);

            String modified_title= title.replaceAll(" ", "_").replaceAll("\\.", "").toLowerCase();
            Log.i("modified_title T_T", modified_title);
            int drawable_id = getContext().getResources().getIdentifier(modified_title, "drawable", getContext().getPackageName());
            Picasso.with(getContext()).load(drawable_id).into(bookImg);

            loading.animate()
                    .translationY(loading.getHeight())
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            loading.setVisibility(View.GONE);
                            doneLoading.animate()
                                    .translationY(0)
                                    .alpha(1.0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            doneLoading.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }
                    });

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
