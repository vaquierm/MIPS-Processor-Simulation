package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.CacheBlock;
import edumips64.core.cache.cacheExceptions.BlockNotFoundException;
import edumips64.core.cache.cacheExceptions.CacheAlreadyContainsBlockException;
import edumips64.core.cache.cacheExceptions.InvalidCacheSizeException;
import edumips64.core.cache.cacheExceptions.InvalidPowerOfTwoException;
import edumips64.core.cache.util.Log2;

/**
 * Cache layer which obeys the direct mapping rule
 */
public class DirectMappedCacheLayer extends CacheLayer {

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
    public DirectMappedCacheLayer(int cacheSize, int blockSize, int accessTime, WriteStrategy writeStrategy) throws InvalidCacheSizeException, InvalidPowerOfTwoException {
        super(cacheSize, blockSize, accessTime, writeStrategy);

        this.blocksArray = new CacheBlock[this.numberOfBlocks];

        this.blockIndexBits = Log2.compute(this.numberOfBlocks);

        // Create the block index mask
        int mask = 0;
        for (int i = 0; i < this.blockIndexBits; i++) {
            mask = (mask++) << 1;
        }
        mask = mask << this.offsetBits;
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
    public void dirtyBlock(int address) throws BlockNotFoundException {

        if (!contains(address)) {
            throw new BlockNotFoundException();
        }

        int blockIndex = ((address & this.blockIndexMask) >>> this.offsetBits);

        this.blocksArray[blockIndex].setDirty(true);
    }

    @Override
    public CacheBlock put(int address) throws CacheAlreadyContainsBlockException {

        // Check if this block already exists
        if (this.contains(address)) {
            throw new CacheAlreadyContainsBlockException();
        }

        int blockIndex = ((address & this.blockIndexMask) >>> this.offsetBits);

        CacheBlock oldBlock = this.blocksArray[blockIndex];

        // Create the new block and insert it
        this.blocksArray[blockIndex] = new CacheBlock(address, this.offsetBits, this.blockIndexBits);

        // Return the old block
        return  oldBlock;
    }

    @Override
    public int computeTag(int address) {
        return address >>> (this.offsetBits + this.blockIndexBits);
    }

}
