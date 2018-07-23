package com.interns.team3.openstax.myttsapplication.ssml;

import java.util.function.Consumer;

public class SsmlBuilder implements SsmlPhrase {
    private final StringBuilder stringBuilder;
    private boolean built = false;

    public SsmlBuilder() {
        stringBuilder = new StringBuilder();
    }

    public SsmlBuilder appendSSML(SsmlBuilder ssml) {
        stringBuilder.append(ssml.toString());
        return this;
    }

    public int length() {
        return stringBuilder.length();
    }

    public SsmlBuilder reset() {
        stringBuilder.setLength(0);
        this.built = false;
        return this;
    }

    public SsmlBuilder text(String format, Object... params) {
        return text(String.format(format, params));
    }

    public SsmlBuilder text(String text) {
        stringBuilder.append(" ");
        stringBuilder.append(text);
        return this;
    }

    public SsmlBuilder sentence(String text) {
        return sentence(builder -> builder.text(text));
    }

    public SsmlBuilder sentence(String format, Object... params) {
        return sentence(builder -> builder.text(String.format(format, params)));
    }

    public SsmlBuilder sentence(Consumer<SsmlPhrase> consumer) {
        stringBuilder.append("<s>");
        consumer.accept(this);
        stringBuilder.append("</s>");
        return this;
    }

    public SsmlBuilder paragraph(String text) {
        return paragraph(builder -> builder.text(text));
    }

    public SsmlBuilder paragraph(String format, Object... params) {
        return paragraph(builder -> builder.text(String.format(format, params)));
    }

    public SsmlBuilder paragraph(Consumer<SsmlPhrase> consumer) {
        stringBuilder.append("<p>");
        consumer.accept(this);
        stringBuilder.append("</p>");
        return this;
    }

    public SsmlBuilder timeBreak(String length) {
        String breakStr = String.format(" <break time=\"%s\"/> ", length);
        stringBuilder.append(breakStr);
        return this;
    }

    public SsmlBuilder comma() {
//        stringBuilder.append(" <break strength=\"medium\"/> ");
//        return this;
        return timeBreak("300ms");
    }

    public SsmlBuilder strongBreak() {
//        stringBuilder.append(" <break strength=\"strong\" /> ");
//        return this;
        return timeBreak("500ms");
    }

    public SsmlBuilder newParagraph() {
//        stringBuilder.append(" <break strength=\"x-strong\" /> ");
//        return this;
        return timeBreak("750ms");
    }

    public SsmlBuilder cardinalNumber(long number) {
        return cardinalNumber(Long.toString(number));
    }

    public SsmlBuilder cardinalNumber(String number) {
        return sayAs("cardinal", number);
    }

    public SsmlBuilder ordinalNumber(long number) {
        return ordinalNumber(Long.toString(number));
    }

    public SsmlBuilder ordinalNumber(String number) {
        return sayAs("ordinal", number);
    }

    public SsmlBuilder spellOut(String text) {
        return sayAs("spell-out", text);
    }

    private SsmlBuilder sayAs(String kind, String text) {
        stringBuilder.append(" <say-as interpret-as=\"")
                .append(kind)
                .append("\">")
                .append(text)
                .append("</say-as> ");
        return this;
    }

    public String build() {
        if (!built) {
            built = true;
            stringBuilder.insert(0, "<amazon:auto-breaths frequency=\"low\" volume=\"soft\" duration=\"x-short\">").append("</amazon:auto-breaths>");
            stringBuilder.insert(0, "<speak>").append("</speak>");
        }
        return stringBuilder.toString();
    }
}