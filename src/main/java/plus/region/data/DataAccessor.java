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


    public <T> Field<T> newField(){
        return new Field<>(this, id++);
    }


    public void use(Accessor accessor){
        this.data = (this.accessor = accessor).get();
    }


    public void set(int id, Object value){
        if(data == null)
            data = new Object[id + 1];
        else if(id >= data.length)
            accessor.set(data = Arrays.copyOf(data, id + 1));
        this.data[id] = value;
    }


    public Object get(int id){
        return id >= data.length ? null : data[id];
    }


    public interface Accessor{
        Object[] get();
        void set(Object[] value);
    }


    public static final class Field<T>{
        private final DataAccessor parent;
        private final int id;

        private Field(DataAccessor parent, int id){
            this.parent = parent;
            this.id = id;
        }


        public void set(T value){
            parent.set(id, value);
        }


        public T get(){
            return (T) parent.get(id);
        }
    }
}