package plus.region.data;

import java.util.Arrays;


/**
 * Help to access to abstract data
 */
public class DataAccessor {
    private int id = 0;
    Accessor accessor;
    Object[] data;     // cached data

    public DataAccessor(){}


    /**
     * Create new field. It is recommended to use strictly inside your accessor
     */
    public <T> Field<T> newField(){
        return new Field<>(this, id++);
    }


    /**
     * Use this accessor to access data
     */
    public void use(final Accessor accessor){
        this.data = (this.accessor = accessor).get();
    }


    /**
     * Edit field value
     * @param id Field ID
     * @param value new field value
     */
    public void set(final int id, final Object value){
        if(data == null)
            accessor.set(data = new Object[id + 1]);
        else if(id >= data.length)
            accessor.set(data = Arrays.copyOf(data, id + 1));
        this.data[id] = value;
    }


    /**
     * Get field value
     * @param id Field ID
     * @return Field value
     */
    public Object get(final int id){
        return data == null || id >= data.length ? null : data[id];
    }


    /**
     * Abstract Object[] accessor
     */
    public interface Accessor{
        Object[] get();
        void set(Object[] value);
    }


    /**
     * Final field to access to data by ID
     */
    public static final class Field<T>{
        private final DataAccessor parent;
        private final int id;

        private Field(final DataAccessor parent, final int id){
            this.parent = parent;
            this.id = id;
        }


        public void set(final T value){
            parent.set(id, value);
        }


        public T get(){
            return (T) parent.get(id);
        }
    }
}