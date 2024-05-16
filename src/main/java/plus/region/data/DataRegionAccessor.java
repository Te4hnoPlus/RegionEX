package plus.region.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import plus.region.Region;


/**
 * Help to access region data
 */
public class DataRegionAccessor extends DataAccessor implements DataAccessor.Accessor {
    private final Int2ObjectFunction<Object[]> manager;
    private Region region;

    public DataRegionAccessor(Int2ObjectFunction<Object[]> manager) {
        this.manager = manager;
        this.accessor = this;
    }


    public void use(Region region) {
        this.region = region;
        this.data = region.getData(manager);
    }


    @Override
    public Object[] get() {
        return region.getData(manager);
    }


    @Override
    public void set(Object[] value) {
        region.setData(manager, value);
    }
}