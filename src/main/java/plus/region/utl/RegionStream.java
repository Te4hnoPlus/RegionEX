package plus.region.utl;

import plus.region.Region;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;


/**
 * Reads regions from an input stream and writes them to an output stream
 * It is recommended to use it repeatedly to ensure better performance
 */
public class RegionStream implements Iterable<Region>, Iterator<Region> {
    private InputStream stream;
    private Region next;

    /**
     * Restart iterator
     * @param stream the input stream to read from
     */
    public RegionStream start(InputStream stream) {
        this.stream = stream;
        next();
        return this;
    }

    @Override
    public Iterator<Region> iterator() {
        return this;
    }


    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public Region next() {
        InputStream stream = this.stream;
        Region prev = next;
        try {
            next = new Region(
                    readInt(stream), //id
                    readInt(stream), //min x
                    readInt(stream), //min y
                    readInt(stream), //min z
                    readInt(stream), //max x
                    readInt(stream), //max y
                    readInt(stream)  //max z
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FastExitException e) {
            next = null;
        }
        return prev;
    }


    /**
     * @param stream the input stream to read from
     * @return Iterator of regions
     */
    public static RegionStream read(InputStream stream) {
        return new RegionStream().start(stream);
    }


    /**
     * @param stream the output stream to write to
     * @param regions the regions iterator to write
     */
    public static void write(OutputStream stream, Iterator<Region> regions) {
        byte[] arr = new byte[4];
        while (regions.hasNext()) try {
            Region region = regions.next();
            write(stream, region.id,   arr); //id
            write(stream, region.minX, arr); //min x
            write(stream, region.minY, arr); //min y
            write(stream, region.minZ, arr); //min z
            write(stream, region.maxX, arr); //max x
            write(stream, region.maxY, arr); //max y
            write(stream, region.maxZ, arr); //max z
        } catch (IOException e) {
            return;
        }
    }


    private static void write(OutputStream stream, int value, byte[] temp) throws IOException {
        codeInt(value, temp);
        stream.write(temp);
    }


    /**
     * Code int to temporal byte array
     */
    public static void codeInt(int value, byte[] temp){
        temp[0] = (byte) ((value >> 24) & 0xFF);
        temp[1] = (byte) ((value >> 16) & 0xFF);
        temp[2] = (byte) ((value >> 8) & 0xFF);
        temp[3] = (byte) (value & 0xFF);
    }


    /**
     * Read and encode int from input stream
     * @return read int
     * @throws IOException if read fails
     * @throws FastExitException on end of stream
     */
    public static int readInt(InputStream stream) throws IOException, FastExitException{
        int first = stream.read();
        if(first == -1) throw FastExitException.INSTANCE;
        int second = stream.read();
        if(second == -1) throw FastExitException.INSTANCE;
        int third = stream.read();
        if(third == -1) throw FastExitException.INSTANCE;
        int fourth = stream.read();
        if(fourth == -1) throw FastExitException.INSTANCE;
        return (first << 24) + (second << 16) + (third << 8) + fourth;
    }
}