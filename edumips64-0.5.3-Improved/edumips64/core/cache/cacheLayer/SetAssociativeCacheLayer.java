package edumips64.core.cache.cacheLayer;

import edumips64.core.cache.CacheBlock;
import edumips64.core.cache.cacheExceptions.*;
import edumips64.core.cache.util.Log2;

/**
 * Set associative caches have multiple sets which contain multiple blocks
 */
public class SetAssociativeCacheLayer extends AssociativeCacheLayer {

    /**
     * 2D array holding sets, each entry corresponds to all blocks in the set
     */
    private PriorityCacheBlock[][] setsArray;

    /**
     * Number of sets in the cache
     */
    private final int numberOfSets;

    /**
     * Number of blocks per set
     */
    private final int numberOfBlocksPerSet;

    /**
     * Number of bits for the set index
     */
    private final int setIndexBits;

    /**
     * Mask to isolate bits from set index
     * In the form of 00000000111111110000000
     */
    private final int setIndexMask;

    /**
     * Creates an instance of Associative cache
     * @param cacheSize  Total cache size
     * @param blockSize  Size of each block
     * @param blocksPerSet  Number of blocks per set
     * @param accessTime  Access time of the cache
     * @param writeStrategy  The write strategy
     * @param evictionPolicy  The eviction policy
     * @throws InvalidCacheSizeException
     */
    public SetAssociativeCacheLayer(int cacheSize, int blockSize, int blocksPerSet, int accessTime, WriteStrategy writeStrategy, EvictionPolicy evictionPolicy) throws InvalidCacheSizeException, InvalidPowerOfTwoException, InvalidBlocksPerSetException {
        super(cacheSize, blockSize, accessTime, writeStrategy, evictionPolicy);

        // Check that the number of blocks per set is a power of two
        if (Log2.compute(blocksPerSet) <= 1 || blocksPerSet > numberOfBlocks) {
            throw new InvalidBlocksPerSetException();
        }

        this.numberOfBlocksPerSet = blocksPerSet;

        this.numberOfSets = this.numberOfBlocks / numberOfBlocksPerSet;

        this.setIndexBits = Log2.compute(this.numberOfSets);

        // Create the block index mask
        int mask = 0;
        for (int i = 0; i < this.setIndexBits; i++) {
            mask = (mask + 1) << 1;
        }
        mask = mask << this.offsetBits;
        this.setIndexMask = mask;

        this.setsArray = new PriorityCacheBlock[numberOfSets][numberOfBlocksPerSet];

        // Populate the cache with invalid blocks
        for (int i = 0; i < this.numberOfSets; i ++) {
            for (int j = 0; j < this. numberOfBlocksPerSet; j++) {
                this.setsArray[i][j] = new PriorityCacheBlock();
            }
        }

    }

    /**
     * Finds a block in a specific set, null is returned if it is not found
     * @param setIndex  Index of the set in which to look
     * @param tag  Tag of the target block
     * @return  The block in the set with matching tag. Null if it is not present
     */
    private CacheBlock findBlockInSet(int setIndex, int tag) {
        // Get the set in which we are looking for

        CacheBlock[] set = this.setsArray[setIndex];
        for (int i = 0; i < this.numberOfBlocksPerSet; i++) {
            if (set[i].tag == tag) {
                return set[i];
            }
        }

        // If it was never found, return null
        return null;
    }



    @Override
    public boolean contains(int address) {

        boolean contained = containsBlock(address);

        if (contained) {
            // Contains is called when an element is being accessed from outside we want to update priorities
            int setIndex = ((address & this.setIndexMask) >>> this.offsetBits);

            updateEvictionPriorities(this.setsArray[setIndex], setIndex);
        }

        return contained;
    }

    private boolean containsBlock(int address) {
        int setIndex = ((address & this.setIndexMask) >>> this.offsetBits);

        return findBlockInSet(setIndex, computeTag(address)) != null;
    }

    @Override
    public CacheBlock put(int address) throws CacheAlreadyContainsBlockException {

        // Check if this block already exists
        if (this.containsBlock(address)) {
            throw new CacheAlreadyContainsBlockException();
        }

        // Get the set to put in
        int setIndex = ((address & this.setIndexMask) >>> this.offsetBits);

        PriorityCacheBlock[] set = this.setsArray[setIndex];

        int indexToEvict = findIndexToEvict(set);

        CacheBlock evictedBlock = set[indexToEvict];

        set[indexToEvict] = new PriorityCacheBlock(address, this.offsetBits, this.setIndexBits);

        // If the eviction policy is LRU we just placed a new block and want to update the priorities
        if (this.evictionPolicy == EvictionPolicy.LRU) {
            updateEvictionPriorities(set, indexToEvict);
        }

        return evictedBlock;

    }

    @Override
    public void dirtyBlock(int address) throws BlockNotFoundException {
        if (!containsBlock(address)) {
            throw new BlockNotFoundException();
        }

        int setIndex = ((address & this.setIndexMask) >>> this.offsetBits);

        findBlockInSet(setIndex, computeTag(address)).setDirty(true);
    }

    @Override
    public int computeTag(int address) {
        return address >>> (this.offsetBits + this.setIndexBits);
    }

    @Override
    public void reset() {
        for (int i = 0; i < this.numberOfSets; i ++) {
            for (int j = 0; j < this. numberOfBlocksPerSet; j++) {
                this.setsArray[i][j] = new PriorityCacheBlock();
            }
        }
    }

    public int getNumberOfBlocksPerSet() {
        return this.numberOfBlocksPerSet;
    }
}
