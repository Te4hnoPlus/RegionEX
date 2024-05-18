package plus.region.data;

import plus.region.Region;
import plus.region.utl.CharNum;
import plus.region.utl.FastExitException;
import java.io.*;
import java.util.Iterator;
import static plus.region.data.IoUtils.*;


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


    /**
     * Close stream manually
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        stream.close();
    }


    /**
     * Read 1 region (22 bytes) from stream
     * @throws FastExitException on end of stream
     * @throws IOException if read fails
     */
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


    /**
     * Write 1 region (22 bytes) to stream
     * @param stream stream to write
     * @param region region to write
     * @throws IOException if write fails
     */
    public static void write1(final OutputStream stream, final Region region) throws IOException {
        writeInt(stream, region.id     ); //id
        writeInt(stream, region.minX   ); //min x
        stream.write(region.minY);        //min y (0-255)
        writeInt(stream, region.minZ   ); //min z
        writeInt(stream, region.maxX   ); //max x
        stream.write(region.maxY);        //max y (0-255)
        writeInt(stream, region.maxZ   ); //max z
    }


    /**
     * @param id Geo index
     * @return Slim file name
     */
    private static String fileName(long id){
        return CharNum.Default.getCharNumOf(id>>8) + ".geo";
    }


    /**
     * Read geo (1024 x 1024) container from directory
     * @param id Geo index
     * @param geoDir root directory to store geo region contents
     * @return Iterator of regions from saved geo container
     */
    public static RegionStream readGeo(long id, final File geoDir){
        File file = new File(geoDir, fileName(id));
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


    /**
     * Removes geo (1024 x 1024) container from directory
     * @param id Geo index
     * @param geoDir root directory to store geo region contents
     */
    public static void removeGeo(long id, final File geoDir){
        File file = new File(geoDir, fileName(id));
        if(!file.exists())return;
        file.delete();
    }


    /**
     * Overwrites geo (1024 x 1024) container in directory
     * @param id Geo index
     * @param geoDir root directory to store geo region contents
     * @param regions Iterator of regions in geo
     */
    public static void writeGeo(long id, final File geoDir, Iterator<Region> regions){
        File file = new File(geoDir, fileName(id));
        try {
            if(!file.exists()) file.createNewFile();
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
        while (regions.hasNext()) try {
            write1(stream, regions.next());
        } catch (IOException e) {
            return;
        }
    }
}