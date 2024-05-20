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

    public LIndexList(final long[] list) {
        this.list = list;
        this.size = list.length;
    }

    public LIndexList() {
        this.list = new long[8];
        this.size = 0;
    }


    public LIndexList(final int size) {
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
    public long get(final int index) {
        return list[index];
    }


    public void add(final long value) {
        if(size == list.length) {
            long[] newList;
            System.arraycopy(list, 0, newList = new long[size + 8], 0, size);
            list = newList;
        }
        list[size] = value;
        size++;
    }


    /**
     * Replace first long equal to last element and trim size
     */
    public void removeSwap(long value) {
        int i = 0, s;
        if((s = size) == 0) return;
        if(s == 1) {
            --size;
            return;
        }
        for(long[] list = this.list; i < s; i++)
            if(list[i] == value) {
                list[i] = list[--size];
                return;
            }
    }


    @Override
    public Itr iterator() {
        return new Itr(this);
    }


    /**
     * @return Iterator for fixed state
     */
    public ResItr fixedIter() {
        return new ResItr(list, size);
    }


    @Override
    public String toString() {
        if(size == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<size;i++) {
            sb.append(list[i]).append(",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }


    /**
     * Fast long list iterator
     * <p>
     * It is recommended to use it repeatedly (if possible) to ensure better performance
     */
    public static class Itr implements Iterator<Long>, Iterable<Long> {
        private final LIndexList list;
        private int index;

        public Itr(final LIndexList list) {
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


    /**
     * Fast long list iterator for fixed state
     * <p>
     * Use this iterator only then your need fix list size and array in iterator
     */
    public static class ResItr implements Iterator<Long>, Iterable<Long> {
        private final long[] curArr;
        private final int curSize;
        private int cursor = 0;

        public ResItr(final long[] curArr, final int curSize) {
            this.curArr = curArr;
            this.curSize = curSize;
        }


        @Override
        public Iterator<Long> iterator() {
            cursor = 0;
            return this;
        }


        @Override
        public boolean hasNext() {
            return cursor < curSize;
        }


        @Override
        public Long next() {
            return curArr[cursor++];
        }


        public long nextLong() {
            return curArr[cursor++];
        }
    }
}