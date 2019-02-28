package utils;

import edumips64.core.cache.CacheManager;
import edumips64.core.cache.ICache.CacheLayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.List;

public class JsonWriter {

    CacheManager cm;
    List<CacheLayer> layers;
    public void write() {
        // TODO: Add methods to get the cache manager and the layers
        JSONObject obj = new JSONObject();
        JSONArray blockSizes = new JSONArray();
        for(CacheLayer cl : layers) {
         //   array.add("block_size:" cl.)
        }
    }
}
