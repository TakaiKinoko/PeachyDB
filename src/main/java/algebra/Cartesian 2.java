package algebra;

import java.util.*;

import util.*;

public class Cartesian {

    /** TODO USE TREEMAP!!
     * In type: result from groupby, which is TreeMap<GroupKey<String>, ArrayList>
     *
     *
     * LEFT:        RIGHT
     * [lk1, l1]    [rk1, r1]
     * [lk2, l2]    [rk2, r2]
     * ...          ...
     * [lkn, ln]    [rkn, rn]
     *
     *
     * RESULT:
     * RES = HashSet<IND_PAIR>
     *
     *
     *======================================================================
     * ALGORITHM
     *======================================================================
     * EQUAL:
     * iterate over RIGHT.
     * find in LEFT where lk == rk
     * add all possible combinations of l and r into RES as IND_PAIRs
     *======================================================================
     * NOT EQUAL:
     * outer loop: iterate over RIGHT
     *    inner loop: iterate over LEFT
     *         if rk == lk, continue
     *         else add all possible combinations of l and r into RES as IND_PAIRs
     *======================================================================
     * LESS THAN:  (LEFT less than RIGHT)
     * iterate over RIGHT:
     *     for each lk in LEFT.headMap(K rk, inclusive = false)
     *         add all possible combinations of l and r into RES as IND_PAIRs
     *
     *======================================================================
     * MORE THAN:
     * iterate over RIGHT:
     *     for each lk in LEFT.tailMap(K fromKey, inclusive = false)
     *         add all possible combinations of l and r into RES as IND_PAIRs
     *======================================================================
     * LESS OR EQUAL TO:
     * same as LESS THAN, except headMap.inclusive = true
     *======================================================================
     * MORE THAN OR EQUAL TO:
     * same as MORE THAN, except tailMap.inclusive = true
     *
     *
     *======================================================================
     * SUBROUTINES
     *======================================================================
     * add all possible combination of two lists as IND_PAIRS into a hashset
     * */


    public static HashSet<IND_PAIR> equal(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT){
        HashSet<IND_PAIR> res = new HashSet<>();

        for(GroupKey<String> k: RIGHT.keySet()){
            if(LEFT.containsKey(k)){
                add_combinations(res, LEFT.get(k), RIGHT.get(k));
            }
        }

        //print_pairs(res);
        return res;
    }

    public static HashSet<IND_PAIR> not_equal(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT){
        HashSet<IND_PAIR> res = new HashSet<>();

        for(GroupKey<String> k: RIGHT.keySet()){
            if(!LEFT.containsKey(k)){
                add_combinations(res, LEFT.get(k), RIGHT.get(k));
            }
        }
        //print_pairs(res);
        return res;
    }

    public static HashSet<IND_PAIR> less(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT, boolean inclusive){
        /**
         * if inclusive is set as true then this is less than or equal to
         *
         *      * LESS THAN:  (LEFT less than RIGHT)
         *      * iterate over RIGHT:
         *      *     for each lk in LEFT.headMap(K rk, inclusive = false)
         *      *         add all possible combinations of l and r into RES as IND_PAIRs
         * */
        HashSet<IND_PAIR> res = new HashSet<>();

        for(GroupKey<String> rk: RIGHT.keySet()){
            NavigableMap<GroupKey<String>, ArrayList> S = LEFT.headMap(rk, inclusive);
            for(GroupKey<String> lk: S.keySet()){
                add_combinations(res, S.get(lk), RIGHT.get(rk));
            }

        }
        //print_pairs(res);
        return res;
    }

    public static HashSet<IND_PAIR> more(TreeMap<GroupKey<String>, ArrayList> LEFT, TreeMap<GroupKey<String>, ArrayList> RIGHT, boolean inclusive){
        /**
         * if inclusive is set as true then this is less than or equal to
         *
         *
         * */
        HashSet<IND_PAIR> res = new HashSet<>();

        for(GroupKey<String> rk: RIGHT.keySet()){
            NavigableMap<GroupKey<String>, ArrayList> S = LEFT.tailMap(rk, inclusive);
            for(GroupKey<String> lk: S.keySet()){
                add_combinations(res, S.get(lk), RIGHT.get(rk));
            }
        }
        //print_pairs(res);
        return res;
    }

    private static void print_pairs(HashSet<IND_PAIR> P){
        System.out.println("RESULTING INDEX PAIRS: ");
        for(IND_PAIR p: P){
            System.out.println(p.vals[0] + " " + p.vals[1]);
        }
    }

    private static void add_combinations(HashSet<IND_PAIR> set, ArrayList left, ArrayList right){
        for(Object ind1: left){
            for(Object ind2: right){
                set.add(new IND_PAIR(String.valueOf(ind1), String.valueOf(ind2)));
            }
        }
    }

    public static class IND_PAIR{
        String[] vals = new String[2];

        public IND_PAIR (String v1, String v2){
            this.vals[0] = v1;
            this.vals[1] = v2;
        }

        @Override
        public int hashCode() {
            String res = "";

            res += vals[0];
            res += vals[1];

            return (res).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IND_PAIR other = (IND_PAIR) obj;
            return other.vals[0].equals(this.vals[0]) && other.vals[1].equals(this.vals[1]);
        }
    }
}
