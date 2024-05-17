package plus.region.utl;


/**
 * Convert long to string using character dictionary
 */
public final class CharNum {
    public static final CharNum Default = new CharNum("0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");
    private final char[] charLib;

    public CharNum(String lib){
        charLib = lib.toCharArray();
    }


    public int len(){
        return charLib.length;
    }


    public String getCharNumOf(long i){
        if(i ==  0) return String.valueOf(charLib[0]);
        StringBuilder s = new StringBuilder();
        while (i > 0){
            s.insert(0, charLib[(int) (i % charLib.length)]);
            i /= charLib.length;
        }
        return s.toString();
    }
}