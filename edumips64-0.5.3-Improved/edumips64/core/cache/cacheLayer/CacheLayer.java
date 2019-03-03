package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.cacheExceptions.InvalidCacheSizeException;
import edumips64.core.cache.cacheExceptions.InvalidPowerOfTwoException;
import edumips64.core.cache.cacheLayer.ICache.ICacheLayer;
import edumips64.core.cache.util.Log2;

public abstract class CacheLayer implements ICacheLayer {

    //TODO: All these fields can become public final since they can be initialized with the constructor here
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
     * Number of bits used for offset at this layer
     */
    int offsetBits;

    /**
     * The write strategy that the cache uses
     */
    WriteStrategy writeStrategy;

    public CacheLayer(int cacheSize, int blockSize, int accessTime, WriteStrategy writeStrategy) throws InvalidCacheSizeException, InvalidPowerOfTwoException {
        this.cacheSize = cacheSize;
        this.blockSize = blockSize;

        if (this.cacheSize % this.blockSize != 0) {
            throw new InvalidCacheSizeException();
        }

        this.numberOfBlocks = this.cacheSize / this.blockSize;

        this.offsetBits = Log2.compute(this.blockSize);

        if (this.offsetBits < 0 || Log2.compute(cacheSize) < 0) {
            throw new InvalidPowerOfTwoException();
        }

        this.accessTime = accessTime;
        this.writeStrategy = writeStrategy;
    }

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
