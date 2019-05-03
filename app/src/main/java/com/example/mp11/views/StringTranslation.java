package com.example.mp11.views;

public class StringTranslation {
    public String word,meaning,ex,syns;
    public StringTranslation(String word, String meaning, String syns, String ex){
        this.word=word;
        this.meaning=meaning;
        this.syns=syns;
        this.ex=ex;
    }
    public StringTranslation(){

    }
    public int index=0,id;
    public void setId(int a){
        id=a;
    }
    public void setWord(String a){
        word=a;
    }
    public int getId(){
        return id;
    }
    public void setdefinition(String a){
        meaning=a;
    }
    public void setex(String a){
        ex=a;
    }
    public void setsyns(String a){
        syns=a;
    }
    public String getWord(){
        return word;
    }
    public String getdefinition(){
        return meaning;
    }
    public String getEx(){
        return ex;
    }
    public String getSyns(){
        return syns;
    }
}
