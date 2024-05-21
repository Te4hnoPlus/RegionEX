package plus.region.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Use chunked write to remove unnecessary native calls
 */
public class ChunkedFileOutputStream extends FileOutputStream {
    private final byte[] buff;
    private int curSize;

    public ChunkedFileOutputStream(int size, File file) throws FileNotFoundException {
        super(file);
        this.buff = new byte[size];
    }


    @Override
    public void close() throws IOException {
        if (curSize > 0) {
            super.write(buff, 0, curSize);
            curSize = 0;
        }
        super.close();
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (curSize > 0) {
            super.write(buff, 0, curSize);
            curSize = 0;
        }
        super.write(b, off, len);
    }


    @Override
    public void write(int b) throws IOException {
        if (curSize == buff.length) {
            super.write(buff, 0, buff.length);
            curSize = 0;
        }
        buff[curSize++] = (byte) b;
    }
}
