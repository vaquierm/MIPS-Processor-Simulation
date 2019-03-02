package edumips64.core.cache;

import edumips64.core.cache.cacheExceptions.*;
import edumips64.core.cache.cacheLayer.DirectMappedCacheLayer;
import edumips64.core.cache.cacheLayer.ICache.ICacheLayer;
import edumips64.core.cache.cacheLayer.ICache.ICacheLayer.MemoryAccessType;
import edumips64.core.cache.cacheLayer.CacheLayer;

import edumips64.core.cache.cacheLayer.SetAssociativeCacheLayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

import static java.lang.Math.toIntExact;


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
     *
     * @param configFile config file filepath
     */
    private CacheManager(String configFile) {
        setup(configFile);
    }

    /**
     * Note, if it is the first time that get instance is called, you need to set it up bu calling .setup(filepath)
     *
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
     * Mini class to hold the configurations of each layer
     */
    class CacheLayerConfig {
        int accessTime;
        int ways;
        int size;
        int blockSize;
        ICacheLayer.WriteStrategy strategy;
    }

    /**
     * Sets up the cache layers based on a config file
     *
     * @param configFile config file filepath
     */
    public void setup(String configFile) {
        int mainMemAT;
        CacheLayerConfig[] layerConfigs;

        JSONParser parser = new JSONParser();
        try {
            JSONObject conf = (JSONObject) parser.parse(new FileReader(configFile));

            mainMemAT = toIntExact((long) conf.get("mmat"));

            JSONArray caches = (JSONArray) conf.get("caches");
            layerConfigs = new CacheLayerConfig[caches.size()];
            for (int i = 0; i < layerConfigs.length; i++) {
                JSONObject cache = (JSONObject) caches.get(i);
                CacheLayerConfig layerConfig = new CacheLayerConfig();
                layerConfig.accessTime = toIntExact((long) cache.get("accessTime"));
                layerConfig.ways = toIntExact((long) cache.get("ways"));
                layerConfig.size = byteValueFromString((String) cache.get("size"));
                layerConfig.blockSize = byteValueFromString((String) cache.get("blockSize"));
                layerConfig.strategy = ICacheLayer.WriteStrategy.valueOf((String) cache.get("writeStrategy"));

                validateCacheLayerConfig(layerConfig);

                layerConfigs[i] = layerConfig;

            }
            // Set the configuration values
            INSTANCE.cacheLayers = generateCacheLayers(layerConfigs);
            INSTANCE.mainMemoryAccessTime = mainMemAT;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (InvalidAccessTimeException e) {
            e.printStackTrace();
        } catch (InvalidAssociativityException e) {
            e.printStackTrace();
        } catch (InvalidCacheSizeStringFormat e) {
            e.printStackTrace();
        } catch (InvalidBlocksPerSetException e) {
            e.printStackTrace();
        } catch (InvalidPowerOfTwoException e) {
            e.printStackTrace();
        } catch (InvalidCacheSizeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates cache layers from given configurations
     *
     * @param configs
     * @return
     */
    private CacheLayer[] generateCacheLayers(CacheLayerConfig[] configs) throws InvalidBlocksPerSetException, InvalidCacheSizeException, InvalidPowerOfTwoException {
        CacheLayer[] caches = new CacheLayer[configs.length];
        // Only have Direct mapped cache available right now
        for (int i = 0; i < caches.length; i++) {
            CacheLayerConfig conf = configs[i];
            switch (conf.ways) {
                case 1:
                    caches[i] = new DirectMappedCacheLayer(conf.size, conf.blockSize, conf.accessTime, conf.strategy);
                    break;
                case 0: // TODO implement full associativity
                    break;
                default:
                    caches[i] = new SetAssociativeCacheLayer(conf.size, conf.blockSize, conf.ways, conf.accessTime, conf.strategy);
            }
        }
        return caches;
    }

    /**
     * Validates a given cache layer configuration
     *
     * @param conf the configuration of a given cache layer
     * @return if cache layer is valid or not
     * @throws InvalidPowerOfTwoException
     * @throws InvalidCacheSizeException
     */
    private void validateCacheLayerConfig(CacheLayerConfig conf) throws InvalidAccessTimeException, InvalidAssociativityException {
        if (conf.accessTime < 1) {
            throw new InvalidAccessTimeException();
        }
        if (conf.ways < 0) {
            throw new InvalidAssociativityException();
        }
    }

    /**
     * Converts strings of format XXB,XXKB,XXMB to an int byte count, throws if string is improperly formatted
     *
     * @param size
     */
    private int byteValueFromString(String size) throws InvalidCacheSizeStringFormat {
        if (size.contains("B")) {
            int index = size.indexOf('B');
            switch (size.charAt(index - 1)) {
                case 'K':
                    return Integer.parseInt(size.substring(0, index - 1)) * 1024;
                case 'M':
                    return Integer.parseInt(size.substring(0, index - 1)) * 1024 * 1024;
                case 'G':
                    return Integer.parseInt(size.substring(0, index - 1)) * 1024 * 1024 * 1024;
                default:
                    return Integer.parseInt(size.substring(0, index - 1));
            }
        } else {
            throw new InvalidCacheSizeStringFormat();
        }
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
     *
     * @param address    Address of the memory access
     * @param accessType The type of access to memory
     * @return The latency of the access
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
     *
     * @param layerNumber Index of the cache to write to
     * @param address     Address to write to
     * @return The latency of the access
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
                    delay += layer.getAccessTime();
                    CacheBlock cb = layer.put(address);
                    cb.setDirty(false);
                }

                delay += writeToLayer(layerNumber + 1, address);

                break;
        }

        return delay;
    }

    /**
     * Calculate the latency to read from a particular cache
     *
     * @param layerNumber Index of the cache to read from
     * @param address     Address to read from
     * @return The latency of the access
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
                    delay += layer.getAccessTime();

                }
                else {
                    // Read miss
                    delay += readFromLayer(layerNumber + 1, address);
                    CacheBlock cb = layer.put(address);
                    if(cb.valid && cb.getDirty()) {
                        delay += writeToLayer(layerNumber + 1, cb.baseAddress);
                    }
                }

                break;
            case WRITE_THROUGH:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Read hit
                    delay += layer.getAccessTime();
                }
                else {
                    // Read miss
                    delay += readFromLayer(layerNumber + 1, address);
                }
                break;
        }

        return delay;

    }


}
