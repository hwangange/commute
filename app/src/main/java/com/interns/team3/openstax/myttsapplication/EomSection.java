package com.interns.team3.openstax.myttsapplication;

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
