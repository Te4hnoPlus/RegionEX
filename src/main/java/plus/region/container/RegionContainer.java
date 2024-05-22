package plus.region.container;

import plus.region.Region;
import plus.region.RegionMap;
import plus.region.RegionQuery;
import plus.region.utl.RegionConsumerProxy;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * Base Region container (is empty)
 * <p>
 * Contains a list of regions, used only in {@link plus.region.RegionMap}
 */
public class RegionContainer implements Iterable<Region>{
    public static final Iterator<Region> EMPTY_ITERATOR = new Iterator<Region>() {
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public Region next() {
            return null;
        }
    };
    public static final RegionContainer EMPTY = new RegionContainer();


    /**
     * Finds all regions that contains XYZ point in query, see {@link RegionMap#getRegions(RegionQuery)}
     * @param query Query (maybe reused)
     */
    public void getRegions(final RegionQuery query){}


    /**
     * Apply function to all regions in XYZ point
     * @param x block x
     * @param y block y
     * @param z block z
     * @param func Function to apply
     */
    public void acceptRegions(final int x, final int y, final int z, final Consumer<Region> func){}


    /**
     * Finds all regions that intersect with region, see {@link RegionMap#getRegions(RegionQuery)}
     * @param region Region to intersect
     * @param query Query (maybe reused)
     */
    public void getRegions(final Region region, final RegionQuery query){}


    /**
     * Adds region to container. If it is overflowing, creates new container and returns it
     * @param region Region to add
     * @return New container if overflow or this
     */
    public RegionContainer addRegion(final Region region){
        return new SingleRegionContainer(region);
    }


    public RegionContainer addRegionOrRelink(final Region region){
        return new SingleRegionContainer(region);
    }


    /**
     * Remove region from container. If possible, creates a more optimal container
     * @param region Region to remove
     * @return New container if possible or this
     */
    public RegionContainer removeRegion(final Region region){
        return this;
    }


    /**
     * @return Count regions in container
     */
    public int size(){return 0;}


    /**
     * Accept this operation to all regions. Use {@link RegionConsumerProxy} to remove repeated execution
     * @param func Consumer to apply
     */
    public void acceptRegions(final Consumer<Region> func){}


    /**
     * @return Region by id if exists else null
     */
    public Region getRegion(int id) {
        return null;
    }


    /**
     * @return true if region exists
     */
    public final boolean hasRegion(int id) {
        return getRegion(id) != null;
    }


    /**
     * @return true if region exists
     */
    public final boolean hasRegion(final Region region) {
        return getRegion(region.id) != null;
    }


    @Override
    public Iterator<Region> iterator() {
        return EMPTY_ITERATOR;
    }


    @Override
    public String toString() {
        return "<C>";
    }
}
