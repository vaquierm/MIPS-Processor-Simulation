package edumips64.core.cache;

import edumips64.core.cache.ICache.CacheLayer;
import edumips64.core.cache.cacheExceptions.CacheAlreadyContainsBlockException;
import edumips64.core.cache.cacheExceptions.InvalidCacheSizeException;
import edumips64.core.cache.cacheExceptions.InvalidPowerOfTwoException;
import edumips64.core.cache.util.Log2;

/**
 * Cache layer which obeys the direct mapping rule
 */
public class DirectMappedCacheLayer implements CacheLayer {

    /**
     * The size of the layer
     * Note: Must be a power of two
     */
    public final int cacheSize;

    /**
     * The size of the blocks
     * Note: the cache size mod blockSize must equal zero
     */
    public final int blockSize;

    /**
     * The number of blocks in the cache
     */
    public final int numberOfBlocks;

    /**
     * Number of bits used for offset at this layer
     */
    private final int offsetBits;

    /**
     * Number of bits used for block index
     */
    private final int blockIndexBits;

    /**
     * Mask to isolate bits for the block index
     * In the form of 000001111111000000
     */
    private final int blockIndexMask;

    /**
     * Array storing the cache elements representing which block is stored in each slot
     */
    private final CacheBlock[] blocksArray;

    /**
     * Create a cache layer populated with invalid blocks
     * Note: cacheSize mod blockSize must equal zero
     * @param cacheSize  Size of cache
     * @param blockSize  Size of block.
     * @throws InvalidCacheSizeException
     */
    public DirectMappedCacheLayer(int cacheSize, int blockSize) throws InvalidCacheSizeException, InvalidPowerOfTwoException {
        this.cacheSize = cacheSize;
        this.blockSize = blockSize;

        if (this.cacheSize % this.blockSize == 0) {
            throw new InvalidCacheSizeException();
        }

        this.numberOfBlocks = this.cacheSize / this.blockSize;

        this.blocksArray = new CacheBlock[this.numberOfBlocks];

        this.offsetBits = Log2.compute(this.blockSize);

        if (this.offsetBits < 0 || Log2.compute(cacheSize) < 0) {
            throw new InvalidPowerOfTwoException();
        }

        this.blockIndexBits = Log2.compute(this.numberOfBlocks);

        // Create the block index mask
        int mask = 0;
        for (int i = 0; i < this.blockIndexBits; i++) {
            mask = (mask++) << 1;
        }
        this.blockIndexMask = mask;

        // Populate the cache with invalid blocks
        for (int i = 0; i < this.numberOfBlocks; i++) {
            this.blocksArray[i] = new CacheBlock();
        }
    }

    @Override
    public boolean contains(int address) {

        int blockIndex = ((address & this.blockIndexMask) >>> this.offsetBits);

        return this.blocksArray[blockIndex].tag == computeTag(address);
    }

    @Override
    public CacheBlock put(int address) throws CacheAlreadyContainsBlockException {

        // Check if this block already exists
        if (this.contains(address)) {
            throw new CacheAlreadyContainsBlockException();
        }

        // Get the tag of the block wanted to be added
        int tag = computeTag(address);

        int blockIndex = ((address & this.blockIndexMask) >>> this.offsetBits);

        CacheBlock oldBlock = this.blocksArray[blockIndex];

        // Create the new block and insert it
        this.blocksArray[blockIndex] = new CacheBlock(address, this.offsetBits);

        // Return the old block
        return  oldBlock;
    }

    @Override
    public int computeTag(int address) {
        return address >>> (this.offsetBits + this.blockIndexBits);
    }

}
