package edumips64.core.cache;

/**
 * This class represents the abstraction for a block in the cache
 */
public class CacheBlock {

    /**
     * Block tag
     */
    public final int tag;

    /**
     * True if the data is valid
     */
    public final boolean valid;

    /**
     * True if the block has been modified
     */
    private boolean dirty;

    /**
     * Number of bits for offset
     */
    public final int numBitsOffset;

    /**
     * Number of bits for the index
     */
    public final int numBitsIndex;

    /**
     * Base address of the block
     */
    public final int baseAddress;


    /**
     * Create a valid cache block
     * @param address  Any address in the cache block
     * @param numBitsOffset  Number of bits for the offset
     * @param numBitsIndex  Number of bits used for the block index
     */
    public CacheBlock(int address, int numBitsOffset, int numBitsIndex) {
        this.numBitsOffset = numBitsOffset;
        this.numBitsIndex = numBitsIndex;
        this.dirty = false;
        this.valid = true;
        this.tag = (address >>> (numBitsOffset + numBitsIndex));
        this.baseAddress = this.tag << (numBitsIndex + numBitsOffset);
    }

    /**
     * Create a default cache block with invalid data for when the caches are originally populated
     */
    public CacheBlock() {
        this.valid = false;
        this.dirty = false;
        this.numBitsOffset = 0;
        this.numBitsIndex = 0;
        this.baseAddress = 0;
        this.tag = 0;
    }

    /**
     * Sets the dirty bit
     * @param dirty  New dirty bit
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Gets the dirty bit of the block
     * @return  Dirty bit
     */
    public boolean getDirty() {
        return dirty;
    }

    /**
     * Compares this object with another.
     * If the input is of class Cache block, the tags will be compared
     * If the input is of type Integer, compare with the tag
     * @param o  Object to compare to
     * @return  True if the tags are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CacheBlock) {
            return ((CacheBlock) o).tag == this.tag;
        }

        if (o instanceof Integer) {
            return ((Integer) o) == this.tag;
        }

        return super.equals(o);
    }
}
