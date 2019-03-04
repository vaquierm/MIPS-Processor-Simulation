package utils;
import edumips64.core.CPU;
import edumips64.core.cache.CacheManager;
import edumips64.core.cache.cacheLayer.CacheLayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonWriter {

    public void write() {
        JSONObject obj = new JSONObject();
        CacheManager cm = CacheManager.getInstance();
        CacheLayer[] layers =  cm.getCacheLayers();
        obj.put("number_of_caches", layers.length);
        JSONArray blockSizes = new JSONArray();
        JSONArray strategies = new JSONArray();
        JSONArray hitRates = new JSONArray();
        for(CacheLayer cl : layers) {
            blockSizes.add(cl.getBlockSize());
            strategies.add(cl.getWriteStrategy().name());
            hitRates.add(cl.hits / (double)(cl.accesses));
        }

        obj.put("block_sizes", blockSizes);
        obj.put("write_strategies", strategies);
        obj.put("hit_rates", hitRates);
        obj.put("Memory stalls", CPU.getInstance().getMemoryStalls());
        try (FileWriter file = new FileWriter(getNewFileName("result"))) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNewFileName(String filename) throws IOException {
        File aFile = new File(filename);
        int fileNo = 0;
        String newFileName = "";
        if (aFile.exists() && !aFile.isDirectory()) {
            while(aFile.exists()){
                fileNo++;
                String path = filename + "(" + fileNo + ").json";
                aFile = new File(path);
                newFileName = path;
            }


        } else if (!aFile.exists()) {
            aFile.createNewFile();
            newFileName = filename;
        }
        return newFileName;
    }
}