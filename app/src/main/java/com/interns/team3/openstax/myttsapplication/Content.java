package com.interns.team3.openstax.myttsapplication;

import java.util.ArrayList;
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

        private Document content;
        private ArrayList<String> nonReadingSections = new ArrayList<>();
        private int maxLineLength = 200;

        public Module(String title, String id, String section_num) {
            this.title = title;
            this.id = id;
            this.section_num = section_num;
        }

        public Module(String title, String id, Document doc) throws IOException {
            this.title = title;
            this.id = id;
            this.section_num = "-1";
            this.content = doc;


            this.nonReadingSections.add("summary");
            this.nonReadingSections.add("review-questions");
            this.nonReadingSections.add("critical-thinking");
            this.nonReadingSections.add("personal-application");
        }

        public Module(String bookPath, Element section) throws IOException {
            this.title = section.select("md|title").text();
            this.id = section.attr("document");
            String modulePath = bookPath + this.id + "/index.cnxml.html";
            this.content = Jsoup.parse(new File(modulePath), "UTF-8");
            this.nonReadingSections.add("summary");
            this.nonReadingSections.add("review-questions");
            this.nonReadingSections.add("critical-thinking");
            this.nonReadingSections.add("personal-application");
        }

        public String toString(){
            String s = "Title: " + title + "\t\t" + "ID: " + id;
            for(String sec : nonReadingSections){
                s+="\n"+sec;
            }
            //s+="\n-------------------------------\n" + content;

            return s;

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

        public Document getContent() { return this.content; }

        private Element getBody() { return this.content.body(); }

        //************* JSON FUNCTIONS ******************//

        public JSONObject toJson() throws JSONException {
            JSONObject moduleObj = new JSONObject();
            moduleObj.put("title", this.title);
            moduleObj.put("id", this.id);

            JSONObject openingObj = pullOpening();
            moduleObj.put("opening", openingObj);
            moduleObj.put("reading sections", JSONObject.NULL);
            moduleObj.put("eoc", JSONObject.NULL);
            if (!(this.title.equals("Introduction"))) {
                JSONArray readingSecArray = pullAllReadingSections();
                JSONObject eocObj = pullEocSections();

                moduleObj.put("reading sections", readingSecArray);
                moduleObj.put("eoc", eocObj);
            }

            return moduleObj;
        }

        public JSONObject getOpening() throws JSONException {
            JSONObject module = toJson();
            return module.optJSONObject("opening");
        }

        public JSONArray getReadingSections() throws JSONException {
            JSONObject module = toJson();
            return module.optJSONArray("reading sections");
        }

        public JSONObject getEoc() throws JSONException {
            JSONObject module = toJson();
            return module.optJSONObject("eoc");
        }


        //************* HELPER JSON FUNCTIONS ******************//

        private JSONObject pullAbstract() throws JSONException {
            Element abstractElem = getBody().getElementsByAttributeValue("data-type", "abstract").first();

            JSONObject absObj = new JSONObject();
            if (abstractElem != null) {
                String intro = abstractElem.ownText();
                Elements list = abstractElem.getElementsByTag("li");
                JSONObject absValue = new JSONObject();
                absValue.put("intro", intro);
                absValue.put("list", new JSONArray(list.eachText()));
                absObj.put("abstract", absValue);
            } else {
                absObj.put("abstract", JSONObject.NULL);
            }

            return absObj;
        }

        private JSONObject pullParagraphs(Element section) throws JSONException {
            Elements paragraphs = section.select("> p");
            JSONObject paraObj = new JSONObject();
            if (paragraphs != null) {
                paraObj.put("paragraphs", new JSONArray(paragraphs.eachText()));
            } else {
                paraObj.put("paragraphs", JSONObject.NULL);
            }
            return paraObj;
        }

        private JSONObject pullOpening() throws JSONException {
            JSONObject absObj = pullAbstract();
            JSONObject paraObj = pullParagraphs(getBody());

            JSONObject openingObj = new JSONObject();
            openingObj.put("abstract", absObj.get("abstract"));
            openingObj.put("paragraphs", paraObj.get("paragraphs"));

            return openingObj;
        }

        private JSONObject pullReadingSection(Element section) throws JSONException {
            if(section != null) {
                String title = section.getElementsByAttributeValue("data-type", "title").first().text();
                JSONObject paraObj = pullParagraphs(section);
                JSONObject sectionObj = new JSONObject();
                sectionObj.put("title", title);
                sectionObj.put("paragraphs", paraObj.get("paragraphs"));
                return sectionObj;
            }
            return null;
        }

        private JSONArray pullAllReadingSections() throws JSONException {
            Elements sections = getBody().select("> section");
            sections.removeIf(x -> this.nonReadingSections.contains(x.className()));

            JSONArray readingSecArray = new JSONArray();
            int count = 1;
            for (Element section: sections) {
                JSONObject secObj = pullReadingSection(section);
                secObj.put("section", count);
                readingSecArray.put(secObj);
                count++;
            }

            return readingSecArray;
        }

        private JSONObject pullSummary() throws JSONException {
            Element summary = getBody().getElementsByClass("summary").first();
            return pullReadingSection(summary);
        }

        private JSONObject pullExercise(Element exercise, boolean multiChoice, boolean hasSolution) throws JSONException {
            Element problem = exercise.getElementsByAttributeValue("data-type", "problem").first();
            String question = problem.select("p").text();
            JSONObject exObj = new JSONObject();
            exObj.put("problem", question);
            exObj.put("options", JSONObject.NULL);
            exObj.put("solution", JSONObject.NULL);

            if (multiChoice) {
                Elements options = problem.getElementsByTag("li");
                exObj.put("options", new JSONArray(options.eachText()));
            }

            if (hasSolution) {
                Element solution = exercise.getElementsByAttributeValue("data-type", "solution").first();
                exObj.put("solution", solution.text());
            }

            return exObj;
        }

        private JSONArray pullSectionExercises(Element section, boolean multiChoice, boolean hasSolution) throws JSONException {
            if (section == null) {
                return null;
            }
            Elements exercises = section.getElementsByAttributeValue("data-type", "exercise");
            JSONArray allExArray = new JSONArray();
            int count = 1;
            for (Element exercise: exercises) {
                JSONObject exObj = pullExercise(exercise, multiChoice, hasSolution);
                exObj.put("exercise", count);
                allExArray.put(exObj);
                count++;
            }
            return allExArray;
        }

        private JSONArray pullGlossary() throws JSONException {
            Element glossary = getBody().getElementsByAttributeValue("data-type", "glossary").first();
            if(glossary != null) {
                Elements keyTerms = glossary.getElementsByTag("dl");

                JSONArray glossaryArray = new JSONArray();
                for (Element keyTerm : keyTerms) {
                    JSONObject keyTermObj = new JSONObject();
                    String term = keyTerm.select("dt").text();
                    String definition = keyTerm.select("dd").text();
                    keyTermObj.put("term", term);
                    keyTermObj.put("definition", definition);
                    glossaryArray.put(keyTermObj);
                }

                return glossaryArray;
            }
            return null;
        }

        private JSONObject pullEocSections() throws JSONException {
            Element reviewQs = getBody().getElementsByClass("review-questions").first();
            Element critThink = getBody().getElementsByClass("critical-thinking").first();
            Element personalAp = getBody().getElementsByClass("personal-application").first();

            JSONObject summaryObj = pullSummary();
            JSONArray reviewQsArray = pullSectionExercises(reviewQs, true, true);
            JSONArray critThinkArray = pullSectionExercises(critThink, false, true);
            JSONArray personalApArray = pullSectionExercises(personalAp, false, false);
            JSONArray glossaryObj = pullGlossary();

            JSONObject eocObj = new JSONObject();
            eocObj.put("summary", summaryObj);
            eocObj.put("review questions", reviewQsArray);
            eocObj.put("critical thinking", critThinkArray);
            eocObj.put("personal application", personalApArray);
            eocObj.put("glossary", glossaryObj);

            return eocObj;
        }

        //************* PRINTING FUNCTIONS ******************//

        public void printModulePage(String fileString) throws JSONException {
            try {
                File file = new File(fileString);
                FileWriter fw = new FileWriter(file);
//            System.out.println(this.title + "\n");
                fw.write(this.title + "\n\n");
                printOpening(fw);
                printReadingSections(fw);
                printEoc(fw);
                fw.flush();
                fw.close();
                System.out.println("File successfully written");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //************* HELPER PRINTING FUNCTIONS ******************//
        private void printOpening(FileWriter fw) throws JSONException, IOException {
            JSONObject opening = getOpening();
            if (opening != null) {
                if (!(opening.isNull("abstract"))) {
                    JSONObject absObj = opening.getJSONObject("abstract");
                    String intro = absObj.getString("intro");
                    JSONArray abList = absObj.getJSONArray("list");

//                System.out.println(intro);
                    fw.write(intro + "\n");
                    printJsonStringArray(fw, abList, true);
//                System.out.println("");
                    fw.write("\n");
                }
                if (!(opening.isNull("paragraphs"))) {
                    JSONArray paragraphs = opening.getJSONArray("paragraphs");
                    printJsonStringArray(fw, paragraphs, false);
                }
//            System.out.println("\n");
                fw.write("\n\n");
            }
        }

        public ArrayList<String> returnPrintOpening() throws JSONException, IOException {
            JSONObject opening = getOpening();
            ArrayList<String> lst = new ArrayList<String>();
            if (opening != null) {
                if (!(opening.isNull("abstract"))) {
                    JSONObject absObj = opening.getJSONObject("abstract");
                    String intro = absObj.getString("intro");
                    JSONArray abList = absObj.getJSONArray("list");

//                System.out.println(intro);
                    lst.add(intro);
                    returnPrintJsonStringArray(abList, true).forEach( (stringo) -> lst.add("\t• "+stringo));
//                System.out.println("");
                }
                if (!(opening.isNull("paragraphs"))) {
                    JSONArray paragraphs = opening.getJSONArray("paragraphs");
                    lst.addAll(returnPrintJsonStringArray(paragraphs, false));
                }
//            System.out.println("\n");
            }

            return lst;
        }

        private void printReadingSections(FileWriter fw) throws JSONException, IOException {
            JSONArray readingSec = getReadingSections();
            if (readingSec != null) {
                int length = readingSec.length();
                for (int i = 0; i < length; i++) {
                    printOneReadingSection(fw, readingSec.getJSONObject(i));
//                System.out.println("\n");
                    fw.write("\n\n");
                }
            }
        }

        public ArrayList<String> returnPrintReadingSections() throws JSONException, IOException {
            ArrayList<String> lst = new ArrayList<String>();
            JSONArray readingSec = getReadingSections();
            if (readingSec != null) {
                int length = readingSec.length();
                for (int i = 0; i < length; i++) {
                    lst.addAll(returnPrintOneReadingSection(readingSec.getJSONObject(i)));
//                System.out.println("\n");
                }
            }

            return lst;
        }

        private void printOneReadingSection(FileWriter fw, JSONObject section) throws JSONException, IOException {
            int secNum = section.getInt("section");
            String title = section.getString("title");
//        System.out.println("Section " + secNum + ": " + title);
            fw.write("Section " + secNum + ": " + title + "\n");

            JSONArray paragraphs = section.getJSONArray("paragraphs");
            printJsonStringArray(fw, paragraphs, false);
        }

        public ArrayList<String> returnPrintOneReadingSection(JSONObject section) throws JSONException, IOException {
            ArrayList<String> lst =new ArrayList<String>();

            int secNum = section.getInt("section");
            String title = section.getString("title");
//        System.out.println("Section " + secNum + ": " + title);
            lst.add("<h3><b>Section " + secNum + "</b>: " + title + "</h3>");

            JSONArray paragraphs = section.getJSONArray("paragraphs");
            lst.addAll(returnPrintJsonStringArray(paragraphs, false));
            return lst;
        }

        private void printEoc(FileWriter fw) throws JSONException, IOException {
            JSONObject eoc = getEoc();
            if (eoc != null) {
                if (!(eoc.isNull("summary"))) {
                    JSONObject summary = eoc.getJSONObject("summary");
//                System.out.println(summary.getString("title") + ":");
                    fw.write(summary.getString("title") + ":" + "\n");
                    printJsonStringArray(fw, summary.getJSONArray("paragraphs"), false);
//                System.out.println("\n");
                    fw.write("\n\n");
                }
                if (!(eoc.isNull("review questions"))) {
//                System.out.println("Review Questions:");
                    fw.write("Review Questions:\n");
                    printExercises(fw, eoc.getJSONArray("review questions"), true, true);
//                System.out.println("");
                    fw.write("\n");
                }
                if (!(eoc.isNull("critical thinking"))) {
//                System.out.println("Critical Thinking Questions:");
                    fw.write("Critical Thinking Questions:\n");
                    printExercises(fw, eoc.getJSONArray("critical thinking"), false, true);
//                System.out.println("");
                    fw.write("\n");
                }
                if (!(eoc.isNull("personal application"))) {
//                System.out.println("Personal Application Questions:");
                    fw.write("Personal Application Questions:\n");
                    printExercises(fw, eoc.getJSONArray("personal application"), false, false);
//                System.out.println("");
                    fw.write("\n");
                }
                if (!(eoc.isNull("glossary"))) {
//                System.out.println("Glossary:");
                    fw.write("Glossary:\n");
                    JSONArray glossary = eoc.getJSONArray("glossary");
                    int length = glossary.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject keyTerm = glossary.getJSONObject(i);
                        String term = keyTerm.getString("term");
                        String definition = keyTerm.getString("definition");
//                    System.out.println("\t" + term + " -- " + definition);
                        fw.write("\t" + term + " -- " + definition + "\n");
                    }
                }
            }
        }

        public ArrayList<String> returnPrintEoc() throws JSONException, IOException {
            JSONObject eoc = getEoc();
            ArrayList<String> lst = new ArrayList<String>();
            if (eoc != null) {
                if (!(eoc.isNull("summary"))) {
                    JSONObject summary = eoc.getJSONObject("summary");
//                System.out.println(summary.getString("title") + ":");
                    lst.add("<h4><b>"+summary.getString("title") + "</b>:" + "</h4>");
                    lst.addAll(returnPrintJsonStringArray(summary.getJSONArray("paragraphs"), false));
//                System.out.println("\n");
                }
                if (!(eoc.isNull("review questions"))) {
//                System.out.println("Review Questions:");
                    lst.add("<h4><b>Review Questions</b>:</h4>");
                    lst.addAll(returnPrintExercises(eoc.getJSONArray("review questions"), true, true));
//                System.out.println("");
                }
                if (!(eoc.isNull("critical thinking"))) {
//                System.out.println("Critical Thinking Questions:");
                    lst.add("<h4><b>Critical Thinking Questions</b>:</h4>");
                    lst.addAll(returnPrintExercises(eoc.getJSONArray("critical thinking"), false, true));
//                System.out.println("");
                }
                if (!(eoc.isNull("personal application"))) {
//                System.out.println("Personal Application Questions:");
                    lst.add("<h4><b>Personal Application Questions</b>:</h4>");
                    lst.addAll(returnPrintExercises(eoc.getJSONArray("personal application"), false, false));
//                System.out.println("");
                }
                if (!(eoc.isNull("glossary"))) {
//                System.out.println("Glossary:");
                    lst.add("<h4><b>Glossary</b>:</h4>");
                    JSONArray glossary = eoc.getJSONArray("glossary");
                    int length = glossary.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject keyTerm = glossary.getJSONObject(i);
                        String term = keyTerm.getString("term");
                        String definition = keyTerm.getString("definition");
//                    System.out.println("\t" + term + " -- " + definition);
                        lst.add("<b>" + term + "</b> -- " + definition + "\n");
                    }
                }
            }

            return lst;
        }

        private void printExercises(FileWriter fw, JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException, IOException {
            int length = exercises.length();
            for (int i = 0; i < length; i++) {
                JSONObject ex = exercises.getJSONObject(i);
                int exNum = ex.getInt("exercise");
                String problem = ex.getString("problem");
//            System.out.println("\t" + "Exercise " + exNum + ":");
//            System.out.println("\t" + "Problem: " + problem);
                fw.write("\t" + "Exercise " + exNum + ":\n");
                fw.write("\t" + "Problem: " + problem + "\n");
                if (hasOptions) {
                    printExerciseOptions(fw, ex);
                }
                if (hasSolution) {
                    String solution = ex.getString("solution");
//                System.out.println("\t" + "Solution: " + solution);
                    fw.write("\t" + "Solution: " + solution + "\n");
                }
//            System.out.println("");
                fw.write("\n");
            }
        }

        public ArrayList<String> returnPrintExercises(JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException, IOException {
            int length = exercises.length();
            ArrayList<String> lst = new ArrayList<String>();

            for (int i = 0; i < length; i++) {
                JSONObject ex = exercises.getJSONObject(i);
                int exNum = ex.getInt("exercise");
                String problem = ex.getString("problem");
//            System.out.println("\t" + "Exercise " + exNum + ":");
//            System.out.println("\t" + "Problem: " + problem);
                lst.add("<h5><b>Exercise " + exNum + "</b>:</h5>");
                lst.add("<b>Problem</b>: " + problem);

                if (hasOptions) {
                    lst.addAll(returnPrintExerciseOptions(ex));
                }

                if (hasSolution) {
                    String solution = ex.getString("solution");
//                System.out.println("\t" + "Solution: " + solution);
                    lst.add("<b>Solution</b>: " + solution);
                }
//            System.out.println("");
            }

            return lst;
        }

        private void printExerciseOptions(FileWriter fw, JSONObject exercise) throws JSONException, IOException {
            JSONArray options = exercise.getJSONArray("options");
            int length = options.length();
            char letterChoice = 'A';
            for (int i = 0; i < length; i++) {
                String output = "\t\t" + letterChoice + ". " + options.getString(i);
//            System.out.println(output);
                fw.write(output + "\n");
                letterChoice++;
            }
        }

        public ArrayList<String> returnPrintExerciseOptions(JSONObject exercise) throws JSONException, IOException {
            JSONArray options = exercise.getJSONArray("options");
            ArrayList<String> lst = new ArrayList<String>();
            int length = options.length();
            char letterChoice = 'A';
            for (int i = 0; i < length; i++) {
                String output = "\t\t" + letterChoice + ". " + options.getString(i);
//            System.out.println(output);
                lst.add(output);
                letterChoice++;
            }

            return lst;
        }

        private void printJsonStringArray(FileWriter fw, JSONArray array, boolean indent) throws JSONException, IOException {
            int length = array.length();
            for (int i = 0; i < length; i++) {
                String output = "";
                if (indent) {
                    output += "\t";
                }
                output += array.getString(i);
//            System.out.println(output);
                fw.write(output + "\n");
            }
        }

        public ArrayList<String> returnPrintJsonStringArray(JSONArray array, boolean indent) throws JSONException, IOException {
            ArrayList<String> lst = new ArrayList<String>();
            int length = array.length();
            for (int i = 0; i < length; i++) {
                String output = "";
                if (indent) {
                    output += "\t";
                }
                output += array.getString(i);
//            System.out.println(output);
                lst.add(output + "\n");
            }

            return lst;
        }


    }
}