package com.interns.team3.openstax.myttsapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Content> dataSet;
    public ContentOnClickListener contentOnClickListener;

    public Context context;

    public interface ContentOnClickListener {
        void onClick(View v);
    }



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView modID;
        public TextView modTitle;
        public TextView modChapter;
        public View view;


        public ModuleViewHolder(View v) {
            super(v);
            view = v;
            modID = (TextView) v.findViewById(R.id.modID);
            modTitle = (TextView) v.findViewById(R.id.modTitle);
            modChapter = (TextView) v.findViewById(R.id.modNum);

        }

        public void bind(final ContentOnClickListener listener){

            view.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    listener.onClick(v);
                }
            });
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

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView bookTitle;
        public ImageView bookImg;
        public TextView bookId;
        public View view;
        public BookViewHolder(View v) {
            super(v);
            view = v;
            bookTitle = (TextView) v.findViewById(R.id.book_title);
            bookId = (TextView) v.findViewById(R.id.book_id);
            bookImg= (ImageView) v.findViewById(R.id.book_img);
        }

        public void bind(final ContentOnClickListener listener){

            view.setOnClickListener(new View.OnClickListener(){
                @Override public void onClick(View v){
                    listener.onClick(v);
                }
            });
        }
    }

    public ContentAdapter(ArrayList<Content> dataSet, ContentOnClickListener contentOnClickListener)
    {
        this.dataSet = dataSet;
        this.contentOnClickListener = contentOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // 0 = module, 1 = chapter
        // Note that unlike in ListView adapters, types don't have to be contiguous
        Content item = dataSet.get(position);
        if(item instanceof Content.Module)
            return 0;
        else if(item instanceof Content.Chapter)
            return 1;
        else if(item instanceof Content.Book)
            return 2;
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

                ContentAdapter.ModuleViewHolder vh = new ContentAdapter.ModuleViewHolder(v);

                return vh;
            }
            case 1: // Chapter
            {
                // create a new view
                View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_chapter, parent, false);

                ContentAdapter.ChapterViewHolder vh = new ContentAdapter.ChapterViewHolder(v);
                return vh;
            }
            case 2: // Book
                View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.textbook_card, parent, false);

                ContentAdapter.BookViewHolder vh = new ContentAdapter.BookViewHolder(v);

                return vh;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0: {
                ModuleViewHolder moduleViewHolder = (ModuleViewHolder) holder;
                TextView myModID = moduleViewHolder.modID;
                TextView myModTitle = moduleViewHolder.modTitle;
                TextView myModChapter = moduleViewHolder.modChapter;

                Content.Module module = (Content.Module) dataSet.get(position);
                String section_num = module.getSectionNum();
                String title = module.getTitle();


                myModID.setText(module.getId());

                String tempModuleTitle;
                if (!(module.getTitle().equals("Introduction")))
                    tempModuleTitle = section_num + " " + title;
                else tempModuleTitle = title;

                myModTitle.setText(tempModuleTitle);

                myModChapter.setText(section_num);
                //System.out.println("MODULE: \t" + dataSet.get(position).id + "\t" + dataSet.get(position).title + "\t" + dataSet.get(position).chapter);

                moduleViewHolder.bind(contentOnClickListener);

                break;
            }
            case 1: {
                ChapterViewHolder chapterViewHolder = (ChapterViewHolder) holder;
                TextView myChapterNum = chapterViewHolder.chapterNum;
                TextView myChapterTitle = chapterViewHolder.chapterTitle;

                Content.Chapter chapter = (Content.Chapter) dataSet.get(position);
                String chapter_num = chapter.getChapterNum();
                String title = chapter.getTitle();

                myChapterNum.setText(chapter_num);

                String tempChapterTitle = chapter_num + " " + title;
                myChapterTitle.setText(tempChapterTitle);
                // System.out.println("CHAPTER: \t" + dataSet.get(position).title + "\t" + dataSet.get(position).chapter);
                break;
            }
            case 2: {
                BookViewHolder bookViewHolder = (BookViewHolder) holder;
                TextView myBookTitle = bookViewHolder.bookTitle;
                TextView myBookId = bookViewHolder.bookId;
                ImageView myBookImg = bookViewHolder.bookImg;

                Content.Book book = (Content.Book) dataSet.get(position);
                String title= book.getTitle();
                String id = book.getId();

                String modified_title= title.replaceAll(" ", "_").replaceAll("\\.", "").toLowerCase();
                Log.i("Title; ", modified_title);
                int drawable_id = context.getResources().getIdentifier(modified_title, "drawable", context.getPackageName());

                myBookTitle.setText(title);
                myBookId.setText(id);
                Picasso.with(context).load(drawable_id).into(myBookImg);

                bookViewHolder.bind(contentOnClickListener);

                break;
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setContext(Context c){
        context = c;
    }

}
