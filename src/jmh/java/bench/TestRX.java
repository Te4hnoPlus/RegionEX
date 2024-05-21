package bench;

import org.openjdk.jmh.annotations.*;
import plus.region.RegionMapEx;
import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 10)
@Fork(value = 1, warmups = 1)
@Threads(1)
public class TestRX {
    RegionMapEx map = new RegionMapEx(new File("data"));
    RegionMapEx.Context ctx = map.newContext();
    Random random = new Random(1);

    int dx, dy, dz;
    int total = 0;

    @Setup
    public void setup() {
        ctx.ensureLoaded(-512, -512, 1512, 1512);
    }


    @Setup(Level.Invocation)
    public void pre1(){
        dx = random.nextInt(1024);
        dy = random.nextInt(255);
        dz = random.nextInt(1024);
    }


    @Benchmark
    public void test() {
        total += ctx.getRegions(dx, dy, dz).size();
    }
}
