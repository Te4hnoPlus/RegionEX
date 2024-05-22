package plus.region.data.db;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;


public class RocksProvider<T> implements Int2ObjectFunction<T> {
    private final RocksDB db;
    private final Coder<T> coder;
    private final boolean fastInsert;

    static {
        RocksDB.loadLibrary();
    }


    public RocksProvider(String path, boolean fastInsert, Coder<T> coder) {
        this.fastInsert = fastInsert;
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        this.coder = coder;
    }


    public interface Coder<T> {
        byte[] code(T obj);
        T encode(byte[] bytes);
    }


    private static byte[] intToKey(int id){
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((id >> 24) & 0xFF);
        bytes[1] = (byte) ((id >> 16) & 0xFF);
        bytes[2] = (byte) ((id >> 8) & 0xFF);
        bytes[3] = (byte) (id & 0xFF);
        return bytes;
    }


    @Override
    public T put(int key, T value) {
        byte[] bkey = intToKey(key);
        try {
            if(fastInsert){
                db.put(bkey, coder.code(value));
                return null;
            } else {
                byte[] prev = db.get(bkey);
                db.put(bkey, coder.code(value));
                return coder.encode(prev);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public T get(int key) {
        try {
            return coder.encode(db.get(intToKey(key)));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}