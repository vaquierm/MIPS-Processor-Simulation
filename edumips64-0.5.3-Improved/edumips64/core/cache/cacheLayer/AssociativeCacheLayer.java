package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.cacheExceptions.InvalidCacheSizeException;
import edumips64.core.cache.cacheExceptions.InvalidPowerOfTwoException;

public abstract class AssociativeCacheLayer extends CacheLayer {

    final EvictionPolicy evictionPolicy;

    public AssociativeCacheLayer(int cacheSize, int blockSize, int accessTime, WriteStrategy writeStrategy, EvictionPolicy evictionPolicy) throws InvalidCacheSizeException, InvalidPowerOfTwoException {
        super(cacheSize, blockSize, accessTime, writeStrategy);

        this.evictionPolicy = evictionPolicy;
    }

    /**
     * Get the index of the lowest eviction priority block of a set
     * @param set  The set in question
     * @return  The index of the block with lowest eviction priority in the set
     */
    int findIndexToEvict(PriorityCacheBlock[] set) {

        // First check if any of the blocks are invalid, if so evict them first
        for (int i = 1; i < set.length; i++) {
            if (!set[i].valid)
                return i;
        }

        int highestPriorityIndex = 0;
        long highestPriority = set[0].getEvictionPriority();

        for (int i = 1; i < set.length; i++) {
            if (highestPriority > set[i].getEvictionPriority()) {
                highestPriority = set[i].getEvictionPriority();
                highestPriorityIndex = i;
            }
        }

        return highestPriorityIndex;
    }

    /**
     * Update the eviction priorities of each block in the set with respect to the eviction strategy
     * @param set  Set in which to update priorities
     * @param accessIndex  Index of block being accessed
     */
    void updateEvictionPriorities(PriorityCacheBlock[] set, int accessIndex) {

        switch (this.evictionPolicy) {
            case RANDOM:
                for (PriorityCacheBlock block : set) {
                    block.setEvictionPriority((long)(Math.random() * 0xFFFFFFFF));
                }
                break;
            case FIFO:
                // FIFO does not need to update the eviction policy. it uses the System time in millis from the construction of the cache block
                break;
            case LRU:
                for (int i = 0; i < set.length; i++) {
                    if (i == accessIndex) {
                        set[i].resetEvictionPriority();
                    }
                    else {
                        set[i].decrementEvictionPriority();
                    }
                }
                break;
        }
    }

    /**
     * All possible eviction policies in a set
     */
    public enum EvictionPolicy {
        /**
         * Random eviction will evict any of the blocks in the set
         */
        RANDOM,
        /**
         * FIFO will evict on a first in first out basis
         */
        FIFO,
        /**
         * LRU will evict the least recently used block first
         */
        LRU
    }
}
