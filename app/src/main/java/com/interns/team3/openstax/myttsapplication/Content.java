package com.interns.team3.openstax.myttsapplication;

import com.interns.team3.openstax.myttsapplication.ssml.SsmlBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public interface Content {

    String getTitle();

    String getId();

    class Book implements Content {

        private String title;
        private String id;

        public Book(Book b) {
            this.title = b.getTitle();
            this.id = b.getId();

        }

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

    class Unit implements Content {

        private String title;
        private String id;
        private String unitNum;

        public Unit(Unit u) {
            this.title = u.getTitle();
            this.id = u.getId();
            this.unitNum = this.getUnitNum();
        }

        public Unit(String title, String id, String unitNum) {
            this.title = title;
            this.id = id;
            this.unitNum = unitNum;
        }

        @Override
        public String getTitle() {
            return title;
        }


        @Override
        public String getId() {
            return id;
        }

        public String getUnitNum() {
            return unitNum;
        }
    }

    class Chapter implements Content {

        private String title;
        private String id;
        private String chapterNum;

        public Chapter(Chapter c) {
            this.title = c.getTitle();
            this.id = c.getId();
            this.chapterNum = c.getChapterNum();
        }

        public Chapter(String title, String id, String chapterNum) {
            this.title = title;
            this.id = id;
            this.chapterNum = chapterNum;
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
            return this.chapterNum;
        }

    }

    class Module implements Content {

        private String title;
        private String id;
        private String moduleFile;
        private Document content;
        private String moduleNum;
        private int volume = 6;
        private List<EomSection> eomSections;

        Module(Module mod) {
            this.title = mod.getTitle();
            this.id = mod.getId();
            this.moduleNum = mod.getModuleNum();
            this.moduleFile = mod.getModuleFile();
            this.content = mod.getContent();
            this.volume = mod.getVolume();
            this.eomSections = mod.getEomSections();
        }

        Module(String modId, String moduleFile) {
            this.id = modId;
            this.moduleFile = moduleFile;
            this.moduleNum = "";
            cleanContent();
            this.content = Jsoup.parse(this.moduleFile, "UTF-8");
            this.title = this.content.body().getElementsByAttributeValue("data-type", "document-title").first().text();
            setEomSections();
        }

        Module(Element section, String moduleFile, String moduleNum) {
            this.title = section.select("md|title").text();
            this.id = section.attr("document");
            this.moduleNum = moduleNum;
            this.moduleFile = moduleFile;
            cleanContent();
            this.content = Jsoup.parse(this.moduleFile, "UTF-8");
            setEomSections();
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public String getModuleNum() {
            return this.moduleNum;
        }

        public String getModuleFile() {
            return this.moduleFile;
        }

        public Document getContent() {
            return this.content;
        }

        public int getVolume() {
            return this.volume;
        }

        public List<EomSection> getEomSections() {
            return this.eomSections;
        }

        private void setEomSections() {
            List<EomSection> sections = new ArrayList<>();

            sections.add(new EomSection("summary", "summary"));

            // Psychology
            sections.add(new EomSection("review-questions", "exercise", true, true));
            sections.add(new EomSection("critical-thinking", "exercise", false, true));
            sections.add(new EomSection("personal-application", "exercise", false, false));

            // Biology
            sections.add(new EomSection("multiple-choice", "exercise", true, true));
            sections.add(new EomSection("free-response", "exercise", false, true));
            sections.add(new EomSection("art-exercise", "exercise", true, true));

            sections.add(new EomSection("glossary", "glossary"));

            this.eomSections = sections;
        }

        private Element getBody() {
            return this.content.body();
        }

        private void cleanContent() {
            this.moduleFile = this.moduleFile
                    .replaceAll("&amp;", "and")
                    .replaceAll("________", "blank")
                    .replaceAll("&#8216;|&#8217;", "'")
                    .replaceAll("&#8220;|&#8221;", "\"")
                    .replaceAll("&#8211;", "-")
                    .replaceAll("\\[link]", "");
        }

        public List<TextAudioChunk> initTextAudioChunks() throws JSONException {
            return initTextAudioChunks(6);
        }

        public List<TextAudioChunk> initTextAudioChunks(int volume) throws JSONException {
            this.volume = volume;
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

        private JSONObject pullOpening() throws JSONException {
            JSONObject absObj = pullAbstract();
            JSONObject paraObj = pullParagraphs(getBody());

            JSONObject openingObj = new JSONObject();
            openingObj.put("abstract", absObj.get("abstract"));
            openingObj.put("paragraphs", paraObj.get("paragraphs"));

            return openingObj;
        }

        private JSONArray pullAllReadingSections() throws JSONException {
            Elements sections = getBody().select("> section");
            List<String> eomClasses = this.eomSections.stream().map(EomSection::getClassName).collect(Collectors.toList());
            sections.removeIf(section -> eomClasses.contains(section.className()));

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

        private JSONObject pullReadingSection(Element section) throws JSONException {
            String title = section.getElementsByAttributeValue("data-type", "title").first().text();
            JSONObject paraObj = pullParagraphs(section);
            JSONObject sectionObj = new JSONObject();
            sectionObj.put("title", title);
            sectionObj.put("paragraphs", paraObj.get("paragraphs"));
            return sectionObj;
        }

        // also do section.select  figures with images
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

        private JSONObject pullEomSections() throws JSONException {
            JSONObject eomObj = new JSONObject();
            for (EomSection section : this.eomSections) {
                Element elem = getBody().getElementsByClass(section.getClassName()).first();
                if (elem == null) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                switch (section.getSecType()) {
                    case ("summary"): {
                        obj = pullReadingSection(elem);
                        break;
                    }
                    case ("exercise"): {
                        obj = pullSectionExercises(elem, section.isMultiChoice(), section.hasSolution());
                        break;
                    }
                    case ("glossary"): {
                        obj = pullGlossary();
                    }
                }
                eomObj.put(section.getClassName(), obj);
            }

            return eomObj;
        }

        private JSONObject pullSectionExercises(Element section, boolean multiChoice, boolean hasSolution) throws JSONException {
            if (section == null) {
                return null;
            }
            String title = section.getElementsByAttributeValue("data-type", "title").first().text();
            Elements exercises = section.getElementsByAttributeValue("data-type", "exercise");
            JSONArray allExArray = new JSONArray();
            int count = 1;
            for (Element exercise : exercises) {
                JSONObject exObj = pullExercise(exercise, multiChoice, hasSolution);
                exObj.put("exercise", count);
                allExArray.put(exObj);
                count++;
            }
            return new JSONObject().put("title", title).put("exercises", allExArray);
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

        private JSONObject pullGlossary() throws JSONException {
            Element glossary = getBody().getElementsByAttributeValue("data-type", "glossary").first();
            String title = glossary.getElementsByAttributeValue("data-type", "glossary-title").first().text();
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

            return new JSONObject().put("title", title).put("key terms", glossaryArray);
        }



        //************* SSML FUNCTIONS ******************//
        List<String> buildModuleSSML() throws JSONException {
            List<String> ssmlList = new ArrayList<>();
            buildOpeningSSML(ssmlList);
            buildReadSecSSMLArray(ssmlList);
            buildEomSSML(ssmlList);
            return ssmlList;
        }

        private void buildOpeningSSML(List<String> ssmlList) throws JSONException {
            JSONObject opening = getOpening();

            SsmlBuilder titleSsml = new SsmlBuilder(this.volume);
            titleSsml.text(this.title).newParagraph();
            ssmlList.add(titleSsml.build());

            if (opening != null) {
                if (!(opening.isNull("abstract"))) {
                    JSONObject absObj = opening.getJSONObject("abstract");
                    String intro = absObj.getString("intro");
                    JSONArray abList = absObj.getJSONArray("list");
                    SsmlBuilder absSsml = new SsmlBuilder(this.volume);

                    absSsml.sentence(intro).comma();
                    ssmlList.add(absSsml.build());
                    buildArraySSML(ssmlList, abList, false);
                }
                if (!(opening.isNull("paragraphs"))) {
                    JSONArray paragraphs = opening.getJSONArray("paragraphs");
                    buildArraySSML(ssmlList, paragraphs, true);
                }
            }
        }

        private void buildReadSecSSMLArray(List<String> ssmlList) throws JSONException {
            JSONArray readingSec = getReadingSections();
            if (readingSec != null) {
                int length = readingSec.length();
                for (int i = 0; i < length; i++) {
                    buildOneReadingSectionSSML(ssmlList, readingSec.getJSONObject(i));
                }
            }
        }

        private void buildOneReadingSectionSSML(List<String> ssmlList, JSONObject section) throws JSONException {
            int secNum = section.getInt("section");
            String title = section.getString("title");
            JSONArray paragraphs = section.getJSONArray("paragraphs");
            SsmlBuilder ssml = new SsmlBuilder(this.volume);

            ssml.text("Section " + secNum).strongBreak().text(title).newParagraph();
            ssmlList.add(ssml.build());
            buildArraySSML(ssmlList, paragraphs, true);
        }

        private void buildEomSSML(List<String> ssmlList) throws JSONException {
            JSONObject eom = getEom();
            if (eom != null) {
                for (EomSection section: this.eomSections) {
                    if (!(eom.isNull(section.getClassName()))) {
                        SsmlBuilder ssml = new SsmlBuilder(this.volume);
                        JSONObject obj = eom.getJSONObject(section.getClassName());
                        ssml.text(obj.getString("title")).newParagraph();
                        ssmlList.add(ssml.build());
                        switch (section.getSecType()) {
                            case ("summary"): {
                                buildArraySSML(ssmlList, obj.getJSONArray("paragraphs"), true);
                                break;
                            }
                            case ("exercise"): {
                                buildExerciseSSML(ssmlList, obj.getJSONArray("exercises"), section.isMultiChoice(), section.hasSolution());
                                break;
                            }
                            case ("glossary"): {
                                JSONArray keyTerms = obj.getJSONArray("key terms");
                                int length = keyTerms.length();
                                for (int i = 0; i < length; i++) {
                                    SsmlBuilder keyTermSsml = new SsmlBuilder(this.volume);
                                    JSONObject keyTerm = keyTerms.getJSONObject(i);
                                    String term = keyTerm.getString("term");
                                    String definition = keyTerm.getString("definition");
                                    keyTermSsml.text(term).comma().sentence(definition).strongBreak();
                                    ssmlList.add(keyTermSsml.build());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        private void buildExerciseSSML(List<String> ssmlList, JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException {
            int length = exercises.length();
            for (int i = 0; i < length; i++) {
                SsmlBuilder ssml = new SsmlBuilder(volume);
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
                    SsmlBuilder solutionSsml = new SsmlBuilder(volume);
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
                SsmlBuilder ssml = new SsmlBuilder(volume);
                String output = options.getString(i);
                ssml.text(String.valueOf(letterChoice)).comma().sentence(output).strongBreak();
                ssmlList.add(ssml.build());
                letterChoice++;
            }
        }

        private void buildArraySSML(List<String> ssmlList, JSONArray array, boolean paragraphs) throws JSONException {
            int length = array.length();
            for (int i = 0; i < length; i++) {
                SsmlBuilder ssml = new SsmlBuilder(volume);
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



        //************* TEXT FUNCTIONS ******************//
        List<String> modulePageText() throws JSONException {
            List<String> textList = new ArrayList<>();
            openingText(textList);
            readingSecText(textList);
            eomText(textList);
            return textList;
        }

        private void openingText(List<String> textList) throws JSONException {
            JSONObject opening = getOpening();
            textList.add(this.title);
            if (opening != null) {
                if (!(opening.isNull("abstract"))) {
                    JSONObject absObj = opening.getJSONObject("abstract");
                    String intro = absObj.getString("intro");
                    JSONArray abList = absObj.getJSONArray("list");
                    textList.add(intro);
                    textFromArray(textList, abList, true);
                }
                if (!(opening.isNull("paragraphs"))) {
                    JSONArray paragraphs = opening.getJSONArray("paragraphs");
                    textFromArray(textList, paragraphs, false);
                }
            }
        }

        private void readingSecText(List<String> textList) throws JSONException {
            JSONArray readingSec = getReadingSections();
            if (readingSec != null) {
                int length = readingSec.length();
                for (int i = 0; i < length; i++) {
                    oneReadingSecText(textList, readingSec.getJSONObject(i));
                }
            }
        }

        private void oneReadingSecText(List<String> textList, JSONObject section) throws JSONException {
            int secNum = section.getInt("section");
            String title = section.getString("title");
            textList.add(String.format("<h3><b>Section %s</b>: %s</h3>", secNum, title));

            JSONArray paragraphs = section.getJSONArray("paragraphs");
            textFromArray(textList, paragraphs, false);
        }

        private void eomText(List<String> textList) throws JSONException {
            JSONObject eom = getEom();
            String secTitleFormat = "<h4><b>%s</b>:</h4>";
            if (eom != null) {
                for (EomSection section: this.eomSections) {
                    if (!(eom.isNull(section.getClassName()))) {
                        JSONObject obj = eom.getJSONObject(section.getClassName());
                        String title = String.format(secTitleFormat, obj.getString("title"));
                        textList.add(title);
                        switch (section.getSecType()) {
                            case ("summary"): {
                                textFromArray(textList, obj.getJSONArray("paragraphs"), false);
                                break;
                            }
                            case ("exercise"): {
                                exercisesText(textList, obj.getJSONArray("exercises"), section.isMultiChoice(), section.hasSolution());
                                break;
                            }
                            case ("glossary"): {
                                JSONArray keyTerms = obj.getJSONArray("key terms");
                                int length = keyTerms.length();
                                for (int i = 0; i < length; i++) {
                                    JSONObject keyTerm = keyTerms.getJSONObject(i);
                                    String term = keyTerm.getString("term");
                                    String definition = keyTerm.getString("definition");
                                    textList.add(String.format("<b>%s</b> -- %s", term, definition));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        private void exercisesText(List<String> textList, JSONArray exercises, boolean hasOptions, boolean hasSolution) throws JSONException {
            int length = exercises.length();
            for (int i = 0; i < length; i++) {
                JSONObject ex = exercises.getJSONObject(i);
                int exNum = ex.getInt("exercise");
                String problem = ex.getString("problem");
                textList.add(String.format("<h5><b>Exercise %s</b>:</h5>", exNum));
                textList.add(String.format("<b>Problem</b>: %s", problem));
                if (hasOptions) {
                    exerciseOptionsText(textList, ex);
                }
                if (hasSolution) {
                    String solution = ex.getString("solution");
                    textList.add(String.format("<b>Solution</b>: %s", solution));
                }
            }
        }

        private void exerciseOptionsText(List<String> textList, JSONObject exercise) throws JSONException {
            JSONArray options = exercise.getJSONArray("options");
            int length = options.length();
            char letterChoice = 'A';
            for (int i = 0; i < length; i++) {
                String output = "\t\t" + letterChoice + ". " + options.getString(i);
                textList.add(output);
                letterChoice++;
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
                textList.add(output);
            }
        }
    }
}


class EomSection {
    private String className;
    private String secType;
    private boolean multiChoice = false;
    private boolean hasSolution = false;

    EomSection(String className, String type) {
        this.className = className;
        this.secType = type;
    }

    EomSection(String className, String type, boolean multiChoice, boolean hasSolution) {
        this.className = className;
        this.secType = type;
        this.multiChoice = multiChoice;
        this.hasSolution = hasSolution;
    }

    String getClassName() {
        return this.className;
    }

    String getSecType() {
        return this.secType;
    }

    boolean isMultiChoice() {
        return this.multiChoice;
    }

    boolean hasSolution() {
        return this.hasSolution;
    }
}
