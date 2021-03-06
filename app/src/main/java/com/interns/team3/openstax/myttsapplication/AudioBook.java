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

    public AudioBook (Context context, String bookName) {
        this.context = context;
        this.bookName = bookName;
        this.mainFolder = "Books/" + bookName + "/";
        this.hasUnits = false;
    }

    public AudioBook(Context context, String bookName, boolean hasUnits) {
        this.context = context;
        this.bookName = bookName;
        this.mainFolder = "Books/" + bookName + "/";
        this.hasUnits = hasUnits;
    }

    public String getBookName() {
        return bookName;
    }

    public String getMainFolder() {
        return this.mainFolder;
    }

    /**
     * Reads file and writes it to a string.
     * @param file path to file to be read
     * @return String containing file content
     */
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

    /**
     * Reads book collection file and writes it to a string.
     * @return String containing book collection file content
     */
    private String getCollectionFile() {
        String collection = mainFolder + "collection.xml";
        try {
            return readAsset(collection);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads module index file and writes it to a string.
     * @return String containing module index file content
     */
    public String getModuleFile(String moduleID) {
        String module = mainFolder + moduleID + "/index.cnxml.html";
        try {
            return readAsset(module);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts file to Jsoup doc.
     * @param file path to file to be converted
     * @return Jsoup doc representing file
     */
    private Document toJsoupDoc(String file) {
        return Jsoup.parse(file, "UTF-8");
    }

    /**
     * Pulls units from book if they exist.
     * @return Jsoup Elements representing book units
     */
    public Elements getUnits() {
        if (this.hasUnits) {
            Document doc = toJsoupDoc(getCollectionFile());
            return doc.select("col|collection > col|content > col|subcollection");
        } else {
            return null;
        }
    }

    /**
     * Print book units for the purpose of debugging.
     */
    public void printUnits() {
        if (getUnits() != null) {
            Elements units = getUnits();
            for (Element unit: units) {
                String title = unit.select("md|title").first().text();
                System.out.println(title + "\n");
            }
        }
    }

    /**
     * Pulls chapters from specified unit of book.
     * @param unit Jsoup Element representing book unit
     * @return Jsoup Elements representing unit chapters
     */
    private Elements getUnitChapters(Element unit) {
        if (this.hasUnits) {
            return unit.select("> col|content > col|subcollection");
        } else {
            return null;
        }
    }

    /**
     * Pulls all chapters from book.
     * @return Jsoup Elements representing book chapters
     */
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

    /**
     * Print book chapters for the purpose of debugging.
     */
    public void printChapters() {
        Elements chapters = getChapters();
        for (Element chapter: chapters) {
            String title = chapter.select("md|title").first().text();
            System.out.println(title + "\n");
        }
    }

    /**
     * Pulls modules from specified chapter
     * @param chapter Jsoup Element representing book chapter
     * @return Jsoup Elements representing chapter modules
     */
    public Elements getChapterModules(Element chapter) {
        return chapter.getElementsByTag("col:module");
    }

    /**
     * Print chapter modules for the purpose of debugging.
     * @param chapter Jsoup Element representing book chapter
     */
    public void printChapterModules(Element chapter) {
        Elements modules = getChapterModules(chapter);
        for (Element module: modules) {
            String title = module.select("md|title").text();
            System.out.println(title + "\n");
        }
    }

    /**
     * Gets Content Module object of specified module.
     * @param module Jsoup Element representing book module
     * @param moduleNum String representing module number
     * @return Content Module object representing book module
     */
    public Module getModule(Element module, String moduleNum) {
        String moduleId = module.attr("document");
        return new Module(module, getModuleFile(moduleId), moduleNum);
    }

    /**
     * Converts specified chapter to JSON object.
     * @param chapter Jsoup Element representing book chapter
     * @param chapterNum int representing chapter number
     * @param debug boolean stating whether to provide debugging output
     * @return JSON object representing chapter
     */
    public JSONObject chapterToJson(Element chapter, int chapterNum, boolean debug) throws JSONException {
        JSONObject chapterObj = new JSONObject();
        String title = chapter.select("md|title").first().text();
        chapterObj.put("title", title);

        JSONArray modulesArray = new JSONArray();
        Elements modules = getChapterModules(chapter);
        int modNum = 0;
        for (Element modElem: modules) {
            String fullModNum = chapterNum + "." + modNum;
            Module module = getModule(modElem, fullModNum);
            if (debug) {
                System.out.printf("\t%s -- %s %s%n", module.getId(), fullModNum, module.getTitle());
            }
            JSONObject moduleObj = module.toJson();
            modulesArray.put(moduleObj);
        }

        chapterObj.put("modules", modulesArray);
        return chapterObj;
    }

    /**
     * Converts specified chapter to ssml list
     * @param chapter Jsoup Element representing book chapter
     * @param chapterNum int representing chapter number
     * @param debug boolean stating whether to provide debugging output
     * @return String List full of ssml strings representing book chapter content
     */
    private List<String> chapterToSSML(Element chapter, int chapterNum, boolean debug) throws JSONException {
        List<String> chapterSSML = new ArrayList<>();
        SsmlBuilder ssml = new SsmlBuilder();
        String title = chapter.select("md|title").first().text();
        chapterSSML.add(ssml.text(title).newParagraph().newParagraph().build());

        Elements modules = getChapterModules(chapter);
        int modNum = 0;
        for (Element modElem: modules) {
            String fullModNum = chapterNum + "." + modNum;
            Module module = getModule(modElem, fullModNum);
            if (debug) {
                System.out.printf("\t%s -- %s %s%n", module.getId(), fullModNum, module.getTitle());
            }
            List<String> moduleSSML = module.buildModuleSSML();
            chapterSSML.addAll(moduleSSML);
        }

        return chapterSSML;
    }

    /**
     * Pulls metadata from book collection file.
     * @return Mapping of metadata elements to their values
     */
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

    /**
     * Converts specified book to JSON object.
     * @param debug boolean stating whether to provide debugging output
     * @return JSON object representing book
     */
    public JSONObject bookToJson(boolean debug) throws JSONException {
        Map<String, String> metadata = metadata();
        JSONObject bookObj = new JSONObject(metadata);

        JSONArray chaptersArray = new JSONArray();
        Elements chapters = getChapters();
        int chapterNum = 1;
        for (Element chapter: chapters) {
            if (debug) {
                System.out.println(chapter.select("md|title").first().text());
            }
            JSONObject chapterObj = chapterToJson(chapter, chapterNum, debug);
            chaptersArray.put(chapterObj);
        }

        bookObj.put("chapters", chaptersArray);

        return bookObj;
    }

    /**
     * Converts book ssml list
     * @param debug boolean stating whether to provide debugging output
     * @return String List full of ssml strings representing book content
     */
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
            List<String> chapterSSML = chapterToSSML(chapter, chapterNum, debug);
            bookSSML.addAll(chapterSSML);
        }

        return bookSSML;
    }

    /**
     * Creates JSON file of specified JSON object
     * @param obj JSON object to be converted
     * @param fileString name and path of JSON file
     */
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