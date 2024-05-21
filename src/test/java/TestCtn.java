import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import plus.region.Region;
import plus.region.container.ChunkedRegionContainer;

public class TestCtn {


    public static void main(String[] args) {
        ChunkedRegionContainer ctn = new ChunkedRegionContainer(
                new Region(0, 0,0,0,10,10,10),
                new Region(1,1,1,1,9,19,9),
                new Region(2,2, 2, 2, 8, 28, 8),
                new Region(3,3, 3, 3, 7, 27, 7),
                new Region(4,34, 3, 3, 7, 77, 7)
        );

        int n = 0;
        //IntOpenHashSet set = new IntOpenHashSet();

        for (Region region : ctn) {
            //if(set.add(region.id))
                System.out.println(n+"|"+region);
            ++n;
        }
    }



}
