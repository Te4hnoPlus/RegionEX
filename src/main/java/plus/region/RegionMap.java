package plus.region;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import plus.region.container.RegionContainer;
import plus.region.utl.LIndexList;
import plus.region.utl.RegionConsumerProxy;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * Base data structure for storing all regions
 */
public class RegionMap implements Iterable<Region>{
    protected final Long2ObjectOpenHashMap<RegionContainer> map = new Long2ObjectOpenHashMap<>();

    public RegionMap() {
        map.defaultReturnValue(RegionContainer.EMPTY);
    }


    /**
     * It is not recommended to use, {@link RegionMap#add(LIndexList, Region)}
     * @param region Region to add
     */
    public void add(final Region region) {
        add(new LIndexList(), region);
    }


    /**
     * @param list List of indexes to reuse
     * @param region Region to add
     */
    public void add(final LIndexList list, final Region region) {
        Region.computeIndexes(list, region);
        LIndexList.Itr itr = list.iterator();
        long index;
        while (itr.hasNext()) {
            final RegionContainer prev, cur = (prev = map.get(index = itr.nextLong())).addRegion(region);
            if(cur != prev) map.put(index, cur);
        }
    }


    /**
     * It is not recommended to use, {@link RegionMap#remove(LIndexList, Region)}
     * @param region Region to remove
     */
    public void remove(final Region region) {
        remove(new LIndexList(), region);
    }


    /**
     * @param list List of indexes to reuse
     * @param region Region to remove
     */
    public void remove(final LIndexList list, final Region region) {
        Region.computeIndexes(list, region);
        LIndexList.Itr itr = list.iterator();
        long index;
        while (itr.hasNext()) {
            final RegionContainer prev, cur = (prev = map.get(index = itr.nextLong())).removeRegion(region);

            if(cur != prev) {
                if(cur == RegionContainer.EMPTY) map.remove(index);
                else map.put(index, cur);
            }
        }
    }


    /**
     * Accept operation to all regions, use {@link RegionConsumerProxy}
     * @param func Consumer to accept
     */
    public void acceptRegions(final Consumer<Region> func){
        acceptRegions(new RegionConsumerProxy(func));
    }


    /**
     * Accept operation to all regions
     * @param func Consumer to accept
     */
    public void acceptRegions(final RegionConsumerProxy func){
        for (Long2ObjectMap.Entry<RegionContainer> entry : map.long2ObjectEntrySet())
            entry.getValue().acceptRegions(func);
    }


    /**
     * Finds all regions that contains XYZ point in query
     * @param query Query (maybe reused)
     */
    public void getRegions(final RegionQuery query){
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
    public void getRegions(final Region region, final RegionQuery query){
        getRegions(region, new LIndexList(), query);
    }


    /**
     * Finds all regions that intersect with region
     * @param region Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     */
    public void getRegions(final Region region, final LIndexList list, final RegionQuery query){
        query.clear();
        Region.computeIndexes(list, region);
        LIndexList.Itr itr = list.iterator();
        while (itr.hasNext()) map.get(itr.nextLong()).getRegions(region, query);
    }


    /**
     * Accept operation to all regions, intersected with check. Remove proxy, is safe,
     * see {@link RegionMap#acceptRegions(Region, LIndexList, RegionQuery, Consumer)}
     * @param check Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     * @param func Consumer to accept
     */
    public void acceptRegions(final Region check, final LIndexList list, final RegionQuery query, final RegionConsumerProxy func){
        acceptRegions(check, list, query, func.parent());
    }


    /**
     * Accept operation to all regions, intersected with check
     * @param check Region to intersect
     * @param list List of indexes to reuse
     * @param query Query (maybe reused)
     * @param func Consumer to accept
     */
    public void acceptRegions(final Region check, final LIndexList list, final RegionQuery query, final Consumer<Region> func){
        getRegions(check, list, query);
        LIndexList.Itr itr = list.iterator();
        while (itr.hasNext()) map.get(itr.nextLong()).getRegions(query);
        for (Region region : query) func.accept(region);
    }


    @Override
    public Iterator<Region> iterator(){
        return new Itr(new IntOpenHashSet(), this);
    }


    public static class Itr implements Iterator<Region> {
        private final ObjectIterator<Long2ObjectMap.Entry<RegionContainer>> iter;
        private final IntOpenHashSet set;
        private Iterator<Region> subItr = RegionContainer.EMPTY_ITERATOR;
        private Region next;

        public Itr(final IntOpenHashSet set, final RegionMap map) {
            this.set = set;
            iter = map.map.long2ObjectEntrySet().fastIterator();
            while (!subItr.hasNext())
                subItr = iter.next().getValue().iterator();
            next = calcNext();
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        private Region calcNext(){
            Region next = subItr.next();

            while (!set.add(next.id)){
                while(!subItr.hasNext()){
                    if(iter.hasNext())
                        subItr = iter.next().getValue().iterator();
                    else return null;
                }
                next = subItr.next();
            }

            while(!subItr.hasNext()){
                if(iter.hasNext())
                    subItr = iter.next().getValue().iterator();
                else return null;
            }
            return next;
        }


        @Override
        public Region next() {
            Region prev = next;
            next = calcNext();
            return prev;
        }
    }


    @Override
    public String toString() {
        return map.toString();
    }
}