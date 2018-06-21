package com.interns.team3.openstax.myttsapplication;

import java.util.ArrayList;

public interface Content {

    String getTitle();

    String getId();

    class Book implements Content {
        private String title;
        private String id;

        public Book(String title, String id) {
            this.title = title;
            this.id = id;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getId() {
            return this.id;
        }
    }

    class Chapter implements Content {
        private String title;
        private String id;
        private ArrayList<Module> modules;
        private String chapter_num;

        public Chapter(String title, String id, String chapter_num) {
            this.title = title;
            this.id = id;
            this.chapter_num = chapter_num;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getId() {
            return this.id;
        }


        public String getChapterNum() {
            return this.chapter_num;
        }
    }

    class Module implements Content {
        private String title;
        private String id;
        public String section_num;
        public String book_id;

        public Module(String title, String id, String section_num, String book_id) {
            this.title = title;
            this.id = id;
            this.section_num = section_num;
            this.book_id = book_id;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public String getSectionNum() {
            return this.section_num;
        }

        public String getBookId() { return this.book_id; }
    }
}