package plus.region.utl;

import java.util.Iterator;


/**
 * Fast long list, Used in request operations.
 * <p>
 * It is recommended to use it repeatedly to ensure better performance
 */
public class LIndexList implements Iterable<Long>{
    private long[] list;
    private int size;

    public LIndexList(long[] list) {
        this.list = list;
        this.size = list.length;
    }

    public LIndexList() {
        this.list = new long[8];
        this.size = 0;
    }


    public LIndexList(int size) {
        this.list = new long[size];
        this.size = 0;
    }


    /**
     * @return the number of elements in the list
     */
    public int size() {
        return size;
    }


    /**
     * Reset list size
     */
    public void clear() {
        size = 0;
    }


    /**
     * @return long at index
     * throws IndexOutOfBoundsException if the index is out of range
     */
    public long get(int index) {
        return list[index];
    }


    public void add(long value) {
        if(size == list.length) {
            long[] newList = new long[list.length + 8];
            System.arraycopy(list, 0, newList, 0, list.length);
            list = newList;
        }
        list[size] = value;
        size++;
    }


    @Override
    public Itr iterator() {
        return new Itr(this);
    }


    /**
     * Fast long list iterator
     * <p>
     * It is recommended to use it repeatedly (if possible) to ensure better performance
     */
    public static class Itr implements Iterator<Long>, Iterable<Long> {
        private final LIndexList list;
        private int index;

        public Itr(LIndexList list) {
            this.list = list;
            this.index = 0;
        }


        @Override
        public Iterator<Long> iterator() {
            index = 0;
            return this;
        }


        /**
         * Reset iterator to reuse
         */
        public Itr reset() {
            index = 0;
            return this;
        }


        @Override
        public boolean hasNext() {
            return index < list.size();
        }


        @Override
        public Long next() {
            return list.get(index++);
        }


        /**
         * @return the next long in the list
         */
        public long nextLong() {
            return list.get(index++);
        }
    }
}