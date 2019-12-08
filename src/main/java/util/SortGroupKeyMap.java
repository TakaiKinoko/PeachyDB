package util;

import java.util.*;

public class SortGroupKeyMap {

    /**
     * Didn't use this class. Used tree map instead!
     * */
    public static Map<GroupKey, ArrayList> sort(Map<GroupKey, ArrayList> unsortMap) {
        // 1. Convert Map to List of Map
        List<Map.Entry<GroupKey, ArrayList>> list =
                new LinkedList<Map.Entry<GroupKey, ArrayList>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<GroupKey, ArrayList>>() {
            public int compare(Map.Entry<GroupKey, ArrayList> o1,
                               Map.Entry<GroupKey, ArrayList> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<GroupKey, ArrayList> sortedMap = new LinkedHashMap<GroupKey, ArrayList>();
        for (Map.Entry<GroupKey, ArrayList> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        return sortedMap;
    }


}
