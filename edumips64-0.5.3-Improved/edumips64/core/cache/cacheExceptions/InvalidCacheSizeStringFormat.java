package edumips64.core.cache.cacheExceptions;

public class InvalidCacheSizeStringFormat extends Exception {
    public InvalidCacheSizeStringFormat() { super("String must be of format XXXB, XXXKB, XXXMB, or XXXGB"); }
}
