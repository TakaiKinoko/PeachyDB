package index;

import btree.BTIteratorIF;

/**
 * Inner class to implement BTree iterator
 */
public class BTTestIteratorImpl<K extends Comparable, V> implements BTIteratorIF<K, V> {
    private K mCurrentKey;
    private K mPreviousKey;
    private boolean mStatus;

    public BTTestIteratorImpl() {
        reset();
    }

    @Override
    public boolean item(K key, V value) {
        mCurrentKey = key;
        if ((mPreviousKey != null) && (mPreviousKey.compareTo(key) > 0)) {
            mStatus = false;
            return false;
        }
        mPreviousKey = key;
        return true;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public K getCurrentKey() {
        return mCurrentKey;
    }

    public final void reset() {
        mPreviousKey = null;
        mStatus = true;
    }
}