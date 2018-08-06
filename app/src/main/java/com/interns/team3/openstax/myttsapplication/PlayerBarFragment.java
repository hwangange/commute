package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

//import android.speech.tts.TextToSpeech;


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
    private static final String ARG_MOD_TITLE = "param1";
    private static final String ARG_MOD_ID = "param2";
    private static final String ARG_BOOK_ID = "param3";

    public static int currentPoint = -1; static boolean isPaused = true;

    public static String modId, bookId, modTitle;

    public Context context;

    public MediaPlayer player = new MediaPlayer();

    private int length;

    private OnFragmentInteractionListener mListener;

    private LinearLayout playerBar;
    private ImageButton playButton, forwardButton, reverseButton;
    private SeekBar seekbar;
    private Handler handler = new Handler();


    private int endTime; // duration of file
    public TextbookViewFragment textbookViewFragment;

    public PlayerBarFragment() {
        // Required empty public constructor
        this.setArguments(new Bundle());
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
    public static PlayerBarFragment newInstance(String param1, String param2, String param3) {
        PlayerBarFragment fragment = new PlayerBarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOD_TITLE, param1);
        args.putString(ARG_MOD_ID, param2);
        args.putString(ARG_BOOK_ID, param3);

        modId = param2;
        Log.i("MODTITLE IS CREATED", modId);
        fragment.setArguments(args);
        currentPoint = -1;
        isPaused = false;

        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && modTitle == null) {
            modTitle = getArguments().getString(ARG_MOD_TITLE);
            Log.i("modTitle", "|" + modTitle + "|");
            modId = getArguments().getString(ARG_MOD_ID);
            bookId = getArguments().getString(ARG_BOOK_ID);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("onCreateView", "in here");
        context = getContext();

        View view = inflater.inflate(R.layout.fragment_player_bar, container, false);

        (getActivity()).setTitle("Now Playing");
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        playerBar = view.findViewById(R.id.playerBar);
        // Initialize Buttons

        playButton = (ImageButton) view.findViewById(R.id.nowPlayButton);

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               handlePlayButtonClick();
            }
        });

        forwardButton = (ImageButton) view.findViewById(R.id.nowForwardButton);
        forwardButton.setEnabled(true);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardTTS();
            }
        });

        reverseButton = (ImageButton) view.findViewById(R.id.nowReverseButton);
        reverseButton.setEnabled(true);
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverseTTS();
            }
        });

        seekbar = (SeekBar)view.findViewById(R.id.nowPlayingSeekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressvalue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressvalue = progress;
                //((TextbookViewFragment) getParentFragment()).showChange(progressvalue);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //((TextbookViewFragment) getParentFragment()).onDragStart(progressvalue);


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //((TextbookViewFragment) getParentFragment()).goScrubber(progressvalue);
                seekbar.setProgress(progressvalue);
                player.seekTo(progressvalue);


            }

        });

        // uncomment this if the slide up panel will be visible even when a module isn't selected.
       /* else{
            LinearLayout nowPlayingBar = view.findViewById(R.id.nowPlayingBar);
            seekbar = (SeekBar)view.findViewById(R.id.nowPlayingSeekbar);
            seekbar.setVisibility(View.GONE);
            nowPlayingBar.setVisibility(View.GONE);
            ImageView nowPlayingImage = view.findViewById(R.id.nowPlayingImage);
            nowPlayingImage.setVisibility(View.GONE);
        } */

        // Inflate the layout for this fragment

        textbookViewFragment = ((MainActivity)getActivity()).getTextbookViewFragment();
        return view;
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public void playMergedFile(String output) {
        player.reset();

        Uri uri = Uri.parse("file://" + output);

        player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());

        try {
            player.setDataSource(getContext(), uri);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setPlayButton("Pause");
                player.start();
                endTime = player.getDuration();
                seekbar.setMax(endTime);
                seekbar.setProgress(0);
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("onCompletion", "When is media player stopped?");
                handlePlayButtonClick(); //alternative: setPlayButton("Play");
                forwardButton.setEnabled(false);
                reverseButton.setEnabled(false);

                // go back to the beginning
                activeIndexPosition = 0;
                player.seekTo(0);

                //player.stop();

            }
        });

        handler.postDelayed(UpdateAudioTime,100);
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

    //Called by player bar fragment
    public void pauseTTS() {

        player.pause();
        length = player.getCurrentPosition();


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setPlayButton("Play");
            }
        });
    }

    //Called by player bar fragment
    public void stopTTS(){
        player.stop();
        //player.release(); <-- this would end the media player life cycle.

        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setPlayButton("Play");
            }
        }); */
    }

    //Called by player bar fragment
    public void forwardTTS(){


        length = player.getCurrentPosition();
        player.seekTo(length+15000);
    }

    //Called by player bar fragment
    public void reverseTTS(){

        length = player.getCurrentPosition();
        player.seekTo(length-15000);
    }


    public void handlePlayButtonClick(){
        if (!forwardButton.isEnabled()) forwardButton.setEnabled(true);
        if (!reverseButton.isEnabled()) reverseButton.setEnabled(true);

        ImageView dragViewPlayButton = getActivity().findViewById(R.id.dragViewPlayButton);

        if (playButton.getTag().equals("Play")) {

            playButton.setTag("Pause");
            playButton.setImageResource(R.drawable.pause);
            dragViewPlayButton.setImageResource(R.drawable.pause);

            player.seekTo(player.getCurrentPosition() - 1);
            player.start();

        } else {
            playButton.setTag("Play");
            playButton.setImageResource(R.drawable.play);
            dragViewPlayButton.setImageResource(R.drawable.play);

            // Pause
            pauseTTS();
        }
    }

    public void setPlayButton(String s){
        ImageView dragViewPlayButton = getActivity().findViewById(R.id.dragViewPlayButton);

        if(s.equals("Play"))
        {
            playButton.setTag("Play");
            playButton.setImageResource(R.drawable.play);
            dragViewPlayButton.setImageResource(R.drawable.play);
        }
        else{

            playButton.setTag("Pause");
            playButton.setImageResource(R.drawable.pause);
            dragViewPlayButton.setImageResource(R.drawable.pause);
        }
    }

    // when user wants to listen to a new audiobook
    public void setNewModule(String bookId, String modId, String modTitle){
        this.bookId = bookId;
        this.modId = modId;
        this.modTitle = modTitle;
        currentPoint = -1;
        isPaused = false;
        context = getContext();

    }


    @Override
    public void onPause(){
        super.onPause();

        if(modId != "" && player != null) {
            try {
                currentPoint = player.getCurrentPosition();
                Log.i("Current point upon pausing", String.valueOf(currentPoint));
               /* if (playButton.getTag().equals("Play")) {
                    isPaused = true;
                } else {
                    isPaused = false;
                }*/
            } catch (Exception e) {
                e.printStackTrace();
                currentPoint = -1;
            }

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // use this method if you want to do anything once the fragment is back on the screen
        Log.i("onViewStateRestored", "in here");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("onDetach", "in here");
        mListener = null;
    }

    public String getModule() {
        return modId;
    }

    public void setVisible(boolean boo){

        if(boo)
        {
            playerBar.setVisibility(View.VISIBLE);
        }
        else playerBar.setVisibility(View.GONE);
    }

    private int activeIndexPosition = 0;

    private Runnable UpdateAudioTime = new Runnable() {
        public void run() {

            currentPoint = player.getCurrentPosition();


            if(((MainActivity)getActivity()).getActiveFragment() instanceof TextbookViewFragment)
            {
                textbookViewFragment = (TextbookViewFragment) ((MainActivity)getActivity()).getActiveFragment();
                int currentIndexPosition = textbookViewFragment.getPositionAt(currentPoint);
                if(currentIndexPosition != activeIndexPosition && currentIndexPosition != -1){
                    if(textbookViewFragment.getDataSet().size() != 0 ) {
                        // unhighlight text
                        textbookViewFragment.unhighlightText(activeIndexPosition);

                        // set new active text
                        activeIndexPosition = currentIndexPosition;
                        textbookViewFragment.setSelected(activeIndexPosition);
                        textbookViewFragment.checkIfVisible(activeIndexPosition);
                    }
                }

            }

            seekbar.setProgress((int) currentPoint);
            handler.postDelayed(this, 100);
        }
    };

    public int getDuration(){
        if(player != null)
            return player.getDuration();
        else return -1;
    }

    public MediaPlayer getPlayer(){
        return player;
    }


}
