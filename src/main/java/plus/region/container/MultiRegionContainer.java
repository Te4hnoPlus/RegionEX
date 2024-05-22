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
    public void getRegions(final RegionQuery query) {
        for(Region region: regions) if(region.contains(query.getX(), query.getY(), query.getZ()))
            query.addRegion(region);
    }


    @Override
    public void acceptRegions(final int x, final int y, final int z, final Consumer<Region> func) {
        for(Region region: regions) if(region.contains(x, y, z))
            func.accept(region);
    }


    @Override
    public void getRegions(final Region region, final RegionQuery query) {
        for (Region curRegion : regions)
            if(region.intersects(curRegion)) query.addRegion(curRegion);
    }


    @Override
    public RegionContainer addRegion(final Region region) {
        Region[] newRegions;

        if((newRegions = Region.expand(regions, region)).length > 4){
            ChunkedRegionContainer container = new ChunkedRegionContainer(newRegions);
            if(container.countChunks() == 1)return this;
            return container;
        }

        regions = newRegions;
        return this;
    }


    @Override
    public RegionContainer addRegionOrRelink(Region region) {
        Region[] prev = regions;
        for (int i = 0;i<prev.length;i++)
            if(prev[i].id == region.id) {
                prev[i] = region;
                return this;
            }
        return this.addRegion(region);
    }


    @Override
    public RegionContainer removeRegion(final Region region) {
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
        if(removed)
            if(newRegions.length == 1) return new SingleRegionContainer(newRegions[0]);

        regions = newRegions;
        return this;
    }


    @Override
    public void acceptRegions(final Consumer<Region> func) {
        for (Region region : regions) func.accept(region);
    }


    @Override
    public Region getRegion(int id) {
        for(Region region: regions) if(region.id == id) return region;
        return null;
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


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<C: ");
        for (Region region : regions) {
            builder.append(region);
            builder.append(",");
        }
        builder.setCharAt(builder.length() - 1, '>');
        return builder.toString();
    }
}