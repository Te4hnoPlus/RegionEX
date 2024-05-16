package plus.region.container;

import plus.region.Region;
import plus.region.RegionQuery;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * RegionContainer for a small (2-4) amount of regions
 * <p>
 * on add 5 region -> ChunkedRegionContainer
 * <p>
 * on remove 2 region -> SingleRegionContainer
 */
public class MultiRegionContainer extends RegionContainer{
    private Region[] regions;

    public MultiRegionContainer(Region... regions) {
        this.regions = regions;
    }


    @Override
    public void getRegions(RegionQuery query) {
        for(Region region: regions) {
            if(region.contains(query.getX(), query.getY(), query.getZ())) {
                query.addRegion(region);
            }
        }
    }


    @Override
    public void getRegions(Region region, RegionQuery query) {
        for (Region curRegion : regions) {
            if(region.intersects(curRegion)) query.addRegion(region);
        }
    }


    @Override
    public RegionContainer addRegion(Region region) {
        Region[] newRegions = new Region[regions.length + 1];
        System.arraycopy(regions, 0, newRegions, 0, regions.length);
        newRegions[newRegions.length - 1] = region;

        if(newRegions.length > 4){
            ChunkedRegionContainer container = new ChunkedRegionContainer(newRegions);
            if(container.countChunks() == 1)return this;
            return container;
        }

        regions = newRegions;
        return this;
    }


    @Override
    public RegionContainer removeRegion(Region region) {
        Region[] newRegions = new Region[regions.length - 1];
        int index = 0;
        boolean removed = false;
        for(Region r: regions) {
            if(r.id == region.id) {
                removed = true;
                continue;
            }
            newRegions[index++] = r;
        }
        if(removed){
            if(newRegions.length == 1) return new SingleRegionContainer(newRegions[0]);
        }
        regions = newRegions;
        return this;
    }


    @Override
    public void acceptRegions(Consumer<Region> func) {
        for (Region region : regions) func.accept(region);
    }


    @Override
    public int size() {
        return regions.length;
    }


    /**
     * See {@link RegionQuery.Itr}
     */
    @Override
    public Iterator<Region> iterator() {
        return new RegionQuery.Itr(regions, regions.length);
    }
}