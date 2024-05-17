package plus.region.data;

public final class CharNum {
    public static final CharNum OnlyChars = new CharNum("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
    public static final CharNum Default = new CharNum("0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
    private final char[] charLib;

    public CharNum(String lib){
        charLib = lib.toCharArray();
    }


    public int len(){
        return charLib.length;
    }


    public String getCharNumOf(long i){
        if(i==0) return String.valueOf(charLib[0]);
        StringBuilder s = new StringBuilder();
        while (i>0){
            int index = (int) (i % charLib.length);
            s.insert(0, charLib[index]);
            i = i / charLib.length;
        }
        return s.toString();
    }
}