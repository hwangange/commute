package com.interns.team3.openstax.myttsapplication;

import android.content.Context;

import com.interns.team3.openstax.myttsapplication.ssml.SsmlBuilder;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.interns.team3.openstax.myttsapplication.AudioClient.AmazonClient;

public class Module {
    private String title;
    private String id;
    private String moduleFile;
    private Document content;
    private String moduleNum;
    private ArrayList<String> nonReadingSections = new ArrayList<>();

    Module(Element section, String moduleFile, String moduleNum) {
        this.title = section.select("md|title").text();
        this.id = section.attr("document");
        this.moduleNum = moduleNum;
        this.moduleFile = moduleFile;
        cleanContent();
        this.content = Jsoup.parse(this.moduleFile, "UTF-8");
        this.nonReadingSections.add("summary");
        this.nonReadingSections.add("review-questions");
        this.nonReadingSections.add("critical-thinking");
        this.nonReadingSections.add("personal-application");
    }

    public String getTitle() {
        return this.title;
    }

    public String getId() {
        return this.id;
    }

    public String getModuleNum() {
        return this.moduleNum;
    }

    public Document getContent() {
        return this.content;
    }

    private Element getBody() {
        return this.content.body();
    }

    private void cleanContent() {
        this.moduleFile = this.moduleFile
                .replaceAll("&amp;", "and")
                .replaceAll("________", "blank")
                .replaceAll("&#8217;", "'")
                .replaceAll("&#8220;|&#8221;", "\"")
                .replaceAll("&#8211;", "-")
                .replaceAll("\\[link]", "");
    }

    private List<TextAudioChunk> initTextAudioChunks() throws JSONException {
        List<String> textList = modulePageText();
        List<String> ssmlList = buildModuleSSML();
        if (textList.size() != ssmlList.size()) {
            System.err.printf("ERROR: Text and SSML lists are not of same length \n" +
                            "Text List Length: %d\n " +
                            "SSML List Length: %d\n",
                    textList.size(), ssmlList.size());
        }

        List<TextAudioChunk> chunks = new ArrayList<>();
        int length = textList.size();
        for (int i = 0; i < length; i++) {
            TextAudioChunk chunk = new TextAudioChunk();
            chunk.setId(i);
            chunk.setText(textList.get(i));
            chunk.setSsml(ssmlList.get(i));
            chunks.add(chunk);
        }

        return chunks;
    }


    //************* JSON FUNCTIONS ******************//

    public JSONObject toJson() throws JSONException {
        JSONObject moduleObj = new JSONObject();
        moduleObj.put("title", this.title);
        moduleObj.put("id", this.id);

        JSONObject openingObj = pullOpening();
        moduleObj.put("opening", openingObj);
        moduleObj.put("reading sections", JSONObject.NULL);
        moduleObj.put("eom", JSONObject.NULL);
        if (!(this.title.equals("Introduction"))) {
            JSONArray readingSecArray = pullAllReadingSections();
            JSONObject eomObj = pullEomSections();

            moduleObj.put("reading sections", readingSecArray);
            moduleObj.put("eom", eomObj);
        }

        return moduleObj;
    }

    public JSONObject getOpening() throws JSONException {
        JSONObject module = toJson();
        return module.optJSONObject("opening");
    }

    private JSONArray getReadingSections() throws JSONException {
        JSONObject module = toJson();
        return module.optJSONArray("reading sections");
    }

    private JSONObject getEom() throws JSONException {
        JSONObject module = toJson();
        return module.optJSONObject("eom");
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
        String title = section.getElementsByAttributeValue("data-type", "title").first().text();
        JSONObject paraObj = pullParagraphs(section);
        JSONObject sectionObj = new JSONObject();
        sectionObj.put("title", title);
        sectionObj.put("paragraphs", paraObj.get("paragraphs"));
        return sectionObj;
    }

    private JSONArray pullAllReadingSections() throws JSONException {
        Elements sections = getBody().select("> section");
        sections.removeIf(x -> this.nonReadingSections.contains(x.className()));

        JSONArray readingSecArray = new JSONArray();
        int count = 1;
        for (Element section : sections) {
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
        for (Element exercise : exercises) {
            JSONObject exObj = pullExercise(exercise, multiChoice, hasSolution);
            exObj.put("exercise", count);
            allExArray.put(exObj);
            count++;
        }
        return allExArray;
    }

    private JSONArray pullGlossary() throws JSONException {
        Element glossary = getBody().getElementsByAttributeValue("data-type", "glossary").first();
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

    private JSONObject pullEomSections() throws JSONException {
        Element reviewQs = getBody().getElementsByClass("review-questions").first();
        Element critThink = getBody().getElementsByClass("critical-thinking").first();
        Element personalAp = getBody().getElementsByClass("personal-application").first();

        JSONObject summaryObj = pullSummary();
        JSONArray reviewQsArray = pullSectionExercises(reviewQs, true, true);
        JSONArray critThinkArray = pullSectionExercises(critThink, false, true);
        JSONArray personalApArray = pullSectionExercises(personalAp, false, false);
        JSONArray glossaryObj = pullGlossary();

        JSONObject eomObj = new JSONObject();
        eomObj.put("summary", summaryObj);
        eomObj.put("review questions", reviewQsArray);
        eomObj.put("critical thinking", critThinkArray);
        eomObj.put("personal application", personalApArray);
        eomObj.put("glossary", glossaryObj);

        return eomObj;
    }


    //************* SSML FUNCTIONS ******************//

    public List<String> buildModuleSSML() throws JSONException {
        List<String> opening = buildOpeningSSML();
        List<List<String>> readingSec = buildReadSecSSMLArray();
        List<String> eom = buildEomSSML();

        List<String> module = new ArrayList<>(opening);
        for (List<String> section : readingSec) {
            module.addAll(section);
        }
        module.addAll(eom);

        return module;
    }

    private List<String> buildOpeningSSML() throws JSONException {
        List<String> ssmlList = new ArrayList<>();
        JSONObject opening = getOpening();

        SsmlBuilder titleSsml = new SsmlBuilder();
        titleSsml.text(this.title).newParagraph();
        ssmlList.add(titleSsml.build());

        if (opening != null) {
            if (!(opening.isNull("abstract"))) {
                JSONObject absObj = opening.getJSONObject("abstract");
                String intro = absObj.getString("intro");
                JSONArray abList = absObj.getJSONArray("list");
                SsmlBuilder absSsml = new SsmlBuilder();

                absSsml.sentence(intro).comma();
                ssmlList.add(absSsml.build());
                buildArraySSML(ssmlList, abList, false);
//                ssmlList.add(new SsmlBuilder().newParagraph().build());
            }
            if (!(opening.isNull("paragraphs"))) {
                JSONArray paragraphs = opening.getJSONArray("paragraphs");
                buildArraySSML(ssmlList, paragraphs, true);
            }
        }

        return ssmlList;
    }

    private List<List<String>> buildReadSecSSMLArray() throws JSONException {
        List<List<String>> secArray = new ArrayList<>();
        JSONArray readingSec = getReadingSections();
        if (readingSec != null) {
            int length = readingSec.length();
            for (int i = 0; i < length; i++) {
                JSONObject sectionObj = readingSec.getJSONObject(i);
                List<String> sectionSSML = buildOneReadingSectionSSML(sectionObj);
                secArray.add(sectionSSML);
            }
        }

        return secArray;
    }

    private List<String> buildOneReadingSectionSSML(JSONObject section) throws JSONException {
        List<String> ssmlList = new ArrayList<>();
        int secNum = section.getInt("section");
        String title = section.getString("title");
        JSONArray paragraphs = section.getJSONArray("paragraphs");
        SsmlBuilder ssml = new SsmlBuilder();

        ssml.text("Section " + secNum).strongBreak().text(title).newParagraph();
        ssmlList.add(ssml.build());
        buildArraySSML(ssmlList, paragraphs, true);
        return ssmlList;
    }

    private List<String> buildEomSSML() throws JSONException {
        List<String> ssmlList = new ArrayList<>();
        JSONObject eom = getEom();
        if (eom != null) {
            if (!(eom.isNull("summary"))) {
                SsmlBuilder summarySsml = new SsmlBuilder();
                JSONObject summary = eom.getJSONObject("summary");
                summarySsml.text(summary.getString("title")).newParagraph();
                ssmlList.add(summarySsml.build());

                buildArraySSML(ssmlList, summary.getJSONArray("paragraphs"), true);
            }
            if (!(eom.isNull("review questions"))) {
                SsmlBuilder revQSsml = new SsmlBuilder();
                revQSsml.text("Review Questions:").newParagraph();
                ssmlList.add(revQSsml.build());

                buildExerciseSSML(ssmlList, eom.getJSONArray("review questions"), true, true);
            }
            if (!(eom.isNull("critical thinking"))) {
                SsmlBuilder critSsml = new SsmlBuilder();
                critSsml.text("Critical Thinking Questions:").newParagraph();
                ssmlList.add(critSsml.build());

                buildExerciseSSML(ssmlList, eom.getJSONArray("critical thinking"), false, true);
            }
            if (!(eom.isNull("personal application"))) {
                SsmlBuilder pAppSsml = new SsmlBuilder();
                pAppSsml.text("Personal Application Questions:").newParagraph();
                ssmlList.add(pAppSsml.build());

                buildExerciseSSML(ssmlList, eom.getJSONArray("personal application"), false, false);
            }
            if (!(eom.isNull("glossary"))) {
                SsmlBuilder glossSsml = new SsmlBuilder();
                glossSsml.text("Glossary:").newParagraph();
                ssmlList.add(glossSsml.build());

                JSONArray glossary = eom.getJSONArray("glossary");
                int length = glossary.length();
                for (int i = 0; i < length; i++) {
                    SsmlBuilder keyTermSsml = new SsmlBuilder();
                    JSONObject keyTerm = glossary.getJSONObject(i);
                    String term = keyTerm.getString("term");
                    String definition = keyTerm.getString("definition");
                    keyTermSsml.text(term).comma().sentence(definition).strongBreak();
                    ssmlList.add(keyTermSsml.build());
                }
            }
        }

        return ssmlList;
    }

    private void buildExerciseSSML(List<String> ssmlList, JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException {
        int length = exercises.length();
        for (int i = 0; i < length; i++) {
            SsmlBuilder ssml = new SsmlBuilder();
            JSONObject ex = exercises.getJSONObject(i);
            int exNum = ex.getInt("exercise");
            String problem = ex.getString("problem");
            ssml.text("Exercise " + exNum).newParagraph();
            ssmlList.add(ssml.build());
            ssml.reset().sentence(problem).strongBreak();
            ssmlList.add(ssml.build());

            if (hasOptions) {
                buildExerciseOptionsSSML(ssmlList, ex);
            }

            if (hasSolution) {
                SsmlBuilder solutionSsml = new SsmlBuilder();
                solutionSsml.newParagraph().newParagraph();
                solutionSsml.text("Solution").strongBreak();
                String solution = ex.getString("solution");
                solutionSsml.sentence(solution).newParagraph();
                ssmlList.add(solutionSsml.build());
            }
        }
    }

    private void buildExerciseOptionsSSML(List<String> ssmlList, JSONObject exercise) throws JSONException {
        JSONArray options = exercise.getJSONArray("options");
        int length = options.length();
        char letterChoice = 'A';
        for (int i = 0; i < length; i++) {
            SsmlBuilder ssml = new SsmlBuilder();
            String output = options.getString(i);
            ssml.text(String.valueOf(letterChoice)).comma().sentence(output).strongBreak();
            ssmlList.add(ssml.build());
            letterChoice++;
        }
    }

    private void buildArraySSML(List<String> ssmlList, JSONArray array, boolean paragraphs) throws JSONException {
        int length = array.length();
        for (int i = 0; i < length; i++) {
            SsmlBuilder ssml = new SsmlBuilder();
            String output = array.getString(i);
            if (paragraphs) {
                ssml.paragraph(output).newParagraph();
            } else {
                ssml.sentence(output).strongBreak();
            }

            if (i == length - 1) {
                ssml.newParagraph();
            }
            ssmlList.add(ssml.build());
        }
    }


    //************* AUDIO FUNCTIONS ******************//

    public void makeOpeningAudio(String folder, Context context) {
        makeOpeningAudio(folder, context, false);
    }

    public void makeOpeningAudio(String folder, Context context, boolean debug) {
        try {
            String audioFolder = folder + this.id + "/";
            AmazonClient client = new AmazonClient(audioFolder, context);
            List<String> openingSSML = buildOpeningSSML();
            System.out.format("Synthesizing audio of file \"%s.mp3\" (%s)\n", this.id + "/opening", this.title);
            client.synthesizeAudio("opening", true, openingSSML, debug);
            System.out.format("Finished audio file \"%s.mp3\" (%s)\n", this.id + "/opening", this.title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeReadingSecAudio(String folder, Context context) {
        makeReadingSecAudio(folder, context, false);
    }

    public void makeReadingSecAudio(String folder, Context context, boolean debug) {
        try {
            String audioFolder = folder + this.id + "/";
            AmazonClient client = new AmazonClient(audioFolder, context);
            List<List<String>> readingSecSSML = buildReadSecSSMLArray();
//            for (List<String> ssmlList : readingSecSSML) {
//                int secNum = readingSecSSML.indexOf(ssmlList) + 1;
//                System.out.format("Synthesizing audio of file \"%s-%dmp3\" (%s)\n", this.id + "/section", secNum, this.title);
//                client.synthesizeAudio(String.format(Locale.ENGLISH, "section-%d", secNum),
//                        true, ssmlList, debug);
//                System.out.format("Finished audio file \"%s-%dmp3\" (%s)\n", this.id + "/section", secNum, this.title);
//            }
            readingSecSSML.parallelStream().forEach(ssmlList -> {
                int secNum = readingSecSSML.indexOf(ssmlList) + 1;
                System.out.format("Synthesizing audio of file \"%s-%d.mp3\" (%s)\n", this.id + "/section", secNum, this.title);
                client.synthesizeAudio(String.format(Locale.ENGLISH, "section-%d", secNum),
                        true, ssmlList, debug);
                System.out.format("Finished audio file \"%s-%d.mp3\" (%s)\n", this.id + "/section", secNum, this.title);
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeEomAudio(String folder, Context context) {
        makeEomAudio(folder, context, false);
    }

    public void makeEomAudio(String folder, Context context, boolean debug) {
        try {
            String audioFolder = folder + this.id + "/";
            AmazonClient client = new AmazonClient(audioFolder, context);
            List<String> eomSSML = buildEomSSML();
            System.out.format("Synthesizing audio of file \"%s.mp3\" (%s)\n", this.id + "/eom", this.title);
            client.synthesizeAudio("eom", true, eomSSML, debug);
            System.out.format("Finished audio file \"%s.mp3\" (%s)\n", this.id + "/eom", this.title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeModuleAudio(String folder, Context context) {
        makeModuleAudio(folder, context,  false, false);
    }

    public void makeModuleAudio(String folder, Context context, boolean sectioned) {
        makeModuleAudio(folder, context, sectioned, false);
    }

    public void makeModuleAudio(String folder, Context context, boolean sectioned, boolean debug) {
//        try {
//            Thread openingThr = new Thread (() -> makeOpeningAudio(folder, context, debug));
//            openingThr.start();
//            if (!(this.title.equals("Introduction"))) {
//                Thread readSecThr = new Thread (() -> makeReadingSecAudio(folder, context, debug));
//                Thread eomThr = new Thread (() -> makeEomAudio(folder, context, debug));
//                readSecThr.start();
//                eomThr.start();
//                readSecThr.join();
//                eomThr.join();
//            }
//            openingThr.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (!sectioned) {
//            File moduleFolder = new File(folder + "/" + this.id);
//            List<String> sectionFiles = Arrays.stream(moduleFolder.list())
//                    .filter(file -> file.contains("section"))
//                    .map(file -> this.id + "/" + file.replaceAll(".mp3", ""))
//                    .peek(System.out::println)
//                    .collect(Collectors.toList());
//
//            List<String> audioFiles = new ArrayList<>();
//            audioFiles.add(this.id + "/opening");
//            audioFiles.addAll(sectionFiles);
//            audioFiles.add(this.id + "/eom");
////
//            combineMP3(folder, this.id + "/module", audioFiles, true, false);
        if (sectioned) {
            try {
                Thread openingThr = new Thread (() -> makeOpeningAudio(folder, context, debug));
                openingThr.start();
                if (!(this.title.equals("Introduction"))) {
                    Thread readSecThr = new Thread (() -> makeReadingSecAudio(folder, context, debug));
                    Thread eomThr = new Thread (() -> makeEomAudio(folder, context, debug));
                    readSecThr.start();
                    eomThr.start();
                    readSecThr.join();
                    eomThr.join();
                }
                openingThr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String audioFolder = folder + this.id + "/";
                AmazonClient client = new AmazonClient(audioFolder, context);
                List<String> moduleSSML = buildModuleSSML();
                System.out.format("Synthesizing audio of file \"%s.mp3\" (%s)\n", this.id + "/module", this.title);
                client.synthesizeAudio("module", true, moduleSSML, debug);
                System.out.format("Finished audio file \"%s.mp3\" (%s)\n", this.id + "/module", this.title);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void makeFollowAlongAudio(String folder, Context context) {
        makeFollowAlongAudio(folder, context, false);
    }

    public void makeFollowAlongAudio(String folder, Context context, boolean debug) {
        try {
            List<TextAudioChunk> chunks = initTextAudioChunks();
            String audioFolder = folder + this.id + "/";
            AmazonClient client = new AmazonClient(audioFolder, context);
            chunks.parallelStream().forEach(chunk -> {
                int id = chunk.getId();
                String ssml = chunk.getSsml();
//                System.out.println(ssml);
                client.synthesizeAudio(String.valueOf(id), true, ssml, debug);
                chunk.setAudioFile(audioFolder + id + ".mp3");
                chunk.synthesized();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //************* PRINTING/TEXT FUNCTIONS ******************//

    public void printModulePage(String fileString) {
        try {
            File file = new File(fileString);
            FileWriter fw = new FileWriter(file);
//            System.out.println(this.title + "\n");
            fw.write(this.title + "\n\n");
            printOpening(fw);
            printReadingSections(fw);
            printEom(fw);
            fw.flush();
            fw.close();
            System.out.println("File successfully written");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> modulePageText() throws JSONException {
        List<String> textList = new ArrayList<>();
//        System.out.println(this.title + "\n");
//        textList.add(this.title);
        openingText(textList);
        readingSecText(textList);
        eomText(textList);
        return textList;
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

    private void openingText(List<String> textList) throws JSONException {
        JSONObject opening = getOpening();
        textList.add(this.title);
        if (opening != null) {
            if (!(opening.isNull("abstract"))) {
                JSONObject absObj = opening.getJSONObject("abstract");
                String intro = absObj.getString("intro");
                JSONArray abList = absObj.getJSONArray("list");

//                System.out.println(intro);
                textList.add(intro);
                textFromArray(textList, abList, true);
//                System.out.println("");
            }
            if (!(opening.isNull("paragraphs"))) {
                JSONArray paragraphs = opening.getJSONArray("paragraphs");
                textFromArray(textList, paragraphs, false);
            }
//            System.out.println("\n");
        }
    }

    private void printReadingSections(FileWriter fw) throws JSONException, IOException{
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

    private void readingSecText(List<String> textList) throws JSONException {
        JSONArray readingSec = getReadingSections();
        if (readingSec != null) {
            int length = readingSec.length();
            for (int i = 0; i < length; i++) {
                oneReadingSecText(textList, readingSec.getJSONObject(i));
//                System.out.println("\n");
            }
        }
    }

    private void printOneReadingSection(FileWriter fw, JSONObject section) throws JSONException, IOException {
        int secNum = section.getInt("section");
        String title = section.getString("title");
//        System.out.println("Section " + secNum + ": " + title);
        fw.write("Section " + secNum + ": " + title + "\n");

        JSONArray paragraphs = section.getJSONArray("paragraphs");
        printJsonStringArray(fw, paragraphs, false);
    }

    private void oneReadingSecText(List<String> textList, JSONObject section) throws JSONException {
        int secNum = section.getInt("section");
        String title = section.getString("title");
//        System.out.println("Section " + secNum + ": " + title);
        textList.add(String.format(Locale.ENGLISH, "<h3><b>Section %d</b>: %s</h3>", secNum, title));


        JSONArray paragraphs = section.getJSONArray("paragraphs");
        textFromArray(textList, paragraphs, false);
    }

    private void printEom(FileWriter fw) throws JSONException, IOException {
        JSONObject eom = getEom();
        if (eom != null) {
            if (!(eom.isNull("summary"))) {
                JSONObject summary = eom.getJSONObject("summary");
//                System.out.println(summary.getString("title") + ":");
                fw.write(summary.getString("title") + ":" + "\n");
                printJsonStringArray(fw, summary.getJSONArray("paragraphs"), false);
//                System.out.println("\n");
                fw.write("\n\n");
            }
            if (!(eom.isNull("review questions"))) {
//                System.out.println("Review Questions:");
                fw.write("Review Questions:\n");
                printExercises(fw, eom.getJSONArray("review questions"), true, true);
//                System.out.println("");
                fw.write("\n");
            }
            if (!(eom.isNull("critical thinking"))) {
//                System.out.println("Critical Thinking Questions:");
                fw.write("Critical Thinking Questions:\n");
                printExercises(fw, eom.getJSONArray("critical thinking"), false, true);
//                System.out.println("");
                fw.write("\n");
            }
            if (!(eom.isNull("personal application"))) {
//                System.out.println("Personal Application Questions:");
                fw.write("Personal Application Questions:\n");
                printExercises(fw, eom.getJSONArray("personal application"), false, false);
//                System.out.println("");
                fw.write("\n");
            }
            if (!(eom.isNull("glossary"))) {
//                System.out.println("Glossary:");
                fw.write("Glossary:\n");
                JSONArray glossary = eom.getJSONArray("glossary");
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

    private void eomText(List<String> textList) throws JSONException {
        JSONObject eom = getEom();
        if (eom != null) {
            if (!(eom.isNull("summary"))) {
                JSONObject summary = eom.getJSONObject("summary");
//                System.out.println(summary.getString("title") + ":");
                String sumTitle = String.format(Locale.ENGLISH, "<h4><b>%s</b>:</h4>", summary.getString("title"));
                textList.add(sumTitle);
                textFromArray(textList, summary.getJSONArray("paragraphs"), false);
//                System.out.println("\n");
            }
            if (!(eom.isNull("review questions"))) {
//                System.out.println("Review Questions:");
                String revQuesTitle = "<h4><b>Review Questions</b>:</h4>";
                textList.add(revQuesTitle);
                exercisesText(textList, eom.getJSONArray("review questions"), true, true);
//                System.out.println("");
            }
            if (!(eom.isNull("critical thinking"))) {
//                System.out.println("Critical Thinking Questions:");
                String critTitle = "<h4><b>Critical Thinking Questions</b>:</h4>";
                textList.add(critTitle);
                exercisesText(textList, eom.getJSONArray("critical thinking"), false, true);
//                System.out.println("");
            }
            if (!(eom.isNull("personal application"))) {
//                System.out.println("Personal Application Questions:");
                String perAppTitle = "<h4><b>Personal Application Questions</b>:</h4>";
                textList.add(perAppTitle);
                exercisesText(textList, eom.getJSONArray("personal application"), false, false);
//                System.out.println("");
            }
            if (!(eom.isNull("glossary"))) {
//                System.out.println("Glossary:");
                String glossTitle = "<h4><b>Glossary</b>:</h4>";
                textList.add(glossTitle);
                JSONArray glossary = eom.getJSONArray("glossary");
                int length = glossary.length();
                for (int i = 0; i < length; i++) {
                    JSONObject keyTerm = glossary.getJSONObject(i);
                    String term = keyTerm.getString("term");
                    String definition = keyTerm.getString("definition");
//                    System.out.println("\t" + term + " -- " + definition);
                    textList.add(String.format(Locale.ENGLISH, "<b>%s</b> -- %s", term, definition));
                }
            }
        }
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

    private void exercisesText(List<String> textList, JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException {
        int length = exercises.length();
        for (int i = 0; i < length; i++) {
            JSONObject ex = exercises.getJSONObject(i);
            int exNum = ex.getInt("exercise");
            String problem = ex.getString("problem");
//            System.out.println("\t" + "Exercise " + exNum + ":");
//            System.out.println("\t" + "Problem: " + problem);
            textList.add(String.format(Locale.ENGLISH, "<h5><b>Exercise %d</b>:</h5>", exNum));
            textList.add(String.format(Locale.ENGLISH, "<b>Problem</b>: %s", problem));
            if (hasOptions) {
                exerciseOptionsText(textList, ex);
            }
            if (hasSolution) {
                String solution = ex.getString("solution");
//                System.out.println("\t" + "Solution: " + solution);
                textList.add(String.format(Locale.ENGLISH, "<b>Solution</b>: %s", solution));
            }
//            System.out.println("");
        }
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

    private void exerciseOptionsText(List<String> textList, JSONObject exercise) throws JSONException {
        JSONArray options = exercise.getJSONArray("options");
        int length = options.length();
        char letterChoice = 'A';
        for (int i = 0; i < length; i++) {
            String output = "\t\t" + letterChoice + ". " + options.getString(i);
//            System.out.println(output);
            textList.add(output);
            letterChoice++;
        }
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

    private void textFromArray(List<String> textList, JSONArray array, boolean indent) throws JSONException {
        int length = array.length();
        for (int i = 0; i < length; i++) {
            String output = "";
            if (indent) {
                output += "\t• ";
            }
            output += array.getString(i);
//            System.out.println(output);
            textList.add(output);
        }
    }
}