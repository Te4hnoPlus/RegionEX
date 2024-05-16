import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.region.Region;
import plus.region.RegionMap;
import plus.region.RegionQuery;
import plus.region.utl.LIndexList;


public class TestMap {
    public static void main(String[] args) {
        Int2ObjectMap<Object[]> manager = new Int2ObjectOpenHashMap<>();
        TestAccess.Adapter adapter = new TestAccess.Adapter(manager);

        RegionMap map = new RegionMap();
        LIndexList indexList = new LIndexList();

        int count = 0;

        for (int i=0;i<120;i+=1){
            map.add(indexList, new Region(1000+i, 10+i, 10+i, 10+i, 50+i, 50+i, 50+i));
            ++count;
        }

        RegionQuery query = new RegionQuery();

        for (int i=40;i<60;i+=4){
            map.getRegions(query.init(12+i, 12+i, 12+i));
            if(query.isEmpty()){
                continue;
            }
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
    }
}