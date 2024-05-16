package plus.region;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import plus.region.utl.LIndexList;


/**
 * Region class, base unit of RegionEX
 * <p>
 * id - Unique region ID. It is assumed that regions with the same ID are equal
 * <p>
 * data - Region data, linked with ID
 */
public class Region {
    public final int id, minX, minY, minZ, maxX, maxY, maxZ;
    private Object data;

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        this(-1, minX, minY, minZ, maxX, maxY, maxZ);
    }


    public Region(int id, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.id = id;
        if(minX > maxX){
            int temp = minX;
            minX = maxX;
            maxX = temp;
        }
        if(minY > maxY){
            int temp = minY;
            minY = maxY;
            maxY = temp;
        }
        if(minZ > maxZ){
            int temp = minZ;
            minZ = maxZ;
            maxZ = temp;
        }

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }


    /**
     * @param manager - Data manager ID -> data
     * @return Region data. If not cached, request and cache data from manager
     */
    public <T> T getData(final Int2ObjectFunction<T> manager) {
        if (data == null) {
            T curData = manager.apply(id);
            data = curData;
            return curData;
        }
        return (T) data;
    }


    /**
     * @param manager Data manager ID -> data
     * @param value new region data
     */
    public <T> void setData(final Int2ObjectFunction<T> manager, final T value) {
        this.data = value;
        manager.put(id, value);
    }


    /**
     * It is assumed that regions with the same ID are equal
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Region && ((Region) obj).id == id;
    }


    /**
     * Reset cached data
     */
    public void cleanCachedData() {
        data = null;
    }


    /**
     * @return True if this region intersects with other
     */
    public boolean intersects(final Region other) {
        return (other.minX <= maxX && other.minY <= maxY && other.minZ <= maxZ && other.maxX >= minX && other.maxY >= minY && other.maxZ >= minZ);
    }


    /**
     * @return True if this region contains point (x, y, z)
     */
    public boolean contains(final int x, final int y, final int z) {
        return (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ);
    }


    /**
     * @return Horizontal index of point (x, z)
     */
    public static long calcIndex(final int x, final int z){
        return (long)(x & 0xFFFFFFF0) << 32 | (z & 0xFFFFFFF0);
    }


    /**
     * @param list List indexes to reuse/fill
     * @param region Region to calculate horizontal indexes
     */
    public static void computeIndexes(final LIndexList list, final Region region){
        list.clear();
        for(int x = region.minX; x <= region.maxX; x+=16)
            for(int z = region.minZ; z <= region.maxZ; z+=16)
                list.add(calcIndex(x, z));
    }


    /**
     * Fast alternative to {@link java.util.Arrays#copyOf(Object[], int)}
     * @param regions Array to expand
     * @param region Region to add
     * @return expanded array
     */
    public static Region[] expand(final Region[] regions, final Region region){
        Region[] newRegions = new Region[regions.length + 1];
        System.arraycopy(regions, 0, newRegions, 0, regions.length);
        newRegions[regions.length] = region;
        return newRegions;
    }


    /**
     * See {@link #expand(Region[], Region)}
     */
    public static Region[] expand(final Region[] regions, final int addSize){
        Region[] newRegions = new Region[regions.length + addSize];
        System.arraycopy(regions, 0, newRegions, 0, regions.length);
        return newRegions;
    }


    @Override
    public String toString() {
        return "Region{" +
                "ID = " + id + (data == null ? ", [" : ", cached, [") +
                    minX + ", " + minY + ", " + minZ + "] -> ["
                + maxX + ", " + maxY + ", " + maxZ +
                "]}";
    }
}