package plus.region.data;

import plus.region.utl.FastExitException;

import java.io.*;

import static plus.region.data.IoUtils.*;


public class NextIdMap {
    private static final int[] EMPTY = new int[0];

    private int[] unused = EMPTY;
    private int unusedCursor = 0;
    private int lastId = 1;
    private boolean dirty = false;


    public int nextId() {
        dirty = true;
        if (unusedCursor > 0) {
            return unused[--unusedCursor];
        }
        return lastId++;
    }


    public void free(int id) {
        dirty = true;
        if(unusedCursor == unused.length) {
            unused = grow(unused, unused.length + 5);
        }
        unused[unusedCursor++] = id;
    }


    public boolean isDirty() {
        return dirty;
    }


    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }


    private static int[] grow(int[] array, int newSize) {
        int[] newArray = new int[newSize];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }


    public static void writeTo(NextIdMap map, OutputStream stream) throws IOException {
        writeInt(stream, map.lastId);
        int uCursor = map.unusedCursor;

        writeInt(stream, uCursor);

        if(uCursor > 0) {
            int[] unused = map.unused;

            for (int i=0;i<uCursor;i++) {
                writeInt(stream, unused[i]);
            }
        }
    }


    public static File nextIdFile(File geoDir){
        return new File(geoDir, "nextid.map");
    }


    public static void writeMap(NextIdMap map, File geoDir){
        writeToFile(nextIdFile(geoDir), stream -> writeTo(map, stream));
    }


    public static void readMap(NextIdMap map, File geoDir){
        File file = nextIdFile(geoDir);
        RegionStream stream = new RegionStream();
        if(!file.exists())return;

        FileInputStream os;
        try {
            os = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            readFrom(map, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FastExitException e) {}
    }


    public static void readFrom(NextIdMap map, InputStream stream) throws FastExitException, IOException {
        map.lastId = readInt(stream);
        int uCursor = readInt(stream);
        if(uCursor > 0) {
            int[] unused = map.unused = new int[uCursor];
            for (map.unusedCursor = 0; map.unusedCursor < uCursor; map.unusedCursor++) {
                unused[map.unusedCursor] = readInt(stream);
            }
        }
    }
}