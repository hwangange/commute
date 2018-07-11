package com.interns.team3.openstax.myttsapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerBarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerBarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerBarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageButton playButton, stopButton, forwardButton, reverseButton, volumeButton;

    private TextToSpeech tts;
    private FragmentManager fm;
    private VolumeFragment volumeFragment;
    private int volume;

    public PlayerBarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerBarFragment newInstance(String param1, String param2) {
        PlayerBarFragment fragment = new PlayerBarFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player_bar, container, false);

        fm = getChildFragmentManager();

        playButton = (ImageButton)view.findViewById(R.id.playButton);
        playButton.setTag("Play");
        playButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                if(!stopButton.isEnabled()) stopButton.setEnabled(true);
                if(!forwardButton.isEnabled()) forwardButton.setEnabled(true);
                if(!reverseButton.isEnabled()) reverseButton.setEnabled(true);
                if(playButton.getTag().equals("Play"))

                {

                    playButton.setTag("Pause");
                    playButton.setImageResource(R.drawable.pause);

                    int selected = ((TextbookView)getActivity()).getSelected();
                    ((TextbookView)getActivity()).checkIfVisible(selected);
                }
                else{
                    playButton.setTag("Play");
                    playButton.setImageResource(R.drawable.play);

                    // Pause
                    ((TextbookView) getActivity()).pauseTTS();
                }
            }
        });

        stopButton = (ImageButton) view.findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                stopButton.setEnabled(false);
                forwardButton.setEnabled(false);
                reverseButton.setEnabled(false);
                ((TextbookView) getActivity()).stopTTS();
            }
        });

        forwardButton = (ImageButton) view.findViewById(R.id.forwardButton);
        forwardButton.setEnabled(false);
        forwardButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((TextbookView) getActivity()).forwardTTS();
            }
        });

        reverseButton = (ImageButton) view.findViewById(R.id.reverseButton);
        reverseButton.setEnabled(false);
        reverseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((TextbookView) getActivity()).reverseTTS();
            }
        });

        volume = 50;
        volumeButton = (ImageButton) view.findViewById(R.id.volumeButton);
        volumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //Add player bar fragment to this activity
                volumeFragment = VolumeFragment.newInstance(volume,"");
                volumeFragment.show(fm, "Volume");
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setVolume(int val){
        volume = val;
    }

    public void setPlayButton(String s){
        if(s.equals("Play"))

        {

            playButton.setTag("Play");
            playButton.setImageResource(R.drawable.play);
        }
        else{
            playButton.setTag("Pause");
            playButton.setImageResource(R.drawable.pause);
        }
    }

    public void setStopButton(boolean boo){ stopButton.setEnabled(boo);}

    public void setForwardButton(boolean boo) {
        forwardButton.setEnabled(boo);}

    public void setReverseButton(boolean boo) {
        reverseButton.setEnabled(boo);}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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


}
