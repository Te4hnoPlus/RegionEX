package plus.region.data.db;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import org.rocksdb.*;
import plus.region.Region;


/**
 * Base DataManager for {@link Region}, see {@link Region#getData(Int2ObjectFunction)}
 */
public class RocksDataManager<T> implements Int2ObjectFunction<T>, AutoCloseable{
    protected final RocksDB db;
    protected final Coder<T> coder;
    protected final boolean fastInsert; //should ignore get previous value on put

    static {
        RocksDB.loadLibrary();
    }


    public RocksDataManager(String path, boolean fastInsert, Coder<T> coder) {
        this.fastInsert = fastInsert;
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setCompressionType(CompressionType.ZSTD_COMPRESSION);
        options.setInfoLogLevel(InfoLogLevel.FATAL_LEVEL);
        options.setMaxLogFileSize(1024*32);
        options.setKeepLogFileNum(1);

        try {
            db = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        this.coder = coder;
    }


    /**
     * Call this before destroy this
     */
    @Override
    public void close(){
        db.close();
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
                if(prev == null) return null;
                return coder.encode(prev);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public T get(int key) {
        try {
            byte[] res = db.get(intToKey(key));
            if(res == null) return null;
            return coder.encode(res);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}