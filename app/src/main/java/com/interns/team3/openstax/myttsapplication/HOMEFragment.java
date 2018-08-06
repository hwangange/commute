package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HOMEFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HOMEFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HOMEFragment extends Fragment implements BookshelfFragment.OnFragmentInteractionListener, TableOfContentsFragment.OnFragmentInteractionListener, TextbookViewFragment.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;

    public Context context;

    public FragmentManager fragmentManager;
    public BookshelfFragment bookshelfFragment;
    public TableOfContentsFragment tableOfContentsFragment;
    public TextbookViewFragment textbookViewFragment;

    public HOMEFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param c Parameter 1.
     * @return A new instance of fragment HOMEFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HOMEFragment newInstance(Context c) {
        HOMEFragment fragment = new HOMEFragment();
        fragment.setContext(c);
        return fragment;
    }

    public void setContext(Context c){ context = c;}

    public boolean newInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nested fragments
        fragmentManager = getChildFragmentManager();
        Log.i("Home", "in onCreate");

        newInstance= true; // assuming onCreate only gets called when this fragment is FIRST created.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Log.i("HOME", "onCreateVIew and newInstance is: " + String.valueOf(newInstance));

        if(newInstance) {
            newInstance = false;
            Log.i("Creating new bookshelf", "uh");
            //Add Bookshelf fragment to this activity
            FragmentTransaction ft = fragmentManager.beginTransaction();
            bookshelfFragment = BookshelfFragment.newInstance("", "");
            ft.replace(R.id.homeFragmentContainer, bookshelfFragment);
            ft.addToBackStack(null); // allow user to go back
            ft.commit();
        }


        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    }

    public void onFragmentInteraction(Uri uri){
        Log.i("onFragmentInteraction", uri.toString());
    }

    public void sendBookInfo(String bookID, String bookTitle){
        // from Bookshelf fragment to Table of Contents fragment

        Log.i("sendBookInfo", bookID + ", " + bookTitle);
        TableOfContentsFragment tableOfContentsFragment = TableOfContentsFragment.newInstance(bookTitle, bookID);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.homeFragmentContainer, tableOfContentsFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.commit();
    }

    public void sendModuleInfo(String bookTitle, String bookID, String modID, String modTitle){
        // from Table of Contents fragment to TextbookView Fragment
        Log.i("sendModuleInfo", bookTitle + ", " + bookID + ", " + modID + ", " + modTitle);
        TextbookViewFragment textbookViewFragment = TextbookViewFragment.newInstance(modTitle, modID, bookID);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.homeFragmentContainer, textbookViewFragment);
        ft.addToBackStack(null); // allow user to go back
        ft.commit();
    }

    public void playEntireModule(String bookTitle, String modID, String modTitle){
        ((MainActivity)getActivity()).playEntireModule(bookTitle, modID, modTitle);
    }

    public void onRecyclerViewCreated(RecyclerView recyclerView){
        ((MainActivity)getActivity()).onRecyclerViewCreated(recyclerView);

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("Heh", "Kek");
        Log.i("onSaveInstanceState", "this is fking annoying");
    }
}
