package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.cacheLayer.ICache.ICacheLayer;

public abstract class CacheLayer implements ICacheLayer {

    /**
     * The access time of this cache
     */
    int accessTime;

    /**
     * The size of the layer
     * Note: Must be a power of two
     */
    int cacheSize;

    /**
     * The size of the blocks
     * Note: the cache size mod blockSize must equal zero
     */
    int blockSize;

    /**
     * The number of blocks in the cache
     */
    int numberOfBlocks;

    /**
     * The write strategy that the cache uses
     */
    WriteStrategy writeStrategy;

    /**
     * @return  The access time of this cache
     */
    public int getAccessTime() {
        return accessTime;
    }

    /**
     * @return  The block size of the cache
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * @return  The cache size
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * @return  The number of blocks in the cache
     */
    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    /**
     * @return  The write strategy of this layer
     */
    public WriteStrategy getWriteStrategy() {
        return writeStrategy;
    }
}
