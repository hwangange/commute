package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "Settings";

    private static final String KEY_SELECTED_VOICE_POSITION = "SelectedVoicePosition";
    private static final String KEY_VOICES = "Voices";
    private static final String KEY_SAMPLE_TEXT = "SampleText";

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // Amazon Polly permissions.
    private static final String COGNITO_POOL_ID = "us-east-2:055c2658-ed41-4198-b2d7-2b343fdf739a";

    // Region of Amazon Polly.
    private static final Regions MY_REGION = Regions.US_EAST_2;

    CognitoCachingCredentialsProvider credentialsProvider;

    private AmazonPollyPresigningClient client;
    private List<Voice> voices;

    private ProgressBar voicesProgressBar;
    private SeekBar volumeSeekBar;


    private Spinner voicesSpinner;
    private String sampleText = "Team 3 worked extremely hard on their project!";
    private Button playButton, saveButton;

    private int selectedPosition;

    MediaPlayer mediaPlayer;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private class SpinnerVoiceAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<Voice> voices;

        SpinnerVoiceAdapter(Context ctx, List<Voice> voices) {
            this.inflater = LayoutInflater.from(ctx);
            this.voices = voices;
        }

        @Override
        public int getCount() {
            return voices.size();
        }

        @Override
        public Object getItem(int position) {
            return voices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.voice_spinner_row, parent, false);
            }
            Voice voice = voices.get(position);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.voiceName);
            nameTextView.setText(voice.getName());

            TextView languageCodeTextView = (TextView) convertView.findViewById(R.id.voiceLanguageCode);
            languageCodeTextView.setText(voice.getLanguageName() +
                    " (" + voice.getLanguageCode() + ")");

            return convertView;
        }
    }

    private class GetPollyVoices extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (voices != null) {
                return null;
            }

            // Create describe voices request.
            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

            DescribeVoicesResult describeVoicesResult;
            try {
                // Synchronously ask the Polly Service to describe available TTS voices.
                describeVoicesResult = client.describeVoices(describeVoicesRequest);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unable to get available voices. " + e.getMessage());
                return null;
            }

            // Get list of voices from the result.
            voices = describeVoicesResult.getVoices();

            // Log a message with a list of available TTS voices.
            Log.i(TAG, "Available Polly voices: " + voices);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (voices == null) {
                return;
            }

            voicesSpinner.setAdapter(new SpinnerVoiceAdapter(getContext(), voices));

            voicesProgressBar.setVisibility(View.INVISIBLE);
            voicesSpinner.setVisibility(View.VISIBLE);

            voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (view == null) {
                        return;
                    }

                    // usually settings the default text in an EditText
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Restore previously selected voice (e.g. after screen orientation change).
            voicesSpinner.setSelection(selectedPosition);

            playButton.setEnabled(true);

            getPreferences();
        }
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        (getActivity()).setTitle("Settings");
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        voicesProgressBar = view.findViewById(R.id.voicesProgressBar);
        playButton = (Button) view.findViewById(R.id.readButton);
        voicesSpinner = (Spinner) view.findViewById(R.id.voicesSpinner);
        volumeSeekBar = (SeekBar) view.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(12);
        saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                saveSettings();
            }
        });

        initPollyClient();
        setupNewMediaPlayer();
        setupPlayButton();
        setupVoicesSpinner();


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

    void initPollyClient() {
        // Initialize the Amazon Cognito credentials provider.
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        // Create a client that supports generation of presigned URLs.
        client = new AmazonPollyPresigningClient(credentialsProvider);
    }

    void setupNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                playButton.setEnabled(true);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                playButton.setEnabled(true);
                return false;
            }
        });
    }

    void setupVoicesSpinner() {
        voicesProgressBar.setVisibility(View.VISIBLE);

        // Asynchronously get available Polly voices.
        new GetPollyVoices().execute();
    }

    void setupPlayButton() {
        playButton.setEnabled(false);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setEnabled(false);

                Voice selectedVoice = (Voice) voicesSpinner.getSelectedItem();
                int volume = volumeSeekBar.getProgress() - 6;
                String decibelString = String.valueOf(volume)+"dB";

                String ssmlSampleText = "<speak><prosody volume=\"" +decibelString +"\">" + sampleText + "</prosody></speak>";
                Log.i("New Sample Text", ssmlSampleText);

                // Create speech synthesis request.
                SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                        new SynthesizeSpeechPresignRequest()
                                // Set text to synthesize.
                                .withText(ssmlSampleText).withTextType("ssml")
                                // Set voice selected by the user.
                                .withVoiceId(selectedVoice.getId())
                                // Set format to MP3.
                                .withOutputFormat(OutputFormat.Mp3);

                new SpeakSampleTextTask().execute(synthesizeSpeechPresignRequest);
                /*
                // Get the presigned URL for synthesized speech audio stream.
                URL presignedSynthesizeSpeechUrl =
                        client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

                Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

                // Create a media player to play the synthesized audio stream.
                if (mediaPlayer.isPlaying()) {
                    setupNewMediaPlayer();
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    // Set media player's data source to previously obtained URL.
                    mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
                } catch (IOException e) {
                    Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
                }

                // Start the playback asynchronously (since the data source is a network stream).
                mediaPlayer.prepareAsync();
                */
            }
        });
    }

    private class SpeakSampleTextTask extends AsyncTask<SynthesizeSpeechPresignRequest, Void, URL> {

        protected URL doInBackground(SynthesizeSpeechPresignRequest... requests) {
            // Get the presigned URL for synthesized speech audio stream.
            SynthesizeSpeechPresignRequest synthReq = requests[0];
            URL synthResURL = client.getPresignedSynthesizeSpeechUrl(synthReq); // formerly, this.client...
            return synthResURL;

        }

        protected void onProgressUpdate() {
        }

        protected void onPostExecute(URL presignedSynthesizeSpeechUrl) {
            Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

            // Create a media player to play the synthesized audio stream.
            if (mediaPlayer.isPlaying()) {
                setupNewMediaPlayer();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                // Set media player's data source to previously obtained URL.
                mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
            } catch (IOException e) {
                Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
            }

            // Start the playback asynchronously (since the data source is a network stream).
            mediaPlayer.prepareAsync();
        }
    }



    public void saveSettings(){
        Voice selectedVoice = (Voice) voicesSpinner.getSelectedItem();
        int selectedVolume = volumeSeekBar.getProgress() -6;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("voice", selectedVoice.getId());
        editor.putInt("volume", selectedVolume);
        editor.commit();
        Toast.makeText(getContext(), "Saved Changes:  " + selectedVoice.getId() + ",  " + String.valueOf(selectedVolume), Toast.LENGTH_SHORT).show();
    }

    public void getPreferences(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("settings", 0);
        String selectedVoice = sharedPreferences.getString("voice", "None");
        int selectedVolume = sharedPreferences.getInt("volume", -6); // -6 to 6.
        int index = 0;
        for(Voice voice : voices){
            if(voice.getId().equals(selectedVoice))
                break;
            index++;
        }
        if(index >= voices.size()) index = 0;

        voicesSpinner.setSelection(index);
        volumeSeekBar.setProgress(selectedVolume +6);

    }

}
