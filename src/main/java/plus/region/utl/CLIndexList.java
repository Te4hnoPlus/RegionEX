package plus.region.utl;


public class CLIndexList extends LIndexList{
    private final Itr itr = new Itr(this);

    @Override
    public Itr iterator() {
        return itr.reset();
    }
}