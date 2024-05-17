package bench;

import org.openjdk.jmh.annotations.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 1)
@Threads(2)
public class TestNormalLink {
    public static int count = 10_000;
    public static int size  = 50;

    @Benchmark
    public void test1(){
        ArrayList<Integer> list = getList();
        for (int i = 0; i <count; i++) {
            fillList(list);
        }
        if(list.contains(-1))throw new IllegalArgumentException();
    }


    @Benchmark
    public void test2(){
        ArrayList<Integer> list = null;
        for (int i = 0; i <count; i++) {
            list = getList();
        }
        if(list.contains(-1))throw new IllegalArgumentException();
    }


    public static void fillList(ArrayList<Integer> list){
        list.clear();
        for (int i = 0; i <size; i++) {
            list.add(i);
        }
    }


    public static ArrayList<Integer> getList(){
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i <size; i++) {
            list.add(i);
        }
        return list;
    }
}