package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Module> {

    private ArrayList<Module> list;
    Context mContext;


    public CustomAdapter(ArrayList<Module> data, Context context){
        super(context, 0, data);
        this.list = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Module module = getItem(position);

        //Check if an existing view is being reused, otherwise inflate the view
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.table_of_contents, parent, false);
        }

        // Lookup view for data population
        TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
        TextView itemID = (TextView) convertView.findViewById(R.id.itemID);
        // Populate the data into the template view using the data object

        itemTitle.setText(module.title);
        itemID.setText(module.id);

        // Return the completed view to render on screen
        return convertView;
    }
}
