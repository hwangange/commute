package com.interns.team3.openstax.myttsapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TableOfContentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Module> dataSet;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView modID;
        public TextView modTitle;
        public TextView modChapter;
        public ModuleViewHolder(View v) {
            super(v);
            modID = (TextView) v.findViewById(R.id.modID);
            modTitle = (TextView) v.findViewById(R.id.modTitle);
            modChapter = (TextView) v.findViewById(R.id.modChapter);
        }
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView chapterNum;
        public TextView chapterTitle;
        public ChapterViewHolder(View v) {
            super(v);
            chapterNum = (TextView) v.findViewById(R.id.chapterNum);
            chapterTitle = (TextView) v.findViewById(R.id.chapterTitle);
        }
    }

    public TableOfContentsAdapter(ArrayList<Module> dataSet)
    {
        this.dataSet = dataSet;
    }

    @Override
    public int getItemViewType(int position) {
        // 0 = module, 1 = chapter
        // Note that unlike in ListView adapters, types don't have to be contiguous
        String type = dataSet.get(position).type;
        if(type == "module")
            return 0;
        else if(type == "chapter")
            return 1;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: // Module
            {
                // create a new view
                View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_module, parent, false);

                TableOfContentsAdapter.ModuleViewHolder vh = new TableOfContentsAdapter.ModuleViewHolder(v);
                return vh;
            }
            case 1: // Chapter
            {
                // create a new view
                View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_chapter, parent, false);

                TableOfContentsAdapter.ChapterViewHolder vh = new TableOfContentsAdapter.ChapterViewHolder(v);
                return vh;
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ModuleViewHolder moduleViewHolder = (ModuleViewHolder) holder;
                TextView myModID = moduleViewHolder.modID;
                TextView myModTitle = moduleViewHolder.modTitle;
                TextView myModChapter = moduleViewHolder.modChapter;



                myModID.setText(dataSet.get(position).id);

                String tempModuleTitle;
                if(!(dataSet.get(position).title.equals("Introduction")))
                    tempModuleTitle = dataSet.get(position).chapter + " " + dataSet.get(position).title;
                else tempModuleTitle = dataSet.get(position).title;
                myModTitle.setText(tempModuleTitle);

                myModChapter.setText(dataSet.get(position).chapter);
                //System.out.println("MODULE: \t" + dataSet.get(position).id + "\t" + dataSet.get(position).title + "\t" + dataSet.get(position).chapter);
                break;

            case 1:
                ChapterViewHolder chapterViewHolder = (ChapterViewHolder) holder;
                TextView myChapterNum = chapterViewHolder.chapterNum;
                TextView myChapterTitle = chapterViewHolder.chapterTitle;

                myChapterNum.setText(dataSet.get(position).chapter);

                String tempChapterTitle = dataSet.get(position).chapter + " " + dataSet.get(position).title;
                myChapterTitle.setText(tempChapterTitle);
               // System.out.println("CHAPTER: \t" + dataSet.get(position).title + "\t" + dataSet.get(position).chapter);
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
