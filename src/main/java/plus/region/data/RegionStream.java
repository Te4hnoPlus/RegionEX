package plus.region.data;

import plus.region.Region;
import plus.region.utl.FastExitException;
import java.io.*;
import java.util.Iterator;


/**
 * Reads regions from an input stream and writes them to an output stream
 * <p>
 * It is recommended to use it repeatedly to ensure better performance
 */
public class RegionStream implements Iterable<Region>, Iterator<Region> {
    private InputStream stream;
    private Region next;

    /**
     * Restart iterator
     * @param stream the input stream to read from
     */
    public RegionStream start(final InputStream stream) {
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
        final InputStream stream = this.stream;
        final Region prev = next;
        try {
            next = read1(stream);
        } catch (IOException e) {
            try {
                stream.close();
            } catch (IOException ex) {}
            throw new RuntimeException(e);
        } catch (FastExitException e) {
            next = null;
            try {
                stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return prev;
    }


    public void close() throws IOException {
        stream.close();
    }


    public static Region read1(final InputStream stream) throws FastExitException, IOException {
        return new Region(
                readInt(stream), //id
                readInt(stream), //min x
                stream.read(),   //min y (0-255)
                readInt(stream), //min z
                readInt(stream), //max x
                stream.read(),   //max y (0-255)
                readInt(stream)  //max z
        );
    }


    public static RegionStream readGeo(long id, final File geoDir){
        File file = new File(geoDir, id + ".geo");
        RegionStream stream = new RegionStream();
        if(!file.exists())return stream;

        FileInputStream os;
        try {
            os = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return stream.start(os);
    }


    public static void writeGeo(long id, final File geoDir, Iterator<Region> regions){
        File file = new File(geoDir, id + ".geo");
        if(!file.exists())return;
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);

            write(os, regions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * @param stream the input stream to read from
     * @return Iterator of regions
     */
    public static RegionStream read(final InputStream stream) {
        return new RegionStream().start(stream);
    }


    /**
     * @param stream the output stream to write to
     * @param regions the regions iterator to write
     */
    public static void write(final OutputStream stream, final Iterator<Region> regions) {
        final byte[] arr = new byte[4];
        while (regions.hasNext()) try {
            Region region = regions.next();
            write(stream, region.id,   arr); //id
            write(stream, region.minX, arr); //min x
            stream.write(region.minY);       //min y (0-255)
            write(stream, region.minZ, arr); //min z
            write(stream, region.maxX, arr); //max x
            stream.write(region.maxY);       //max y (0-255)
            write(stream, region.maxZ, arr); //max z
        } catch (IOException e) {
            return;
        }
    }


    private static void write(final OutputStream stream, final int value, final byte[] temp) throws IOException {
        codeInt(value, temp);
        stream.write(temp);
    }


    /**
     * Code int to temporal byte array
     */
    public static void codeInt(final int value, final byte[] temp){
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
    public static int readInt(final InputStream stream) throws IOException, FastExitException{
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