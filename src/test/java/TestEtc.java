import plus.region.Region;
import plus.region.RegionMap;
import plus.region.RegionQuery;

public class TestEtc {
    public static void main(String[] args) {

        RegionQuery query = new RegionQuery();

        for (int i=0;i<4;i++){
            query.addRegion(new Region(1000+i, 10+i, 10+i, 10+i, 20+i, 20+i, 20+i));
        }

        System.out.println(query);

        query.clear();

        System.out.println(query);




    }
}
