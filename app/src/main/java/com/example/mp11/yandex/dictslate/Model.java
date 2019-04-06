package com.example.mp11.yandex.dictslate;

public class Model {
    //public  String head;
    public Def[] def;




    public class Def{
   public String text;
   public String pos;
    public String ts;
        public String gen;
        public String num;
        public  Tr[] tr;



    public class Tr{
        public String text;
        public String pos;

        public String gen;
        public String num;
        public Syn[] syn;
        public Mean[] mean;
        public Ex[] ex;


        public class Syn{
            public String pos;
            public String gen;
            public String num;
            public String text;
        }
        public class Mean{
            public String pos;
            public String gen;
            public String num;
            public String text;
        }

    }
        public class Ex{
            public String pos;
            public String gen;
            public String num;
            public String text;
            public Tr[] tr;
        }
    }
//public boolean endOfWord;
//    public int pos;
//    public String[] text;

}

//public class Head
//{
//}
//
//
//public class Syn
//{
//    private String text;
//
//
//}
//
//
//public class Mean
//{
//    private String text;
//
//
//}
//
//public class Tr
//{
//    private String text;
//
//
//}
//
//public class Ex
//{
//    private String text;
//
//    private List<Tr> tr;
//
//
//}
//
//
//public class Tr
//{
//    private String text;
//
//    private String pos;
//
//    private List<Syn> syn;
//
//    private List<Mean> mean;
//
//    private List<Ex> ex;
//
//
//}
//
//
//public class Def
//{
//    private String text;
//
//    private String pos;
//
//    private List<Tr> tr;
//
//
//}
//
//
//
//public class Root
//{
//    private Head head;
//
//    private List<Def> def;
//
//
//}





//successResponse: {"def":[{"pos":"noun","text":"grunting","tr":[{"gen":"ср","mean":[{"text":"grumbling"}],"pos":"noun","syn":[{"gen":"ср","pos":"noun","text":"ворчание"}],"text":"хрюканье"}]},{"pos":"participle","text":"grunting","tr":[{"mean":[{"text":"rattling"}],"pos":"participle","syn":[{"pos":"participle","text":"хрипящий"}],"text":"хрюкающий"}]}]}