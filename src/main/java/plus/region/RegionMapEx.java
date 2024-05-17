package plus.region;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import plus.region.data.RegionStream;
import plus.region.utl.LIndexList;
import java.io.File;
import java.util.concurrent.Executor;

import static plus.region.Region.GEO_MASK;


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
        if(geoDir.isFile())throw new IllegalArgumentException("Input file must be a directory");
        if(!geoDir.exists()){
            if(!geoDir.mkdirs())throw new IllegalArgumentException("Cannot create directory here");
        }
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
        LIndexList.Itr itr = list.iterator();
        while (itr.hasNext()) dirtyGeo.add(itr.nextLong());

        ensureLoadedGeo(itr, list);
    }


    /**
     * It is not recommended to use, {@link RegionMapEx#ensureLoaded(LIndexList, int, int)}
     * @param x block x
     * @param z block z
     */
    public void ensureLoaded(int x, int z){
        ensureLoaded(new LIndexList(), x, z);
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

        GeoSaveQuery query = null;
        for (long index: prev){
            if(query == null) query =  new GeoSaveQuery(index, geoDir);
            else              query.editRegion(index);

            getRegions(query.areaToSave, query);

            if(query.isEmpty()) {
                long remove = query.index;
                executor.execute(() -> RegionStream.readGeo(remove, geoDir));
            } else {
                executor.execute(query);
                query = null;
            }
        }
    }


    /**
     * Geo saving query / task
     */
    private static class GeoSaveQuery extends LargeRegionQuery implements Runnable{
        private Region areaToSave;
        private long index;
        private final File geoDir;

        public GeoSaveQuery(long geoIndex, File geoDir){
            this.index = geoIndex;
            this.geoDir = geoDir;
            editRegion(geoIndex);
        }


        public void editRegion(long geoIndex){
            int startX = ((int)(geoIndex >> 32)) & GEO_MASK;
            int startZ = ((int)(geoIndex))       & GEO_MASK;

            areaToSave = new Region(
                    startX, 0, startZ, startX + 1024, 256, startZ + 1024
            );
            System.out.println(areaToSave);
        }


        @Override
        public void run() {
            RegionStream.writeGeo(index, geoDir, iterator());
        }
    }
}