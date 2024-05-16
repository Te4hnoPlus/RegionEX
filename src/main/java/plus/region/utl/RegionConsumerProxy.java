package plus.region.utl;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import plus.region.Region;
import java.util.function.Consumer;


/**
 * Proxies accept of the function through itself to exclude repeated execution
 * <p>
 * It is recommended to use it repeatedly to ensure better performance
 */
public class RegionConsumerProxy implements Consumer<Region> {
    private final IntOpenHashSet set;
    private Consumer<Region> parent;

    public RegionConsumerProxy(Consumer<Region> parent) {
        this.parent = parent;
        set = new IntOpenHashSet();
    }

    public RegionConsumerProxy(IntOpenHashSet set, Consumer<Region> parent) {
        this.set = set;
        this.parent = parent;
    }


    public RegionConsumerProxy init(Consumer<Region> consumer) {
        this.set.clear();
        this.parent = consumer;
        return this;
    }


    /**
     * @return Parent consumer
     */
    public Consumer<Region> parent() {
        return parent;
    }


    /**
     * Accept region if it is not accepted yet
     */
    @Override
    public void accept(Region region) {
        if(set.add(region.id)) parent.accept(region);
    }
}