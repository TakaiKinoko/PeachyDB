package pair;

public interface IPair<K,V> {

    public K getKey();

    public V getValue();

    public boolean remove();

    public boolean update(V value);
}
