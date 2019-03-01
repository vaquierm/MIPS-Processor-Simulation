package utils;

import edumips64.core.cache.CacheManager;
import edumips64.core.cache.cacheLayer.CacheLayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.List;

public class JsonWriter {

    public void write() {
        JSONObject obj = new JSONObject();
        CacheManager cm = CacheManager.getInstance();
        CacheLayer[] layers =  cm.getCacheLayers();
        obj.put("number_of_caches", layers.length);
        JSONArray blockSizes = new JSONArray();
        JSONArray strategies = new JSONArray();
        for(CacheLayer cl : layers) {
            blockSizes.add(cl.getBlockSize());
            strategies.add(cl.getWriteStrategy().name());
        }

        obj.put("block_sizes", blockSizes);
        obj.put("write_strategies", strategies);
    }
}
