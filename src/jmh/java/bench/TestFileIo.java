package bench;

import org.openjdk.jmh.annotations.*;
import plus.region.data.IoUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 1)
@Threads(1)
public class TestFileIo {
    private final IoUtils.IoTask task = initTask();

    public static IoUtils.IoTask initTask(){
        int[] data = new int[2048];
        Random random = new Random(1);
        for (int i=0;i<data.length;i++){
            data[i] = random.nextInt(255);
        }
        return stream -> {
            for (int datum : data) stream.write(datum);
        };
    }


    @Benchmark
    public void testDef(){
        writeToFile(new File("test.txt"), task);
    }


    @Benchmark
    public void testCh(){
        IoUtils.writeToFile(new File("test.txt"), task);
    }


    public static void writeToFile(File file, IoUtils.IoTask task) {
        try {
            if(!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            task.accept(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
