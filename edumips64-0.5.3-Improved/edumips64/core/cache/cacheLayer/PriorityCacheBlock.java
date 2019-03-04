package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.CacheBlock;

/**
 * CacheBlock also holding an integer representing the priority of non-eviction
 */
public class PriorityCacheBlock extends CacheBlock {

    /**
     * The larger this number, the more likely it is to be evicted
     */
    private long evictionPriority;

    /**
     * Default Cache block constructor with high eviction priority
     */
    public PriorityCacheBlock() {
        super();
        // The eviction priority of the default invalid block is very high
        evictionPriority = System.currentTimeMillis();
    }

    /**
     * Cache block with low eviction priority
     * @param address  Address in the block
     * @param numBitsOffset  Number of bits that are used for offset
     * @param numBitsIndex  Number of bits to index the block
     */
    public PriorityCacheBlock(int address, int numBitsOffset, int numBitsIndex) {
        super(address, numBitsOffset, numBitsIndex);

        evictionPriority = 0;
    }

    public void setEvictionPriority(long evictionPriority) {
        this.evictionPriority = evictionPriority;
    }

    public long getEvictionPriority() {
        return evictionPriority;
    }

    public void resetEvictionPriority() {
        this.evictionPriority = 0;
    }

    public void decrementEvictionPriority() {
        this.evictionPriority--;
    }
}
