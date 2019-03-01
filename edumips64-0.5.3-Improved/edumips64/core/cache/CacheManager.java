package edumips64.core.cache;

import edumips64.core.cache.cacheExceptions.BlockNotFoundException;
import edumips64.core.cache.cacheExceptions.CacheAlreadyContainsBlockException;
import edumips64.core.cache.cacheLayer.ICache.ICacheLayer.MemoryAccessType;
import edumips64.core.cache.cacheLayer.CacheLayer;

/**
 * Manages the memory hierarchy. Main interface the that the Memory goes through.
 */
public class CacheManager {

    /**
     * Instance
     */
    public static CacheManager INSTANCE;

    /**
     * Access time of the main memory
     */
    private int mainMemoryAccessTime;

    /**
     * Layers of caches
     */
    private CacheLayer[] cacheLayers;

    /**
     * Create an empty cache with no access time to main memory
     */
    private CacheManager() {
        reset();
    }

    /**
     * Create the cache layers based on a config file
     * @param configFile  config file filepath
     */
    private CacheManager(String configFile) {
        setup(configFile);
    }

    /**
     * Note, if it is the first time that get instance is called, you need to set it up bu calling .setup(filepath)
     * @return The CacheManager Instance
     */
    public static CacheManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CacheManager();
        }
        return INSTANCE;
    }

    public CacheLayer[] getCacheLayers() {
        return this.cacheLayers;
    }

    /**
     * Sets up the cache layers based on a config file
     * @param configFile  config file filepath
     */
    public void setup(String configFile) {

        // TODO : Parse the file and create the layers
        // Do this in a encoder decoder file
        // Throw exception if file not found
    }

    /**
     * Resets the cache to default with no caches and 0CC of access time to main memory
     */
    public void reset() {
        this.mainMemoryAccessTime = 0;
        this.cacheLayers = new CacheLayer[0];
    }

    public int getMainMemoryAccessTime() {
        return this.mainMemoryAccessTime;
    }

    /**
     * Calculate the latency of the memory access
     * @param address  Address of the memory access
     * @param accessType  The type of access to memory
     * @return  The latency of the access
     */
    public int calculateLatency(int address, MemoryAccessType accessType) throws CacheAlreadyContainsBlockException, BlockNotFoundException {

        int delay = 0;

        try {
            switch (accessType) {
                case READ:
                    delay = readFromLayer(0, address);
                    break;
                case WRITE:
                    delay = writeToLayer(0, address);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Delay Calculation error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return delay;
    }

    /**
     * Calculate the memory latency to write to a particular cache
     * @param layerNumber  Index of the cache to write to
     * @param address  Address to write to
     * @return  The latency of the access
     */
    private int writeToLayer(int layerNumber, int address) throws CacheAlreadyContainsBlockException, BlockNotFoundException {

        if (layerNumber < 0 || layerNumber >= cacheLayers.length)
            return mainMemoryAccessTime;

        CacheLayer layer = cacheLayers[layerNumber];

        int delay = 0;

        switch (layer.getWriteStrategy()) {
            case WRITE_BACK:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Write hit, just write to this layer
                    delay += layer.getAccessTime();
                }
                else {
                    // Write miss
                    // First act like a read miss, go get the block from lower layer
                    delay += readFromLayer(layerNumber + 1, address);

                    // Place the block in the cache
                    CacheBlock evictedBlock = layer.put(address);

                    // If the evicted block is dirty, write it back to lower layer
                    if (evictedBlock.getDirty() && evictedBlock.valid) {
                        delay += writeToLayer(layerNumber + 1, evictedBlock.baseAddress);
                    }

                    // Write the data to the cache block that was just fetched to this layer
                    delay += layer.getAccessTime();
                }

                // In both cases, make the block dirty since we just wrote to it
                layer.dirtyBlock(address);
                break;
            case WRITE_THROUGH:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Write hit
                    // TODO
                }
                else {
                    // Write miss
                    // TODO
                }
                break;
        }

        return delay;
    }

    /**
     * Calculate the latency to read from a particular cache
     * @param layerNumber  Index of the cache to read from
     * @param address  Address to read from
     * @return  The latency of the access
     */
    private int readFromLayer(int layerNumber, int address) throws CacheAlreadyContainsBlockException, BlockNotFoundException {

        if (layerNumber < 0 || layerNumber >= cacheLayers.length)
            return mainMemoryAccessTime;

        CacheLayer layer = cacheLayers[layerNumber];

        int delay = 0;

        switch (layer.getWriteStrategy()) {
            case WRITE_BACK:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Read hit
                }
                else {
                    // Read miss
                }
                break;
            case WRITE_THROUGH:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Read hit
                    //TODO
                }
                else {
                    // Read miss
                    //TODO
                }
                break;
        }

        return delay;
    }


}
