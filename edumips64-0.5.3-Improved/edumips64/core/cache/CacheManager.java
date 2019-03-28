package edumips64.core.cache;

import edumips64.core.cache.cacheExceptions.*;
import edumips64.core.cache.cacheLayer.AssociativeCacheLayer;
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
import static javax.swing.JOptionPane.*;


/**
 * Manages the memory hierarchy. Main interface the that the Memory goes through.
 */
public class CacheManager {

    /**
     * Instance
     */
    private static CacheManager INSTANCE;

    /**
     * Access time of the main memory
     */
    private int mainMemoryAccessTime;

    /**
     * Layers of caches
     */
    private CacheLayer[] cacheLayers;

    /**
     * If not enabled all memory accesses have a delay of 0
     */
    private boolean enabled;

    /**
     * Variables for calculating AMAT
     */
    private int readTotal;
    private int reads;
    private int writes;
    private int writeTotal;

    /**
     * Create an empty cache with no access time to main memory
     */
    private CacheManager() {
        resetDefault();
    }

    /**
     * Create the cache layers based on a config file
     *
     * @param configFile config file filepath
     */
    private CacheManager(String filename, String configFile) {
        setup(filename, configFile);
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
     * Whether or not cache has been configured yet.
     */
    private boolean configured = false;

    /**
     * Getter for whether or not instance has been congigured
     * @return configured
     */
    public boolean getConfigured(){
        return this.configured;
    }

    /**
     * Name for the configuration, from filename
     */
    private String configName;

    /**
     * getter for configName
     * @return configName
     */
    public String getConfigName() {return configName;}


    /**
     * Mini class to hold the configurations of each layer
     */
    class CacheLayerConfig {
        int accessTime;
        String mappingScheme;
        int size;
        int blockSize;
        ICacheLayer.WriteStrategy strategy;
    }

    /**
     * Sets up the cache layers based on a config file
     *
     * @param configFile config file filepath
     */
    public void setup(String fileName, String configFile) {
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
                layerConfig.mappingScheme = (String) cache.get("mappingScheme");
                layerConfig.size = byteValueFromString((String) cache.get("size"));
                layerConfig.blockSize = byteValueFromString((String) cache.get("blockSize"));
                layerConfig.strategy = ICacheLayer.WriteStrategy.valueOf((String) cache.get("writeStrategy"));

                validateCacheLayerConfig(layerConfig);

                layerConfigs[i] = layerConfig;

            }
            // Set the configuration values
            this.cacheLayers = generateCacheLayers(layerConfigs);
            this.mainMemoryAccessTime = mainMemAT;
            this.configured = true;
            this.configName = fileName.substring(0, fileName.indexOf('.'));

        } catch (FileNotFoundException e) {
            showMessageDialog(null, e.getMessage(), "FileNotFoundException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (IOException e) {
            showMessageDialog(null, e.getMessage(), "IOException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (org.json.simple.parser.ParseException e) {
            showMessageDialog(null, e.getMessage(), "ParseException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidAccessTimeException e) {
            showMessageDialog(null, e.getMessage(), "InvalidAccessTimeException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidCacheSizeStringFormatException e) {
            showMessageDialog(null, e.getMessage(), "InvalidCacheSizeStringFormatException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidBlocksPerSetException e) {
            showMessageDialog(null, e.getMessage(), "InvalidBlocksPerSetException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidPowerOfTwoException e) {
            showMessageDialog(null, e.getMessage(), "InvalidPowerOfTwoException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidCacheSizeException e) {
            showMessageDialog(null, e.getMessage(), "InvalidCacheSizeException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        } catch (InvalidMappingSchemeException e) {
            showMessageDialog(null, e.getMessage(), "InvalidMappingSchemeException", INFORMATION_MESSAGE);
            CacheManager.getInstance().resetDefault();
        }

    }

    /**
     * Generates cache layers from given configurations
     *
     * @param configs
     * @return
     */
    private CacheLayer[] generateCacheLayers(CacheLayerConfig[] configs) throws InvalidBlocksPerSetException, InvalidCacheSizeException, InvalidPowerOfTwoException, InvalidMappingSchemeException {
        CacheLayer[] caches = new CacheLayer[configs.length];
        // Only have Direct mapped cache available right now
        for (int i = 0; i < caches.length; i++) {
            CacheLayerConfig conf = configs[i];

            if (conf.mappingScheme.equals("DIRECT")) {
                caches[i] = new DirectMappedCacheLayer(conf.size, conf.blockSize, conf.accessTime, conf.strategy);
            }
            else if (conf.mappingScheme.contains("_WAY_SET_ASSOCIATIVE_")) {
                try {
                    String wayString = conf.mappingScheme.split("_")[0];
                    String evictionScheme = conf.mappingScheme.split("_")[conf.mappingScheme.split("_").length - 1];

                    int ways = Integer.parseInt(wayString);
                    caches[i] = new SetAssociativeCacheLayer(conf.size, conf.blockSize, ways, conf.accessTime, conf.strategy, AssociativeCacheLayer.EvictionPolicy.valueOf(evictionScheme));
                }
                catch (InvalidBlocksPerSetException e) {
                    throw e;
                }
                catch (InvalidCacheSizeException e) {
                    throw e;
                }
                catch (InvalidPowerOfTwoException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new InvalidMappingSchemeException();
                }
            }
            else if (conf.mappingScheme.contains("FULLY_ASSOCIATIVE")) {
                //TODO : The fully associative cache needs a class
                try {
                    String evictionScheme = conf.mappingScheme.split("_")[conf.mappingScheme.split("_").length - 1];
                    caches[i] = new SetAssociativeCacheLayer(conf.size, conf.blockSize, conf.size/conf.blockSize, conf.accessTime, conf.strategy, AssociativeCacheLayer.EvictionPolicy.valueOf(evictionScheme));
                }
                catch (InvalidBlocksPerSetException e) {
                    throw e;
                }
                catch (InvalidCacheSizeException e) {
                    throw e;
                }
                catch (InvalidPowerOfTwoException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new InvalidMappingSchemeException();
                }
            }
            else {
                throw new InvalidMappingSchemeException();
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
    private void validateCacheLayerConfig(CacheLayerConfig conf) throws InvalidAccessTimeException, InvalidMappingSchemeException {
        if (conf.accessTime < 1) {
            throw new InvalidAccessTimeException();
        }
        if (!conf.mappingScheme.equals("DIRECT") && !conf.mappingScheme.contains("FULLY_ASSOCIATIVE") && !conf.mappingScheme.contains("_WAY_SET_ASSOCIATIVE")) {
            throw new InvalidMappingSchemeException();
        }
    }

    /**
     * Converts strings of format XXB,XXKB,XXMB to an int byte count, throws if string is improperly formatted
     *
     * @param size
     */
    private int byteValueFromString(String size) throws InvalidCacheSizeStringFormatException {
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
                    return Integer.parseInt(size.substring(0, index));
            }
        } else {
            throw new InvalidCacheSizeStringFormatException();
        }
    }

    /**
     * Resets the cache to default with no caches and 0CC of access time to main memory
     */
    public void resetDefault() {
        this.mainMemoryAccessTime = 0;
        this.reads = this.writes = this.readTotal = this.writeTotal = 0;
        this.cacheLayers = new CacheLayer[0];
        this.configured = false;
        this.configName = null;
        this.enabled = true;
    }

    /**
     * Empties all caches and reset
     */
    public void reset() {
        this.reads = this.writes = this.readTotal = this.writeTotal = 0;
        for (CacheLayer layer : this.cacheLayers) {
            layer.reset();
        }
    }

    public int getMainMemoryAccessTime() {
        return this.mainMemoryAccessTime;
    }

    /**
     * Calculate AMAT for write
     */

    public double calculateAMATWrite(){
        return writeTotal / (double) writes;
    }

    /**
     * Calculate AMAT for read
     */

    public double calculateAMATRead() {
        return readTotal / (double) reads;
    }

    /**
     * Calculate total AMAT
     * @return
     */
    public double calculateAMAT() {
        return (reads / (double) (reads + writes)) * calculateAMATRead()+  (writes / (double) (reads + writes)) * calculateAMATWrite();
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

        if (!enabled)
            return 0;

        try {
            switch (accessType) {
                case READ:
                    reads++;
                    delay = readFromLayer(0, address);
                    readTotal += delay;
                    break;
                case WRITE:
                    writes++;
                    delay = writeToLayer(0, address);
                    writeTotal += delay;
                    break;
                case NONE:
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

        layer.accesses += 1;
        switch (layer.getWriteStrategy()) {
            case WRITE_BACK:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Write hit, just write to this layer
                    delay += layer.getAccessTime();
                    layer.hits += 1;
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

                // Take into account the time it takes to check if the block is in this layer
                delay += layer.getAccessTime();

                //If the block is contained, write it
                if (layer.contains(address)) {
                    // Write hit
                    layer.hits += 1;
                }

                // In both cases, write the block to the lower layer
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
        layer.accesses += 1;
        switch (layer.getWriteStrategy()) {
            case WRITE_BACK:
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Read hit
                    delay += layer.getAccessTime();
                    layer.hits += 1;

                }
                else {
                    // Read miss
                    // Access the layer even if it is a miss
                    delay += layer.getAccessTime();

                    // Get the block from the lower layer
                    delay += readFromLayer(layerNumber + 1, address);

                    // Put the block in this layer
                    CacheBlock cb = layer.put(address);

                    // If the evicted block is dirty, write it back to lower layer
                    if(cb.valid && cb.getDirty()) {
                        delay += writeToLayer(layerNumber + 1, cb.baseAddress);
                    }
                }

                break;
            case WRITE_THROUGH:

                //Time to check if hit or miss
                delay += layer.getAccessTime();
                // Check if hit or miss
                if (layer.contains(address)) {
                    // Read hit
                    layer.hits += 1;

                }
                else {
                    // Read miss
                    delay += readFromLayer(layerNumber + 1, address);

                    // Get the block from lower layers
                    layer.put(address);

                    // The evicted block is always clean
                }
                break;
        }

        return delay;

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
