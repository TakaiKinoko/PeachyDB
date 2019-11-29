package util;

import java.util.Comparator;

public class GroupKey implements Comparable<GroupKey>, Comparator<GroupKey>{
    /**
     * Customized class for composite HashMap key
     * */
    private Object[] key;

    public GroupKey(Object[] key) {
        this.key = key;
    }

    public Object[] getKey(){
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
        //System.out.println("INSIDE COMPARETO" + this.getKey()[0] + " v.s. " + anotherKey.getKey()[0]);
        if(this.key[0] instanceof Integer) {
            Integer res = Integer.parseInt(String.valueOf(this.key[0])) - Integer.parseInt(String.valueOf(anotherKey.getKey()[0]));
            //return Integer.parseInt(String.valueOf(this.key[0])) - Integer.parseInt(String.valueOf(anotherKey.getKey()[0]));
            //System.out.println(res);
            return res;
        }
        return String.valueOf(this.key[0]).compareTo(String.valueOf(anotherKey.getKey()[0]));
    }


    @Override
    public int compare(GroupKey k1, GroupKey k2){
        if(k1.key[0] instanceof Integer) {
            Integer res = Integer.parseInt(String.valueOf(k1.key[0])) - Integer.parseInt(String.valueOf(k2.getKey()[0]));
            //return Integer.parseInt(String.valueOf(this.key[0])) - Integer.parseInt(String.valueOf(anotherKey.getKey()[0]));
            //System.out.println(res);
            if(res < 0)
                return -1;
            if(res == 0)
                return 0;
            return 1;
        }
        return String.valueOf(k1.key[0]).compareTo(String.valueOf(k2.getKey()[0]));
    }

    @Override
    public String toString(){
        String res = "";
        for(Object k: key)
            res += k + " ";
        return res;
    }
}
