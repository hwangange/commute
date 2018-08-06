package com.interns.team3.openstax.myttsapplication;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.interns.team3.openstax.myttsapplication.Content.Module;

public class ModuleTest {
    private String booksPath = "C:/Users/Damon/Documents/OpenStax/oer.exports/data";
    private String id1 = "m48993";
    private String id2 = "m49003";
    private String id3 = "m44392";
    private String file1 = readFile(String.format("%s/psychology/%s/index.cnxml.html", booksPath, id1));
    private String file2 = readFile(String.format("%s/psychology/%s/index.cnxml.html", booksPath, id2));
    private String file3 = readFile(String.format("%s/biology/%s/index.cnxml.html", booksPath, id3));

    //    private AudioBook testBook = new AudioBook(bookPath, false);
//    private Elements modules = testBook.getChapterModules(testBook.getChapters().first());
//    private Content.Module introModule = testBook.getModule(modules.first());
    private Module testModule = new Module(id1, file1);
    private Module testModule2 = new Module(id2, file2);
    private Module testModule3 = new Module(id3, file3);


    public ModuleTest() throws IOException {
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    public void toJsonTest() throws JSONException {
        JSONObject moduleObj = testModule.toJson();
        System.out.println(moduleObj.toString(4));
    }

    @Test
    public void correctChunks() throws JSONException {
        List<TextAudioChunk> chunks = testModule3.initTextAudioChunks();
        chunks.forEach(chunk -> {
            System.out.println(chunk.getText());
            System.out.println(chunk.getSsml());
            System.out.println();
        });
    }

//    @Test
//    public void getReadingSectionsTest() throws JSONException {
//        System.out.println(introModule.getReadingSections());
//        System.out.println(testModule.getReadingSections().toString(4));
//    }
//
//    @Test
//    public void getEocTest() throws JSONException {
//        System.out.println(introModule.getEom());
//        System.out.println(testModule.getEom().toString(4));
//    }

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