import org.rocksdb.RocksDBException;
import plus.region.data.db.RocksDataManager;
import java.nio.charset.StandardCharsets;


public class TestRocks {

    public static void main(String[] args) throws RocksDBException {
        for (int i=0;i<20;i++){
            main();
        }
    }


    public static void main() throws RocksDBException {
        RocksDataManager<String> db = new RocksDataManager<>("test.db", true, new RocksDataManager.Coder<String>() {
            @Override
            public byte[] code(String obj) {
                return obj.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String encode(byte[] bytes) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        });

        for (int i=0;i<100;i++){
            db.put(i, "value"+i);
        }

        for (int i=0;i<100;i++){
            System.out.println(db.get(i));
        }

        db.close();
    }
}
