package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookshelfFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private ContentAdapter adapter;
    private List<Content> dataSet;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    // required empty constructor
    public BookshelfFragment(){}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookshelfFragment newInstance(String param1, String param2) {
        BookshelfFragment fragment = new BookshelfFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(false);
        // Customize action bar
        getActivity().setTitle("OpenStax Commute");
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookshelf, container, false);
        Log.i("Bookshelf", "onCreateView");

        if(savedInstanceState ==null) {
            Log.i("Bookshelf", "onCreateView, savedInstanceState is null");

            // Construct the data source
            dataSet = new ArrayList<>();
            // Create the adapter to convert the array to views
            adapter = new ContentAdapter(dataSet, v -> {
                String targetId = ((TextView) v.findViewById(R.id.book_id)).getText().toString();
                String targetTitle = ((TextView) v.findViewById(R.id.book_title)).getText().toString();
                //Toast.makeText(getApplicationContext(), targetId, Toast.LENGTH_SHORT).show();

                ((HOMEFragment) getParentFragment()).sendBookInfo(targetId, targetTitle);
            });

            adapter.setContext(view.getContext());

            // Attach the adapter to a ListView
            recyclerView = view.findViewById(R.id.book_recycler_view);
            recyclerView.setAdapter(adapter);

            // use a GRID layout manager
            layoutManager = new GridLayoutManager(view.getContext(), 2);
            // layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            addItems();
            adapter.notifyDataSetChanged();
        }


        return view;


    }

    public void addItems(){

        try {
            String[] bookTitles = getContext().getAssets().list("Books");
            for (String s : bookTitles) {
                dataSet.add(new Content.Book(s, s));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookshelfFragment.OnFragmentInteractionListener) {
            mListener = (BookshelfFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Customize action bar
        (getActivity()).setTitle("OpenStax Commute");
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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

        void sendBookInfo(String bookID, String bookTitle);
    }

}
