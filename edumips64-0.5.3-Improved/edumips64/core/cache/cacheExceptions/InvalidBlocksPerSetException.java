package edumips64.core.cache.cacheExceptions;

public class InvalidBlocksPerSetException extends Exception {
    public InvalidBlocksPerSetException() {
        super("The number of blocks per set must be greater than 0, a power of two and it cannot be equal to or larger than the number of blocks.");
    }
}
