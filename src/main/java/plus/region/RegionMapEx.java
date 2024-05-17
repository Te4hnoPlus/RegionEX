package plus.region;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import plus.region.data.RegionStream;
import plus.region.utl.LIndexList;
import java.io.File;
import java.util.concurrent.Executor;


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

    public RegionMapEx(File geoDir) {
        this.geoDir = geoDir;
    }


    private void ensureLoadedGeo(LIndexList.Itr itr, LIndexList list){
        itr.reset();
        while (itr.hasNext()){
            final long geoIndex = itr.nextLong();
            if(!loadedGeo.contains(geoIndex)){
                loadedGeo.add(geoIndex);
                boolean reuse;

                LIndexList temp = (reuse = itr.hasNext())? new LIndexList() : list;

                RegionStream toAdd = RegionStream.readGeo(geoIndex, geoDir);

                while (toAdd.hasNext()) systemAdd(temp, toAdd.next());

                if(reuse)return;
            }
        }
    }



    /**
     * Ensure that geo region for check region will be loaded
     * @param list List of indexes to reuse
     * @param region check region
     */
    public void ensureLoaded(final LIndexList list, final Region region){
        list.clear();
        Region.computeGeoIndexes(list, region);
        LIndexList.Itr itr = list.iterator();
        while (itr.hasNext()) dirtyGeo.add(itr.nextLong());

        ensureLoadedGeo(itr, list);
    }


    /**
     * Ensure that geo region for check point will be loaded
     * @param list List of indexes to reuse
     * @param x block x
     * @param z block z
     */
    public void ensureLoaded(LIndexList list, int x, int z){
        list.add(Region.calcGeoIndex(x, z));
        ensureLoadedGeo(list.iterator(), list);
    }


    @Override
    public void add(final LIndexList list, final Region region) {
        ensureLoaded(list, region);
        super.add(list, region);
    }


    @Override
    public void remove(final LIndexList list, final Region region) {
        ensureLoaded(list, region);
        super.remove(list, region);
    }


    /**
     * Ignore handle modified regions. See {@link RegionMap#add(LIndexList, Region)}
     * @param list List of indexes to reuse
     * @param region region to add
     */
    protected final void systemAdd(LIndexList list, Region region){
        super.add(list, region);
    }


    /**
     * Ignore handle modified regions. See {@link RegionMap#remove(LIndexList, Region)}
     * @param list List of indexes to reuse
     * @param region region to remove
     */
    protected final void systemRemove(LIndexList list, Region region){
        super.remove(list, region);
    }


    /**
     * Flush all modified regions to disk
     * @param executor Async executor. If null, will use current thread as executor
     */
    public void flushToDisk(Executor executor){
        if(dirtyGeo.isEmpty())return;

        LongOpenHashSet prev = dirtyGeo;
        dirtyGeo = new LongOpenHashSet();

        if(executor == null)executor = DEFAULT;

        for (long index: prev){
            GeoSaveQuery query = new GeoSaveQuery(index, geoDir);
            getRegions(query.areaToSave, query);
            executor.execute(query);
        }
    }


    /**
     * Geo saving query / task
     */
    private static class GeoSaveQuery extends LargeRegionQuery implements Runnable{
        private final Region areaToSave;
        private final long index;
        private final File geoDir;

        public GeoSaveQuery(long geoIndex, File geoDir){
            this.index = geoIndex;
            this.geoDir = geoDir;
            int startX = (int)(geoIndex >> 32);
            int startZ = (int)(geoIndex);

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