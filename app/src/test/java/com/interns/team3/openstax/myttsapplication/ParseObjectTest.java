package com.interns.team3.openstax.myttsapplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ParseObjectTest {
    private String fileString = "/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/main/assets/Books/Psychology/";
    private ParseObject test = new ParseObject(fileString);

    @Test
    public void getFileStringTest() {
        assertEquals(fileString, test.getMainFolder());
    }

    @Test
    public void getFileTest() {
        File file = new File(test.getMainFolder() + "collection.xml");
        assertTrue(file.equals(test.getCollectionFile()));
    }

    @Test
    public void printChaptersTest() throws IOException {
        test.printChapters();
    }

    @Test
    public void printChapterModulesTest() throws IOException {
        test.printChapterModules(test.getChapters().first());
    }

    @Test
    public void chapterToJsonTest() throws IOException, JSONException {
        JSONObject chapterObj = test.chapterToJson(test.getChapters().first(), false);
        System.out.println(chapterObj.toString());
    }

    @Test
    public void bookToJsonTest() throws IOException, JSONException {
        JSONObject bookObj = test.bookToJson(false);
        System.out.println(bookObj.toString(4));
    }

    @Test
    public void toFileTest() throws IOException, JSONException {
        JSONObject chapterObj = test.chapterToJson(test.getChapters().first(), false);
        JSONObject bookObj = test.bookToJson(false);
        test.jsonToFile(chapterObj,"/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/test/chapterjson.json");
        test.jsonToFile(bookObj,"/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/test/bookjson.json");
    }

//    @Test
//    public void printUnitsTest() throws IOException {
//        test.printUnits();
//    }
//
//    @Test
//    public void printChaptersTest() throws IOException {
//        test.printChapters(test.getUnits().first());
//    }
//
//    @Test
//    public void printAllChaptersTest() throws IOException {
//        test.printAllChapters();
//    }
}