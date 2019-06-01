package com.example.mp11.views;

public class Dictionary {
    public Word[] words;

    public class Word {
        public StringTrans[] meanings;

        public class StringTrans {
            public String word, definition, meaning, ex, syns;
            public int id, index;


        }
    }
}
