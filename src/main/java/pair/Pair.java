package pair;

public class Pair<K,V> implements IPair<K,V> {
    K key;
    V val;

    public Pair(K k, V v) {
        key = k;
        val = v;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return val;
    }

    public boolean remove() {
        try{
            val = null;
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public boolean update(V value) {
        try{
            val = value;
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean deleted() {
        return val == null;
    }

}
