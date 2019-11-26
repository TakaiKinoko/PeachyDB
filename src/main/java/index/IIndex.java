package index;

// potentially create indexing for every column
public interface IIndex<K, V> {

    public boolean insert(K key, V value);

    public V search(K key);

    public boolean delete(K key);

}
