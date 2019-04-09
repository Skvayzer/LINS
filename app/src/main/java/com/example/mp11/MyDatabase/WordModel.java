package com.example.mp11.MyDatabase;

import java.io.Serializable;

public class WordModel implements Serializable {

    private String word, definition, syns, ex;
    private int id;


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getdefinition() {
        return definition;
    }

    public void setdefinition(String definition) {
        this.definition = definition;
    }

    public String getSyns() {
        return syns;
    }

    public void setSyns(String syns) {
        this.syns = syns;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEx() {
        return ex;
    }

    public void setEx(String ex) {
        this.ex = ex;
    }
}