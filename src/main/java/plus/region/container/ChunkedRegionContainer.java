package plus.region.container;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
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


    public ChunkedRegionContainer(final Region... regions){
        this();
        for (Region region : regions) {
            final int maxY = clamp15(region.maxY);
            for (int y = region.minY; y < maxY; y += 16)
                addToIndex(y >> 4, region);
        }
    }


    public int countChunks(){
        int count = 0;
        for (Region[] curRegions : regions) if (curRegions != null)++count;
        return count;
    }


    @Override
    public RegionContainer addRegion(final Region region) {
        final int maxY = clamp15(region.maxY);
        for(int y = region.minY; y < maxY; y+=16)
            addToIndex(y >> 4, region);
        return this;
    }


    @Override
    public RegionContainer removeRegion(final Region region) {
        final int maxY = clamp15(region.maxY);
        for(int y = region.minY; y < maxY; y+=16)
            removeFromIndex(y >> 4, region);

        //todo if size == 4 use MultiRegionContainer
        if(size() == 0)return RegionContainer.EMPTY;

        return this;
    }


    private void addToIndex(final int index, final Region region){
        regions[index] = regions[index] == null ? new Region[]{region} : Region.expand(regions[index], region);
    }


    private void removeFromIndex(final int vertIndex, final Region region){
        final Region[] curRegions = regions[vertIndex];
        if(curRegions.length == 1)
            if(curRegions[0].id == region.id) regions[vertIndex] = null;
        else {
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
    public void getRegions(final RegionQuery query) {
        final Region[] curRegions;
        if((curRegions = regions[query.getY() >> 4]) != null)
            for(Region region: curRegions)
                if(region.contains(query.getX(), query.getY(), query.getZ()))
                    query.addRegion(region);
    }


    @Override
    public void getRegions(final Region region, final RegionQuery query) {
        final int maxY = clamp15(region.maxY);
        for(int y = region.minY; y < maxY; y+=16){
            final Region[] curRegions;
            if((curRegions = regions[y >> 4]) == null) continue;
            for(Region curRegion: curRegions)
                if(curRegion.intersects(region)) query.addRegion(curRegion);
        }
    }


    @Override
    public int size() {
        int count = 0;
        for (Region[] curRegions : regions)
            if (curRegions != null) count += curRegions.length;
        return count;
    }


    @Override
    public void acceptRegions(final Consumer<Region> func) {
        for (Region[] curRegions : regions) if (curRegions != null)
            for (Region region : curRegions) func.accept(region);
    }


    @Override
    public void acceptRegions(final int x, final int y, final int z, final Consumer<Region> func) {
        for (Region[] curRegions : regions) if (curRegions != null)
            for (Region region : curRegions) if(region.contains(x, y, z)) func.accept(region);
    }


    @Override
    public Itr iterator() {
        return new Itr(regions);
    }



    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<C: ");
        for (Region[] curRegions : regions) {
            if (curRegions == null) continue;
            for (Region region : curRegions)
                builder.append(region).append(", ");
        }
        builder.setCharAt(builder.length() - 1, '>');
        return builder.toString();
    }


    private static int clamp15(int posY){
        if(posY < 0) posY = 0;
        if(posY > 255) posY = 255;
        return posY | 15;
    }


    public static class Itr implements Iterator<Region> {
        private final IntOpenHashSet visited = new IntOpenHashSet();
        private final Region[][] regions;
        private int chunkIndex, regionIndex;
        Region next;

        public Itr(final Region[][] regions) {
            this.regions = regions;
            findNext();
        }


        private void findNext() {
            for (;chunkIndex < regions.length;++chunkIndex) {
                final Region[] curRegions = regions[chunkIndex];
                if (curRegions != null){
                    for (;regionIndex < curRegions.length;++regionIndex) {
                        final Region region = curRegions[regionIndex];
                        if (visited.add(region.id)) {
                            next = region;
                            return;
                        }
                    }
                }
                regionIndex = 0;
            }
            next = null;
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public Region next() {
            final Region prev = next;
            findNext();
            return prev;
        }
    }
}