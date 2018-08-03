package com.interns.team3.openstax.myttsapplication;

import android.content.Context;

import com.interns.team3.openstax.myttsapplication.Content.Module;
import com.interns.team3.openstax.myttsapplication.ssml.SsmlBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioBook {
    private final Context context;
    private final String mainFolder;
    private final String bookName;
    private final boolean hasUnits;

    AudioBook (Context context, String bookName) {
        this.context = context;
        this.bookName = bookName;
        this.mainFolder = "Books/" + bookName + "/";
        this.hasUnits = false;
    }

    AudioBook(Context context, String bookName, boolean hasUnits) {
        this.context = context;
        this.bookName = bookName;
        this.mainFolder = "Books/" + bookName + "/";
        this.hasUnits = hasUnits;
    }

    public String getBookName() { return bookName;}
    public String getMainFolder() {
        return this.mainFolder;
    }


    private String readAsset(String file) throws IOException {
        InputStreamReader input = new InputStreamReader(this.context.getAssets().open(file));
        StringBuilder buf = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(input);
        String str;
        while ((str=bufferedReader.readLine()) != null) {
            buf.append(str);
        }
        return buf.toString();
    }

    private String getCollectionFile() {
        String collection = mainFolder + "collection.xml";
        try {
            return readAsset(collection);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getModuleFile(String moduleID) {
        String module = mainFolder + moduleID + "/index.cnxml.html";
        try {
            return readAsset(module);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document toJsoupDoc(String file) {
        return Jsoup.parse(file, "UTF-8");
    }

    public Elements getUnits() {
        if (this.hasUnits) {
            Document doc = toJsoupDoc(getCollectionFile());
            return doc.select("col|collection > col|content > col|subcollection");
        } else {
            return null;
        }
    }

    public void printUnits() {
        if (getUnits() != null) {
            Elements units = getUnits();
            for (Element unit: units) {
                String title = unit.select("md|title").first().text();
                System.out.println(title + "\n");
            }
        }
    }

    private Elements getUnitChapters(Element unit) {
        if (this.hasUnits) {
            return unit.select("> col|content > col|subcollection");
        } else {
            return null;
        }
    }

    public Elements getChapters() {
        if (this.hasUnits) {
            Elements chapters = new Elements();
            for (Element unit: getUnits()) {
                chapters.addAll(getUnitChapters(unit));
            }
            return chapters;
        } else {
            Document doc = toJsoupDoc(getCollectionFile());
            return doc.select("col|collection > col|content > col|subcollection");
        }
    }

    public void printChapters() {
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

    public Module getModule(Element module) {
        String moduleId = module.attr("document");
        return new Module(module, getModuleFile(moduleId));
    }

    public Module getModule(Element module, String moduleNum) {
        String moduleId = module.attr("document");
        return new Module(module, getModuleFile(moduleId), moduleNum);
    }

    public JSONObject chapterToJson(Element chapter, boolean debug) throws JSONException {
        JSONObject chapterObj = new JSONObject();
        String title = chapter.select("md|title").first().text();
        chapterObj.put("title", title);

        JSONArray modulesArray = new JSONArray();
        Elements parts = getChapterModules(chapter);
        for (Element part: parts) {
            Module module = getModule(part);
            if (debug) {
                System.out.println("    " + module.getId() + " -- " + module.getTitle());
            }
            JSONObject moduleObj = module.toJson();
            modulesArray.put(moduleObj);
        }

        chapterObj.put("modules", modulesArray);
        return chapterObj;
    }

    private List<String> chapterToSSML(Element chapter, boolean debug) throws JSONException {
        List<String> chapterSSML = new ArrayList<>();
        SsmlBuilder ssml = new SsmlBuilder();
        String title = chapter.select("md|title").first().text();
        chapterSSML.add(ssml.text(title).newParagraph().newParagraph().build());

        Elements parts = getChapterModules(chapter);
        for (Element part: parts) {
            Module module = getModule(part);
            if (debug) {
                System.out.println("    " + module.getId() + " -- " + module.getTitle());
            }
            List<String> moduleSSML = module.buildModuleSSML();
            chapterSSML.addAll(moduleSSML);
        }

        return chapterSSML;
    }

    public Map<String, String> metadata() {
        Document doc = toJsoupDoc(getCollectionFile());
        String contentUrl = doc.select("metadata md|content-url").text();
        String contentId = doc.select("metadata md|content-id").text();
        String title = doc.selectFirst("metadata md|title").text();
        String version = doc.selectFirst("metadata md|version").text();
        String created = doc.selectFirst("metadata md|created").text();
        String revised = doc.selectFirst("metadata md|revised").text();
        String subject = doc.selectFirst("metadata md|subject").text();
        String summary = doc.selectFirst("metadata md|abstract").text();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("content-url", contentUrl);
        metadata.put("content-id", contentId);
        metadata.put("title", title);
        metadata.put("version", version);
        metadata.put("created", created);
        metadata.put("revised", revised);
        metadata.put("subject", subject);
        metadata.put("summary", summary);

        return metadata;
    }

    public JSONObject bookToJson(boolean debug) throws JSONException {
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

    public List<String> bookToSSML(boolean debug) throws JSONException {
        Document doc = toJsoupDoc(getCollectionFile());
        String title = doc.select("metadata md|title").text();
        List<String> bookSSML = new ArrayList<>();
        bookSSML.add(new SsmlBuilder().text(title).newParagraph().newParagraph().build());

        Elements chapters = getChapters();
        for (Element chapter: chapters) {
            int chapterNum = chapters.indexOf(chapter);
            String chapterTitle = chapter.select("md|title").first().text();
            if (debug) {
                System.out.format("Chapter %d: %s\n", chapterNum, chapterTitle);
            }
            bookSSML.add(new SsmlBuilder().text("Chapter %d", chapterNum).strongBreak()
                    .text(title).newParagraph().build());
            List<String> chapterSSML = chapterToSSML(chapter, debug);
            bookSSML.addAll(chapterSSML);
        }

        return bookSSML;
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

}