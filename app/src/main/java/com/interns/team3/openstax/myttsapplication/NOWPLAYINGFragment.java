package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NOWPLAYINGFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NOWPLAYINGFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NOWPLAYINGFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MOD_TITLE = "param1";
    private static final String ARG_MOD_ID = "param2";
    private static final String ARG_BOOK_ID = "param3";
    private static final String ARG_CONTEXT = "param4";

    public static int currentPoint = -1; static boolean isPaused;

    public static String modId, bookId, modTitle;
    public TextView titleView;
    public Context context;

    public MediaPlayer player = new MediaPlayer();

    private int length;

    private OnFragmentInteractionListener mListener;

    private ImageButton playButton, forwardButton, reverseButton;

    public NOWPLAYINGFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NOWPLAYINGFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NOWPLAYINGFragment newInstance(String param1, String param2, String param3, Context param4) {
        NOWPLAYINGFragment fragment = new NOWPLAYINGFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOD_TITLE, param1);
        args.putString(ARG_MOD_ID, param2);
        args.putString(ARG_BOOK_ID, param3);

        modId = param2;
        Log.i("MODTITLE IS CREATED", modId);
        fragment.setContext(param4);
        fragment.setArguments(args);
        currentPoint = -1;
        isPaused = false;

        return fragment;
    }

    public void setContext(Context c){ context = c;}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modTitle = getArguments().getString(ARG_MOD_TITLE);
            Log.i("modTitle", "|" + modTitle + "|");
            modId = getArguments().getString(ARG_MOD_ID);
            bookId = getArguments().getString(ARG_BOOK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nowplaying, container, false);

        titleView = view.findViewById(R.id.nowPlayingTitle);
        titleView.setText(modTitle);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);

        (getActivity()).setTitle("Now Playing");
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        // buttons

        if(modId !="") {

            playButton = (ImageButton) view.findViewById(R.id.nowPlayButton);

            playButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!forwardButton.isEnabled()) forwardButton.setEnabled(true);
                    if (!reverseButton.isEnabled()) reverseButton.setEnabled(true);

                    if (playButton.getTag().equals("Play")) {

                        playButton.setTag("Pause");
                        playButton.setImageResource(R.drawable.pause);
                        player.seekTo(player.getCurrentPosition() - 1);
                        player.start();

                    } else {
                        playButton.setTag("Play");
                        playButton.setImageResource(R.drawable.play);

                        // Pause
                        pauseTTS();
                    }
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

            Log.i("Current Point", String.valueOf(currentPoint));

            if(currentPoint == -1) {
                //player = new MediaPlayer(); <-- shouldn't be needed if player is "reset" in "playMergedFile"
                setPlayButton("Pause");
                String output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output" + modId + ".mp3";
                playMergedFile(output);

            } else {
                boolean isPlaying = false;
                try{ isPlaying= player.isPlaying(); } catch(Exception e) {e.printStackTrace();}

                // isPlaying is true regardless of whether player was "playing" or "paused"
                if(isPlaying)
                {
                    Log.i("isPlaying", "Meaning playButton was 'play'");
                    setPlayButton("Pause");
                    player.seekTo(player.getCurrentPosition());
                }

                else
                {
                    Log.i("NOT isPlaying", "Meaning playButton was 'pause'");
                    setPlayButton("Play");
                    player.seekTo(currentPoint);
                }
                currentPoint = -1;
            }

        }
        else{
            LinearLayout nowPlayingBar = view.findViewById(R.id.nowPlayingBar);
            nowPlayingBar.setVisibility(View.GONE);
            ImageView nowPlayingImage = view.findViewById(R.id.nowPlayingImage);
            nowPlayingImage.setVisibility(View.GONE);
        }

        // Inflate the layout for this fragment
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
            player.setDataSource(context, uri);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setPlayButton("Pause");
                player.start();
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("onCompletion", "When is media player stopped?");
                //player.stop();

            }
        });
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

    // when user wants to listen to a new audiobook
    public void setNewModule(String bookId, String modId, String modTitle){
        this.bookId = bookId;
        this.modId = modId;
        this.modTitle = modTitle;
        currentPoint = -1;
        isPaused = false;


    }


    @Override
    public void onPause(){
        super.onPause();

        if(modId != "" && player != null) {
            try {
                currentPoint = player.getCurrentPosition();
                Log.i("Current point upon pausing", String.valueOf(currentPoint));
                if (playButton.getTag().equals("Play")) {
                    isPaused = true;
                } else {
                    isPaused = false;
                }
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

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public String getModule() {
        return modId;
    }


}
