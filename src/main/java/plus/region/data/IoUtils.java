package plus.region.data;

import plus.region.utl.FastExitException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Helper class to read and write data to streams
 */
public class IoUtils {
    /**
     * Code and write short to output stream
     * @param stream stream to write
     * @param value int (0-65535) to write
     * @throws IOException if write fails
     */
    public static void writeShort(final OutputStream stream, final int value) throws IOException {
        stream.write((byte) ((value >> 8) & 0xFF));
        stream.write((byte) (value & 0xFF));
    }



    /**
     * Read and encode short from input stream
     * @return read int (0-65535)
     * @throws IOException if read fails
     * @throws FastExitException on end of stream
     */
    public static int readShort(final InputStream stream) throws IOException, FastExitException{
        int first = stream.read();
        if(first == -1) throw FastExitException.INSTANCE;
        int second = stream.read();
        if(second == -1) throw FastExitException.INSTANCE;
        return (first << 8) | second;
    }


    /**
     * Code and write int to output stream
     * @param stream stream to write
     * @param value int to write
     * @throws IOException if write fails
     */
    public static void writeInt(final OutputStream stream, final int value) throws IOException {
        stream.write((byte) ((value >> 24) & 0xFF));
        stream.write((byte) ((value >> 16) & 0xFF));
        stream.write((byte) ((value >> 8) & 0xFF));
        stream.write((byte) (value & 0xFF));
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
        return (first << 24) | (second << 16) | (third << 8) | fourth;
    }


    /**
     * Code and write long to output stream
     * @param stream stream to write
     * @param value long to write
     * @throws IOException if write fails
     */
    public static void writeLong(final OutputStream stream, final long value) throws IOException {
        stream.write((byte) ((value >> 56) & 0xFF));
        stream.write((byte) ((value >> 48) & 0xFF));
        stream.write((byte) ((value >> 40) & 0xFF));
        stream.write((byte) ((value >> 32) & 0xFF));
        stream.write((byte) ((value >> 24) & 0xFF));
        stream.write((byte) ((value >> 16) & 0xFF));
        stream.write((byte) ((value >> 8) & 0xFF));
        stream.write((byte) (value & 0xFF));
    }


    /**
     * Read and encode long from input stream
     * @return read long
     * @throws IOException if read fails
     * @throws FastExitException on end of stream
     */
    public static long readLong(final InputStream stream) throws IOException, FastExitException{
        return ((long) readInt(stream) << 32) | readInt(stream);
    }


    /**
     * Code and write UUID to output stream
     * @param stream stream to write
     * @param uuid UUID to write
     * @throws IOException if write fails
     */
    public static void writeUUID(final OutputStream stream, final UUID uuid) throws IOException {
        writeLong(stream, uuid.getMostSignificantBits());
        writeLong(stream, uuid.getLeastSignificantBits());
    }


    /**
     * Read and encode UUID from input stream
     * @return read UUID
     * @throws IOException if read fails
     * @throws FastExitException on end of stream
     */
    public static UUID readUUID(final InputStream stream) throws IOException, FastExitException {
        long firstLong = readLong(stream);
        long secondLong = readLong(stream);
        return new UUID(firstLong, secondLong);
    }



    /**
     * Code and write string to output stream
     * @param stream stream to write
     * @param value string (0-255 bytes) to write
     * @throws IOException if write fails
     */
    public static void writeShortString(final OutputStream stream, final String value) throws IOException {
        byte[] bytes = value.getBytes();
        if(bytes.length > 255) throw new IOException("String too long");
        stream.write(bytes.length);
        stream.write(bytes);
    }


    /**
     * Read and encode string from input stream
     * @return read (0-255 bytes) as string
     * @throws IOException if read fails
     * @throws FastExitException on end of stream
     */
    public static String readShortString(final InputStream stream) throws IOException, FastExitException {
        int len = stream.read();
        if(len == -1) throw FastExitException.INSTANCE;
        byte[] bytes = new byte[len];
        if(stream.read(bytes, 0, len) != len) throw FastExitException.INSTANCE;
        return new String(bytes);
    }
}
