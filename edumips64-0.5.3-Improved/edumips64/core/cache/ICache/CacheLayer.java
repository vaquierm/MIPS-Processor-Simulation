package edumips64.core.cache.ICache;

import edumips64.core.cache.CacheBlock;
import edumips64.core.cache.cacheExceptions.CacheAlreadyContainsBlockException;

public interface CacheLayer {

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
     * Compute the tag to identify the block in which the address would be mapped to in this cache
     * @param address  Target address
     * @return  The tag
     */
    int computeTag(int address);
}
