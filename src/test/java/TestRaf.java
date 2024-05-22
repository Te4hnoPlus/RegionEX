import plus.region.data.st.RafIdMap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;


public class TestRaf {
    public static void main(String[] args) throws IOException {
        File file = new File("test.data");
        //if(file.exists())file.delete();
        if(!file.exists())file.createNewFile();
        RafIdMap map = new RafIdMap(new RandomAccessFile(file, "rw"));

//        set(map, 10, "test1");
//        set(map, 11, "test2");
//        set(map, 12, "test3");
//        set(map, 13, "test4");
//
//        map.rem(10);
//        map.rem(12);

        set(map, 14, "test1");
        set(map, 15, "test6");
        set(map, 14, "test51");
        set(map, 15, "test16");
//        set(map, 16, "test7");
//        set(map, 17, "test8");
//
        print(map.get(10));
        print(map.get(11));
        print(map.get(12));
        print(map.get(13));

        print(map.get(14));
        print(map.get(15));
        print(map.get(16));
        print(map.get(17));

//        map.rem(10);
//        map.rem(12);
    }


    private static void set(RafIdMap map, int id, String str) throws IOException {
        map.set(id, str.getBytes(StandardCharsets.UTF_8));
    }


    public static void print(byte[] bytes){
        if(bytes == null) System.out.println("NULL");
        else System.out.println(new String(bytes, StandardCharsets.UTF_8));
    }
}
