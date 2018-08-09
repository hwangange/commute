package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class BookshelfFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private SectionedRecyclerViewAdapter sectionedAdapter;


    private RecyclerView recyclerView;


    private HashMap<String, ArrayList<AudioBook>> genres;

    private NestedScrollView nestedScrollView;

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
        getActivity().setTitle("OpenStax On the Go");
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookshelf, container, false);

        if(savedInstanceState ==null || genres == null) {
            Log.i("Bookshelf", "onCreateView, savedInstanceState or list 'genres' is null");

            // Construct the data source
            genres = new HashMap<String, ArrayList<AudioBook>>();

            // Create the adapter to convert the array to views
            sectionedAdapter = new SectionedRecyclerViewAdapter();

            // Attach the adapter to a ListView
            recyclerView = view.findViewById(R.id.book_recycler_view);
            recyclerView.setAdapter(sectionedAdapter);
            recyclerView.setNestedScrollingEnabled(false); // fix unsmooth scrolling?

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);

            addItems();
            sectionedAdapter.notifyDataSetChanged();

            // automatically "scroll" to top upon first opening
            nestedScrollView = view.findViewById(R.id.nested_scrollview);
            nestedScrollView.setFocusableInTouchMode(true);
            nestedScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        } else {
            recyclerView = view.findViewById(R.id.book_recycler_view);
            recyclerView.setAdapter(sectionedAdapter);
            recyclerView.setNestedScrollingEnabled(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            sectionedAdapter.notifyDataSetChanged();
        }


        return view;


    }

    public void addItems(){

        try {
            String[] bookTitles = getContext().getAssets().list("Books");
            for (String s : bookTitles) {
                AudioBook book = new AudioBook(getContext(), s);
                String genre = book.metadata().get("subject");

                if(genres.containsKey(genre)){
                   // ArrayList<AudioBook> list = ((GenreSection) sectionedAdapter.getSection(genre)).getList();
                    ArrayList<AudioBook> list = genres.get(genre);
                    list.add(book);
                    genres.put(genre, list);

                }
                else {
                    ArrayList<AudioBook> list = new ArrayList<AudioBook>();
                    list.add(book);
                    //sectionedAdapter.addSection(genre, new GenreSection(genre, list));
                    genres.put(genre, list);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String genre : new TreeSet<String>(genres.keySet())){
            sectionedAdapter.addSection(genre, new GenreSection(genre, genres.get(genre)));
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
        (getActivity()).setTitle("OpenStax On the Go");
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

    public class GenreSection extends StatelessSection {
        String title;
        ArrayList<AudioBook> list;
        ShelfAdapter shelfAdapter;

        GenreSection(String title, ArrayList<AudioBook> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.scrollable_shelf)
                    .headerResourceId(R.layout.shelf_header)
                    .build());

            this.title = title;
            this.list = list;
        }

        public ArrayList<AudioBook> getList() {return list; }

        @Override
        public int getContentItemsTotal() {
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new BookshelfFragment.ShelfHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final BookshelfFragment.ShelfHolder linearLayoutHolder = (BookshelfFragment.ShelfHolder) holder;

            RecyclerView shelf = linearLayoutHolder.getShelf();
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            shelfAdapter = new ShelfAdapter(list);
            shelf.setAdapter(shelfAdapter);
            shelf.setLayoutManager(layoutManager);

            shelfAdapter.notifyDataSetChanged();

        }

        public void notifyChange(){
            shelfAdapter.notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new BookshelfFragment.HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            BookshelfFragment.HeaderViewHolder headerHolder = (BookshelfFragment.HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        }
    }

    private class ShelfHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final RecyclerView recyclerView;

        ShelfHolder(View view) {
            super(view);

            rootView = view;
            recyclerView = view.findViewById(R.id.shelf);
        }

        public RecyclerView getShelf() {return recyclerView;}
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView cover;
        private final TextView id;
        private final TextView title;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            cover = (ImageView) view.findViewById(R.id.book_img);
            id = (TextView) view.findViewById(R.id.book_id);
            title = (TextView) view.findViewById(R.id.book_title);
        }

        public ImageView getBookImage(){ return cover; }
        public TextView getBookId() {return id;}
        public TextView getBookTitle() {return title;}
    }

    private class ShelfAdapter extends RecyclerView.Adapter {

        public List<AudioBook> dataSet;

        // Provide a suitable constructor (depends on the kind of dataSet)
        public ShelfAdapter(List<AudioBook> dataSet) {
            this.dataSet = dataSet;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.textbook_card, parent, false);

            return new ItemViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            final BookshelfFragment.ItemViewHolder itemHolder = (BookshelfFragment.ItemViewHolder) holder;


            TextView myBookTitle = itemHolder.getBookTitle();
            TextView myBookId = itemHolder.getBookId();
            ImageView myBookImg = itemHolder.getBookImage();

            AudioBook book = dataSet.get(position);
            String title= book.getBookName();
            String id = book.getBookName();

            String modified_title= title.replaceAll(" ", "_").replaceAll("\\.", "").toLowerCase();
            int drawable_id = getContext().getResources().getIdentifier(modified_title, "drawable", getContext().getPackageName());

            myBookTitle.setText(title);
            myBookId.setText(id);
            Picasso.with(getContext()).load(drawable_id).into(myBookImg);


            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HOMEFragment) getParentFragment()).sendBookInfo(id, title);
                }
            });


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder vh) {
            // view appears.
            //Log.wtf(TAG,"onViewRecycled "+vh);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder viewHolder) {
            // view leaves.
            //Log.wtf(TAG,"onViewDetachedFromWindow "+viewHolder);
        }


    }




}
