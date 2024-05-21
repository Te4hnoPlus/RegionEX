package bench;

import org.openjdk.jmh.annotations.*;
import plus.region.Region;
import plus.region.RegionMapEx;
import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@Fork(value = 1, warmups = 1)
@Threads(1)
public class TestRX {
    RegionMapEx map = new RegionMapEx(new File("data"));
    RegionMapEx.Context ctx = map.newContext();
    Random random = new Random(1);
    Consumer<Region> consumer = new Consumer<Region>() {
        int total = 0;
        @Override
        public void accept(Region region) {
            ++total;
        }
    };

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
        for (int i=0;i<5;i++)
            total += ctx.getRegions(dx, dy, dz).size();
    }


    @Benchmark
    public void testEX() {
        for (int i=0;i<5;i++)
            total += ctx.getRegionsEx(dx, dy, dz).size();
    }


//    @Benchmark
//    public void testAccept(){
//        ctx.acceptRegions(dx, dy, dz, consumer);
//    }
//
//
//    @Benchmark
//    public void testAcceptEX(){
//        ctx.acceptRegionEx(dx, dy, dz, consumer);
//    }
}
