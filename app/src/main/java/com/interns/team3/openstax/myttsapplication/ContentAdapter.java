package com.interns.team3.openstax.myttsapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.interns.team3.openstax.myttsapplication.Content;
import com.interns.team3.openstax.myttsapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Content> dataSet;
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
            modID = v.findViewById(R.id.modID);
            modTitle = v.findViewById(R.id.modTitle);
            modChapter = v.findViewById(R.id.modNum);
        }

        public void bind(final ContentOnClickListener listener){

            view.setOnClickListener(listener::onClick);
        }


    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView chapterNum;
        public TextView chapterTitle;
        public ChapterViewHolder(View v) {
            super(v);
            chapterNum = v.findViewById(R.id.chapterNum);
            chapterTitle = v.findViewById(R.id.chapterTitle);
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
            bookTitle = v.findViewById(R.id.book_title);
            bookId = v.findViewById(R.id.book_id);
            bookImg= v.findViewById(R.id.book_img);
        }

        public void bind(final ContentOnClickListener listener){

            view.setOnClickListener(listener::onClick);
        }
    }

    public ContentAdapter(List<Content> dataSet, ContentOnClickListener contentOnClickListener)
    {
        this.dataSet = dataSet;
        this.contentOnClickListener = contentOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        // 0 = module, 1 = chapter
        // Note that unlike in ListView adapters, types don't have to be contiguous
        Content item = dataSet.get(position);
        if (item instanceof Content.Module)
            return 0;
        else if (item instanceof Content.Chapter)
            return 1;
        else if (item instanceof Content.Book)
            return 2;
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0: {   // Module
                // create a new view
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_module, parent, false);

                return new ModuleViewHolder(v);
            }
            case 1: {   // Chapter
                // create a new view
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.toc_chapter, parent, false);

                return new ChapterViewHolder(v);
            }
            case 2: {   // Book
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.textbook_card, parent, false);

                return new BookViewHolder(v);
            }
            default:
                return null;
        }
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
                String modNum = module.getModuleNum();
                String title = module.getTitle();

                myModID.setText(module.getId());

                String tempModuleTitle;
                if (!(module.getTitle().equals("Introduction"))) {
                    tempModuleTitle = modNum + " " + title;
                } else {
                    tempModuleTitle = title;
                }

                myModTitle.setText(tempModuleTitle);

                myModChapter.setText(modNum);
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
