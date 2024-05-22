package bench;

import org.openjdk.jmh.annotations.*;
import plus.region.data.db.RocksProvider;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(value = 1, warmups = 1)
@Threads(1)
public class TestRocks {
    RocksProvider<String> db = new RocksProvider<>("test.db", true, new RocksProvider.Coder<String>() {
        @Override
        public byte[] code(String obj) {
            return obj.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String encode(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    });

    private int id;
    String latest;


    @Benchmark
    public void testPut() {
        db.put(id++, "value"+id);
    }


    @Benchmark
    public void testGet(){
        latest = db.get(id--);
    }
}