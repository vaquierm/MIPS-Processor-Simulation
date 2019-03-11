package utils;
import edumips64.core.CPU;
import edumips64.core.cache.CacheManager;
import edumips64.core.cache.cacheLayer.CacheLayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class JsonWriter {

    public static Report write(String runningFile) {
        JSONObject obj = new JSONObject();
        CacheManager cm = CacheManager.getInstance();
        CacheLayer[] layers =  cm.getCacheLayers();
        obj.put("benchmark_name", runningFile);
        obj.put("MMAT", cm.getMainMemoryAccessTime());
        JSONArray caches = new JSONArray();
        double amat = cm.calculateAMAT();
        double amatRead = cm.calculateAMATRead();
        double amatWrite = cm.calculateAMATWrite();
        obj.put("amat", amat);
        obj.put("amat_read", amatRead);
        obj.put("amat_write", amatWrite);
        obj.put("number_layers", layers.length);
        for(CacheLayer cl : layers) {
            JSONObject temp = new JSONObject();
            temp.put("block_size", cl.getBlockSize());
            temp.put("size", cl.getCacheSize());
            temp.put("strategy", cl.getWriteStrategy().name());
            temp.put("access_time", cl.getAccessTime());
            temp.put("hit_rate", cl.hits / (double)(cl.accesses));
            caches.add(temp);
        }
        obj.put("caches", caches);

        return new Report(generateReportName(runningFile.substring(0, runningFile.indexOf('.'))), obj.toJSONString());
    }

    /**
     * Generate a name for the computed report,
     * @return
     */
    private static String generateReportName(String runningFile){
        //TODO decide on file naming scheme
        // Temp scheme is RunFileName_CacheFileName
        return runningFile + "_" + CacheManager.getInstance().getConfigName()+".json";
    }

    private static String getNewFileName(String filename) throws IOException {
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