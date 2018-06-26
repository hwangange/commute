package com.interns.team3.openstax.myttsapplication;

public class TextChunk {

    private boolean selected;
    private String text;

    public TextChunk(String text){
        this.text = text;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean boo){
        selected = boo;
    }

    public String getText(){
        return text;
    }
}
