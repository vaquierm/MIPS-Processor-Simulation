package edumips64.core.cache;

import edumips64.core.cache.ICache.CacheLayer;

import java.util.LinkedList;
import java.util.List;

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
    List<CacheLayer> cacheLayers;

    /**
     * Create an empty cache with no access time to main memory
     */
    private CacheManager() {
        this.mainMemoryAccessTime = 0;
        this.cacheLayers = new LinkedList<>();
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
            return new CacheManager();
        }
        return INSTANCE;
    }

    public List<CacheLayer> getCacheLayers() {
        return this.cacheLayers;
    }

    /**
     * Sets up the cache layers based on a config file
     * @param configFile  config file filepath
     */
    private void setup(String configFile) {

        // TODO : Parse the file and create the layers
        // Do this in a encoder decoder file
        // Throw exception if file not found
    }

    /**
     * Resets the cache to default with no caches and 0CC of access time to main memory
     */
    public void reset() {
        this.mainMemoryAccessTime = 0;
        this.cacheLayers = new LinkedList<>();
    }

    public int getMainMemoryAccessTime() {
        return this.mainMemoryAccessTime;
    }

    /**
     * Calculate the latency of the memory access
     * @param address  address of the memory access
     * @return  The latency of the access
     */
    public int calculateLatency(int address) {
        // TODO Calculate the latency
        return 0;
    }
}
