package utils;

import edumips64.core.cache.CacheManager;
import edumips64.core.cache.ICache.CacheLayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.util.List;

public class JsonWriter {

    public void write() {
        JSONObject obj = new JSONObject();
        CacheManager cm = CacheManager.getInstance();
        CacheLayer[] layers = (CacheLayer[]) cm.getCacheLayers();
        obj.put("number_of_caches", layers.length);
        JSONArray blockSizes = new JSONArray();
        for(CacheLayer cl : layers) {
            blockSizes.add(cl)
        }
    }
}
