package com.example.mp11.googleEmulatorApi;

import com.example.mp11.views.StringTranslation;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GoogleTranslation {
    @SerializedName("word")
    @Expose
    public String word;
    @SerializedName("phonetic")
    @Expose
    public String phonetic;
    @SerializedName("meaning")
    @Expose
    public Meaning meaning;
    @SerializedName("pronunciation")
    @Expose
    public String pronunciation;
    @SerializedName("origin")
    @Expose
    public String origin;

    public class Meaning{
        public Def[] noun;
        public Def[] adjective;
        public Def[] verb;
        public Def[] adverb;

        public class Def{
            public String definition,example;
            public String[] synonyms;
        }
    }
}
