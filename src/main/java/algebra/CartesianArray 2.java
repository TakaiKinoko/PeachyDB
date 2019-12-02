package algebra;

import java.util.*;

import util.*;

public class CartesianArray {
    String[][] res;
    int i;

    public CartesianArray (){
        res = new String[500][2];
        int i;
    }

    private void add(String v1, String v2){
        if(i >= res.length){
            String[][] doubled = new String[res.length*2][2];
            System.arraycopy(res, 0, doubled,
                    0, res.length);
            res = doubled;
        }
        res[i++][0] = v1;
        res[i++][1] = v2;
    }

    public void equal(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT){
        for(GroupKey<String> k: RIGHT.keySet()){
            if(LEFT.containsKey(k)){
                add_combinations(LEFT.get(k), RIGHT.get(k));
            }
        }

        //print_pairs(res);
    }

    public void not_equal(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT){
        for(GroupKey<String> k: RIGHT.keySet()){
            if(!LEFT.containsKey(k)){
                add_combinations(LEFT.get(k), RIGHT.get(k));
            }
        }
        //print_pairs(res);

    }

    public void less(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT, boolean inclusive){
        /**
         * if inclusive is set as true then this is less than or equal to
         *
         *      * LESS THAN:  (LEFT less than RIGHT)
         *      * iterate over RIGHT:
         *      *     for each lk in LEFT.headMap(K rk, inclusive = false)
         *      *         add all possible combinations of l and r into RES as IND_PAIRs
         * */

        for(GroupKey<String> rk: RIGHT.keySet()){
            NavigableMap<GroupKey<String>, ArrayList> S = LEFT.headMap(rk, inclusive);
            for(GroupKey<String> lk: S.keySet()){
                add_combinations(S.get(lk), RIGHT.get(rk));
            }

        }
        //print_pairs(res);
    }

    public void more(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT, boolean inclusive){
        /**
         * if inclusive is set as true then this is less than or equal to
         *
         *
         * */
        for(GroupKey<String> rk: RIGHT.keySet()){
            NavigableMap<GroupKey<String>, ArrayList> S = LEFT.tailMap(rk, inclusive);
            for(GroupKey<String> lk: S.keySet()){
                add_combinations(S.get(lk), RIGHT.get(rk));
            }
        }
        //print_pairs(res);
    }


    private void add_combinations(ArrayList left, ArrayList right){
        for(Object ind1: left){
            for(Object ind2: right){
                add(String.valueOf(ind1), String.valueOf(ind2));
            }
        }
    }

}
