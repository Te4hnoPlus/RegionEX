package bench;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.openjdk.jmh.annotations.*;
import plus.region.utl.CLIndexList;
import plus.region.utl.LIndexList;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 1)
@Threads(1)
public class TestLists {
    private static int loops = 100;
    private LIndexList list;
    private LongArrayList list2;
    private long summ;

    @Setup
    public void init(){
        list = new CLIndexList();
        list2 = new LongArrayList();
    }

    @Benchmark
    public void self() {
        for (int i = 0; i < loops; i++)
            list.add(i);
        for (long l : list)summ += l;
        list.clear();
    }


    @Benchmark
    public void futl() {
        for (int i = 0; i < loops; i++)
            list2.add(i);
        for (long l : list2)summ += l;
        list2.clear();
    }
}