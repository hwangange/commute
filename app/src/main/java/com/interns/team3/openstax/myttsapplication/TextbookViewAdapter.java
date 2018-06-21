package com.interns.team3.openstax.myttsapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TextbookViewAdapter extends RecyclerView.Adapter<TextbookViewAdapter.ViewHolder>{

    public ArrayList<String> dataSet;
    public View.OnClickListener textOnClickListener = new TextOnClickListener();


    public static class TextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            String text = ((TextView) v.findViewById(R.id.item)).getText().toString();
            Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();

            // where TTS function will go
        }
    }

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
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TextbookViewAdapter(ArrayList<String> dataSet){
        this.dataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TextbookViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view, parent, false);


        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(textOnClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView item = (TextView) holder.textView;
        item.setText(dataSet.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }


}
