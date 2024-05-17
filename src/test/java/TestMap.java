import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.region.Region;
import plus.region.RegionMapEx;
import plus.region.RegionQuery;
import java.io.File;


public class TestMap {
    public static void main(String[] args) {
        Int2ObjectMap<Object[]> manager = new Int2ObjectOpenHashMap<>();
        TestAccess.Adapter adapter = new TestAccess.Adapter(manager);

        RegionMapEx map = new RegionMapEx(new File("data"));
        map.ensureLoaded(new Region(-512, 0, -512, 1512, 0, 1512));

        int count = 0;
        RegionMapEx.Context ctx = new RegionMapEx.Context(map);

        for (int i=0;i<1250;i+=37){
            int h = i % 50;
            ctx.add(new Region(1000+i, 10+i, 10+h, 10+i, 50+i, 30+h, 50+i));
            ++count;
        }

        for (int i=40;i<60;i+=4){
            RegionQuery query = ctx.getRegions(12+i, 12+i, 12+i);
            if(query.isEmpty()) continue;

            System.out.println("-------["+query.getX()+", "+query.getY()+", "+query.getZ()+"]-----------------------------------------------");
            for (Region region : query){
                adapter.use(region);
                adapter.name.set("SELECTED-"+i);
                System.out.println(region);
            }
        }

        for (Region region: map){
            adapter.use(region);
            if(adapter.name.get() == null){
                System.out.println(region);
            }
            --count;
        }

        System.out.println("COUNT: "+count); // should be 0

        map.flushToDisk(null);

        System.out.println("SAVED");

        test2();
    }


    public static void test2(){
        RegionMapEx map = new RegionMapEx(new File("data"));
        RegionMapEx.Context ctx = new RegionMapEx.Context(map);
        ctx.ensureLoaded(new Region(-512, 0, -512, 1512, 0, 1512));

        for (int i=40;i<60;i+=4){
            RegionQuery query = ctx.getRegions(12+i, 12+i, 12+i);
            if(query.isEmpty()){
                continue;
            }
            System.out.println("-------["+query.getX()+", "+query.getY()+", "+query.getZ()+"]-----------------------------------------------");
            for (Region region : query){
                System.out.println(region);
            }
        }

        System.out.println("READ TESTED");

        System.out.println(map);
    }
}