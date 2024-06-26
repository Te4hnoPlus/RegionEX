import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.region.Region;
import plus.region.RegionQuery;
import plus.region.data.DataRegionAccessor;


public class TestAccess {
    public static void main(String[] args) {
        Int2ObjectMap<Object[]> manager = new Int2ObjectOpenHashMap<>();
        RegionQuery regions = new RegionQuery();
        Adapter adapter = new Adapter(manager);

        for (int i = 0; i < 10; i++) {
            Region region;
            regions.addRegion(region = new Region(1000+i, 10+i, 10+i, 10+i, 20+i, 20+i, 20+i));

            adapter.use(region);

            adapter.name.set("REG: "+region.id);
            adapter.age.set(i);
        }

        for (Region region : regions) {
            adapter.use(region);

            System.out.println(adapter.name.get() + ", " + adapter.age.get());
        }

        System.out.println("---------------------------------");

        test2();
    }

    public static void test2(){
        Int2ObjectMap<ExampleField> manager = new Int2ObjectOpenHashMap<>();
        RegionQuery regions = new RegionQuery();

        for (int i = 0; i < 10; i++) {
            Region region;
            regions.addRegion(region = new Region(1000+i, 10+i, 10+i, 10+i, 20+i, 20+i, 20+i));

            ExampleField field;
            region.setData(manager, field = new ExampleField());
            field.name = "REG: "+region.id;
            field.age = i;
        }

        for (Region region : regions) {
            ExampleField field = region.getData(manager);

            System.out.println(field.name + ", " + field.age);
        }
    }


    public static class ExampleField{
        public String name;
        public int age;
    }


    public static class Adapter extends DataRegionAccessor {
        public final Field<String> name = newField();
        public final Field<Integer> age = newField();

        public Adapter(Int2ObjectFunction<Object[]> manager) {
            super(manager);
        }
    }
}