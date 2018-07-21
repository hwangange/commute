package com.interns.team3.openstax.myttsapplication;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OldPlayerBarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OldPlayerBarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OldPlayerBarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MAX_PROGRESS = "param1";
    private static final String PROGRESS = "param2";

    // TODO: Rename and change types of parameters
    private int maxProgress, progress;

    private OnFragmentInteractionListener mListener;
    private ImageButton playButton, stopButton, forwardButton, reverseButton, volumeButton;
    private SeekBar seekbar;

    private TextToSpeech tts;
    private FragmentManager fm;
    private VolumeFragment volumeFragment;
    private int volume;

    public OldPlayerBarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param maxProgress
     * @return A new instance of fragment PlayerBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OldPlayerBarFragment newInstance(int maxProgress) {
        OldPlayerBarFragment fragment = new OldPlayerBarFragment();
        Bundle args = new Bundle();
        args.putInt(MAX_PROGRESS, maxProgress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.maxProgress = getArguments().getInt(MAX_PROGRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.old_fragment_player_bar, container, false);

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

                    int selected = ((TextbookViewFragment)getParentFragment()).getSelected();
                    ((TextbookViewFragment)getParentFragment()).checkIfVisible(selected);
                }
                else{
                    playButton.setTag("Play");
                    playButton.setImageResource(R.drawable.play);

                    // Pause
                    ((TextbookViewFragment) getParentFragment()).pauseTTS();
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
                ((TextbookViewFragment) getParentFragment()).stopTTS();
            }
        });

        forwardButton = (ImageButton) view.findViewById(R.id.forwardButton);
        forwardButton.setEnabled(false);
        forwardButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((TextbookViewFragment) getParentFragment()).forwardTTS();
            }
        });

        reverseButton = (ImageButton) view.findViewById(R.id.reverseButton);
        reverseButton.setEnabled(false);
        reverseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((TextbookViewFragment) getParentFragment()).reverseTTS();
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

        seekbar = (SeekBar)view.findViewById(R.id.seekbar);
        seekbar.setMax(maxProgress);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressvalue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressvalue = progress;
                if(fromUser)
                    ((TextbookViewFragment) getParentFragment()).showChange(progressvalue);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((TextbookViewFragment) getParentFragment()).onDragStart(progressvalue);


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((TextbookViewFragment) getParentFragment()).onDragStop(progressvalue);

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

    public void setSeekbarProgress(int progress){
        seekbar.setProgress(progress);
    }

    public void setSeekbarMax(int max){
        seekbar.setMax(max);
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
