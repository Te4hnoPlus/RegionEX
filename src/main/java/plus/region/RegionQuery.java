package plus.region;

import java.util.Arrays;
import java.util.Iterator;


/**
 * Query for RegionMap
 * <p>
 * It is recommended to use it repeatedly to ensure better performance
 * <p>
 * XYZ - point, in which to search for regions
 */
public class RegionQuery implements Iterable<Region>{
    private static final Region[] EMPTY = new Region[0];
    private Region[] regions = EMPTY;
    private int x, y, z, size;

    public RegionQuery(){}

    public RegionQuery(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public RegionQuery init(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    public final int getX() {
        return x;
    }


    public final int getY() {
        return y;
    }


    public final int getZ() {
        return z;
    }


    /**
     * Used only in RegionContainer
     * @param region the region to add
     */
    public void addRegion(final Region region){
        if(!hasRegion(region.id)) {
            if (regions.length == size)
                regions = Region.expand(regions, 4);
            regions[size++] = region;
        }
    }


    /**
     * @param region the region to check
     * @return true if region exists
     */
    public boolean hasRegion(final Region region){
        return hasRegion(region.id);
    }


    /**
     * @param id ID of region
     * @return true if region exists
     */
    public boolean hasRegion(final int id){
        for(int i = 0; i < size; i++) if(regions[i].id == id) return true;
        return false;
    }


    /**
     * @return Region at index
     * throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    public Region getRegion(final int index){
        return regions[index];
    }


    /**
     * @return Count of regions
     */
    public int size(){
        return size;
    }


    /**
     * @return true if there are no regions
     */
    public boolean isEmpty(){
        return size == 0;
    }


    /**
     * Reset RegionQuery state
     */
    public void clear(){
        if(size > 0) {
            //need to work GC
            Arrays.fill(regions, null);
            size = 0;
        }
    }


    @Override
    public Itr iterator() {
        return new Itr(this.regions, this.size);
    }


    /**
     * @return Raw array of regions (may contain nulls)
     */
    public Region[] toRawArray(){
        return regions;
    }


    /**
     * Fast Region iterator
     * <p>
     * It is recommended to use it repeatedly (if possible) to ensure better performance
     */
    public static class Itr implements Iterator<Region>, Iterable<Region>{
        private final Region[] regions;
        private final int size;
        private int index = 0;

        public Itr(Region[] regions, int size){
            this.regions = regions;
            this.size = size;
        }


        @Override
        public Iterator<Region> iterator() {
            index = 0;
            return this;
        }


        /**
         * Reset iterator to reuse
         */
        public Itr reset(){
            index = 0;
            return this;
        }


        @Override
        public boolean hasNext() {
            return index < size;
        }


        @Override
        public Region next() {
            return regions[index++];
        }
    }
}
