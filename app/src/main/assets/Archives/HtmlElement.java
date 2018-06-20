package com.interns.team3.openstax.myttsapplication;

import java.util.HashMap;

public class HtmlElement {

    private String rawText;
    private String formattedText;
    private String tag;
    private HashMap<String, String> attributes;

    public HtmlElement(String rawText, String formattedText, String tag, HashMap<String, String> attributes){
        this.rawText = rawText;
        this.formattedText = formattedText;
        this.tag = tag;
        this.attributes = attributes;
    }

    public String getRawText(){
        return rawText;
    }

    public String getFormattedText(){
        return formattedText;
    }

    public String getTag(){
        return tag;
    }

    public HashMap<String, String> getElementAttributes(){
        return attributes;
    }
}
