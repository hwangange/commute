package com.interns.team3.openstax.myttsapplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

public class BookModuleTest {
    private String bookPath = "/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/main/assets/Books/Psychology/";
    private ParseObject testParse = new ParseObject(bookPath);
    private Elements modules = testParse.getChapterModules(testParse.getChapters().first());
    private Content.Module introModule = new Content.Module(bookPath, modules.first());
    private Content.Module testModule = new Content.Module(bookPath, modules.last());

    public BookModuleTest() throws IOException {
    }

    @Test
    public void toJsonTest() throws JSONException {
        JSONObject moduleObj = introModule.toJson();
        System.out.println(moduleObj.toString(4));
    }

    @Test
    public void getOpeningTest() throws JSONException {
        System.out.println(testModule.getOpening().toString(4));
    }

    @Test
    public void getReadingSectionsTest() throws JSONException {
        System.out.println(introModule.getReadingSections());
        System.out.println(testModule.getReadingSections().toString(4));
    }

    @Test
    public void getEocTest() throws JSONException {
        System.out.println(introModule.getEoc());
        System.out.println(testModule.getEoc().toString(4));
    }

   /* @Test
    public void buildOpeningSSMLTest() throws JSONException {
        System.out.println(testModule.buildOpeningSSML());
    }

    @Test
    public void buildReadingSectionsSSMLTest() throws JSONException {
        testModule.buildReadSecSSMLArray().forEach(System.out::println);
    }

    @Test
    public void buildEocSSMLTest() throws JSONException {
        System.out.println(testModule.buildEocSSML());
    } */

    @Test
    public void printModulePageTest() throws JSONException {
        testModule.printModulePage("/Users/Linda/AndroidStudioProjects1/MyTTSApplication/app/src/test/modulePage.txt");
    }
/*
    @Test
    public void makeSectionsAudioTest() throws Exception {
        AudioClient client = new AudioClient("C:/Users/Damon/Documents/OpenStax/audio_files/", true);
        List<String> openingSSML = testModule.buildOpeningSSML();
        List<List<String>> readingSSMLArray = testModule.buildReadSecSSMLArray();
        List<String> eocSSML = testModule.buildEocSSML();
        client.synthesizeAudio("opening", true, openingSSML, false);
        for (List<String> ssmlList: readingSSMLArray) {
            client.synthesizeAudio(String.format("section-%d", readingSSMLArray.indexOf(ssmlList) + 1), true, ssmlList, false);
        }
        client.synthesizeAudio("eoc", true, eocSSML, false);
    }

    @Test
    public void makeModuleAudioTest() throws Exception {
        AudioClient client = new AudioClient("C:/Users/Damon/Documents/OpenStax/audio_files/", true);
        List<String> moduleSSML = testModule.buildModuleSSML();
        client.synthesizeAudio("module", true, moduleSSML, false);
    } */

//    @Test
//    public void pullReviewQuestionsTest() throws JSONException {
//        JSONObject reviewQsObj = testModule.pullReviewQuestions();
//        System.out.println(reviewQsObj.toString(4));
//    }
//
//    @Test
//    public void pullCriticalThinkingTest() throws JSONException {
//        JSONObject critThinkObj = testModule.pullCriticalThinking();
//        System.out.println(critThinkObj.toString(4));
//    }
}

//    @Test
//    public void pullOneReadingSectionTest() throws JSONException, IOException {
//        Elements modules = testParse.getChapterModules(testParse.getChapters().first());
//        Content.Module testModule = new Content.Module(bookPath, modules.last());
//        JSONObject sectionsObj = testModule.pullAllReadingSections();
//        System.out.println(sectionsObj.toString(4));
//    }