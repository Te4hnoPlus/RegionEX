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
    public static final int EFFECTIVE_MAX_VOLUME = 131_071   ;
    public static final int GEO_SIZE             = 1024      ;
    public static final int CHUNK_SIZE           = 16        ;
    public static final int CHUNK_MASK           = 0xFFFFFFF0;
    public static final int GEO_MASK             = 0xFFFFF400;

    public final int id, minX, minY, minZ, maxX, maxY, maxZ;
    private Object data;

    public Region(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ){
        this(-1, minX, minY, minZ, maxX, maxY, maxZ);
    }


    public Region(final int id, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
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
     * @return Mathematical cuboid volume
     */
    public int volume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }


    /**
     * @return Horizontal chunk (16 x 16) index of point (x, z)
     * @param x block x
     * @param z block z
     */
    public static long calcIndex(final int x, final int z){
        return (long)(x & CHUNK_MASK) << 32 | (z & CHUNK_MASK);
    }


    /**
     * @param x block x
     * @param z block z
     * @return Horizontal geo (1024 x 1024) index
     */
    public static long calcGeoIndex(final int x, final int z) {
        return (long)(x & GEO_MASK) << 32 | (z & GEO_MASK);
    }


    /**
     * @param list List indexes to reuse/fill
     * @param region Region to calculate horizontal chunk (16 x 16) indexes
     */
    public static void computeIndexes(final LIndexList list, final Region region){
        list.clear();
        for(int x = region.minX; x <= region.maxX; x += CHUNK_SIZE)
            for(int z = region.minZ; z <= region.maxZ; z += CHUNK_SIZE)
                list.add(calcIndex(x, z));
    }


    /**
     * @param list List indexes to reuse/fill
     * @param region Region to calculate horizontal geo (1024 x 1024) indexes
     */
    public static void computeGeoIndexes(final LIndexList list, final Region region) {
        list.clear();
        for (int x = region.minX; x <= region.maxX; x += GEO_SIZE)
            for (int z = region.minZ; z <= region.maxZ; z += GEO_SIZE)
                list.add(calcGeoIndex(x, z));
    }


    /**
     * @param list List indexes to reuse/fill
     * @param x min area x
     * @param z min area z
     * @param maxX max area x
     * @param maxZ max area z
     */
    public static void computeGeoIndexes(final LIndexList list, int x, int z, final int maxX, final int maxZ) {
        for (list.clear(); x <= maxX; x += GEO_SIZE)
            for (; z <= maxZ; z += GEO_SIZE)
                list.add(calcGeoIndex(x, z));
    }


    /**
     * Fast alternative to {@link java.util.Arrays#copyOf(Object[], int)}
     * @param regions Array to expand
     * @param region Region to add
     * @return expanded array
     */
    public static Region[] expand(final Region[] regions, final Region region){
        Region[] result;
        System.arraycopy(regions, 0, result = new Region[regions.length + 1], 0, regions.length);
        result[regions.length] = region;
        return result;
    }


    /**
     * See {@link #expand(Region[], Region)}
     */
    public static Region[] expand(final Region[] regions, final int addSize){
        Region[] result;
        System.arraycopy(regions, 0, result = new Region[regions.length + addSize], 0, regions.length);
        return result;
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