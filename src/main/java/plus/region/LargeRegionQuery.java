package plus.region;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;


/**
 * Use this for large queries (more than 16 chunks)
 * <p>
 * This type of request uses {@link IntOpenHashSet} to speed up checking for the presence of a region
 */
public class LargeRegionQuery extends RegionQuery{
    private final IntOpenHashSet set = new IntOpenHashSet();

    @Override
    public void addRegion(Region region) {
        if(set.add(region.id)) super.add(region);
    }


    @Override
    public boolean hasRegion(int id) {
        return set.contains(id);
    }


    @Override
    public void clear() {
        set.clear();
        super.clear();
    }
}