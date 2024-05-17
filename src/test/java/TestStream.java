import plus.region.Region;
import plus.region.RegionQuery;
import plus.region.data.RegionStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class TestStream {
    public static void main(String[] args) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        RegionQuery query = new RegionQuery();

        for (int i=0;i<20;i++){
            query.addRegion(new Region(1000+i, 10+i, 10+i, 10+i, 20+i, 20+i, 20+i));
        }

        System.out.println("Writing...");
        RegionStream.write(stream, query.iterator());

        System.out.println(stream.toByteArray().length);

        InputStream input = new ByteArrayInputStream(stream.toByteArray());

        System.out.println("Reading...");

        for (Region region : RegionStream.read(input)){
            System.out.println(query.hasRegion(region.id));
            System.out.println(region);
        }
    }
}
