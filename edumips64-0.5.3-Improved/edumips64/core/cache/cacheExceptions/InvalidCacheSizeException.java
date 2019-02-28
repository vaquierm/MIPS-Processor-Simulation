package edumips64.core.cache.cacheExceptions;

public class InvalidCacheSizeException extends Exception {

    public InvalidCacheSizeException() {
        super("Cache size mod cache block must equal zero");
    }

}
