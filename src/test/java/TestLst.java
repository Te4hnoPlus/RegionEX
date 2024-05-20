import plus.region.Region;
import plus.region.utl.LIndexList;

public class TestLst {
    public static void main(String[] args) {

        LIndexList list = new LIndexList();

        System.out.println("DEFAULT");
        Region.computeGeoIndexes(list, -512, -512, 1512, 1512);

        System.out.println(list);

        System.out.println("REGION");
        Region.computeGeoIndexes(list, new Region(-512, 0, -512, 1512, 255, 1512));

        System.out.println(list);

    }
}
