package plus.region;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import plus.region.data.IoUtils;
import plus.region.data.NextIdMap;
import plus.region.data.RegionStream;
import plus.region.utl.LIndexList;
import plus.region.utl.RegionConsumerProxy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.function.Consumer;


/**
 * Directory-linked map of regions
 * <p>
 * Uses the specified directory to store basic data about the regions.
 * <p>
 * Before requiring regions from disk, use {@link #ensureLoaded(LIndexList, Region)} to ensure that geo regions will be loaded
 * <p>
 * Saving does not happen automatically. Use {@link #flushToDisk(Executor)} to flush all modified regions to disk
 */
public class RegionMapEx extends RegionMap {
    protected static final Executor DEFAULT = Runnable::run;
    protected final LongOpenHashSet loadedGeo = new LongOpenHashSet();
    protected LongOpenHashSet dirtyGeo  = new LongOpenHashSet();
    protected final File geoDir;
    protected final NextIdMap nextIdMap = new NextIdMap();

    public RegionMapEx(final File geoDir) {
        if(geoDir != null) {
            if (geoDir.isFile()) throw new IllegalArgumentException("Input file must be a directory");
            if (!geoDir.exists()) {
                if (!geoDir.mkdirs()) throw new IllegalArgumentException("Cannot create directory here");
            }
        }
        this.geoDir = geoDir;
        NextIdMap.readMap(nextIdMap, geoDir);
    }


    public NextIdMap idMap(){
        return nextIdMap;
    }


    private void ensureLoadedGeo(final LIndexList.Itr itr, final LIndexList list){
        if(geoDir == null) return;
        itr.reset();
        LIndexList temp = null;
        while (itr.hasNext()){
            final long geoIndex = itr.nextLong();
            if(!loadedGeo.contains(geoIndex)){
                loadedGeo.add(geoIndex);

                if(temp == null) temp = itr.hasNext()? new LIndexList() : list;

                RegionStream toAdd = RegionStream.readGeo(geoIndex, geoDir);

                while (toAdd.hasNext()) systemAdd(temp, toAdd.next());
            }
        }
    }


    public void clearDirty(){
        dirtyGeo  = new LongOpenHashSet();
    }


    public boolean hasDirty(){
        return !dirtyGeo.isEmpty();
    }


    /**
     * Unload all non-dirty and non-used geo regions
     * @param chunkPosIter Iterator of all loaded chunk indexes
     * @return List of dirty geo indexes to be unloaded
     */
    public LIndexList checkToUnload(Iterator<Long> chunkPosIter){
        LIndexList list;
        checkToUnload(list = new LIndexList(), chunkPosIter);
        return list;
    }


    /**
     * Unload all non-dirty and non-used geo regions
     * @param chunkPosIter Iterator of all loaded chunk indexes
     * @param list List of indexes to reuse. On completion, it will be filled dirty geo indexes to be unloaded
     */
    public void checkToUnload(LIndexList list, Iterator<Long> chunkPosIter){
        LongOpenHashSet temp = new LongOpenHashSet(); //is really loaded

        while(chunkPosIter.hasNext()){
            temp.add(Region.mcChunkToGeoIndex(chunkPosIter.next()));
        }

        list.clear(); //to unload
        for (long index: loadedGeo){
            if(!temp.contains(index)) list.add(index);
        }

        temp.clear(); //now to remove
        LIndexList.ResItr itr = list.fixedIter();
        list.clear();

        for (long index: itr){
            if(!dirtyGeo.contains(index)) {
                loadedGeo.remove(index);
                temp.add(index);
            }
            else list.add(index);
        }
        map.keySet().removeIf(value -> temp.contains(Region.chunkIndexToGeoIndex(value)));
    }


    /**
     * Ensure that geo region for check area will be loaded
     * <p>
     * It is not recommended to use, {@link RegionMapEx#ensureLoaded(LIndexList, int, int, int, int)}
     * @param minX min block x
     * @param minZ min block z
     * @param maxX max block x
     * @param maxZ max block z
     */
    public void ensureLoaded(final int minX, final int minZ, final int maxX, final int maxZ){
        ensureLoaded(new LIndexList(), minX, minZ, maxX, maxZ);
    }


    /**
     * Ensure that geo region for check area will be loaded
     * @param list List of indexes to reuse
     * @param minX min block x
     * @param minZ min block z
     * @param maxX max block x
     * @param maxZ max block z
     */
    public void ensureLoaded(final LIndexList list, final int minX, final int minZ, final int maxX, final int maxZ){
        Region.computeGeoIndexes(list, minX, minZ, maxX, maxZ);
        ensureLoadedGeo(list.iterator(), list);
    }


    /**
     * It is not recommended to use, {@link RegionMapEx#ensureLoaded(LIndexList, Region)}
     * @param region check region
     */
    public void ensureLoaded(final Region region){
        ensureLoaded(new LIndexList(), region);
    }


    /**
     * Ensure that geo region for check region will be loaded
     * @param list List of indexes to reuse
     * @param region check region
     */
    public void ensureLoaded(final LIndexList list, final Region region){
        list.clear();
        Region.computeGeoIndexes(list, region);
        ensureLoadedGeo(list.iterator(), list);
    }


    /**
     * Handle modify regions
     * @param list List of indexes to reuse
     * @param region check region
     */
    protected void onModify(final LIndexList list, final Region region){
        list.clear();
        Region.computeGeoIndexes(list, region);
        LIndexList.Itr itr = list.iterator();
        while (itr.hasNext()) dirtyGeo.add(itr.nextLong());

        ensureLoadedGeo(itr, list);
    }


    /**
     * It is not recommended to use, {@link RegionMapEx#ensureLoaded(LIndexList, int, int)}
     * @param x block x
     * @param z block z
     */
    public void ensureLoaded(final int x, final int z){
        ensureLoaded(new LIndexList(), x, z);
    }


    /**
     * Ensure that geo region for check point will be loaded
     * @param list List of indexes to reuse
     * @param x block x
     * @param z block z
     */
    public void ensureLoaded(final LIndexList list, final int x, final int z){
        list.clear();
        list.add(Region.calcGeoIndex(x, z));
        ensureLoadedGeo(list.iterator(), list);
    }


    @Override
    public void add(final LIndexList list, final Region region) {
        onModify(list, region);
        super.add(list, region);
    }


    @Override
    public void remove(final LIndexList list, final Region region) {
        onModify(list, region);
        super.remove(list, region);
    }


    /**
     * Ignore handle modified regions. See {@link RegionMap#add(LIndexList, Region)}
     * @param list List of indexes to reuse
     * @param region region to add
     */
    protected final void systemAdd(final LIndexList list, final Region region){
        super.add(list, region);
    }


    /**
     * Ignore handle modified regions. See {@link RegionMap#remove(LIndexList, Region)}
     * @param list List of indexes to reuse
     * @param region region to remove
     */
    protected final void systemRemove(final LIndexList list, final Region region){
        super.remove(list, region);
    }


    /**
     * Flush all modified regions to disk
     * @param executor Async executor. If null, will use current thread as executor
     */
    public void flushToDisk(Executor executor){
        if(executor == null)executor = DEFAULT;

        if(nextIdMap.isDirty() && geoDir != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                NextIdMap.writeTo(nextIdMap, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            nextIdMap.setDirty(false);
            executor.execute(() -> IoUtils.writeToFile(NextIdMap.nextIdFile(geoDir), out::writeTo));
        }

        if(dirtyGeo.isEmpty())return;

        final LongOpenHashSet prev = dirtyGeo;
        dirtyGeo = new LongOpenHashSet();

        if(geoDir == null) return;

        GeoSaveQuery query = null;
        final LIndexList list = new LIndexList();
        final LIndexList.Itr itr = list.iterator();
        final RegionConsumerProxy proxy = new RegionConsumerProxy(null);
        for (long index: prev){
            if(query == null) query =  new GeoSaveQuery(index, geoDir);
            else              query.editRegion(index);

            query.clear();
            Region.computeIndexes(list, query.areaToSave);
            itr.reset();
            proxy.init(query::add);
            while (itr.hasNext()) map.get(itr.nextLong()).acceptRegions(proxy);

            if(query.isEmpty()) {
                long remove = query.index;
                executor.execute(() -> RegionStream.removeGeo(remove, geoDir));
            } else {
                executor.execute(query);
                query = null;
            }
        }
    }


    /**
     * @return new context for this
     */
    public Context newContext() {
        return new Context(this);
    }


    /**
     * Context to effective access to {@link RegionMapEx}
     */
    public static class Context{
        private final LIndexList list = new LIndexList();
        private final RegionQuery query = new RegionQuery();
        private final RegionMapEx map;
        private RegionConsumerProxy proxy;

        public Context(final RegionMapEx map) {
            this.map = map;
        }

        /**
         * @return Linked {@link RegionMapEx}
         */
        public final RegionMapEx map() {
            return map;
        }


        /**
         * Add region to map
         * @param region region to add
         */
        public void add(final Region region) {
            map.add(list, region);
        }


        /**
         * Remove region from map
         * @param region region to remove
         */
        public void remove(final Region region) {
            map.remove(list, region);
        }


        /**
         * Ensure that geo region for check region will be loaded, see {@link RegionMapEx#ensureLoaded(LIndexList, Region)}
         * @param check check region
         */
        public void ensureLoaded(final Region check) {
            map.ensureLoaded(list, check);
        }


        /**
         * Ensure that geo region for check point will be loaded, see {@link RegionMapEx#ensureLoaded(LIndexList, int, int)}
         * @param x block x
         * @param z block z
         */
        public void ensureLoaded(final int x, final int z) {
            map.ensureLoaded(list, x, z);
        }


        /**
         * Ensure that geo region for check area will be loaded, see {@link RegionMapEx#ensureLoaded(LIndexList, int, int, int, int)}
         * @param minX min block x
         * @param minZ min block z
         * @param maxX max block x
         * @param maxZ max block z
         */
        public void ensureLoaded(final int minX, final int minZ, final int maxX, final int maxZ){
            LIndexList list;
            Region.computeGeoIndexes(list = this.list, minX, minZ, maxX, maxZ);
            map.ensureLoadedGeo(list.iterator(), list);
        }


        /**
         * @param x block x
         * @param y block y
         * @param z block z
         * @return Completed pooled query with all regions in ZYX point, see {@link RegionMap#getRegions(RegionQuery)}
         */
        public RegionQuery getRegions(final int x, final int y, final int z) {
            RegionQuery query;
            map.getRegions(query = this.query.init(x, y, z));
            return query;
        }


        /**
         * Accept operation to all regions, intersected with check, see {@link RegionMap#acceptRegions(Region, LIndexList, RegionQuery, RegionConsumerProxy)}
         * @param check Region to intersect
         * @param func Consumer to accept
         */
        public void acceptRegions(final Region check, final RegionConsumerProxy func){
            map.acceptRegions(check, list, query, func.parent());
        }


        /**
         * Accept operation to all regions, intersected with check, see {@link RegionMap#acceptRegions(Region, LIndexList, RegionQuery, Consumer)}
         * @param check Region to intersect
         * @param func Consumer to accept
         */
        public void acceptRegions(final Region check, final Consumer<Region> func){
            map.acceptRegions(check, list, query, func);
        }


        /**
         * Accept operation to all regions, see {@link RegionMap#acceptRegions(RegionConsumerProxy)}
         * @param func Consumer to accept
         */
        public void acceptRegions(RegionConsumerProxy func){
            map.acceptRegions(func);
        }


        /**
         * Accept operation to all regions, see {@link RegionMap#acceptRegions(Consumer)}
         * @param func Consumer to accept
         */
        public void acceptRegions(Consumer<Region> func){
            map.acceptRegions(taskProxy(func));
        }


        /**
         * @param region Region to intersect
         * @return Completed pooled query with all regions intersecting with region, see {@link RegionMap#getRegions(Region, LIndexList, RegionQuery)}
         */
        public RegionQuery getRegions(final Region region) {
            RegionQuery query;
            map.getRegions(region, list, query = this.query);
            return query;
        }


        /**
         * @param region Region to intersect
         * @return Completed pooled or effective query with all regions intersecting with region, see {@link RegionMap#getRegionsAuto(Region, LIndexList)}
         */
        public RegionQuery getRegionsAuto(final Region region) {
            int volume = region.volume();
            RegionQuery query = volume > Region.EFFECTIVE_MAX_VOLUME ? new LargeRegionQuery() : this.query;
            map.getRegions(region, list, query);
            return query;
        }


        /**
         * @param chunkPosIter Iterator of all loaded chunk indexes
         * @return Dirty geo indexes to be unloaded, see {@link RegionMapEx#checkToUnload(LIndexList, Iterator)}
         */
        public LIndexList checkToUnload(Iterator<Long> chunkPosIter){
            map.checkToUnload(list, chunkPosIter);
            return list;
        }


        /**
         * Create new region and choose ID automatically
         * @param minX min region block x
         * @param minY min region block y (0-255)
         * @param minZ min region block z
         * @param maxX max region block x
         * @param maxY max region block y (0-255)
         * @param maxZ max region block z
         * @return new region
         */
        public Region createRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            if(minY > maxY){
                int t = minY;
                minY = maxY;
                maxY = t;
            }
            if(minY < 0)   minY = 0;
            if(maxY > 255) maxY = 255;
            Region region = new Region(map.nextIdMap.nextId(), minX, minY, minZ, maxX, maxY, maxZ);
            map.add(list, region);
            return region;
        }


        /**
         * @return inner list which is reused in all calculations. Don't call any methods, while using it
         */
        public LIndexList list(){
            return list;
        }


        /**
         * Reset all cached data and allocated memory for sub-items if needed
         */
        public void resetIfNeed(){
            proxy = null;
            list.resetIfNeed();
            query.resetIfNeed();
        }


        /**
         * Make region task proxy from func. Reuse proxy if can
         * @param func func to proxy
         */
        public RegionConsumerProxy taskProxy(Consumer<Region> func){
            return proxy == null? proxy = new RegionConsumerProxy(func) : proxy.init(func);
        }
    }


    /**
     * Geo saving query / task
     */
    private static final class GeoSaveQuery extends LargeRegionQuery implements Runnable{
        private Region areaToSave;
        private long index;
        private final File geoDir;

        public GeoSaveQuery(final long geoIndex, final File geoDir){
            this.index = geoIndex;
            this.geoDir = geoDir;
            editRegion(geoIndex);
        }


        public void editRegion(final long geoIndex){
            this.index = geoIndex;
            int startX = (int)(geoIndex >> 32) ;
            int startZ = (int)(geoIndex)       ;

            areaToSave = new Region(
                    startX, 0, startZ, startX + 1024, 256, startZ + 1024
            );
        }


        @Override
        public void run() {
            RegionStream.writeGeo(index, geoDir, iterator());
        }
    }
}