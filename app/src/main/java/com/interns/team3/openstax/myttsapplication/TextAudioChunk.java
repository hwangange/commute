package com.interns.team3.openstax.myttsapplication;

public class TextAudioChunk {
    private int id;
    private String text;
    private String ssml;
    private String audioFile;
    private boolean synthesized = false;
    private boolean selected;

    TextAudioChunk() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSsml() {
        return this.ssml;
    }

    public void setSsml(String ssml) {
        this.ssml = ssml;
    }

    public String getAudioFile() {
        return this.audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public boolean isSynthesized() {
        return this.synthesized;
    }

    public void synthesized() {
        this.synthesized = true;
    }

    public boolean isSelected(){
        return this.selected;
    }

    public void select() {
        this.selected = true;
    }

    public void unselect() {
        this.selected = false;
    }
}