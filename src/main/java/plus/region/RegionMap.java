package plus.region;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import plus.region.container.RegionContainer;
import plus.region.utl.LIndexList;
import plus.region.utl.RegionConsumerProxy;
import java.util.function.Consumer;


/**
 * Base data structure for storing all regions
 */
public class RegionMap{
    protected final Long2ObjectOpenHashMap<RegionContainer> map = new Long2ObjectOpenHashMap<>();

    public RegionMap() {
        map.defaultReturnValue(RegionContainer.EMPTY);
    }


    /**
     * It is not recommended to use, {@link RegionMap#add(LIndexList, Region)}
     * @param region Region to add
     */
    public void add(Region region) {
        add(new LIndexList(), region);
    }


    /**
     * @param list List of indexes to reuse
     * @param region Region to add
     */
    public void add(LIndexList list, Region region) {
        Region.computeIndexes(list, region);
        for (long l : list) {
            RegionContainer prev = map.get(l);
            RegionContainer newContainer = prev.addRegion(region);

            if(newContainer != prev) map.put(l, newContainer);
        }
    }


    /**
     * It is not recommended to use, {@link RegionMap#remove(LIndexList, Region)}
     * @param region Region to remove
     */
    public void remove(Region region) {
        remove(new LIndexList(), region);
    }


    /**
     * @param list List of indexes to reuse
     * @param region Region to remove
     */
    public void remove(LIndexList list, Region region) {
        Region.computeIndexes(list, region);
        for (long l : list) {
            RegionContainer prev = map.get(l);
            RegionContainer newContainer = prev.removeRegion(region);

            if(newContainer != prev) map.put(l, newContainer);
        }
    }


    /**
     * Accept operation to all regions, use {@link RegionConsumerProxy}
     * @param func Consumer to accept
     */
    public void acceptRegions(Consumer<Region> func){
        acceptRegions(new RegionConsumerProxy(func));
    }


    /**
     * Accept operation to all regions
     * @param func Consumer to accept
     */
    public void acceptRegions(RegionConsumerProxy func){
        for (Long2ObjectMap.Entry<RegionContainer> entry : map.long2ObjectEntrySet())
            entry.getValue().acceptRegions(func);
    }


    /**
     * Finds all regions that contains XYZ point in query
     * @param query Query (maybe reused)
     */
    public void getRegions(RegionQuery query){
        query.clear();
        map.get(Region.calcIndex(query.getX(), query.getZ())).getRegions(query);
    }


    /**
     * Finds all regions that intersect with region
     * <p>
     * It is not recommended to use, {@link RegionMap#getRegions(Region, LIndexList, RegionQuery)}
     * @param region Region to intersect
     * @param query Query (maybe reused)
     */
    public void getRegions(Region region, RegionQuery query){
        getRegions(region, new LIndexList(), query);
    }


    /**
     * Finds all regions that intersect with region
     * @param region Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     */
    public void getRegions(Region region, LIndexList list,  RegionQuery query){
        query.clear();
        Region.computeIndexes(list, region);
        for (long l : list) map.get(l).getRegions(region, query);
    }


    /**
     * Accept operation to all regions, intersected with check. Remove proxy, is safe,
     * see {@link RegionMap#acceptRegions(Region, LIndexList, RegionQuery, Consumer)}
     * @param check Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     * @param func Consumer to accept
     */
    public void acceptRegions(Region check, LIndexList list,  RegionQuery query, RegionConsumerProxy func){
        acceptRegions(check, list, query, func.parent());
    }


    /**
     * Accept operation to all regions, intersected with check
     * @param check Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     * @param func Consumer to accept
     */
    public void acceptRegions(Region check, LIndexList list,  RegionQuery query, Consumer<Region> func){
        getRegions(check, list, query);
        for (long l : list) map.get(l).getRegions(query);
        for (Region region : query) func.accept(region);
    }
}