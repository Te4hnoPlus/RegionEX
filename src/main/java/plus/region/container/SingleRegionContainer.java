package plus.region.container;

import plus.region.Region;
import plus.region.RegionQuery;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * Region container for single region
 * <p>
 * on add region -> MultiRegionContainer
 * <p>
 * on remove region -> RegionContainer (empty)
 */
public class SingleRegionContainer extends RegionContainer{
    private final Region region;

    public SingleRegionContainer(Region region) {
        this.region = region;
    }


    @Override
    public void getRegions(RegionQuery query) {
        if(region.contains(query.getX(), query.getY(), query.getZ()))
            query.addRegion(region);
    }


    @Override
    public void getRegions(Region region, RegionQuery query) {
        if(region.intersects(this.region))
            query.addRegion(region);
    }


    @Override
    public RegionContainer addRegion(Region region) {
        return new MultiRegionContainer(this.region, region);
    }


    @Override
    public RegionContainer removeRegion(Region region) {
        return RegionContainer.EMPTY;
    }


    @Override
    public int size() {
        return 1;
    }


    @Override
    public void acceptRegions(Consumer<Region> func) {
        func.accept(region);
    }


    @Override
    public Iterator<Region> iterator() {
        return new SingleItr(region);
    }


    /**
     * Fast iterator for single region
     */
    public static class SingleItr implements Iterator<Region>{
        private Region region;
        public SingleItr(Region region) {
            this.region = region;
        }


        @Override
        public boolean hasNext() {
            return region != null;
        }


        @Override
        public Region next() {
            Region prev = region;
            region = null;
            return prev;
        }
    }
}