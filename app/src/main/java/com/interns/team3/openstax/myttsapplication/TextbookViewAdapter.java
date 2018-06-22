package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TextbookViewAdapter extends RecyclerView.Adapter<TextbookViewAdapter.ViewHolder>{

    public ArrayList<String> dataSet;
    public TextOnClickListener textOnClickListener; //public View.OnClickListener textOnClickListener = new TextOnClickListener();
    public static Context context;
    public static TextToSpeech tts;

    public interface TextOnClickListener {
        void onClick(String text, View v);
    }

    /*public static class TextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            String text = ((TextView) v.findViewById(R.id.item)).getText().toString();

            // where TTS function will go
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id");
            while(tts.isSpeaking()){
                v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelected));
            }
            v.findViewById(R.id.item).setBackgroundColor(ContextCompat.getColor(context, R.color.defaultGrey));

        }
    } */

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.item);
        }

        public void bind(final String text, final TextOnClickListener listener){
            textView.setText(text);
            textView.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    listener.onClick(text, v);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TextbookViewAdapter(ArrayList<String> dataSet, TextOnClickListener textOnClickListener){
        this.dataSet = dataSet;
        this.textOnClickListener = textOnClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TextbookViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view, parent, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.bind(dataSet.get(position), textOnClickListener);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setContext(Context context) {
        this.context= context;
        tts = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    //mButtonSpeak.setEnabled(true);
                    Log.e("Initialization", "Initialization succeeded");

                } else {
                    Log.e("Initialization", "Initialization failed");
                }

            }
        });
    }


}
