package edumips64.core.cache.cacheExceptions;

public class CacheAlreadyContainsBlockException extends Exception {
    public CacheAlreadyContainsBlockException() {
        super("The block that is trying to be added to cache is already in the cache");
    }
}
