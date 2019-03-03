package edumips64.core.cache.cacheExceptions;

public class InvalidAccessTimeException extends Exception {
    public InvalidAccessTimeException() { super("Cache layer access time must be at least 1 cycle"); }
}
