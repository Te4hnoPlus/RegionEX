import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.region.Region;
import plus.region.RegionMapEx;
import plus.region.RegionQuery;
import java.io.File;
import java.util.ArrayList;


public class TestMap {
    public static void main(String[] args) {
        main(false);
    }

    public static void main(boolean printID) {

        if (1 == 1) {
            Int2ObjectMap<Object[]> manager = new Int2ObjectOpenHashMap<>();
            TestAccess.Adapter adapter = new TestAccess.Adapter(manager);

            RegionMapEx map = new RegionMapEx(new File("data"));
            RegionMapEx.Context ctx = map.newContext();
            ctx.ensureLoaded(-512, -512, 51_000, 51_000);

            int count = 0;

            for (int i = 0; i < 50_000; i += 1) {
                int h = i % 50;
                Region reg = ctx.createRegion(10 + i, 10 + h, 10 + i, 50 + i, 30 + h, 50 + i);
                if(printID) System.out.println("NEW: " + reg.id);
                ++count;
            }

            for (int i = 40; i < 60; i += 4) {
                RegionQuery query = ctx.getRegions(12 + i, 12 + i, 12 + i);
                if (query.isEmpty()) continue;

                System.out.println("-------[" + query.getX() + ", " + query.getY() + ", " + query.getZ() + "]-----------------------------------------------");
                int lim = 5;
                for (Region region : query) {
                    adapter.use(region);
                    adapter.name.set("SELECTED-" + i);
                    if(--lim <= 0) {
                        System.out.println("And "+(query.size()-5)+" more...");
                        break;
                    }
                    System.out.println(region);
                }
            }

            System.out.println("COUNT: " + count);
            System.out.println("CID: "+ctx.map().idMap().curId());

            map.flushToDisk(null);

            System.out.println("SAVED");
        }

        test2();
    }


    public static void test2(){
        RegionMapEx map = new RegionMapEx(new File("data"));
        RegionMapEx.Context ctx = new RegionMapEx.Context(map);
        ctx.ensureLoaded(-512, -512, 1512, 1512);

        for (int i=40;i<60;i+=4){
            RegionQuery query = ctx.getRegions(12+i, 12+i, 12+i);
            if(query.isEmpty()){
                continue;
            }
            System.out.println("-------["+query.getX()+", "+query.getY()+", "+query.getZ()+"]-----------------------------------------------");
            int lim = 5;
            for (Region region : query){
                System.out.println(region);
                if(--lim <= 0) {
                    System.out.println("And "+(query.size()-5)+" more...");
                    break;
                }
            }
        }
        System.out.println("REGIONS IN POINT: "+ctx.getRegions(350, 50, 350).size());

        System.out.println("READ TESTED");

        System.out.println("REGIONS:"+map.calcRegionCount());

        ArrayList<Long> temp = new ArrayList<>();
        temp.add(Region.calcIndex(-1128, -1128));

        map.checkToUnload(ctx.list(), temp.iterator());
        System.out.println(ctx.list());

        System.out.println("REGIONS:"+map.calcRegionCount());
    }
}