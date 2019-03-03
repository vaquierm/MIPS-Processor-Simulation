package edumips64.core.cache.cacheLayer.ICache;

import edumips64.core.cache.CacheBlock;
import edumips64.core.cache.cacheExceptions.BlockNotFoundException;
import edumips64.core.cache.cacheExceptions.CacheAlreadyContainsBlockException;

public interface ICacheLayer {

    /**
     * Queries if the block containing the input address is in the cache
     * @param address  The address in question
     * @return  True if the cache block is in the cache, False otherwise
     */
    boolean contains(int address);

    /**
     * Put the cache block in the cache.
     * If another cache block is being evicted, it returns the block being evicted
     * @param address  Any address in the new cache block
     * @return  Cache block being evicted
     */
    CacheBlock put(int address) throws CacheAlreadyContainsBlockException;

    /**
     * Dirtys the block at the address
     * @param address  Address to dirty
     * @throws BlockNotFoundException
     */
    void dirtyBlock(int address) throws BlockNotFoundException;

    /**
     * Compute the tag to identify the block in which the address would be mapped to in this cache
     * @param address  Target address
     * @return  The tag
     */
    int computeTag(int address);

    /**
     * The Cache write strategies
     */
    enum WriteStrategy {
        /**
         * Write back means that information is only written to the block in cache
         * The modified memory block is only written to main memory when it is evicted
         * This also comes with the Write allocate strategy which means that a Write miss will first act like a read miss then the data will be written to cache
         */
        WRITE_BACK,
        /**
         * Write through writes to the cache as well as lower level memory stages every time
         * This strategy also uses no-write allocate. When a write misses, it does not bring the cache block cache but instead only writes to the lower level
         */
        WRITE_THROUGH
    }

    /**
     * Different types of memory access
     */
    enum MemoryAccessType {
        /**
         * Read access
         */
        READ,
        /**
         * Write access
         */
        WRITE,
        /**
         * This is for when the we do not want to calculate the delay
         */
        NONE
    }
}
