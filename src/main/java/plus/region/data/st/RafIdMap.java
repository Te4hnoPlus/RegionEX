package plus.region.data.st;

import plus.region.utl.FastExitException;
import java.io.*;
import static plus.region.data.IoUtils.*;


public class RafIdMap {
    private final RandomAccessFile raf;
    private final Table table = new Table();
    private final RafOStream oStream;

    public RafIdMap(RandomAccessFile raf) {
        this.raf = raf;
        RafIStream stream = new RafIStream();
        oStream = new RafOStream();
        try {
            table.readFrom(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FastExitException e) {}
    }



    public void close() throws IOException {
        raf.close();
    }


    public void set(int id, byte[] buff, int off, int len) throws IOException {
        table.write(id, buff, off, len);
    }


    public void set(int id, byte[] buf) throws IOException {
        table.write(id, buf, 0, buf.length);
    }


    public byte[] get(int id) throws IOException {
        int idx = table.idIndex(id);
        if(idx == -1)return null;
        int size;

        byte[] buf = new byte[size = table.sizes[idx]];
        raf.seek(table.cursor(idx));
        raf.read(buf, 0, size);

        return buf;
    }


    public void rem(int id) throws IOException {
        int idx = table.freeId(id);
        if(idx != -1)table.onEditId(idx);
    }


    class RafIStream extends InputStream{
        public void setCursor(int cursor) throws IOException {
            raf.seek(cursor);
        }
        @Override
        public int read() throws IOException {
            return raf.read();
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return raf.read(b, off, len);
        }
    }


    class RafOStream extends OutputStream{
        public void setCursor(int cursor) throws IOException {
            raf.seek(cursor);
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            raf.write(b, off, len);
        }
        @Override
        public void write(int b) throws IOException {
            raf.write(b);
        }
    }


    private static final int[] EMPTY = new int[0];
    class Table {
        private int[] ids = EMPTY;
        private int[] shifts = EMPTY;
        private int[] sizes = EMPTY;
        private int size = 0;
        private int limTable = 5;

        public void write(int id, byte[] buf, int off, int len) throws IOException {
            int prev = freeId(id);
            int idx = alloc(id, len);
            if(prev != idx && prev != -1) onEditId(prev);
            onEdit(idx);
            write0(cursor(idx), buf, off, len);
        }


        private int cursor(int idx){
            return shifts[idx]+tableSize();
        }


        private int tableSize(){
            return 4 + (8 * limTable);
        }


        public int alloc(int id, int len) throws IOException {
            int idx;
            for (idx = 0; idx < size; idx++) {
                if (ids[idx] != -1) continue;

                int avl = aviliable(idx);
                if (avl >= len) {
                    ids  [idx] = id;
                    sizes[idx] = len;
                    return idx;
                }
            }

            ids      = add(id, idx = size, ids);
            int prev = prevShift(idx);
            shifts   = add(prev + len, idx, shifts);
            sizes    = add(len, idx, sizes);

            if(++size > limTable){
                expandTable();
                limTable += 5;
                updateLim();
            }
            updateSize();
            return idx;
        }


        private void updateSize() throws IOException {
            oStream.setCursor(0);
            writeShort(oStream, size);
        }


        private void updateLim() throws IOException {
            oStream.setCursor(2);
            writeShort(oStream, limTable);
        }


        private void onEditId(int idx) throws IOException {
            oStream.setCursor(4 + (8 * idx));
            writeInt(oStream, ids[idx]);
        }


        private void onEdit(int idx) throws IOException {
            oStream.setCursor(4 + (8 * idx));
            writeInt(  oStream, ids[idx]   );
            writeShort(oStream, shifts[idx]);
            writeShort(oStream, sizes[idx] );
        }


        private void expandTable() throws IOException {
            RandomAccessFile raf = RafIdMap.this.raf;

            int curSize = (limTable * 3) + 1;
            int newSize = curSize + 20;

            int len = (int) raf.length();

            byte[] temp = new byte[256];

            while (len > newSize){
                int amount = 256;
                int pos = len - amount;
                if(pos < newSize){
                    amount = newSize - pos;
                    pos = len - newSize;
                }

                raf.seek(pos);
                raf.read(temp);
                raf.seek(pos+20);
                raf.write(temp);

                len -= amount;
            }
        }


        private int aviliable(int idx){
            if(idx + 1 == size)return Integer.MAX_VALUE;
            int cur  = shifts[idx];
            int next = shifts[idx+1];
            return next - cur;
        }


        private int prevShift(int idx){
            if(idx == 0)return 0;
            return shifts[idx-1];
        }


        private int freeId(int id) throws IOException {
            int prevSize = size;
            int index = idIndex(id);
            if(index == -1)return -1;
            ids[index] = -1;

            while (index + 1 == size && ids[index] == -1){
                --size;
                --index;
            }
            if(size != prevSize)updateSize();
            return index;
        }


        private void write0(int cursor, byte[] buf, int shift, int count) throws IOException {
            raf.seek(cursor);
            raf.write(buf, shift, count);
        }


        private int idIndex(int id){
            int res = 0;
            for (; res < size; ++res) if(ids[res] == id) return res;
            return -1;
        }


        public void write(RafOStream stream) throws IOException {
            writeShort(stream, size);
            writeShort(stream, limTable);
            for (int i = 0; i < size; ++i) {
                writeInt(  stream,  ids[i]  );
                writeShort(stream, shifts[i]);
                writeShort(stream, sizes[i] );
            }
        }


        public void readFrom(RafIStream stream) throws IOException, FastExitException {
            size     = readShort(stream);
            limTable = readShort(stream);
            if(limTable == 0)limTable = 5;
            ids = new int[size];
            shifts = new int[size];
            sizes = new int[size];
            for (int i = 0; i < size; ++i) {
                ids[i]    = readInt(  stream);
                shifts[i] = readShort(stream);
                sizes[i]  = readShort(stream);
            }
        }
    }


    private static int[] add(final int value, final int size, int[] list) {
        if(size == list.length) {
            int[] newList;
            System.arraycopy(list, 0, newList = new int[size + 5], 0, size);
            list = newList;
        }
        list[size] = value;
        return list;
    }
}
