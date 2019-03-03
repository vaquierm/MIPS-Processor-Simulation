package edumips64.core.cache.cacheExceptions;

public class InvalidCacheSizeStringFormatException extends Exception {
    public InvalidCacheSizeStringFormatException() { super("String must be of format XXXB, XXXKB, XXXMB, or XXXGB"); }
}
