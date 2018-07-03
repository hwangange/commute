package com.interns.team3.openstax.myttsapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParseObject {
    private final String mainFolder;

    ParseObject(String mainFolder) {
        this.mainFolder = mainFolder;
    }

    public String getMainFolder() {
        return this.mainFolder;
    }

    public File getCollectionFile() {
        String collection = mainFolder + "collection.xml";
        return new File(collection);
    }

    public File getModuleFile(String moduleID) {
        String module = mainFolder + moduleID + "/index.cnxml.html";
        return new File(module);
    }

    private Document toJsoupDoc(File file) throws IOException {
        return Jsoup.parse(file, "UTF-8");
    }

    public Elements getChapters() throws IOException {
        Document doc = toJsoupDoc(getCollectionFile());
        return doc.select("col|collection > col|content > col|subcollection");
    }

    public void printChapters() throws IOException {
        Elements chapters = getChapters();
        for (Element chapter: chapters) {
            String title = chapter.select("md|title").first().text();
            System.out.println(title + "\n");
        }
    }

    public Elements getChapterModules(Element chapter) {
        return chapter.getElementsByTag("col:module");
    }

    public void printChapterModules(Element chapter) {
        Elements modules = getChapterModules(chapter);
        for (Element module: modules) {
            String title = module.select("md|title").text();
            System.out.println(title + "\n");
        }
    }

    public JSONObject chapterToJson(Element chapter, boolean debug) throws JSONException, IOException {
        JSONObject chapterObj = new JSONObject();
        String title = chapter.select("md|title").first().text();
        chapterObj.put("title", title);

        JSONArray modulesArray = new JSONArray();
        Elements parts = getChapterModules(chapter);
        for (Element part: parts) {
            Content.Module module = new Content.Module(this.mainFolder, part);
            if (debug) {
                System.out.println("    " + module.getId() + " -- " + module.getTitle());
            }
            JSONObject moduleObj = module.toJson();
            modulesArray.put(moduleObj);
        }

        chapterObj.put("modules", modulesArray);
        return chapterObj;
    }

    private Map<String, String> metadata() throws IOException {
        Document doc = toJsoupDoc(getCollectionFile());
        String contentUrl = doc.select("metadata md|content-url").text();
        String contentId = doc.select("metadata md|content-id").text();
        String title = doc.select("metadata md|title").text();
        String version = doc.select("metadata md|version").text();
        String created = doc.select("metadata md|created").text();
        String revised = doc.select("metadata md|revised").text();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("content-url", contentUrl);
        metadata.put("content-id", contentId);
        metadata.put("title", title);
        metadata.put("version", version);
        metadata.put("created", created);
        metadata.put("revised", revised);

        return metadata;
    }

    public JSONObject bookToJson(boolean debug) throws JSONException, IOException {
        Map<String, String> metadata = metadata();
        JSONObject bookObj = new JSONObject(metadata);

        JSONArray chaptersArray = new JSONArray();
        Elements chapters = getChapters();
        for (Element chapter: chapters) {
            if (debug) {
                System.out.println(chapter.select("md|title").first().text());
            }
            JSONObject chapterObj = chapterToJson(chapter, debug);
            chaptersArray.put(chapterObj);
        }

        bookObj.put("chapters", chaptersArray);

        return bookObj;
    }

    public void jsonToFile(JSONObject obj, String fileString) {
        try {
            FileWriter fileWriter = new FileWriter(fileString);
            fileWriter.write(obj.toString(4));
            fileWriter.flush();
            System.out.println("File successfully written");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public Elements getUnits() throws IOException {
//        Document doc = toJsoupDoc(getCollectionFile());
//        return doc.select("col|collection > col|content > col|subcollection");
//    }

//    public void printUnits() throws IOException {
//        Elements units = getUnits();
//        for (Element unit: units) {
//            String title = unit.select("md|title").first().text();
//            System.out.println(title + "\n");
//        }
//    }

//    public Elements getUnitChapters(Element unit) {
//        return unit.select("> col|content > col|subcollection");
//    }

//    public Elements getAllChapters() throws IOException {
//        Elements all = new Elements();
//        for (Element unit: getUnits()) {
//            all.addAll(getUnitChapters(unit));
//        }
//        return all;
//    }

//    public void printChapters(Element unit) {
//        Elements chapters = getUnitChapters(unit);
//        for (Element chapter: chapters) {
//            String title = chapter.select("md|title").first().text();
//            System.out.println(title + "\n");
//        }
//    }

//    public void printAllChapters() throws IOException {
//        Elements chapters = getAllChapters();
//        for (Element chapter: chapters) {
//            String title = chapter.select("md|title").first().text();
//            System.out.println(title + "\n");
//        }
//    }
}