package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
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

import static android.content.ContentValues.TAG;

public class TextbookViewAdapter extends RecyclerView.Adapter<TextbookViewAdapter.ViewHolder>{

    public ArrayList<TextChunk> dataSet;
    public TextOnClickListener textOnClickListener; //public View.OnClickListener textOnClickListener = new TextOnClickListener();
    public static Context context;

    public interface TextOnClickListener {
        void onClick(int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
            textView = (TextView) v.findViewById(R.id.item);
        }

        public void bind(final String text, final TextOnClickListener listener){
            textView.setText(text);
            textView.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    listener.onClick(getAdapterPosition());
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TextbookViewAdapter(ArrayList<TextChunk> dataSet, TextOnClickListener textOnClickListener){
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


        //taking precautions: https://stackoverflow.com/questions/39424212/how-to-stop-recyclerview-from-recycling-items-that-are-toggled-visible-and-gone?noredirect=1&lq=1
        TextChunk tc = dataSet.get(position);
        if(tc.isSelected()) holder.textView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSelected));
        else holder.textView.setBackgroundColor(ContextCompat.getColor(context, R.color.defaultGrey));

        holder.bind(tc.getText(), textOnClickListener);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setContext(Context context) {
        this.context= context;
    }

    @Override
    public void onViewRecycled(ViewHolder vh){
        // view appears.
        //Log.wtf(TAG,"onViewRecycled "+vh);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder viewHolder){
        // view leaves.
        //Log.wtf(TAG,"onViewDetachedFromWindow "+viewHolder);
    }


}


