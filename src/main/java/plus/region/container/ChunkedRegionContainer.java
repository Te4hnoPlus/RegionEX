package plus.region.container;

import plus.region.Region;
import plus.region.RegionQuery;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * The largest container implementation, vertical-optimized for a large amount of regions
 * <p>
 * on add region -> expand this
 * <p>
 * on remove 1 region -> RegionContainer (empty)
 * <p>
 * todo if size == 4 use MultiRegionContainer
 */
public class ChunkedRegionContainer extends RegionContainer{
    private final Region[][] regions;

    public ChunkedRegionContainer() {
        regions = new Region[16][];
    }


    public ChunkedRegionContainer(Region... regions){
        this();
        for (Region region : regions) {
            for(int y = region.minY; y <= region.maxY; y+=16){
                addToIndex(y >> 4, region);
            }
        }
    }


    public int countChunks(){
        int count = 0;
        for (Region[] curRegions : regions) if (curRegions != null)++count;
        return count;
    }


    @Override
    public RegionContainer addRegion(Region region) {
        for(int y = region.minY; y <= region.maxY; y+=16)
            addToIndex(y >> 4, region);
        return this;
    }


    @Override
    public RegionContainer removeRegion(Region region) {
        for(int y = region.minY; y <= region.maxY; y+=16)
            removeFromIndex(y >> 4, region);

        //todo if size == 4 use MultiRegionContainer
        if(size() == 0)return RegionContainer.EMPTY;

        return this;
    }


    private void addToIndex(int index, Region region){
        if(regions[index] == null){
            regions[index] = new Region[]{region};
        } else {
            Region[] curRegion = regions[index];

            Region[] newRegions = new Region[curRegion.length + 1];
            System.arraycopy(regions, 0, newRegions, 0, curRegion.length);
            newRegions[newRegions.length - 1] = region;

            regions[index] = newRegions;
        }
    }


    private void removeFromIndex(int vertIndex, Region region){
        Region[] curRegions = regions[vertIndex];
        if(curRegions.length == 1){
            if(curRegions[0].id == region.id) regions[vertIndex] = null;
        } else {
            Region[] newRegions = new Region[curRegions.length - 1];
            int index = 0;
            boolean removed = false;
            for(Region r: curRegions) {
                if(r.id == region.id) {
                    removed = true;
                    continue;
                }
                newRegions[index++] = r;
            }
            if(removed){
                if(newRegions.length == 0) curRegions[vertIndex] = null;
                else regions[vertIndex] = newRegions;
            }
        }
    }


    @Override
    public void getRegions(RegionQuery query) {
        Region[] curRegions;
        if((curRegions = regions[query.getY() >> 4]) != null)
            for(Region region: curRegions)
                if(region.contains(query.getX(), query.getY(), query.getZ()))
                    query.addRegion(region);
    }


    @Override
    public void getRegions(Region region, RegionQuery query) {
        Region[] curRegions;
        if((curRegions = regions[query.getY() >> 4]) != null)
            for(Region curRegion: curRegions)
                if(curRegion.intersects(region)) query.addRegion(curRegion);
    }


    @Override
    public int size() {
        int count = 0;

        for (Region[] curRegions : regions) {
            if (curRegions != null)
                count += curRegions.length;
        }
        return count;
    }


    @Override
    public void acceptRegions(Consumer<Region> func) {
        for (Region[] curRegions : regions)
            if (curRegions != null) for (Region region : curRegions) func.accept(region);
    }


    @Override
    public Itr iterator() {
        return new Itr(regions);
    }


    public static class Itr implements Iterator<Region> {
        private final Region[][] regions;
        private int chunkIndex, regionIndex;
        private Region[] curChunk;
        private Region next;

        public Itr(Region[][] regions) {
            this.regions = regions;
            this.curChunk = regions[0];
            next = curChunk == null?findNext():curChunk[0];
        }


        private Region findNext() {
            if(curChunk.length == ++regionIndex){
                for (;chunkIndex < regions.length;++chunkIndex){
                    if(regions[chunkIndex] != null){
                        regionIndex = 0;
                        return regions[chunkIndex][0];
                    }
                }
                return null;
            } else {
                return curChunk[regionIndex];
            }
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public Region next() {
            Region prev = next;
            next = findNext();
            return prev;
        }
    }
}