package util;

import java.util.Comparator;

public class GroupKey<V> implements Comparable<GroupKey>, Comparator<GroupKey>{
    /**
     * Customized class for composite HashMap key
     * */
    private V[] key;

    public GroupKey(V[] key) {
        this.key = key;
    }

    public V[] getKey(){
        return this.key;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GroupKey) {
            GroupKey tmp = (GroupKey)obj;
            boolean flag = true;
            for(int i = 0; i < key.length; i++){
                flag = flag && key[i].equals(tmp.key[i]);
            }
            return flag;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String res = "";
        for(Object o: key){
            res += String.valueOf(o);
        }
        return (res).hashCode();
    }


    @Override
    public int compareTo(GroupKey anotherKey){
        /**
         * @param   anotherKey - The key to be compared.
         * @return  A negative integer, zero, or a positive integer as this key
         *          is less than, equal to, or greater than the supplied GroupKey object.
         **/
        for(int i = 0; i < this.key.length; i++){
            int res = compareSingleObj(this.key[i], anotherKey.key[i]);
            if(res != 0)
                return res;
        }
        return 0;
    }


    @Override
    public int compare(GroupKey k1, GroupKey k2){
        /**
         * compare recursively
         * */
        for(int i = 0; i < k1.key.length; i++){
            int res = compareSingleObj(k1.key[i], k2.key[i]);
            if(res != 0)
                return res;
        }
        return 0;
    }

    @Override
    public String toString(){
        String res = "";
        for(Object k: key)
            res += k + " ";
        return res;
    }

    private int compareSingleObj(Object v1, Object v2){
        if(isNumeric(String.valueOf(v1))){
            Double res = Double.parseDouble(String.valueOf(v1)) - Double.parseDouble(String.valueOf(v2));
            if(res < 0)
                return -1;
            if(res == 0)
                return 0;
            else return 1;
        }else
            return String.valueOf(v1).compareTo(String.valueOf(v2));
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
