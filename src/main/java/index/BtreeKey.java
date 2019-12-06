package index;


public class BtreeKey<V> implements Comparable<BtreeKey> {
    private V value;

    public BtreeKey(V key){
        this.value = key;
    }

    @Override
    public int compareTo(BtreeKey anotherKey){
        /**
         * @param   anotherKey - The key to be compared.
         * @return  A negative integer, zero, or a positive integer as this key
         *          is less than, equal to, or greater than the supplied GroupKey object.
         **/
        //System.out.println("==== compareTo ====");
        return compareSingleObj(this.value, anotherKey.value);
    }

    @Override
    public boolean equals(Object obj) {
        //System.out.println("==== equals ====");
        if(obj instanceof BtreeKey) {
            BtreeKey tmp = (BtreeKey)obj;
            if(isNumeric(String.valueOf(tmp.value))){
                //System.out.println("!!!! COMPARING NUMERICAL EQUALITY !!!!");
                return Double.valueOf(String.valueOf(tmp.value)).equals(Double.valueOf(String.valueOf(value)));
            }
            return tmp.value.equals(value);
        }
        return false;
    }

    @Override
    public String toString(){
        return String.valueOf(value);
    }

    private int compareSingleObj(Object v1, Object v2){
        if(isNumeric(String.valueOf(v1))){
            Double res = Double.parseDouble(String.valueOf(v1)) - Double.parseDouble(String.valueOf(v2));
            //System.out.println(v1 + " - " + v2 + " = " + res);
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
