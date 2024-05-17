package plus.region.utl;


/**
 * See {@link LIndexList}
 */
public class CLIndexList extends LIndexList{
    private final Itr itr = new Itr(this);

    /**
     * Use final iterator to speedy
     * @return reset iterator
     */
    @Override
    public Itr iterator() {
        return itr.reset();
    }
}