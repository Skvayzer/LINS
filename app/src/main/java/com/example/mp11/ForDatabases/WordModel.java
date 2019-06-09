package com.example.mp11.ForDatabases;

import java.io.Serializable;
import java.util.ArrayList;
//здесь хранятся определения, синонимы и примеры употребления слова
//с геттерами и сеттерами
public class WordModel implements Serializable {

    private String word;
    public ArrayList<String> definition=new ArrayList<String>(),syns=new ArrayList<String>(), ex=new ArrayList<String>();
    private int id;


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getdefinition(int i) {
        if(i<definition.size()) return definition.get(i);
        return"no def";
    }

    public void adddefinition(String definition) {
        this.definition.add(definition);
    }

    public String getSyns(int i) {
        if(i<syns.size()) return syns.get(i);
        return"no syns";
    }

    public void addSyns(String syns) {
        this.syns.add(syns);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEx(int i) {
        if(i<ex.size()) return ex.get(i);
        return"no examples";
    }

    public void addEx(String ex) {
        this.ex.add(ex);
    }
}